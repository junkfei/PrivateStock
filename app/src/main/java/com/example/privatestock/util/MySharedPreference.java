package com.example.privatestock.util;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreference {

	private Context context;

	public MySharedPreference(Context context) {
		// TODO Auto-generated constructor stub

		this.context = context;
	}

	public boolean saveData(String fileName, String stockcodes) {
		boolean flag = false;
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				fileName, Context.MODE_PRIVATE);

		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString("stockcodes", stockcodes);
	
		flag = editor.commit();
		return flag;

	}

	public Map<String, Object> getData(String fileName) {
		Map<String, Object> map = new HashMap<String, Object>();
		SharedPreferences sharedPreferences = context.getSharedPreferences(
				fileName, Context.MODE_PRIVATE);

		String stockCodesValue = sharedPreferences.getString("stockcodes", "");
		map.put("stockCodesValue", stockCodesValue);
	
		return map;
	}

}
