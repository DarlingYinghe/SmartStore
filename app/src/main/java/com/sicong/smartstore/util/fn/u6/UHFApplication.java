package com.sicong.smartstore.util.fn.u6;

import android.app.Application;
import android.content.Context;

import org.litepal.LitePal;

public class UHFApplication extends Application {
	public static Context applicationContext ;
	@Override
	public void onCreate() {
		super.onCreate();
		applicationContext = getApplicationContext();
		CrashHandler crashHandler = CrashHandler.getInstance();
		crashHandler.init(applicationContext);
		LitePal.initialize(applicationContext);
	}
}
