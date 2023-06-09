package com.example.otpgeneratorwithrecovery;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;
import com.example.otpgeneratorwithrecovery.crypto.SharedSecretToRecover;
import com.example.otpgeneratorwithrecovery.databinding.FragmentListSharedBackupBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.Map;

public class ListSharedBackupFragment extends Fragment {
    private FragmentListSharedBackupBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentListSharedBackupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Handler refreshHandler = new Handler();
        listSharedBackup();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    listSharedBackup();
                    refreshHandler.postDelayed(this, 1000);
                } catch (Exception e) {
                    // do nothing
                }
            }
        };
        refreshHandler.postDelayed(runnable, 1000);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void listSharedBackup() {
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.shared_backup_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> sharedBackups = sharedPref.getAll();

        binding.linearLayoutListSharedBackup.removeAllViews();

        int i = 1;
        for (String k : Util.getSortedMapKey(sharedBackups)) {
            try {
                SharedSecretToRecover sharedSecret = new SharedSecretToRecover((String)sharedBackups.get(k));

                // title
                TextView textViewOTPIdentifier = new TextView(getContext());
                textViewOTPIdentifier.setText(String.format("%d. Backup for %s:%s", i, sharedSecret.getIssuer(), sharedSecret.getAccountName()));
                textViewOTPIdentifier.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                binding.linearLayoutListSharedBackup.addView(textViewOTPIdentifier);

                TextView textViewUID = new TextView(getContext());
                textViewUID.setText(String.format("Unique ID: %s", sharedSecret.getUID()));
                textViewUID.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                binding.linearLayoutListSharedBackup.addView(textViewUID);

                TextView textViewRecipient = new TextView(getContext());
                textViewRecipient.setText(String.format("Recipient: %s", sharedSecret.getRecipient()));
                textViewRecipient.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                binding.linearLayoutListSharedBackup.addView(textViewRecipient);

                TextView textViewThreshold = new TextView(getContext());
                textViewThreshold.setText(String.format("Minimum number of backup needed to recover: %d", sharedSecret.getThreshold()));
                textViewThreshold.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                binding.linearLayoutListSharedBackup.addView(textViewThreshold);

                Button buttonShowShareSecret = new Button(getContext());
                buttonShowShareSecret.setText("Show QR Code");
                buttonShowShareSecret.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                buttonShowShareSecret.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // https://stackoverflow.com/questions/16036572/how-to-pass-values-between-fragments
                        Bundle bundle = new Bundle();
                        bundle.putString(ShowQRCodeFragment.MESSAGE, sharedSecret.getFormat().toString());

                        NavHostFragment.findNavController(ListSharedBackupFragment.this)
                                .navigate(R.id.action_ListSharedBackupFragment_to_ShowQRCodeFragment, bundle);
                    }
                });
                binding.linearLayoutListSharedBackup.addView(buttonShowShareSecret);

                Button buttonCopyToClipboard = new Button(getContext());
                buttonCopyToClipboard.setText("Copy To Clipboard");
                buttonCopyToClipboard.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                buttonCopyToClipboard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // https://stackoverflow.com/questions/19253786/how-to-copy-text-to-clip-board-in-android
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(getContext(), ClipboardManager.class);
                        ClipData clip = ClipData.newPlainText("copy_of_backup", Base32Wrapper.encodeStringToString(sharedSecret.getFormat().toString()));
                        clipboard.setPrimaryClip(clip);
                    }
                });
                binding.linearLayoutListSharedBackup.addView(buttonCopyToClipboard);

                Button buttonDeleteBackup = new Button(getContext());
                buttonDeleteBackup.setText("Delete");
                buttonDeleteBackup.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                buttonDeleteBackup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Delete");
                        alert.setMessage("Are you sure you want to delete this backup?");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.shared_backup_shared_preferences_file), Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.remove(k);
                                editor.apply();
                                dialog.dismiss();
                            }
                        });

                        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        alert.show();
                    }
                });
                binding.linearLayoutListSharedBackup.addView(buttonDeleteBackup);

                TextView divider3 = new TextView(getContext());
                divider3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                binding.linearLayoutListSharedBackup.addView(divider3);
                i++;
            } catch (Exception e) {
                Log.v("ListSharedBackupFragment", e.getMessage());
            }

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}