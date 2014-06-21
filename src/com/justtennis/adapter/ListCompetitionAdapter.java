package com.justtennis.adapter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.justtennis.ApplicationConfig;
import com.justtennis.R;
import com.justtennis.adapter.manager.RankingViewManager;
import com.justtennis.db.service.ScoreSetService;
import com.justtennis.domain.Invite;
import com.justtennis.domain.Tournament;
import com.justtennis.notifier.NotifierMessageLogger;

public class ListCompetitionAdapter extends BaseExpandableListAdapter {

	private ScoreSetService scoreSetService;
	private RankingViewManager rankingViewManager;

	private Context context;
	private List<Tournament> listTournament; // header titles
	// child data in format of header title, child title
	private HashMap<Tournament, List<Invite>> listInviteByTournament;
	@SuppressLint("SimpleDateFormat")
	private final static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

	public ListCompetitionAdapter(Context context, List<Tournament> listTournament, HashMap<Tournament, List<Invite>> listInviteByTournament) {
		this.context = context;
		this.listTournament = listTournament;
		this.listInviteByTournament = listInviteByTournament;
		NotifierMessageLogger notifier = NotifierMessageLogger.getInstance();
		scoreSetService = new ScoreSetService(context, notifier);
		rankingViewManager = RankingViewManager.getInstance(context, notifier);
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this.listInviteByTournament.get(this.listTournament.get(groupPosition)).get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

		Invite invite = (Invite) getChild(groupPosition, childPosition);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_competition_item, null);
		}
		convertView.setTag(invite);


		TextView tvPlayer = (TextView) convertView.findViewById(R.id.tv_player);
		TextView tvDate = (TextView) convertView.findViewById(R.id.tv_date);
		TextView tvScore = (TextView) convertView.findViewById(R.id.tv_score);
		TextView tvPoint = (TextView) convertView.findViewById(R.id.tv_point);

		rankingViewManager.manageRanking(convertView, invite, true);

		tvPlayer.setText(invite.getPlayer()==null ? "" : Html.fromHtml("<b>" + invite.getPlayer().getFirstName() + "</b> " + invite.getPlayer().getLastName()));
		tvDate.setText(invite.getDate()==null ? "" : sdf.format(invite.getDate()));
		tvPoint.setText(invite.getPoint() > 0 ? ""+invite.getPoint() : "");

		if (ApplicationConfig.SHOW_ID) {
			tvPlayer.setText(tvPlayer.getText() + " [" + invite.getPlayer().getId() + "|" + invite.getPlayer().getIdExternal() + "]");
			tvDate.setText(tvDate.getText() + " [" + invite.getId() + "|" + invite.getIdExternal() + "]");
		}

		String textScore = scoreSetService.buildTextScore(invite);
		if (textScore != null) {
			tvScore.setVisibility(View.VISIBLE);
			tvScore.setText(Html.fromHtml(textScore));
		} else  {
			tvScore.setVisibility(View.GONE);
		}

		switch(invite.getScoreResult()) {
			case VICTORY:
				break;
			case DEFEAT:
				break;
			default:
		}

		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return listInviteByTournament == null || listTournament == null ? 0 : listInviteByTournament.get(listTournament.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.listTournament.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return listTournament == null ? 0 : listTournament.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Tournament tournament = (Tournament) getGroup(groupPosition);
		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.list_competition_header, null);
		}

		TextView lblListHeader = (TextView) convertView.findViewById(R.id.textHeader);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(tournament.getName());

		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}