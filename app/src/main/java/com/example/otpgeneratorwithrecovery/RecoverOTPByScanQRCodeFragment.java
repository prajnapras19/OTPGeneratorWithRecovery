package com.example.otpgeneratorwithrecovery;

import android.Manifest;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.example.otpgeneratorwithrecovery.crypto.SharedSecretToRecover;
import com.example.otpgeneratorwithrecovery.databinding.FragmentRecoverOtpByScanQrCodeBinding;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionBackgroundThreadPermissionListener;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionErrorListener;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class RecoverOTPByScanQRCodeFragment extends NeedPermissionFragment implements ZXingScannerView.ResultHandler  {
    private FragmentRecoverOtpByScanQrCodeBinding binding;
    private ZXingScannerView mScannerView;
    private PermissionListener cameraPermissionListener;
    private PermissionRequestErrorListener errorListener;
    private Map<String, SharedSecretToRecover> sharedSecrets;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRecoverOtpByScanQrCodeBinding.inflate(inflater, container, false);
        sharedSecrets = new HashMap<>();
        this.askForPermission();
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Handler refreshHandler = new Handler();
        listSharedSecrets();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    listSharedSecrets();
                    refreshHandler.postDelayed(this, 1000);
                } catch (Exception e) {
                    // do nothing
                }
            }
        };
        refreshHandler.postDelayed(runnable, 1000);
    }

    public void initScanner() {
        mScannerView = new ZXingScannerView(getContext());
        mScannerView.setAutoFocus(true);
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        binding.frameLayoutCamera.addView(mScannerView);
    }

    @Override
    public void handleResult(Result rawResult) {
        try {
            SharedSecretToRecover sharedSecret = new SharedSecretToRecover(rawResult.getText());
            sharedSecrets.put(String.valueOf(sharedSecret.getRecipientNumber()), sharedSecret);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Success");
            builder.setMessage("Backup received successfully from " + sharedSecret.getRecipient());
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage(e.getMessage());
            AlertDialog alert = builder.create();
            alert.show();
        }
        this.mScannerView.resumeCameraPreview(this);
    }

    public void listSharedSecrets() {
        // TODO
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mScannerView = null;
        sharedSecrets = null;
    }
}