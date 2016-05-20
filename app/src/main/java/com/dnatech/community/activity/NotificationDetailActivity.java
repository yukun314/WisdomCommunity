package com.dnatech.community.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.dnatech.community.R;
import com.dnatech.community.app.BaseActivity;
import com.dnatech.community.entity.MessageEntity;
import com.dnatech.community.sqlite.WCDatebaseHelper;
import com.dnatech.community.utils.Utils;

import java.io.File;

/**
 * Created by zyk on 2016/5/16.
 */
public class NotificationDetailActivity extends BaseActivity {

	private MessageEntity mMessage;
	private WebView mWebView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_notification_detail);
		mMessage = (MessageEntity) getIntent().getExtras().getSerializable(NotificationActivity.MESSAGE);
		mWebView = (WebView) findViewById(R.id.activity_notification_detail_webview);
		init();
		new Thread(){
			@Override
			public void run() {
				super.run();
				WCDatebaseHelper.getInstance(NotificationDetailActivity.this).updateMessageRead(mMessage.title, mMessage.url, true);
			}
		}.start();
	}

	private void init() {
		mWebView.setWebViewClient(new WebViewClient());

		WebSettings settings = mWebView.getSettings();
		//由于这里是显示推送的消息富文本 内容几乎不会变动。所以设为只要本地有缓存就不从网上加载
		settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
		settings.setLoadsImagesAutomatically(true);
		//html5 缓存及数据库设置
		settings.setAppCacheEnabled(true);
		settings.setDatabaseEnabled(true);

		// 开启javascript设置
//		settings.setJavaScriptEnabled(true);
		mWebView.loadUrl(mMessage.url);
	}

	@Override
	public File getCacheDir() {
//		return super.getCacheDir();
		return getApplicationContext().getCacheDir();
	}
}
