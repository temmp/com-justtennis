package com.justtennis.db.sqlite.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cameleon.common.android.inotifier.INotifierMessage;
import com.justtennis.db.sqlite.helper.DBPlayerHelper;
import com.justtennis.db.sqlite.helper.DBUserHelper;
import com.justtennis.domain.User;
import com.justtennis.tool.DbTool;

public class DBUserDataSource extends GenericDBDataSource<User> {

	private static final String TAG = DBUserDataSource.class.getCanonicalName();

	// Database fields
	private String[] allColumns = {
		DBUserHelper.COLUMN_ID,
		DBUserHelper.COLUMN_ID_TOURNAMENT,
		DBUserHelper.COLUMN_ID_CLUB,
		DBUserHelper.COLUMN_ID_ADDRESS,
		DBUserHelper.COLUMN_ID_RANKING,
		DBUserHelper.COLUMN_ID_RANKING_ESTIMAGE,
		DBUserHelper.COLUMN_FIRSTNAME,
		DBUserHelper.COLUMN_LASTNAME,
		DBUserHelper.COLUMN_BIRTHDAY,
		DBUserHelper.COLUMN_PHONENUMBER,
		DBUserHelper.COLUMN_ADDRESS,
		DBUserHelper.COLUMN_POSTALCODE,
		DBUserHelper.COLUMN_LOCALITY
	};

	public DBUserDataSource(Context context, INotifierMessage notificationMessage) {
		super(new DBUserHelper(context, notificationMessage), notificationMessage);
	}

	@Override
	protected String[] getAllColumns() {
		return allColumns;
	}

	@Override
	protected void putContentValue(ContentValues values, User player) {
		values.put(DBUserHelper.COLUMN_ID_TOURNAMENT, player.getIdClub());
		values.put(DBUserHelper.COLUMN_ID_CLUB, player.getIdClub());
		values.put(DBUserHelper.COLUMN_ID_ADDRESS, player.getIdAddress());
		values.put(DBUserHelper.COLUMN_ID_RANKING, player.getIdRanking());
		values.put(DBUserHelper.COLUMN_ID_RANKING_ESTIMAGE, player.getIdRankingEstimate());
		values.put(DBUserHelper.COLUMN_FIRSTNAME, player.getFirstName());
		values.put(DBUserHelper.COLUMN_LASTNAME, player.getLastName());
		values.put(DBUserHelper.COLUMN_BIRTHDAY, player.getBirthday());
		values.put(DBUserHelper.COLUMN_PHONENUMBER, player.getPhonenumber());
		values.put(DBUserHelper.COLUMN_ADDRESS, player.getAddress());
		values.put(DBUserHelper.COLUMN_POSTALCODE, player.getPostalCode());
		values.put(DBUserHelper.COLUMN_LOCALITY, player.getLocality());
	}

	@Override
	protected User cursorToPojo(Cursor cursor) {
		int col = 0;
		User player = new User();
		player.setId(DbTool.getInstance().toLong(cursor, col++));
		player.setIdTournament(DbTool.getInstance().toLong(cursor, col++));
		player.setIdClub(DbTool.getInstance().toLong(cursor, col++));
		player.setIdAddress(DbTool.getInstance().toLong(cursor, col++));
		player.setIdRanking(DbTool.getInstance().toLong(cursor, col++));
		player.setIdRankingEstimate(DbTool.getInstance().toLong(cursor, col++));
		player.setFirstName(cursor.getString(col++));
		player.setLastName(cursor.getString(col++));
		player.setBirthday(cursor.getString(col++));
		player.setPhonenumber(cursor.getString(col++));
		player.setAddress(cursor.getString(col++));
		player.setPostalCode(cursor.getString(col++));
		player.setLocality(cursor.getString(col++));
		return player;
	}
	
	@Override
	protected String getTag() {
		return TAG;
	}
}