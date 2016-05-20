package com.dnatech.community.okhttp;

import com.dnatech.community.entity.QRCodeContentsEntity;
import com.dnatech.community.entity.UserInfo;
import com.dnatech.community.utils.NetworkUtils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zyk on 2016/5/19.
 */
public class OkHttpClientManager {
	/**
	 *
	 * @param doamin 模块名
	 * @param method 方法名
	 * @param params 参数的json字符串
	 */
	public static void request(final String doamin, final String method, final String params, final Callback responseCallback) {
		//创建okHttpClient对象
		OkHttpClient mOkHttpClient = new OkHttpClient();
		String url = NetworkUtils.ServerURL+getParams(doamin, method, params);
		//创建一个Request
		final Request request = new Request.Builder()
				.url(url)
				.build();

		Call call = mOkHttpClient.newCall(request);
		//请求加入调度
		call.enqueue(responseCallback);
	}

	private static String getParams(String doamin, String method, String param){
		String params = "?";
		params+="method="+method;
		params+="&params="+param;
		return params;
	}

//	public static FormBody.Builder getParams(String doamin, String method, String param){
//		FormBody.Builder params = new FormBody.Builder();
//		StringBuilder hashStr = new StringBuilder();
//		appendItem(hashStr,params,"client_key","1");
//		appendItem(hashStr,params,"timestamp",String.valueOf(new Date().getTime() / 1000));
//		appendItem(hashStr,params,"domain",doamin);
//		appendItem(hashStr,params,"method",method);
////		Gson gson = new Gson();
////		QRCodeContentsEntity qrc = gson.fromJson(param,QRCodeContentsEntity.class);
////		appendItem(hashStr,params,"communityId",qrc.communityId);
////		appendItem(hashStr,params,"userId",qrc.dev);
////		appendItem(hashStr,params,"devId",qrc.userId);
////		String a = URLEncoder.encode(param);
////		System.out.println("a:"+a);
//		appendItem(hashStr,params,"param", param);
//		appendItem(hashStr,params,"state","110");
//		appendItem(hashStr,params,"auth_code", UserInfo.getInstance().authCode);
//
////		try {
////			String signature = HMACSHA1.byte2hex(HMACSHA1.HmacSHA1Encrypt(
////					hashStr.toString(), HASH_KEY));
////			appendItem(hashStr,params,"signature",signature);
////		} catch (InvalidKeyException e) {
////			e.printStackTrace();
////		} catch (UnsupportedEncodingException e) {
////			e.printStackTrace();
////		} catch (NoSuchAlgorithmException e) {
////			e.printStackTrace();
////		}
//		return params;
//	}
////
////	//	static int  i = 0;
//	private static void appendItem(StringBuilder hashStr, FormBody.Builder params, String name, String value) {
//		if (value != null) {
//			params.add(name, value);
//			hashStr.append(name);
//			hashStr.append(value);
//		}
//	}
}
