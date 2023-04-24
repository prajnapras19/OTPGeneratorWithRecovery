package com.example.otpgeneratorwithrecovery.background;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class GetSharedBackupFromServerService extends Service {
    private Handler mHandler;
    private Runnable mRunnable;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mRunnable = new Runnable() {
            @Override
            public void run() {
                // TODO
                mHandler.postDelayed(mRunnable, 5000); // 5 second delay
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.post(mRunnable);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}