package com.dnatech.community.sqlite;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import com.dnatech.community.entity.MessageEntity;
import com.dnatech.community.entity.SubscribeEntity;
import com.dnatech.community.utils.DESUtil;
import com.dnatech.community.utils.SQLiteUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.os.Build.*;

/**
 * Created by zyk on 2016/5/14.
 */
public class WCDatebaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "wisdomcommunity.db";

	private static final String TABLE_MESSAGE = "message";
	private static final String TABLE_SUBSCRIBE ="subscribe";

	private static final String ID = "_id";
	private static final String TITLE = "title";
	private static final String DESCRIPTION = "description";
	private static final String TIME = "time";
	private static final String URL = "url";
	private static final String PUBLISHER = "publisher";
	private static final String VALIDITY = "validity";
	private static final String ISREAD = "isread";
	private static final String TOPIC = "topic";
	private static final String QOS = "qos";



	// database version, used to recognise when we need to upgrade
	// (delete and recreate)
	private static final int DATABASE_VERSION = 1;

	public WCDatebaseHelper(Context context){
		this(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	public WCDatebaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@TargetApi(VERSION_CODES.HONEYCOMB)
	public WCDatebaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
		super(context, name, factory, version, errorHandler);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String createMessageTable = "CREATE TABLE "+TABLE_MESSAGE+" ("+
				ID+" INTEGER PRIMARY KEY ," +
				TITLE+" TEXT NOT NULL ," +
				DESCRIPTION+" TEXT ," +
				TIME+" DATETIME NOT NULL," +
				URL+" TEXT NOT NULL ," +
				PUBLISHER+" TEXT ," +
				VALIDITY+" DATETIME NOT NULL , " +
				ISREAD+" BOOLEAN DEFAULT false" +
				");";

		String createSubscribeTable = "CREATE TABLE " +TABLE_SUBSCRIBE+" ("+
				ID+" INTEGER PRIMARY KEY ," +
				TOPIC+" TEXT NOT NULL," +
				QOS+" INTEGER NOT NULL" +
				");";

		try {
			db.execSQL(createMessageTable);
			db.execSQL(createSubscribeTable);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	private SQLiteStatement mInsertMessageStatement;
	/**
	 * 往message插入一条数据
	 * @param title 标题
	 * @param description 简介
	 * @param time 日期
	 * @param url 内容的URL
	 * @param publisher 推送者
	 * @param validity 有效日期
	 * @return
	 */
	public long insertMessage(String title, String description, Date time, String url,String publisher, Date validity) {
		if (mInsertMessageStatement == null) {
			mInsertMessageStatement = getWritableDatabase().compileStatement(
					"INSERT OR IGNORE INTO "+TABLE_MESSAGE+" ("+TITLE+", "+DESCRIPTION+", "+TIME+", "+URL+","+PUBLISHER+", "+VALIDITY+") VALUES (?,?,?,?,?,?)"
			);
		}
		SQLiteUtil.bindString(mInsertMessageStatement,1, DESUtil.encrypt(title));
		SQLiteUtil.bindString(mInsertMessageStatement,2,DESUtil.encrypt(description));
		SQLiteUtil.bindDate(mInsertMessageStatement,3,time);
		SQLiteUtil.bindString(mInsertMessageStatement,4,DESUtil.encrypt(url));
		SQLiteUtil.bindString(mInsertMessageStatement,5,DESUtil.encrypt(publisher));
		SQLiteUtil.bindDate(mInsertMessageStatement,6,validity);
		return mInsertMessageStatement.executeInsert();
	}

	private SQLiteStatement mInsertSubscribeStatement;
	public long insertSubscribe(String topic, int qos) {
		if (mInsertSubscribeStatement == null) {
			mInsertSubscribeStatement = getWritableDatabase().compileStatement(
					"INSERT OR IGNORE INTO "+TABLE_SUBSCRIBE+" ("+TOPIC+", "+QOS+") VALUES (?,?)"
			);
		}
		SQLiteUtil.bindString(mInsertSubscribeStatement,1, DESUtil.encrypt(topic));
		mInsertSubscribeStatement.bindLong(2,qos);
		return mInsertSubscribeStatement.executeInsert();
	}

	public List<MessageEntity> selectMessageAll() {
		deleteMessage();
		List<MessageEntity> list = new ArrayList<>();
		final Cursor cursor = getReadableDatabase().rawQuery(
				"select * from "+TABLE_MESSAGE,
				null
		);
		while(cursor.moveToNext()) {
			MessageEntity me = new MessageEntity();
			me.id          = cursor.getInt(cursor.getColumnIndex(ID));
			me.title       = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(TITLE)));
			me.description = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
			me.url         = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(URL)));
			me.publisher   = DESUtil.decrypt(cursor.getColumnName(cursor.getColumnIndex(PUBLISHER)));
			me.time        = SQLiteUtil.getDate(cursor, cursor.getColumnIndex(TIME));
			me.validity    = SQLiteUtil.getDate(cursor, cursor.getColumnIndex(VALIDITY));
			me.isRead      = cursor.getInt(cursor.getColumnIndex(ISREAD)) == 0 ? false:true;
			list.add(me);
		}
		cursor.close();
		return list;
	}

	public List<SubscribeEntity> selectSubscribeAll() {
		List<SubscribeEntity> list = new ArrayList<>();
		SubscribeEntity se = new SubscribeEntity();
		se.id     = -1;
		se.topic  = "default";
		se.qos    = 2;
		list.add(se);
		final Cursor cursor = getReadableDatabase().rawQuery(
				"select * from "+TABLE_SUBSCRIBE,
				null
		);
		while(cursor.moveToNext()) {
			se = new SubscribeEntity();
			se.id     = cursor.getInt(cursor.getColumnIndex(ID));
			se.topic  = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(TOPIC)));
			se.qos    = cursor.getInt(cursor.getColumnIndex(QOS));
			list.add(se);
		}
		cursor.close();
		return list;
	}

	private SQLiteStatement mUpdateMessageStatement;
	public int updateMessageReadById(int id, boolean isRead){
		if(mUpdateMessageStatement == null) {
			mUpdateMessageStatement = getWritableDatabase().compileStatement(
					"UPDATE "+TABLE_MESSAGE+" SET "+ISREAD+" = ? WHERE "+ID+" = ?"
			);
		}
		mUpdateMessageStatement.bindLong(1,isRead?1:0);
		mUpdateMessageStatement.bindLong(2,id);
		if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB){
			return mUpdateMessageStatement.executeUpdateDelete();
		} else {
			mUpdateMessageStatement.execute();
			return 1;
		}

	}

	private SQLiteStatement mDeleteSubscribeStatement;
	public int deleteSubscribe(String topic, int qos){
		if (mDeleteSubscribeStatement == null) {
			mDeleteSubscribeStatement = getWritableDatabase().compileStatement(
					"DELETE FROM "+TABLE_SUBSCRIBE+" WHERE "+TOPIC+" = ? AND "+QOS+" = ?"
			);
		}
		SQLiteUtil.bindString(mDeleteSubscribeStatement,1,topic);
		mDeleteSubscribeStatement.bindLong(2,qos);
		if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB){
			return mDeleteSubscribeStatement.executeUpdateDelete();
		} else {
			mDeleteSubscribeStatement.execute();
			return 1;
		}
	}

	private SQLiteStatement mDeleteMessageStatement;
	private int deleteMessage(){
		if(mDeleteMessageStatement == null) {
			mDeleteMessageStatement = getWritableDatabase().compileStatement(
					"DELETE FROM "+TABLE_MESSAGE+" WHERE "+VALIDITY+" < ?"
			);
		}
		mDeleteMessageStatement.bindLong(1,System.currentTimeMillis());
		if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB){
			return mDeleteMessageStatement.executeUpdateDelete();
		} else {
			mDeleteMessageStatement.execute();
			return 1;
		}

	}
}
