package com.dnatech.community.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;

import com.dnatech.community.entity.MessageEntity;
import com.dnatech.community.entity.SubscribeEntity;
import com.dnatech.community.sqlite.WCDatebaseHelper;
import com.dnatech.community.utils.NetworkUtils;
import com.dnatech.community.utils.Utils;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.File;
import java.util.List;

/**
 * Created by zyk on 2016/5/12.
 */
public class NotificationService extends Service implements MqttCallback {

	final String TAG = "NotificationService";
	private static String AlarmManagerAction = "com.dnatech.community.alarm";
	// our client object - instantiated on connect
	private MqttAsyncClient mClient = null;
	private MqttConnectOptions connectOptions;

	// An intent receiver to deal with changes in network connectivity
	private NetworkConnectionIntentReceiver networkConnectionMonitor;
	private BackgroundDataPreferenceReceiver backgroundDataPreferenceMonitor;
	private AlarmManagerReceiver alarmManager;
	private volatile boolean backgroundDataEnabled = true;

	private WCDatebaseHelper mDatebase;

	private ConnectActionListener mConnectAction = new ConnectActionListener();
	private SubscribeActionListener mSubscribeAction = new SubscribeActionListener();

	@Override
	public void onCreate() {
		super.onCreate();
		mDatebase = new WCDatebaseHelper(this);
		long id = mDatebase.insertMessage("title","简介",null,"url","publisher",null);
//		mDatebase.selectMessageAll();
		mDatebase.updateMessageReadById((int)id,true);
		//新建客户端并连接MQTT服务器
		newClient();
		reconnect();

	}

	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId) {
		System.out.println("NotificationService onStartCommand");
		// run till explicitly stopped, restart when
		// process restarted
		registerBroadcastReceivers();
		//被系统kill之后，尝试重新启动
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		unregisterBroadcastReceivers();
		disconnect();
		mDatebase = null;
		super.onDestroy();
		System.out.println("NotificationService onDestroy");
		//当启动该service的app在前台时 service被杀掉不会执行onDestroy，当app退出后才会执行onDestroy
		//重启该服务
		Intent sevice = new Intent(this, NotificationService.class);
		this.startService(sevice);
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void connectionLost(Throwable cause) {
		System.out.println("NotificationService connectionLost");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("NotificationService messageArrived:"+message);
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("NotificationService deliveryComplete");
	}

	/**
	 * @return whether the android service can be regarded as online
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isAvailable()
				&& cm.getActiveNetworkInfo().isConnected()
				&& backgroundDataEnabled) {
			return true;
		}
		return false;
	}

	private void reconnect() {
		if (mClient == null) {
			newClient();
		}
		if(connectOptions == null){
			getConnectOptions();
		}
		try {
			mClient.connect(connectOptions, null, mConnectAction);
		} catch (MqttException e) {
			e.printStackTrace();
			System.out.println("NotificationService reconnect:"+e);
			//FIXME
		}
	}

	private void disconnect(){
		if(mClient != null && mClient.isConnected()) {
			try {
				mClient.disconnect(null, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	//FIXME
	private void notifyClientsOffline(){
		//网络中断时被调用
	}

	private void newClient(){
		try {
			// ask Android where we can put files
			File myDir = getExternalFilesDir(TAG);
			String directory = "";
			if (myDir == null) {
				// No external storage, use internal storage instead.
				myDir = getDir(TAG, Context.MODE_PRIVATE);
				if (myDir == null) {
					directory = System.getProperty("java.io.tmpdir");
//				return;
				} else {
					directory = myDir.getAbsolutePath();
				}
			} else {
				directory = myDir.getAbsolutePath();
			}
			// use that to setup MQTT client persistence storage
			MqttDefaultFilePersistence persistence = new MqttDefaultFilePersistence(directory);

			mClient = new MqttAsyncClient(NetworkUtils.MessageServerURL, Utils.getClientId(),
					persistence, new AlarmPingSender(this));

			mClient.setCallback(this);
		} catch (MqttException e){
			System.out.println("NotificationService newClient;"+e);
			//FIXME
		}
	}

	private void getConnectOptions(){
		connectOptions = new MqttConnectOptions();
		//FIXME
		if(NetworkUtils.MessageServerURL.startsWith("ssl")){
//			FileInputStream key = new FileInputStream(ssl_key);
//			connectOptions.setSocketFactory();
		}
//		connectOptions.setCleanSession(true);//默认为true
//		connectOptions.setConnectionTimeout(30);//默认超时时间为30s
//		connectOptions.setKeepAliveInterval(60);//默认60
//		connectOptions.setUserName(username);//userName
//		connectOptions.setPassword(password.toCharArray());//密码
		//？
//		connectOptions.setWill(topic, message.getBytes(), qos.intValue(),
//				retained.booleanValue());
	}

	private void registerBroadcastReceivers() {
		if (networkConnectionMonitor == null) {
			networkConnectionMonitor = new NetworkConnectionIntentReceiver();
			registerReceiver(networkConnectionMonitor, new IntentFilter(
					ConnectivityManager.CONNECTIVITY_ACTION));
		}

		if(alarmManager == null) {
			alarmManager = new AlarmManagerReceiver();
			registerReceiver(alarmManager, new IntentFilter(AlarmManagerAction));
		}


		if (Build.VERSION.SDK_INT < 14 /**Build.VERSION_CODES.ICE_CREAM_SANDWICH**/) {
			// Support the old system for background data preferences
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			backgroundDataEnabled = cm.getBackgroundDataSetting();
			if (backgroundDataPreferenceMonitor == null) {
				backgroundDataPreferenceMonitor = new BackgroundDataPreferenceReceiver();
				registerReceiver(
						backgroundDataPreferenceMonitor,
						new IntentFilter(
								ConnectivityManager.ACTION_BACKGROUND_DATA_SETTING_CHANGED));
			}
		}
	}

	private void unregisterBroadcastReceivers() {
		if (networkConnectionMonitor != null) {
			unregisterReceiver(networkConnectionMonitor);
			networkConnectionMonitor = null;
		}

		if(alarmManager != null) {
			unregisterReceiver(alarmManager);
			alarmManager = null;
		}

		if (Build.VERSION.SDK_INT < 14 /**Build.VERSION_CODES.ICE_CREAM_SANDWICH**/) {
			if (backgroundDataPreferenceMonitor != null) {
				unregisterReceiver(backgroundDataPreferenceMonitor);
			}
		}
	}

	/*
	 * Called in response to a change in network connection - after losing a
	 * connection to the server, this allows us to wait until we have a usable
	 * data connection again
	 */
	private class NetworkConnectionIntentReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("NetworkConnectionIntentReceiver");
