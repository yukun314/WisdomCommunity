package com.dnatech.community.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.dnatech.community.R;
import com.dnatech.community.service.NotificationService;

public class MainActivity extends AppCompatActivity {

	int i = 0;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, NotificationService.class);
				if(i%2 == 0) {
					System.out.println("启动service");
					startService(intent);
				} else {
					System.out.println("停止 service");
					stopService(intent);
				}
				i++;
			}
		});
	}
}
