package com.ant.solitaire;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameSurface extends SurfaceView implements SurfaceHolder.Callback {
	private Context mContext;
	private GameThread mThread;
	private SharedPreferences pref;
	protected boolean saveStateWhenFinish;

	public GameSurface(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		mThread = new GameThread(holder, context, new Handler());
		saveStateWhenFinish = true;
	}
	
	public GameThread getThread() {
		return mThread;
	}
	
	/*@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		if (mThread.getMode() != GameThread.MODE_NULL) {
			if (!hasWindowFocus) mThread.setPause();
			else mThread.setResume();
		}
	}*/
	
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		//all which need to be done is being done in surfaceChanged
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		boolean flag = mThread.setSurfaceSize(width, height);
		
		//if the mThread is alive, then just need to resume it
		if (mThread.isAlive()) {
			mThread.setResume();
			return ;
		}
		//if the mThread is dead, then start it
		mThread.start();
		pref = mContext.getSharedPreferences("LastSurfaceState", Context.MODE_PRIVATE);
		
		//check whether to start new game or restore the saved one
		if (!pref.getBoolean("SavedState", false)) { 
			mThread.setMode(GameThread.MODE_READY);
			mThread.setMode(GameThread.MODE_DEALING);
			mThread.setResume();
		} else {
			mThread.restoreSavedState();
			mThread.setResume();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mThread.setPause();
		if (saveStateWhenFinish) 
			mThread.saveCurrentState();
		
		/*boolean retry = true;
		while (retry) {
			try {
				mThread.join();
				retry = false;
			} catch (InterruptedException e) {
				//do nothing
			}
		}*/
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mThread.isRunning())
			mThread.doTouchEvent(event);
		return true;
	}
}
