package com.example.otpgeneratorwithrecovery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.crypto.OTPFriend;
import com.example.otpgeneratorwithrecovery.databinding.FragmentAddFriendByScanQrCodeBinding;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionBackgroundThreadPermissionListener;
import com.example.otpgeneratorwithrecovery.permissionlistener.AskForPermissionErrorListener;
import com.example.otpgeneratorwithrecovery.util.Util;
import com.google.zxing.Result;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.single.PermissionListener;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class AddFriendByScanQRCodeFragment extends NeedPermissionFragment implements ZXingScannerView.ResultHandler {
    private FragmentAddFriendByScanQrCodeBinding binding;
    private ZXingScannerView mScannerView;
    private PermissionListener cameraPermissionListener;
    private PermissionRequestErrorListener errorListener;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddFriendByScanQrCodeBinding.inflate(inflater, container, false);
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
            OTPFriend friend = new OTPFriend(rawResult.getText());

            // prompt input name
            // reference: https://mkyong.com/android/android-prompt-user-input-dialog-example/
            LayoutInflater li = LayoutInflater.from(getContext());
            View promptsView = li.inflate(R.layout.add_friend_name_prompt, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());

            alertDialogBuilder.setView(promptsView);

            final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    friend.setName(userInput.getText().toString());
                                    NavHostFragment.findNavController(AddFriendByScanQRCodeFragment.this).navigateUp();

                                    SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.friends_shared_preferences_file), Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putString(Util.getNextSharedPreferenceID(sharedPref.getAll()), friend.toString());
                                    editor.apply();

                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                    builder.setTitle("Success");
                                    builder.setMessage(String.format("Friend %s added successfully", friend.getName()));
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                }
                            })
                    .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                    mScannerView.resumeCameraPreview(AddFriendByScanQRCodeFragment.this);
                                }
                            });

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage(e.getMessage());
            AlertDialog alert = builder.create();
            alert.show();
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