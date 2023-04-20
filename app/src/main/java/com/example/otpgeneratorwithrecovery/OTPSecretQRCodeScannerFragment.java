package com.example.otpgeneratorwithrecovery;

import android.Manifest;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.databinding.FragmentOtpSecretQrCodeScannerBinding;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionBackgroundThreadPermissionListener;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionErrorListener;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * OTP secret QR code scanner fragment.
 * This will use the camera (permission needed) to scan the QR code, and saved it into storage.
 */
public class OTPSecretQRCodeScannerFragment extends NeedPermissionFragment implements ZXingScannerView.ResultHandler {
   /**
    *
    * references:
    * - https://codepolitan.com/blog/cara-membuat-qr-code-scanner-pada-android-studio-599fa33d3d66e
    * - https://medium.com/nusanet/qr-code-scanner-view-di-android-dengan-zxing-1df8914f1ef5
    */
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
            OTPSecret secret = new OTPSecret(rawResult.getText());
            // TODO
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage(e.getMessage());
            AlertDialog alert1 = builder.create();
            alert1.show();
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

