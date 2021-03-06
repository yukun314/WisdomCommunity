package com.dnatech.community.sqlite;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.dnatech.community.entity.KeysEntity;
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
	private static final String TABLE_KEYS = "keys";

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
	private static final String NICK_NAME = "nickName";
	private static final String KEY_NAME = "keyName";
	private static final String KEY_NO = "keyNo";



	// database version, used to recognise when we need to upgrade
	// (delete and recreate)
	private static final int DATABASE_VERSION = 1;

	private static WCDatebaseHelper mHelper;

	public static WCDatebaseHelper getInstance(Context context){
		if(mHelper == null) {
			mHelper = new WCDatebaseHelper(context);
		}
		return mHelper;
	}

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
				TITLE+" TEXT NOT NULL ," +
				DESCRIPTION+" TEXT ," +
				TIME+" DATETIME NOT NULL," +
				URL+" TEXT NOT NULL ," +
				PUBLISHER+" TEXT ," +
				VALIDITY+" DATETIME NOT NULL , " +
				ISREAD+" BOOLEAN DEFAULT false , " +
				TOPIC+" TEXT NOT NULL , " +
				"constraint pk_message primary key ("+TITLE+","+URL+")" +
				");";

		String createSubscribeTable = "CREATE TABLE " +TABLE_SUBSCRIBE+" ("+
				TOPIC+" TEXT NOT NULL , " +
				QOS+" INTEGER NOT NULL , " +
				"constraint pk_subscribe primary key ("+TOPIC+","+QOS+")" +
				");";

		String createKeysTable = "CREATE TABLE "+TABLE_KEYS+" ("+
				NICK_NAME+" TEXT NOT NULL ," +
				KEY_NAME+" TEXT NOT NULL ," +
				KEY_NO+" TEXT NOT NULL ," +
				"constraint pk_message primary key ("+KEY_NO+")" +
				");";

		try {
			db.execSQL(createMessageTable);
			db.execSQL(createSubscribeTable);
			db.execSQL(createKeysTable);
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
	 * @param topic 订阅的主题
	 * @return
	 */
	public long insertMessage(String title, String description, Date time, String url,String publisher, Date validity, String topic) throws SQLException {
		if (mInsertMessageStatement == null) {
			mInsertMessageStatement = getWritableDatabase().compileStatement(
					"INSERT OR IGNORE INTO "+TABLE_MESSAGE+" ("+TITLE+", "+DESCRIPTION+", "+TIME+", "+URL+","+PUBLISHER+", "+VALIDITY+", "+TOPIC+") VALUES (?,?,?,?,?,?,?)"
			);
		}
		SQLiteUtil.bindString(mInsertMessageStatement,1, DESUtil.encrypt(title));
		SQLiteUtil.bindString(mInsertMessageStatement,2,DESUtil.encrypt(description));
		SQLiteUtil.bindDate(mInsertMessageStatement,3,time);
		SQLiteUtil.bindString(mInsertMessageStatement,4,DESUtil.encrypt(url));
		SQLiteUtil.bindString(mInsertMessageStatement,5,DESUtil.encrypt(publisher));
		SQLiteUtil.bindDate(mInsertMessageStatement,6,validity);
		SQLiteUtil.bindString(mInsertMessageStatement,7,DESUtil.encrypt(topic));
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

	private SQLiteStatement mInsertKeysStatement;
	public long insertKeys(String nickName, String keyName, String keyNo){
		if (mInsertKeysStatement == null) {
			mInsertKeysStatement = getWritableDatabase().compileStatement(
					"INSERT OR IGNORE INTO "+TABLE_KEYS+" ("+NICK_NAME+", "+KEY_NAME+", "+KEY_NO+") VALUES (?,?,?)"
			);
		}
		SQLiteUtil.bindString(mInsertKeysStatement,1, nickName);
		SQLiteUtil.bindString(mInsertKeysStatement,2, keyName);
		SQLiteUtil.bindString(mInsertKeysStatement,3, keyNo);
		return mInsertKeysStatement.executeInsert();
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
			me.title       = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(TITLE)));
			me.description = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(DESCRIPTION)));
			me.url         = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(URL)));
			me.publisher   = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(PUBLISHER)));
			me.setTime(SQLiteUtil.getDate(cursor, cursor.getColumnIndex(TIME)));
			me.setValidity(SQLiteUtil.getDate(cursor, cursor.getColumnIndex(VALIDITY)));
			me.isRead      = cursor.getInt(cursor.getColumnIndex(ISREAD)) == 0 ? false:true;
			me.topic       = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(TOPIC)));
			list.add(me);
		}
		cursor.close();
		return list;
	}

	public List<SubscribeEntity> selectSubscribeAll() {
		List<SubscribeEntity> list = new ArrayList<>();
		SubscribeEntity se = new SubscribeEntity();
		se.topic  = "default";
		se.qos    = 2;
		list.add(se);
		final Cursor cursor = getReadableDatabase().rawQuery(
				"select * from "+TABLE_SUBSCRIBE,
				null
		);
		while(cursor.moveToNext()) {
			se = new SubscribeEntity();
			se.topic  = DESUtil.decrypt(cursor.getString(cursor.getColumnIndex(TOPIC)));
			se.qos    = cursor.getInt(cursor.getColumnIndex(QOS));
			list.add(se);
		}
		cursor.close();
		return list;
	}

	public List<KeysEntity> selectKeysAll() {
		List<KeysEntity> list = new ArrayList<>();
		KeysEntity keys;
		final Cursor cursor = getReadableDatabase().rawQuery(
				"select * from "+TABLE_KEYS,
				null
		);
		while(cursor.moveToNext()) {
			keys = new KeysEntity();
			keys.nickName  = cursor.getString(cursor.getColumnIndex(NICK_NAME));
			keys.keyName   = cursor.getString(cursor.getColumnIndex(KEY_NAME));
			keys.keyNo   = cursor.getString(cursor.getColumnIndex(KEY_NO));
			list.add(keys);
		}
		cursor.close();
		return list;
	}

	private SQLiteStatement mUpdateMessageStatement;
	public int updateMessageRead(String title, String url, boolean isRead){
		if(mUpdateMessageStatement == null) {
			mUpdateMessageStatement = getWritableDatabase().compileStatement(
					"UPDATE "+TABLE_MESSAGE+" SET "+ISREAD+" = ? WHERE "+TITLE+" = ? AND "+URL+" = ?"
			);
		}
		mUpdateMessageStatement.bindLong(1,isRead?1:0);
		mUpdateMessageStatement.bindString(2,DESUtil.encrypt(title));
		mUpdateMessageStatement.bindString(3,DESUtil.encrypt(url));
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

	private SQLiteStatement mDeleteAllSubscribeStatement;
	public int deleteAllSubscribe(){
		if (mDeleteAllSubscribeStatement == null) {
			mDeleteAllSubscribeStatement = getWritableDatabase().compileStatement(
					"DELETE FROM "+TABLE_SUBSCRIBE
			);
		}
		if(VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB){
			return mDeleteAllSubscribeStatement.executeUpdateDelete();
		} else {
			mDeleteAllSubscribeStatement.execute();
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
