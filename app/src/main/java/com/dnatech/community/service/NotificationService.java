package com.dnatech.community.service;

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
import java.io.FileInputStream;

/**
 * Created by zyk on 2016/5/12.
 */
public class NotificationService extends Service implements MqttCallback ,IMqttActionListener {

	final String TAG = "NotificationService";
	// our client object - instantiated on connect
	private MqttAsyncClient mClient = null;
	private MqttConnectOptions connectOptions;

	// An intent receiver to deal with changes in network connectivity
	private NetworkConnectionIntentReceiver networkConnectionMonitor;
	private BackgroundDataPreferenceReceiver backgroundDataPreferenceMonitor;
	private volatile boolean backgroundDataEnabled = true;

	@Override
	public void onCreate() {
		super.onCreate();

		newClient();
		reconnect();
	}

	@Override
	public int onStartCommand(final Intent intent, int flags, final int startId) {
		// run till explicitly stopped, restart when
		// process restarted
		registerBroadcastReceivers();
		//被系统kill之后，尝试重新启动
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
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
		System.out.println("NotificationService messageArrived");
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		System.out.println("NotificationService deliveryComplete");
	}

	@Override
	public void onSuccess(IMqttToken asyncActionToken) {

	}

	@Override
	public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

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
			mClient.connect(connectOptions, null, this);
		} catch (MqttException e) {
			e.printStackTrace();
			//FIXME
		}
	}

	//FIXME
	private void notifyClientsOffline(){

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
}
