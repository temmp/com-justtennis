package com.justtennis.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.annotation.SuppressLint;
import android.content.Context;

import com.justtennis.ApplicationConfig;
import com.justtennis.R;
import com.justtennis.domain.Invite;
import com.justtennis.domain.Invite.STATUS;
import com.justtennis.domain.Message;
import com.justtennis.domain.Player;
import com.justtennis.domain.User;
import com.justtennis.tool.CryptoTool;

public class SmsParser {
	private static final String TAG = SmsParser.class.getSimpleName();
	
//	private static final String TAG_FIRSTNAME = "\\[FIRSTNAME\\]";
//	private static final String TAG_LASTNAME = "\\[LASTNAME\\]";
//	private static final String TAG_DATE = "\\[DATE\\]";
//	private static final String TAG_HOUR = "\\[HOUR\\]";
//	private static final String TAG_DATE_RELATIVE = "\\[DATE_RELATIVE\\]";
//	private static final String TAG_USER_FIRSTNAME = "\\[USER_FIRSTNAME\\]";
//	private static final String TAG_USER_LASTNAME = "\\[USER_LASTNAME\\]";
	public static final String TAG_FIRSTNAME = "[FIRSTNAME]";
	public static final String TAG_LASTNAME = "[LASTNAME]";
	public static final String TAG_DATE = "[DATE]";
	public static final String TAG_TIME = "[HOUR]";
	public static final String TAG_DATE_RELATIVE = "[DATE_RELATIVE]";
	public static final String TAG_USER_FIRSTNAME = "[USER_FIRSTNAME]";
	public static final String TAG_USER_LASTNAME = "[USER_LASTNAME]";

	private final static String MSG_START = "JUST_TENNIS";
	private final static String DATA_SEPARATOR = "|";
	private final static int MSG_START_LENGTH = MSG_START.length();
	private final static int DATA_SEPARATOR_LENGTH = DATA_SEPARATOR.length();
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", ApplicationConfig.getLocal());
	private static SimpleDateFormat sdfMessageCommonDate = null;
	private static SimpleDateFormat sdfMessageCommonTime = null;
	private String msgInviteDateRelativeToDay;
	private String msgInviteDateRelativeTomorrow;


	public enum MSG_TYPE {
		UNKNOW(""),
		PLAYER_ADD("PA"),
		PLAYER_ADD_YES("PAY"),
		PLAYER_ADD_NO("PAN"),
		PLAY_INVITE("PI"),
		PLAY_INVITE_YES("PIY"),
		PLAY_INVITE_NO("PIN");

		public String code;
		public int length;
		MSG_TYPE(String code) {
			this.code = code;
			this.length = code.length();
		}
	};

	private static SmsParser instance = null;
	private UserParser userParser = null;
	private PlayerParser playerParser = null;

	
	private SmsParser(Context context) {
		this.userParser = UserParser.getInstance();
		this.playerParser = PlayerParser.getInstance();
		this.msgInviteDateRelativeToDay = context.getString(R.string.fr_msg_invite_date_relative_today);
		this.msgInviteDateRelativeTomorrow = context.getString(R.string.fr_msg_invite_date_relative_tomorrow);
		sdfMessageCommonDate = new SimpleDateFormat(context.getString(R.string.msg_common_format_date), ApplicationConfig.getLocal());
		sdfMessageCommonTime = new SimpleDateFormat(context.getString(R.string.msg_common_format_time), ApplicationConfig.getLocal());
	}

	public static SmsParser getInstance(Context context) {
		if (instance==null) {
			instance = new SmsParser(context);
		}
		return instance;
	}

	public static String getDataSeparator() {
		return DATA_SEPARATOR;
	}

	public static int getDataSeparatorLength() {
		return DATA_SEPARATOR_LENGTH;
	}
	
	public static String getMessageStart() {
		return MSG_START;
	}

	public MSG_TYPE getMessageType(String message) {
		MSG_TYPE ret = MSG_TYPE.UNKNOW;
		if (isKnowMessage(message)) {
			String type = extractType(message);
			for(MSG_TYPE msgType : MSG_TYPE.values()) {
				if (msgType.code.equals(type)) {
					ret = msgType;
					break;
				}
			}
		}
		return ret;
	}

