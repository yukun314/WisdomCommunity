package com.dnatech.community.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
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
import android.support.v4.app.NotificationCompat;

import com.dnatech.community.R;
import com.dnatech.community.activity.MainActivity;
import com.dnatech.community.activity.NotificationActivity;
import com.dnatech.community.entity.MessageEntity;
import com.dnatech.community.entity.SubscribeEntity;
import com.dnatech.community.sqlite.WCDatebaseHelper;
import com.dnatech.community.utils.NetworkUtils;
import com.dnatech.community.utils.Utils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

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
import java.util.Date;
import java.util.List;

/**
 * Created by zyk on 2016/5/12.
 * At Most Once (QoS=0)  最多一次
 * At Least Once (QoS=1) 至少一次
 * Exactly Once (QoS=2) 恰好一次
 */
public class NotificationService extends Service implements MqttCallback {

	final String TAG = "NotificationService";
	private static String AlarmManagerAction = "com.dnatech.community.alarm";
	public static String newMessageAction = "com.dnatech.community.newmessage";
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
		mDatebase = WCDatebaseHelper.getInstance(this);
		mDatebase.insertMessage("title","简介",new Date(),"url","publisher",new Date(System.currentTimeMillis()+24*60*60*1000),"a小区");
//		mDatebase.selectMessageAll();
		mDatebase.updateMessageRead("title", "url",true);
		mDatebase.insertSubscribe("青海大厦",2);
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
		// Called when the connection to the server has been lost.
		// An application may choose to implement reconnection
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("NotificationService messageArrived:"+message);
		// Called when a message arrives from the server that matches any
		// subscription made by the client
		try {
			String result = new String(message.getPayload());
			Gson gson = new Gson();
			MessageEntity me = gson.fromJson(result, MessageEntity.class);
			me.topic = topic;
			List<MessageEntity> list = mDatebase.selectMessageAll();
			boolean exist = false;
			int count = list.size();
			for(int i=0;i<count;i++) {
				MessageEntity temp = list.get(i);
				if((temp.title.equals(me.title) && temp.url.equals(me.url))||(temp.title == me.title && temp.url == me.url)) {
					System.out.println("该条消息已经收到了");
					exist = true;
					break;
				}
			}
			//若消息已经接收过 直接忽略，否则进行提醒
			if(!exist){
//				if(Utils.isAppInForeground(this)) {
////					System.out.println("客户端在线直接给客户端发有新消息的广播");
//					Intent intent = new Intent(newMessageAction);
//					sendBroadcast(intent);
//				} else {
//					System.out.println("客户端不在线 进行通知栏提醒");
				System.out.println("收到新消息："+me);
				mDatebase.insertMessage(me.title,me.description,me.time,me.url,me.publisher,me.validity,me.topic);
					sendNotification(me);
//				}
			}
		}catch (JsonSyntaxException e){
			e.printStackTrace();
			System.out.println("JsonSyntaxException :"+e);
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("NotificationService deliveryComplete");
		// Called when a message has been delivered to the
		// server. The token passed in here is the same one
		// that was returned from the original call to publish.
		// This allows applications to perform asynchronous
		// delivery without blocking until delivery completes.
		//
		// This sample demonstrates asynchronous deliver, registering
		// a callback to be notified on each call to publish.
		//
		// The deliveryComplete method will also be called if
		// the callback is set on the client
		//
		// note that token.getTopics() returns an array so we convert to a string
		// before printing it on the console
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

	//FIXME 点击跳转的Activity 和图标 根据需要修改
	private void sendNotification(MessageEntity me) {
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

		builder.setSmallIcon(R.mipmap.ic_launcher);//icon
		builder.setContentTitle(me.title);//标题
		builder.setContentText(me.description);//内容

		builder.setTicker(me.title);//第一次提示消息的时候显示在通知栏上
		builder.setAutoCancel(true);//用户点击消息自动取消
		builder.setPriority(Notification.PRIORITY_MAX);//设置优先级

		/**
		 * DEFAULT_ALL    使用所有默认值，比如声音，震动，闪屏等等
		 * DEFAULT_LIGHTS 使用默认闪光提示
		 * DEFAULT_SOUNDS 使用默认提示声音
		 * DEFAULT_VIBRATE 使用默认手机震动
		 */
		builder.setDefaults(Notification.DEFAULT_ALL);
		builder.setSmallIcon(R.mipmap.ic_launcher);//设置通知小ICON
		//设置通知的点击行为：这里启动一个 Activity
		Intent intent = new Intent(this, NotificationActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(pendingIntent);

		// 发送通知 id 需要在应用内唯一
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(100, builder.build());
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
//			e.printStackTrace();
			//在isConnected、isConnecting、isDisconnecting、isClosed状态会抛出MqttException
			//异常，这里不需要做处理
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

	private void notifyClientsOffline(){
		//网络中断时被调用
		//由于该service用于接收推送消息 网络中断并不需要通知用户，这里不做处理
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

			mClient = new MqttAsyncClient(NetworkUtils.MessageServerURL, Utils.getClientId(this),
					persistence, new AlarmPingSender(this));

			mClient.setCallback(this);
		} catch (MqttException e){
//			e.printStackTrace();
//			System.out.println("NotificationService newClient;"+e);
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
//			System.out.println("NetworkConnectionIntentReceiver");
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
//			System.out.println("BackgroundDataPreferenceReceiver");
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
			System.out.println("链接成功");
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
//				System.out.println("有网络 5分钟 后重新链接");
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
			System.out.println("订阅失败 重启该service："+exception);
			Intent sevice = new Intent(NotificationService.this, NotificationService.class);
			NotificationService.this.stopService(sevice);
		}
	}

	private class AlarmManagerReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(AlarmManagerAction) || action == AlarmManagerAction) {
				if (isOnline()) {
					reconnect();
				}
			}
		}
	}
}
