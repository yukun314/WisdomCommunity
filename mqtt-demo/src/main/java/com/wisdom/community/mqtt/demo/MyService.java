package com.wisdom.community.mqtt.demo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by zyk on 2016/5/12.
 */
public class MyService extends Service {

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		//其他对象通过bindService方法通知该Service时该方法会被调用
		System.out.println("MyService onBind");
		return null;
	}

	@Override
	public void onCreate() {
		System.out.println("MyService onCreate");
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		System.out.println("MyService onDestory");
		super.onDestroy();
		//重启该服务
		Intent sevice = new Intent(this, MyService.class);
		this.startService(sevice);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("MyService onStartCommand");
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onLowMemory() {
		System.out.println("MyService onLowMemory");
		super.onLowMemory();
	}
}
