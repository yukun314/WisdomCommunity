package com.dnatech.community;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.dnatech.community.activity.MainActivity;
import com.dnatech.community.sqlite.SQLiteConfig;


/**
 * Created by zyk on 2016/5/12.
 * 启动页Activity FIXME 暂直接跳到MainActivity
 */
public class SplashActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new SQLiteConfig(this);
        Intent intent = new Intent();
        intent.setClass(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
