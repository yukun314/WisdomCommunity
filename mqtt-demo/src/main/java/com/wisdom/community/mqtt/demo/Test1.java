package com.wisdom.community.mqtt.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by zyk on 2016/5/10.
 */
public class Test1 extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test1);

		Button button = (Button) findViewById(R.id.buttonpublisher);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});

		Button button1 = (Button) findViewById(R.id.buttonsubscriber);
		button1.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}
}
