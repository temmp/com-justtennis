package com.justtennis.listener.ok;

import java.io.IOException;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.justtennis.db.sqlite.helper.DBInviteHelper;
import com.justtennis.db.sqlite.helper.DBMessageHelper;
import com.justtennis.db.sqlite.helper.DBPlayerHelper;
import com.justtennis.db.sqlite.helper.DBRankingHelper;
import com.justtennis.db.sqlite.helper.DBScoreSetHelper;
import com.justtennis.db.sqlite.helper.DBUserHelper;
import com.justtennis.db.sqlite.helper.GenericDBHelper;
import com.justtennis.notifier.NotifierMessageLogger;

public class OnClickDBRestoreListenerOk implements OnClickListener {

	private Activity context;

	public OnClickDBRestoreListenerOk(Activity context) {
		this.context = context;
	}

	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			NotifierMessageLogger notifier = NotifierMessageLogger.getInstance();
			GenericDBHelper[] listHelper  = new GenericDBHelper[] {
					new DBInviteHelper(context, notifier),
					new DBMessageHelper(context, notifier),
					new DBPlayerHelper(context, notifier),
					new DBRankingHelper(context, notifier),
					new DBScoreSetHelper(context, notifier),
					new DBUserHelper(context, notifier)
			};
			
			for(GenericDBHelper helper : listHelper) {
				try {
					helper.restoreDbFromSdcard();
				} catch (IOException e) {
					notifier.notifyError(e);
				}
			}
		}
	}

}