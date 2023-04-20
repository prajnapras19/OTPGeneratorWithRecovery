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

import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.databinding.FragmentOtpSecretQrCodeScannerBinding;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionBackgroundThreadPermissionListener;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionErrorListener;
import com.example.otpgeneratorwithrecovery.util.Util;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Map;

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

            SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(Util.getNextSharedPreferenceID(sharedPref.getAll()), secret.getFormat().toString());
            editor.apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Success");
            builder.setMessage("Secret saved successfully for " + secret.getIdentifier());
            AlertDialog alert1 = builder.create();
            alert1.show();

            NavHostFragment.findNavController(OTPSecretQRCodeScannerFragment.this).navigateUp();
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

