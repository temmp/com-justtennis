package com.justtennis.db.sqlite.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.cameleon.common.android.inotifier.INotifierMessage;
import com.justtennis.db.sqlite.helper.DBAddressHelper;
import com.justtennis.domain.Address;
import com.justtennis.tool.DbTool;

public class DBAddressDataSource extends GenericDBDataSource<Address> {

	private static final String TAG = DBAddressDataSource.class.getCanonicalName();

	// Database fields
	private String[] allColumns = {
		DBAddressHelper.COLUMN_ID,
		DBAddressHelper.COLUMN_NAME,
		DBAddressHelper.COLUMN_POSTAL_CODE,
		DBAddressHelper.COLUMN_CITY,
		DBAddressHelper.COLUMN_GPS
	};

	public DBAddressDataSource(Context context, INotifierMessage notificationMessage) {
		super(new DBAddressHelper(context, notificationMessage), notificationMessage);
	}

	@Override
	protected String[] getAllColumns() {
		return allColumns;
	}

	@Override
	protected void putContentValue(ContentValues values, Address address) {
		values.put(DBAddressHelper.COLUMN_NAME, address.getName());
		values.put(DBAddressHelper.COLUMN_LINE1, address.getLine1());
		values.put(DBAddressHelper.COLUMN_POSTAL_CODE, address.getPostalCode());
		values.put(DBAddressHelper.COLUMN_CITY, address.getCity());
		values.put(DBAddressHelper.COLUMN_GPS, address.getGps());
	}

	@Override
	protected Address cursorToPojo(Cursor cursor) {
		int col = 0;
		Address message = new Address();
		message.setId(DbTool.getInstance().toLong(cursor, col++));
		message.setName(cursor.getString(col++));
		message.setLine1(cursor.getString(col++));
		message.setPostalCode(cursor.getString(col++));
		message.setCity(cursor.getString(col++));
		message.setGps(cursor.getString(col++));
		return message;
	}
	
	@Override
	protected String getTag() {
		return TAG;
	}
}