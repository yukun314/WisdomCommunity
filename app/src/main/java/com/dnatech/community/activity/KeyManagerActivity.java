package com.dnatech.community.activity;

import android.annotation.TargetApi;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dnatech.community.R;
import com.dnatech.community.app.BaseActivity;
import com.dnatech.community.entity.KeysEntity;
import com.dnatech.community.entity.ResponseEntity;
import com.dnatech.community.okhttp.OkHttpClientManager;
import com.dnatech.community.sqlite.WCDatebaseHelper;
import com.dnatech.community.utils.Utils;
import com.dnatech.community.widget.swipe.BaseSwipListAdapter;
import com.dnatech.community.widget.swipe.SwipeMenu;
import com.dnatech.community.widget.swipe.SwipeMenuCreator;
import com.dnatech.community.widget.swipe.SwipeMenuItem;
import com.dnatech.community.widget.swipe.SwipeMenuListView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by zyk on 2016/5/20.
 * 钥匙管理
 */
public class KeyManagerActivity extends BaseActivity {

	private SwipeMenuListView mListView;
	private List<KeysEntity> mList;
	private MyAdapter mAdapter;
	private SwipeRefreshLayout srl;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == 1){ //成功
				System.out.println("1 message.obj:"+msg.obj);
				String json = (String)msg.obj;
				Gson gson = new Gson();
				Type listType = new TypeToken<ArrayList<KeysEntity>>(){}.getType();
				ArrayList<KeysEntity> keys = gson.fromJson(json, listType);
				int count = keys.size();
				for(int i = 0;i<count;i++) {
					KeysEntity k = keys.get(i);
					long number = WCDatebaseHelper.getInstance(KeyManagerActivity.this).insertKeys(k.keyName,k.keyName,k.keyNo);
					if(number >0) {
						mList.add(k);
					}
				}
				mAdapter.notifyDataSetChanged();
			} else if(msg.what == 2) {//失败
				System.out.println("2 message.obj:"+msg.obj);
				Toast.makeText(KeyManagerActivity.this,"失败："+msg.obj,Toast.LENGTH_LONG).show();
			} else if (msg.what == 3) {
				mAdapter.notifyDataSetChanged();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_key_manager);
		init();
	}

	private void init(){

		Button button = (Button) findViewById(R.id.activity_key_manager_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//发送网络请求
				OkHttpClientManager.request("authorize", "getkeys", "{\"devId\":\""+Utils.getClientId(KeyManagerActivity.this)+"\"}", new Callback() {
					@Override
					public void onFailure(Call call, IOException e) {
						Message message = new Message();
						message.what = 2;
						message.obj = e;
						mHandler.sendMessage(message);
					}

					@Override
					public void onResponse(Call call, final Response response) throws IOException {
						String result = response.body().string();
						Gson gson = new Gson();
						System.out.println("result:"+result);
						ResponseEntity re = gson.fromJson(result, ResponseEntity.class);
						Message message = new Message();
						if(re.code == 0){//操作成功
							message.what = 1;
							message.obj = re.data;
						} else {
							message.what = 2;
							message.obj = re.msg;
						}

						mHandler.sendMessage(message);
					}
				});
			}
		});

		mList = new ArrayList<>();
		mListView = (SwipeMenuListView) findViewById(R.id.activity_key_manager_listview);

		mAdapter = new MyAdapter();
		mListView.setAdapter(mAdapter);

		SwipeMenuCreator creator = new SwipeMenuCreator() {
			@Override
			public void create(SwipeMenu menu) {
				SwipeMenuItem item = new SwipeMenuItem(KeyManagerActivity.this);
				item.setBackground(new ColorDrawable(Color.rgb(2,200,67)));
				item.setTitle("开锁");
				item.setWidth(Utils.dp2px(90, KeyManagerActivity.this));
				item.setTitleSize(18);
				item.setTitleColor(Color.WHITE);
				menu.addMenuItem(item);
			}
		};
		mListView.setMenuCreator(creator);
		mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
				if(index == 0) {
					System.out.println("执行开锁操作");
				}
				return false;
			}
		});

//		srl = (SwipeRefreshLayout) findViewById(R.id.activity_key_manager_srl);
//		srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//			@Override
//			public void onRefresh() {
//
//			}
//		});
//		// 顶部刷新的样式
////		srl.setColorScheme(getResources().getColor(android.R.color.holo_red_light), getResources().getColor(android.R.color.holo_green_light),
////				getResources().getColor(android.R.color.holo_blue_bright), getResources().getColor(android.R.color.holo_orange_light));
//		srl.setColorSchemeColors(Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW);
//		srl.setProgressBackgroundColorSchemeColor(Color.LTGRAY);

		loadData();
	}

	private void loadData(){
		new Thread(){
			@Override
			public void run() {
				super.run();
				mList.clear();
				mList = WCDatebaseHelper.getInstance(KeyManagerActivity.this).selectKeysAll();
				if(mList.size() > 0) {
					mHandler.sendEmptyMessage(3);
				}
			}
		}.start();
	}
	private class MyAdapter extends BaseSwipListAdapter {
		private ViewHolder holder;
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
				convertView = View.inflate(getApplicationContext(),
						R.layout.activity_key_manager_item, null);
				holder = new ViewHolder();
				holder.mText = (TextView) convertView.findViewById(R.id.activity_key_manager_item_text);
				convertView.setTag(holder);
			}else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.mText.setText(mList.get(position).nickName);
			return convertView;
		}
	}

	private class ViewHolder {
		public TextView mText;
	}
}
