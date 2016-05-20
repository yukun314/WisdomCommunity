package com.dnatech.community.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.dnatech.community.R;
import com.dnatech.community.app.BaseActivity;
import com.dnatech.community.entity.MessageEntity;
import com.dnatech.community.sqlite.WCDatebaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by zyk on 2016/5/16.
 */
public class NotificationActivity extends BaseActivity{

	public static final String MESSAGE = "message";

	private List<MessageEntity> mList;
	private MyAdapter mAdapter;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == 1){
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification);
		init();
	}

	private void init(){
		mList = new ArrayList<>();
		ListView listview = (ListView) findViewById(R.id.activity_notification_list);
		mAdapter = new MyAdapter();
		listview.setAdapter(mAdapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent();
				intent.setClass(NotificationActivity.this,NotificationDetailActivity.class);
				intent.putExtra(MESSAGE,mList.get(position));
				NotificationActivity.this.startActivity(intent);
			}
		});
		new Thread(){
			@Override
			public void run() {
				super.run();
				mList = WCDatebaseHelper.getInstance(NotificationActivity.this).selectMessageAll();
				mHandler.sendEmptyMessage(1);
			}
		}.start();
	}


	private class MyAdapter extends BaseAdapter {

		private ViewHolder mHolder;
		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public Object getItem(int position) {
			return mList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				mHolder = new ViewHolder();
				convertView = LayoutInflater.from(NotificationActivity.this)
						.inflate(R.layout.activity_notification_item, null);
				mHolder.mTitle = (TextView) convertView.findViewById(R.id.activity_notification_item_title);
				mHolder.mDescription = (TextView) convertView.findViewById(R.id.activity_notification_item_description);
				mHolder.mIsRead = (TextView) convertView.findViewById(R.id.activity_notification_item_isread);
				convertView.setTag(mHolder);
			} else {
				mHolder = (ViewHolder) convertView.getTag();
			}

			MessageEntity me = mList.get(position);
			mHolder.mTitle.setText(me.title);
			mHolder.mDescription.setText(me.description);
			if(me.isRead){
				mHolder.mIsRead.setBackgroundColor(Color.argb(0,255,0,0));
			} else {
				mHolder.mIsRead.setBackgroundColor(Color.argb(255,255,0,0));
			}

			return convertView;
		}
	}

	private class ViewHolder{
		public TextView mTitle;
		public TextView mDescription;
		public TextView mIsRead;
	}
}
