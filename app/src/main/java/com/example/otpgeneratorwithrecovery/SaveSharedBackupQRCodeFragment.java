package com.example.otpgeneratorwithrecovery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.crypto.SharedSecretToRecover;
import com.example.otpgeneratorwithrecovery.databinding.FragmentOtpSecretQrCodeScannerBinding;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionBackgroundThreadPermissionListener;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionErrorListener;
import com.example.otpgeneratorwithrecovery.util.Util;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Save shared backup in QR Code format
 */
public class SaveSharedBackupQRCodeFragment extends NeedPermissionFragment implements ZXingScannerView.ResultHandler {
    private FragmentOtpSecretQrCodeScannerBinding binding;
    private ZXingScannerView mScannerView;
    private PermissionListener cameraPermissionListener;
    private PermissionRequestErrorListener errorListener;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOtpSecretQrCodeScannerBinding.inflate(inflater, container, false);
        this.askForPermission();
        return binding.getRoot();
    }

    public void initScanner() {
        mScannerView = new ZXingScannerView(getContext());
        mScannerView.setAutoFocus(true);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        binding.frameLayoutCamera.addView(mScannerView);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mScannerView = null;
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            SharedSecretToRecover sharedSecret = new SharedSecretToRecover(rawResult.getText());

            SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.shared_backup_shared_preferences_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Util.getNextSharedPreferenceID(sharedPref.getAll()), sharedSecret.toString());
            editor.apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Success");
            builder.setMessage("Backup saved successfully for " + sharedSecret.getIdentifier());
            AlertDialog alert1 = builder.create();
            alert1.show();

            NavHostFragment.findNavController(SaveSharedBackupQRCodeFragment.this).navigateUp();
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage(e.getMessage());
            AlertDialog alert1 = builder.create();
            alert1.show();
            this.mScannerView.resumeCameraPreview(this);
        }
    }

    @Override
    public void handlePermissionGranted() {
        initScanner();
    }

    public void askForPermission() {
        cameraPermissionListener = new AskForPermissionBackgroundThreadPermissionListener(this);
        errorListener = new AskForPermissionErrorListener();

        new Thread(() -> Dexter.withContext(getContext())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(cameraPermissionListener)
                .withErrorListener(errorListener)
                .onSameThread()
                .check()).start();
    }
}