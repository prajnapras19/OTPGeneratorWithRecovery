package com.example.otpgeneratorwithrecovery.permissionlistener;

import android.util.Log;

import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequestErrorListener;

public class AskForPermissionErrorListener implements PermissionRequestErrorListener {
    @Override
    public void onError(DexterError error) {
        Log.e("Dexter", "There was an error: " + error.toString());
    }
}
