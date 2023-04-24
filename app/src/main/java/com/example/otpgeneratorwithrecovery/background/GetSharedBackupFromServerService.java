package com.example.otpgeneratorwithrecovery.background;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;

import com.example.otpgeneratorwithrecovery.R;
import com.example.otpgeneratorwithrecovery.client.Client;
import com.example.otpgeneratorwithrecovery.client.SharedBackup;
import com.example.otpgeneratorwithrecovery.crypto.SharedSecretToRecover;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.List;

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
                try {
                    List<SharedBackup> sharedBackups = Client.getSharedBackup(getApplicationContext());

                    SharedPreferences sharedPref = getApplicationContext().getSharedPreferences(getString(R.string.shared_backup_shared_preferences_file), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    String nextID = Util.getNextSharedPreferenceID(sharedPref.getAll());
                    for (SharedBackup sharedBackup : sharedBackups) {
                        try {
                            SharedSecretToRecover sharedSecret = new SharedSecretToRecover(sharedBackup.getShare());
                            editor.putString(nextID, sharedSecret.toString());
                            nextID = String.valueOf(Integer.valueOf(nextID) + 1);
                        } catch (Exception e) {
                            // do nothing
                        }
                    }
                    editor.apply();
                } catch (Exception e) {
                    // do nothing
                }


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