package com.example.otpgeneratorwithrecovery;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

public class AskForPermissionPermissionListener implements PermissionListener {
    private final NeedPermissionFragment fragment;
    public AskForPermissionPermissionListener(NeedPermissionFragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        Log.v("Dexter", "permission granted: " + response.getPermissionName());
        fragment.handlePermissionGranted();
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {
        Log.v("Dexter", "permission denied: " + response.getPermissionName() + ", is permanently denied: " + response.isPermanentlyDenied());
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest request, PermissionToken token) {
        new AlertDialog.Builder(this.fragment.getContext()).setTitle(R.string.camera_permission_rationale_title)
                .setMessage(R.string.camera_permission_rationale_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }
}
