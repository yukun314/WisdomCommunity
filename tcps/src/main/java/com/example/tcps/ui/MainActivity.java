package com.example.tcps.ui;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.example.tcps.R;

import android.support.v4.app.Fragment;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;

@SuppressLint("ShowToast") public class MainActivity extends Activity {

	private Button mButtonRelays1;
	private Button mButtonRelays2;
	private Button mButtonConnect;
	private EditText mEditTextIPAddress;
	private EditText mEditTextViewURL;
	private EditText mEditViewViewPort;
	private RadioGroup mRadioGroupAddress;
	private RadioButton mRadioButtonIP;
	private RadioButton mRadioButtonURL;
	private TextView mTextViewNetRecv;
	private AppContext appContext;
	private String mLocalIPAddress;
	private String mLocalPort;
	private String mLocalURL;
	private boolean mRelays1Status = false;
	private boolean mRelays2Status = false;
	public static final String ip_string = "COM_IP";
	public static final String url_string = "COM_URL";
	private String mRadioGroupStatus = ip_string;
	private String FILE_NAME;

	public boolean NetConnectStatus = false;
	ThreadSendRead1 mThreadSendRead1 = null;
	ThreadSendRead2 mThreadSendRead2 = null;

	boolean mThreadRead1Status = true;
	boolean mThreadRead2Status = true;

	static Socket socket = null;
	static InputStream in = null;
	static OutputStream out = null;
	Lock lockread = new ReentrantLock();
	Lock lockwrite = new ReentrantLock();

