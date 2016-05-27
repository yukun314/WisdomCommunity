package com.example.tcps.ui;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by zyk on 2016/5/12.
 */
//FIXME BaseActivity是继承Activity还是继承AppCompatActivity 迟后再定
public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManager.getAppManager().addActivity(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getAppManager().removeActivity(this);
	}
}
