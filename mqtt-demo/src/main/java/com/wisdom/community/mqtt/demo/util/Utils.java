package com.wisdom.community.mqtt.demo.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.app.ActivityManager.RunningAppProcessInfo;

import java.util.List;

/**
 * Created by zyk on 2016/5/12.
 */
public class Utils {

	/**
	 * 判断App是否在运行
	 */
	public static boolean isAppInForeground(Context context) {
		ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		//activityManager.getRunningServices() 也可以判断service是否正在运行
		List<RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();
		for (RunningAppProcessInfo appProcess : appProcesses) {
			if (appProcess.processName.equals(context.getPackageName())) {
				return appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
			}
		}
		return false;
	}

}