	public void SocketClose(){
		if(mThreadSendRead1 != null){
			mThreadRead1Status = false;
			try {
				mThreadSendRead1.join(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			mThreadSendRead1 = null;
		}
		if(mThreadSendRead2 != null){
			mThreadRead2Status = false;
			try {
				mThreadSendRead2.join(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			mThreadSendRead2 = null;
		}
		if (socket != null) {
			try {
				socket.close();
				socket = null;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	public void SocketConnect(){
		new Thread(){
			@Override
			public void run(){
				try {
					String HostAddress;
					if(mRadioGroupStatus.equals(ip_string)){
						HostAddress = mLocalIPAddress;
					}else{

						HostAddress = InetAddress.getByName(mLocalURL).getHostAddress();
					}
					if(socket == null){

						socket = new Socket(HostAddress, Integer.parseInt(mLocalPort));
						in = socket.getInputStream();
						out = socket.getOutputStream();
						socket.setSoTimeout(1000);
						if(mThreadSendRead2 == null){
							mThreadRead1Status = true;
							mThreadSendRead2 = new ThreadSendRead2();
							mThreadSendRead2.start();
						}
						if(mThreadSendRead1 == null){
							mThreadRead2Status = true;
							mThreadSendRead1 = new ThreadSendRead1();
							mThreadSendRead1.start();
						}
						System.out.println("Connected to server ... sending echo string");
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();

	}
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		appContext = (AppContext) getApplication();
		FILE_NAME = appContext.getString(R.string.settings_filename);
		mButtonRelays1 = (Button) findViewById(R.id.ButtonRelays1);
		mButtonRelays2 = (Button) findViewById(R.id.ButtonRelays2);
		mButtonConnect = (Button) findViewById(R.id.ButtonConnect);
		mEditTextIPAddress = (EditText) findViewById(R.id.EditTextIPAddress);
		mEditTextViewURL = (EditText) findViewById(R.id.EditTextViewURL);
		mEditViewViewPort = (EditText) findViewById(R.id.EditViewViewPort);
		mRadioGroupAddress = (RadioGroup) findViewById(R.id.RadioGroupAddress);
		mRadioButtonIP = (RadioButton) findViewById(R.id.RadioButtonIP);
		mRadioButtonURL = (RadioButton) findViewById(R.id.RadioButtonURL);
		mTextViewNetRecv = (TextView) findViewById(R.id.TextViewNetRecv);

		mRadioButtonIP.setChecked(true);

		SharedPreferences sharedPreferences = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
		//从文件中获取保存的数据
		mLocalIPAddress = sharedPreferences.getString("mLocalIPAddress", "");
		mLocalURL = sharedPreferences.getString("mLocalURL", "");
		mLocalPort = sharedPreferences.getString("mLocalPort", "");
		mEditTextIPAddress.setText(mLocalIPAddress);
		mEditTextViewURL.setText(mLocalURL);
		mEditViewViewPort.setText(mLocalPort);
		mButtonConnect.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLocalIPAddress = mEditTextIPAddress.getText().toString();
				mLocalURL = mEditTextViewURL.getText().toString();
				mLocalPort = mEditViewViewPort.getText().toString();
				if(NetConnectStatus){
					SocketClose();
					NetConnectStatus = false;
					mButtonConnect.setText("连接");
				}else{
					if(mRadioGroupStatus.equals(ip_string)){
						if(mLocalIPAddress.equals("") || mLocalPort.equals("")){
							Toast.makeText(appContext, "IP或端口不能为空", Toast.LENGTH_SHORT).show();
							return;
						}
					}else{
						if(mLocalURL.equals("") || mLocalPort.equals("")){
							Toast.makeText(appContext, "网址或端口不能为空", Toast.LENGTH_SHORT).show();
							return;
						}
					}

					SocketConnect();
					SharedPreferences sharedPreferences = appContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
					Editor editor = sharedPreferences.edit();
					//添加要保存的数据
					editor.putString("mLocalIPAddress",mLocalIPAddress);
					editor.putString("mLocalURL",mLocalURL);
					editor.putString("mLocalPort",mLocalPort);
					//确认保存
					editor.commit();
					NetConnectStatus = true;
					mButtonConnect.setText("断开");

				}
			}
		});
		mButtonRelays1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(mButtonRelays1.getText().equals("继电器1关")){
					TcpSendData("on1");
				}else{
					TcpSendData("off1");
				}

			}


		});
		mButtonRelays2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(mButtonRelays2.getText().equals("继电器2关")){
					TcpSendData("on2");
				}else{
					TcpSendData("off2");
				}
			}
		});
		mRadioGroupAddress.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				if(checkedId == mRadioButtonIP.getId()){
					mRadioGroupStatus = ip_string;
				}
				else{
					mRadioGroupStatus = url_string;
				}
			}

		});
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(mThreadSendRead1 != null){
			mThreadRead1Status = false;
			try {
				mThreadSendRead1.join(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			mThreadSendRead1 = null;
		}
		if(mThreadSendRead2 != null){
			mThreadRead2Status = false;
			try {
				mThreadSendRead2.join(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
			mThreadSendRead2 = null;
		}
		super.onDestroy();

	}
	/**
	 * @param args
	 */
	@SuppressLint("HandlerLeak") public Handler mHandler=new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case 1:
					mButtonRelays1.setText("继电器1开");
					break;
				case 2:
					mButtonRelays1.setText("继电器1关");
					break;
				case 3:
					mButtonRelays2.setText("继电器2开");
					break;
				case 4:
					mButtonRelays2.setText("继电器2关");
					break;
				case 5:
					mTextViewNetRecv.setText((String)msg.obj);
					break;
				default:
					break;
			}
			super.handleMessage(msg);
		}
	};
	public class ThreadSendRead1 extends Thread{
		@Override
		public void run() {
			// create socket that is connected to server on specified port
			while(mThreadRead1Status){
				try{

					byte[] data = new byte[10];

					out.write("read1".getBytes());
					// receive
					int totalBytesRcvd = 0;// total bytes received so far
					int bytesRcvd;// Bytes received in last read
					while (totalBytesRcvd == 0) {
						bytesRcvd = in.read(data, totalBytesRcvd, data.length
								- totalBytesRcvd);
						if (bytesRcvd == -1) {
							throw new SocketException("Connection closed prematurely");
						}
						totalBytesRcvd += bytesRcvd;
					}
					String datarcvd = new String(data).trim();
					Log.i("ThreadSendRead1", datarcvd);
					if(datarcvd.equals("on1")){
						Message msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
					}else if(datarcvd.equals("off1")){
						Message msg = new Message();
						msg.what = 2;
						mHandler.sendMessage(msg);
					}
				}catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
	}
	public class ThreadSendRead2 extends Thread{
		@Override
		public void run() {

			while(mThreadRead2Status){

				try{

					byte[] data = new byte[10];
					out.write("read2".getBytes());
					// receive
					int totalBytesRcvd = 0;// total bytes received so far
					int bytesRcvd;// Bytes received in last read
					while (totalBytesRcvd == 0) {
						bytesRcvd = in.read(data, totalBytesRcvd, data.length
								- totalBytesRcvd);
						if (bytesRcvd == -1) {
							throw new SocketException("Connection closed prematurely");
						}
						totalBytesRcvd += bytesRcvd;
					}
					String datarcvd = new String(data).trim();
					Log.i("ThreadSendRead2", datarcvd);
					if(datarcvd.equals("on2")){
						Message msg = new Message();
						msg.what = 3;
						mHandler.sendMessage(msg);
					}else if(datarcvd.equals("off2")){
						Message msg = new Message();
						msg.what = 4;
						mHandler.sendMessage(msg);
					}
				}catch (IOException e) {
					e.printStackTrace();
				}
				try {
					sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}
	public void TcpSendData(final String sendData) {
		new Thread(){
			@Override
			public void run(){
				byte[] data = new byte[10];
				// create socket that is connected to server on specified port
				try {

					out.write(sendData.getBytes());

					// receive
					int totalBytesRcvd = 0;// total bytes received so far
					int bytesRcvd;// Bytes received in last read

					while (totalBytesRcvd == 0) {
						bytesRcvd = in.read(data, totalBytesRcvd, data.length
								- totalBytesRcvd);
						if (bytesRcvd == -1) {
							throw new SocketException("Connection closed prematurely");
						}
						totalBytesRcvd += bytesRcvd;
					}
					String datarcvd = new String(data).trim();
					if(datarcvd.equals("on1")){
						Message msg = new Message();
						msg.what = 1;
						mHandler.sendMessage(msg);
					}
					else if(datarcvd.equals("off1")){
						Message msg = new Message();
						msg.what = 2;
						mHandler.sendMessage(msg);
					}
					else if(datarcvd.equals("on2")){
						Message msg = new Message();
						msg.what = 3;
						mHandler.sendMessage(msg);
					}else if(datarcvd.equals("off2")){
						Message msg = new Message();
						msg.what = 4;
						mHandler.sendMessage(msg);
					}
					System.out.println("Received:" + new String(data));

				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();


	}
	private static long firstTime;
	/**
	 * 连续按两次返回键就退出
	 */
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (firstTime + 2000 > System.currentTimeMillis()) {
			Log.i("huwei", getPackageName()+"程序退出！");
//			ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//			am.killBackgroundProcesses(getPackageName()); // API Level至少为8才能使用
			AppManager.getAppManager().AppExit(this);
			super.onBackPressed();
		} else {
			DisplayToast(this, getString(R.string.exitAppHit));
		}
		firstTime = System.currentTimeMillis();
	}
	public static void DisplayToast(Context context, CharSequence charSequence)
	{
		Toast.makeText(context, charSequence, Toast.LENGTH_SHORT).show();
	}
}
