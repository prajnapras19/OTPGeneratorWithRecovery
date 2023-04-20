package com.example.otpgeneratorwithrecovery.permissionlistener;

import android.os.Handler;
import android.os.Looper;

import com.example.otpgeneratorwithrecovery.NeedPermissionFragment;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;

public class AskForPermissionBackgroundThreadPermissionListener extends AskForPermissionPermissionListener {
    private Handler handler = new Handler(Looper.getMainLooper());

    public AskForPermissionBackgroundThreadPermissionListener(NeedPermissionFragment fragment) {
        super(fragment);
    }

    @Override public void onPermissionGranted(final PermissionGrantedResponse response) {
        handler.post(
                () -> AskForPermissionBackgroundThreadPermissionListener.super.onPermissionGranted(response));
    }

    @Override public void onPermissionDenied(final PermissionDeniedResponse response) {
        handler.post(() -> AskForPermissionBackgroundThreadPermissionListener.super.onPermissionDenied(response));
    }

    @Override public void onPermissionRationaleShouldBeShown(final PermissionRequest permission,
                                                             final PermissionToken token) {
        handler.post(
                () -> AskForPermissionBackgroundThreadPermissionListener.super.onPermissionRationaleShouldBeShown(
                        permission, token));
    }
}