	public String toMessageCommon(Message message, Invite invite) {
		String text = null;
		if (message!=null) {
			text = message.getMessage();
			text = text.replace(TAG_FIRSTNAME, invite.getPlayer().getFirstName());
			text = text.replace(TAG_LASTNAME, invite.getPlayer().getLastName());
			text = text.replace(TAG_DATE, sdfMessageCommonDate.format(invite.getDate()));
			text = text.replace(TAG_TIME, sdfMessageCommonTime.format(invite.getDate()));
			text = text.replace(TAG_USER_FIRSTNAME, invite.getUser().getFirstName());
			text = text.replace(TAG_USER_LASTNAME, invite.getUser().getLastName());
			if (text.contains(TAG_DATE_RELATIVE)) {
				boolean isDateRelativeReplace = false;
				Calendar cal = Calendar.getInstance();
				Calendar calInvite = Calendar.getInstance();
				calInvite.setTime(invite.getDate());
				if (cal.get(Calendar.YEAR)==calInvite.get(Calendar.YEAR)) {
					if (cal.get(Calendar.MONTH)==calInvite.get(Calendar.MONTH)) {
						int day = cal.get(Calendar.DAY_OF_MONTH);
						int dayInvite = calInvite.get(Calendar.DAY_OF_MONTH);
						if (day==dayInvite) {
							text = text.replace(TAG_DATE_RELATIVE, msgInviteDateRelativeToDay);
							isDateRelativeReplace = true;
						}
						else if ((dayInvite-day)==1) {
							text = text.replace(TAG_DATE_RELATIVE, msgInviteDateRelativeTomorrow);
							isDateRelativeReplace = true;
						}
					}
				}
				if (!isDateRelativeReplace){
					text = text.replace(TAG_DATE_RELATIVE, sdfMessageCommonDate.format(invite.getDate()));
				}
			}
		}
		return text;
	}

	public String toMessageAdd(Invite invite) {
		return toMessage(MSG_TYPE.PLAYER_ADD, invite);
	}
	
	public String toMessageDemandeAddYes(Invite invite) {
		return toMessage(MSG_TYPE.PLAYER_ADD_YES, invite);
	}
	
	public String toMessageDemandeAddNo(Invite invite) {
		return toMessage(MSG_TYPE.PLAYER_ADD_NO, invite);
	}

	public String toMessageInviteConfirmYes(Invite invite) {
		return toMessage(MSG_TYPE.PLAY_INVITE_YES, invite);
	}
	
	public String toMessageInviteConfirmNo(Invite invite) {
		return toMessage(MSG_TYPE.PLAY_INVITE_NO, invite);
	}

	public String toMessageInvite(Invite invite) {
		return toMessage(MSG_TYPE.PLAY_INVITE, invite);
	}

	public Invite fromMessageAdd(String message) {
		return fromMessage(MSG_TYPE.PLAYER_ADD, message, false);
	}
	
	public Invite fromMessageDemandeAddYes(String message) {
		return fromMessage(MSG_TYPE.PLAYER_ADD_YES, message, false);
	}
	
	public Invite fromMessageDemandeAddNo(String message) {
		return fromMessage(MSG_TYPE.PLAYER_ADD_NO, message, false);
	}

	public Invite fromMessageInvite(String message) {
		return fromMessage(MSG_TYPE.PLAY_INVITE, message, true);
	}

	public Invite fromMessageInviteConfirmYes(String message) {
		return fromMessage(MSG_TYPE.PLAY_INVITE_YES, message, true);
	}

	public Invite fromMessageInviteConfirmNo(String message) {
		return fromMessage(MSG_TYPE.PLAY_INVITE_NO, message, true);
	}
	
	private Invite fromMessage(MSG_TYPE type, String message, boolean parseDate) {
		Invite ret = null;
		if (isKnowMessage(message)) {
			message = unprepareMessage(message);
			if (isTypeOf(type, message)) {
				ret = new Invite();
				ret.setId(fromMessageIdInvite(type, message));
				ret.setIdExternal(fromMessageIdExternalInvite(type, message));
				ret.setIdCalendar(fromMessageIdCalendar(type, message));
				ret.setUser(fromMessageUser(type, message));
				ret.setPlayer(fromMessagePlayer(type, message));
				ret.setStatus(fromMessageStatus(type, message));
				if (parseDate) {
					try {
						String date = message.substring(message.lastIndexOf(DATA_SEPARATOR)+DATA_SEPARATOR_LENGTH);
						date = CryptoTool.getInstance().decrypteNumeric(date);
						ret.setDate(sdf.parse(date));
					} catch (ParseException e) {
						logMe(e);
					}
				}
			}
		}
		return ret;
	}

	public String formatDate(Date date) {
		return sdf.format(date);
	}

