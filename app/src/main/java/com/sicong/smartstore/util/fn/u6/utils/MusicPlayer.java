package com.sicong.smartstore.util.fn.u6.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;


import com.sicong.smartstore.R;
import com.sicong.smartstore.util.fn.u6.UHFApplication;

import java.util.Map;
import java.util.TreeMap;

public class MusicPlayer {
	private static MusicPlayer sInstance;

	public static class Type {
		public final static int OK = 1;
		public final static int MUSIC_ERROR = 2;
	}

	private SoundPool mSp;
	private Map<Integer, Integer> sSpMap;

	@SuppressWarnings("deprecation")
	private MusicPlayer(Context context) {
		sSpMap = new TreeMap<Integer, Integer>();
		mSp = new SoundPool(0, AudioManager.STREAM_MUSIC, 100);
		sSpMap.put(Type.OK, mSp.load(context, R.raw.ok, 1));
		sSpMap.put(Type.MUSIC_ERROR, mSp.load(context, R.raw.error, 1));
		
	}

	static {
			sInstance = new MusicPlayer(UHFApplication.applicationContext);
	}

	public static MusicPlayer getInstance() {
		return sInstance;
	}

	public void play(int type) {
		if (sSpMap.get(type) == null)
			return;
		
		mSp.play(sSpMap.get(type), 1, 1, 0, 0, 1);
	}
	public void release(){
		mSp.release();
	}
}