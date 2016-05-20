package com.dnatech.community.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dnatech.community.R;
import com.dnatech.community.entity.QRCodeContentsEntity;
import com.dnatech.community.entity.ResponseEntity;
import com.dnatech.community.okhttp.OkHttpClientManager;
import com.dnatech.community.service.NotificationService;
import com.dnatech.community.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.zxing.client.android.CaptureActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
	TextView tv;
	int i = 0;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 1) {
				tv.setText((String)msg.obj);
			} else if(msg.what == 2){
				Toast.makeText(MainActivity.this, "操作失败："+msg.obj, Toast.LENGTH_SHORT).show();
				tv.setText((String)msg.obj);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		tv = (TextView) findViewById(R.id.activity_main_tv);
		tv.setText(Utils.getClientId(this));
		Button button = (Button) findViewById(R.id.activity_main_button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MainActivity.this,NotificationActivity.class);
				startActivity(intent);
			}
		});

		Button button1 = (Button) findViewById(R.id.activity_main_button1);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
				MainActivity.this.startActivityForResult(intent,1);
			}
		});

		Button button2 = (Button) findViewById(R.id.activity_main_button2);
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
				MainActivity.this.startActivity(intent);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == CaptureActivity.resultCode) {
			String type = data.getStringExtra(CaptureActivity.Type);
			String format = data.getStringExtra(CaptureActivity.Format);
			String contents = data.getStringExtra(CaptureActivity.Contents);
			Gson gson = new Gson();
			try {
				QRCodeContentsEntity qrc = gson.fromJson(contents, QRCodeContentsEntity.class);
				tv.setText(qrc.communityId+"  "+qrc.userId);
				qrc.devId = Utils.getClientId(MainActivity.this);
				OkHttpClientManager.request("authorize", "pull_dev", gson.toJson(qrc), new Callback() {
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
			} catch (JsonSyntaxException e){
				e.printStackTrace();
				Toast.makeText(MainActivity.this, "二维码的格式或内容不正确！", Toast.LENGTH_SHORT).show();
			}

		}
	}
}
