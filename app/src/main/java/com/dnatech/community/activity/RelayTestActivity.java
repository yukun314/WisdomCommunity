package com.dnatech.community.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.dnatech.community.R;
import com.dnatech.community.app.BaseActivity;
import com.dnatech.community.utils.NetworkUtils;
import com.dnatech.community.utils.Tcpclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by zyk on 2016/5/25.
 */
public class RelayTestActivity extends BaseActivity {
	private TextView mMessage;
//	private Tcpclient mClient;
	private final String SERVERIP = "192.168.0.105";
	private final int SERVERPORT = 5000;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 101) {
				mMessage.setText((String) msg.obj);
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_relay);
//		mClient = new Tcpclient();

		mMessage = (TextView) findViewById(R.id.activity_relay_text);
		Button button1 = (Button) findViewById(R.id.activity_relaty_button1);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (mClient.isConnected()) {
					new Thread(){
						@Override
						public void run() {
							super.run();
//							boolean is = mClient.sendToServer("on1:00");
//							System.out.println("sendToServer on1:"+is);
							Tcpclient.tcpSend("on1:20",mHandler);

						}
					}.start();
//					mClient.sendToServer("on1:00");
				}
//			}
		});

		Button button2 = (Button) findViewById(R.id.activity_relaty_button2);
		button2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (mClient.isConnected()) {
					new Thread(){
						@Override
						public void run() {
							super.run();
//							boolean is = mClient.sendToServer("off1");
//							System.out.println("sendToServer off1:"+is);
							Tcpclient.tcpSend("off1",mHandler);
						}
					}.start();
//					mClient.sendToServer("off1");
				}
//			}
		});

		Button button3 = (Button) findViewById(R.id.activity_relaty_button3);
		button3.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (mClient.isConnected()) {
//					mClient.sendToServer("on2:00");
					new Thread(){
						@Override
						public void run() {
							super.run();
//							boolean is = mClient.sendToServer("on2:00");
//							System.out.println("sendToServer on2:"+is);
							Tcpclient.tcpSend("on2:20",mHandler);
						}
					}.start();
				}
//			}
		});

		Button button4 = (Button) findViewById(R.id.activity_relaty_button4);
		button4.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (mClient.isConnected()) {
//					mClient.sendToServer("off2");
					new Thread(){
						@Override
						public void run() {
							super.run();
//							boolean is  = mClient.sendToServer("off2");
//							System.out.println("sendToServer off2:"+is);
							Tcpclient.tcpSend("off2",mHandler);
						}
					}.start();
				}
//			}
		});

		Button button5 = (Button) findViewById(R.id.activity_relaty_button5);
		button5.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (mClient.isConnected()) {
					new Thread(){
						@Override
						public void run() {
							super.run();
//							boolean is = mClient.sendToServer("read1");
//							System.out.println("sendToServer read1:"+is);
							Tcpclient.tcpSend("read1",mHandler);
						}
					}.start();
//					mClient.sendToServer("read1");
				}
//			}
		});

		Button button6 = (Button) findViewById(R.id.activity_relaty_button6);
		button6.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				if (mClient.isConnected()) {
					new Thread(){
						@Override
						public void run() {
							super.run();
//							boolean is = mClient.sendToServer("read2");
//							System.out.println("sendToServer read2:"+is);
							Tcpclient.tcpSend("read2",mHandler);
						}
					}.start();
//					mClient.sendToServer("read2");
				}
//			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
//		if (mClient == null || (mClient != null && !mClient.isConnected())) {
			new Thread(){
				@Override
				public void run() {
					super.run();
//					boolean is = mClient.connectServer(SERVERIP, SERVERPORT);
//					System.out.println("connectServer:"+is);
				}
			}.start();
//			mClient.connectServer(SERVERIP, SERVERPORT);
//		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
//		if (mClient == null || (mClient != null && !mClient.isConnected())) {
			new Thread(){
				@Override
				public void run() {
					super.run();
//					boolean  is = mClient.connectServer(SERVERIP, SERVERPORT);
//					System.out.println("connectServer:"+is);
				}
			}.start();
//			mClient.connectServer(SERVERIP, SERVERPORT);
//		}
	}

	@Override
	protected void onPause() {
		super.onPause();
//		mClient.close();
	}

	private void connect() {
		try {

			Socket connection = new Socket("192.168.0.105", 5000);
			BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			PrintWriter out = new PrintWriter(connection.getOutputStream(), true);
//			String info;
//			info = input.readLine();
			String info = "on1";
			System.out.println(info);
			boolean done = false;
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String sInput;

			// out.println("BYE");

			while (!done) {

				info = input.readLine();
				System.out.println(info);

			}
			connection.close();
		} catch (SecurityException e) {
			System.out.println("SecurityException when connecting Server!" + e);
		} catch (IOException e) {
			System.out.println("IOException when connecting Server!" + e);
		}
	}
}
