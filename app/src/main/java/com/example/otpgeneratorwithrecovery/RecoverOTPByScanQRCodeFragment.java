package com.example.otpgeneratorwithrecovery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.crypto.SharedSecret;
import com.example.otpgeneratorwithrecovery.crypto.SharedSecretToRecover;
import com.example.otpgeneratorwithrecovery.databinding.FragmentRecoverOtpByScanQrCodeBinding;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionBackgroundThreadPermissionListener;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionErrorListener;
import com.example.otpgeneratorwithrecovery.util.Util;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class RecoverOTPByScanQRCodeFragment extends NeedPermissionFragment implements ZXingScannerView.ResultHandler  {
    private FragmentRecoverOtpByScanQrCodeBinding binding;
    private ZXingScannerView mScannerView;
    private PermissionListener cameraPermissionListener;
    private PermissionRequestErrorListener errorListener;
    private Map<Integer, SharedSecretToRecover> sharedSecrets;

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

            if (sharedSecrets.size() > 0) {
                SharedSecretToRecover pickedSharedSecret = sharedSecrets.entrySet().iterator().next().getValue();

                if (pickedSharedSecret.hasSameSource(sharedSecret)) {
                    sharedSecrets.put(sharedSecret.getRecipientNumber(), sharedSecret);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Success");
                    builder.setMessage("Backup received successfully from " + sharedSecret.getRecipient());
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error");
                    builder.setMessage("Scanned backup doesn't belong to the same source with previously scanned backup");
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            } else {
                sharedSecrets.put(sharedSecret.getRecipientNumber(), sharedSecret);
            }
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage(e.getMessage());
            AlertDialog alert = builder.create();
            alert.show();
        }
        this.mScannerView.resumeCameraPreview(this);
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void listSharedSecrets() {
        binding.linearLayoutListScanned.removeAllViews();
        if (sharedSecrets.size() == 0) {
            TextView textViewNoBackupScanned = new TextView(getContext());
            textViewNoBackupScanned.setText("No shared backup scanned. Try to scan a QR code of a backup.");
            textViewNoBackupScanned.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
            binding.linearLayoutListScanned.addView(textViewNoBackupScanned);
            return;
        }

        SharedSecretToRecover pickedSharedSecret = sharedSecrets.entrySet().iterator().next().getValue();
        if (sharedSecrets.size() >= pickedSharedSecret.getThreshold()) {
            mScannerView.stopCamera();
            try {
                ArrayList<String> shares = new ArrayList<>();
                for (Map.Entry<Integer,SharedSecretToRecover> entry : sharedSecrets.entrySet()) {
                    shares.add(entry.getValue().toString());
                }

                OTPSecret otpSecret = SharedSecret.recover(shares.toArray(new String[0]));

                SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(Util.getNextSharedPreferenceID(sharedPref.getAll()), otpSecret.getFormat().toString());
                editor.apply();

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Success");
                builder.setMessage("OTP recovered successfully for " + otpSecret.getIdentifier());
                AlertDialog alert = builder.create();
                alert.show();
            } catch (Exception e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Error");
                builder.setMessage(e.getMessage());
                AlertDialog alert1 = builder.create();
                alert1.show();
            }
            NavHostFragment.findNavController(RecoverOTPByScanQRCodeFragment.this).navigateUp();
            return;
        }

        TextView textViewOTPIdentifier = new TextView(getContext());
        textViewOTPIdentifier.setText(String.format("Recovery attempt for %s:%s", pickedSharedSecret.getIssuer(), pickedSharedSecret.getAccountName()));
        textViewOTPIdentifier.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        binding.linearLayoutListScanned.addView(textViewOTPIdentifier);

        TextView textViewUID = new TextView(getContext());
        textViewUID.setText(String.format("Unique ID: %s", pickedSharedSecret.getUID()));
        textViewUID.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        binding.linearLayoutListScanned.addView(textViewUID);

        TextView textViewThreshold = new TextView(getContext());
        textViewThreshold.setText(String.format("Minimum number of backup needed to recover: %d", pickedSharedSecret.getThreshold()));
        textViewThreshold.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        binding.linearLayoutListScanned.addView(textViewThreshold);

        TextView divider = new TextView(getContext());
        divider.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
        binding.linearLayoutListScanned.addView(divider);

        String[] recipients = pickedSharedSecret.getRecipients();

        TextView textViewRecipients = new TextView(getContext());
        textViewRecipients.setText(String.format("Recipients (%d / %d scanned):", sharedSecrets.size(), recipients.length));
        textViewRecipients.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        binding.linearLayoutListScanned.addView(textViewRecipients);

        for (int i = 0; i < recipients.length; i++) {
            String recipient = String.format("    • %s", recipients[i]);
            if (sharedSecrets.get(i + 1) != null) {
                recipient = String.format("%s (scanned)", recipient);
            }

            TextView textViewRecipient = new TextView(getContext());
            textViewRecipient.setText(recipient);
            textViewRecipient.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            binding.linearLayoutListScanned.addView(textViewRecipient);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mScannerView = null;
        sharedSecrets = null;
    }
}