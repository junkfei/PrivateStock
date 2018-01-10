package com.example.privatestock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.privatestock.ui.MainActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.Toast;

public class Welcome extends Activity {

	private long day = 0;
	private long hour = 0;
	private long min = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences myPreference = getSharedPreferences("myPreference", Context.MODE_PRIVATE);
		boolean firstTime = myPreference.getBoolean("first_time", true);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		if (firstTime) {
			Editor editor = myPreference.edit();
			editor.putBoolean("first_time", false);
			editor.putString("first_date", sdf.format(new Date()));
			editor.commit();
			Toast.makeText(getApplicationContext(), "欢迎使用，试用时间剩余3天", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
			finish();
		} else {
			String firstDate = myPreference.getString("first_date", "2017-12-08 00:00:00");
			try {
				Date now_date = new Date();
				Date first_date = sdf.parse(firstDate);
				long diff = now_date.getTime() - first_date.getTime();
				day = 2 - diff / (24 * 60 * 60 * 1000);
				hour = 23 - (diff / (60 * 60 * 1000) - diff / (24 * 60 * 60 * 1000) * 24);
				min = 59 - ((diff / (60 * 1000)) - diff / (24 * 60 * 60 * 1000) * 24 * 60
						- (diff / (60 * 60 * 1000) - diff / (24 * 60 * 60 * 1000) * 24) * 60);
				if (day >= 0) {
					String notice = "试用时间剩余" + day + "天" + hour + "小时" + min + "分钟";
					Toast.makeText(getApplicationContext(), notice, Toast.LENGTH_LONG).show();
					Intent intent = new Intent(this, MainActivity.class);
					startActivity(intent);
					finish();
				} else {
					Toast.makeText(getApplicationContext(), "试用结束", Toast.LENGTH_LONG).show();
					finish();
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

	}

}