//			traceDebug(TAG, "Internal network status receive.");
			// we protect against the phone switching off
			// by requesting a wake lock - we request the minimum possible wake
			// lock - just enough to keep the CPU running until we've finished
			PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
			PowerManager.WakeLock wl = pm
					.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MQTT");
			wl.acquire();
//			traceDebug(TAG, "Reconnect for Network recovery.");
			if (isOnline()) {
//				traceDebug(TAG, "Online,reconnect.");
				// we have an internet connection - have another try at
				// connecting
				reconnect();
			} else {
				notifyClientsOffline();
			}

			wl.release();
		}
	}

	/**
	 * Detect changes of the Allow Background Data setting - only used below
	 * ICE_CREAM_SANDWICH
	 */
	private class BackgroundDataPreferenceReceiver extends BroadcastReceiver {

		@SuppressWarnings("deprecation")
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("BackgroundDataPreferenceReceiver");
			ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
//			traceDebug(TAG, "Reconnect since BroadcastReceiver.");
			if (cm.getBackgroundDataSetting()) {
				if (!backgroundDataEnabled) {
					backgroundDataEnabled = true;
					// we have the Internet connection - have another try at
					// connecting
					reconnect();
				}
			} else {
				backgroundDataEnabled = false;
				notifyClientsOffline();
			}
		}
	}

	private class ConnectActionListener implements IMqttActionListener{
		@Override
		public void onSuccess(IMqttToken asyncActionToken) {
			//订阅 自己的主题
			try {
				List<SubscribeEntity> list = mDatebase.selectSubscribeAll();
				int count = list.size();
				String[]topic = new String[count];
				int[]qos = new int[count];
				for(int i = 0;i<count;i++){
					SubscribeEntity se = list.get(i);
					topic[i] = se.topic;
					qos[i] = se.qos;
				}
				mClient.subscribe(topic,qos, null, mSubscribeAction);
			} catch (MqttException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
			System.out.println("链接失败:"+exception);
			if(isOnline()) {
				System.out.println("有网络 5分钟 后重新链接");
				Intent intent = new Intent(AlarmManagerAction);
				PendingIntent pendingIntent = PendingIntent.getBroadcast(NotificationService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
				//获取系统进程
				AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
				am.set(AlarmManager.RTC, System.currentTimeMillis()+5*60*1000, pendingIntent);
			}
		}
	}

	private class SubscribeActionListener implements IMqttActionListener{

		@Override
		public void onSuccess(IMqttToken asyncActionToken) {
			System.out.println("订阅成功");
		}

		@Override
		public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
			//FIXME 订阅失败的处理
			System.out.println("订阅失败 重启该service："+exception);
			Intent sevice = new Intent(NotificationService.this, NotificationService.class);
			NotificationService.this.stopService(sevice);
		}
	}

	private class AlarmManagerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			System.out.println("接收到重连的广播 :"+action);
			if (action.equals(AlarmManagerAction) || action == AlarmManagerAction) {
				if (isOnline()) {
					System.out.println("开始重新链接");
					reconnect();
				}
			}
		}
	}
}