	private String toMessage(MSG_TYPE type, Invite invite) {
		Long id = invite.getId();
		Long idExternal = invite.getIdExternal();
		Long idCalendar = invite.getIdCalendar();
		User user = invite.getUser();
		Player player = invite.getPlayer();
		STATUS status = invite.getStatus();
		Date date = invite.getDate();
		String message = 
			type.code + DATA_SEPARATOR +
			id + DATA_SEPARATOR +
			idExternal + DATA_SEPARATOR +
			idCalendar + DATA_SEPARATOR +
			this.userParser.toDataText(user) + DATA_SEPARATOR +
			this.playerParser.toDataText(player) + DATA_SEPARATOR +
			status
		;
		if (date!=null) {
			message += DATA_SEPARATOR + CryptoTool.getInstance().crypteNumeric(sdf.format(date));
		}
		message = prepareMessage(message);
		return message;
	}

	public String prepareMessage(String message) {
		logParser("prepareMessage decrypted message:",message);
		String text = CryptoTool.getInstance().crypte(message);
		text = MSG_START + DATA_SEPARATOR + text;
		return text;
	}

	private Long fromMessageIdInvite(MSG_TYPE type, String message) {
		String idPart = fromMessagePart(type, message, 0);
		logParser("fromMessageIdInvite id:", idPart);
		return (idPart.toLowerCase().equals("null") ? null : Long.parseLong(idPart));
	}

	private Long fromMessageIdExternalInvite(MSG_TYPE type, String message) {
		String idPart = fromMessagePart(type, message, 1);
		logParser("fromMessageIdExternalInvite id:",idPart);
		return (idPart.toLowerCase().equals("null") ? null : Long.parseLong(idPart));
	}

	private Long fromMessageIdCalendar(MSG_TYPE type, String message) {
		String id = fromMessagePart(type, message, 2);
		logParser("fromMessageIdCalendar id:",id);
		return (id.toLowerCase().equals("null") ? null : Long.parseLong(id));
	}

	private User fromMessageUser(MSG_TYPE type, String message) {
		String userPart = fromMessagePart(type, message, 3);
		logParser("fromMessageUser userPart:",userPart);
		return (User)userParser.fromData(userPart);
	}

	private Player fromMessagePlayer(MSG_TYPE type, String message) {
		String playerPart = fromMessagePart(type, message, 4);
		logParser("fromMessagePlayer playerPart:",playerPart);
		return playerParser.fromData(playerPart);
	}

	private STATUS fromMessageStatus(MSG_TYPE type, String message) {
		String statusPart = fromMessagePart(type, message, 5);
		logParser("fromMessageStatus statusPart:",statusPart);
		return STATUS.valueOf(statusPart);
	}

	private String fromMessagePart(MSG_TYPE type, String message, int indexNbStart) {
		message = extractMessage(type, message);
		int idx1 = 0;
		for(int i=0 ; i<indexNbStart ; i++) {
			idx1 = message.indexOf(DATA_SEPARATOR, idx1)+DATA_SEPARATOR_LENGTH;
		}
		int idx2 = message.indexOf(DATA_SEPARATOR, idx1);
		idx2 = idx2>0 ? idx2 : message.length();
		return message.substring(idx1, idx2);
	}

	public String unprepareMessage(String message) {
		message = message.substring(MSG_START_LENGTH + DATA_SEPARATOR_LENGTH);
		message = CryptoTool.getInstance().decrypte(message);
		logParser("unprepareMessage decrypted message:",message);
		return message;
	}

	private String extractType(String message) {
		message = unprepareMessage(message);
		int idx = message.indexOf(DATA_SEPARATOR);
		return (idx>0) ? message.substring(0, idx) : "";
	}

	public String extractMessage(MSG_TYPE type, String message) {
		message = message.substring(type.length + DATA_SEPARATOR_LENGTH);
		return message;
	}
	private boolean isKnowMessage(String message) {
		return message.startsWith(MSG_START + DATA_SEPARATOR);
	}

	private boolean isTypeOf(MSG_TYPE type, String message) {
		return message.startsWith(type.code + DATA_SEPARATOR);
	}
//
//	private int indexOf(String message, String car, int start, int nb) {
//		int ret = 0;
//		for(int i=0 ; i<nb ; i++) {
//			ret = message.indexOf(DATA_SEPARATOR, start);
//			if (ret<=start) {
//				break;
//			}
//			else {
//				start++;
//			}
//		}
//		return ret;
//	}
	
	private void logMe(String message) {
//		System.err.println(message);
		System.out.println(message);
	}
	
	private void logMe(Exception e) {
		e.printStackTrace();
	}

	private void logParser(String title, String data) {
		if (ApplicationConfig.SHOW_LOG_TRACE_PARSER) {
			logMe("SHOW_LOG_TRACE_PARSER "+title+data);
		}
	}
}