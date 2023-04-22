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

import com.example.otpgeneratorwithrecovery.crypto.Base32Wrapper;
import com.example.otpgeneratorwithrecovery.crypto.SharedSecretToRecover;
import com.example.otpgeneratorwithrecovery.databinding.FragmentListCreatedBackupBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * List created backup, including features like show QR code, copy text to clipboard, share QR code, and delete created shares
 */
public class ListCreatedBackupFragment extends Fragment {
    private FragmentListCreatedBackupBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentListCreatedBackupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Handler refreshHandler = new Handler();
        listCreatedBackup();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    listCreatedBackup();
                    refreshHandler.postDelayed(this, 1000);
                } catch (Exception e) {
                    // do nothing
                }
            }
        };
        refreshHandler.postDelayed(runnable, 1000);
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void listCreatedBackup() {
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.created_backup_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> createdBackups = sharedPref.getAll();

        binding.linearLayoutListCreatedBackup.removeAllViews();

        int i = 1;
        for (String k : Util.getSortedMapKey(createdBackups)) {
            try {
                String[] createdBackup = ((String)createdBackups.get(k)).split("-");
                List<SharedSecretToRecover> sharedSecrets = new ArrayList<>();
                for (String backup : createdBackup) {
                    sharedSecrets.add(new SharedSecretToRecover(Base32Wrapper.decodeStringToString(backup)));
                }

                // title
                SharedSecretToRecover pickedSharedSecret = sharedSecrets.get(0);
                TextView textViewOTPIdentifier = new TextView(getContext());
                textViewOTPIdentifier.setText(String.format("%d. Backup for %s:%s", i, pickedSharedSecret.getIssuer(), pickedSharedSecret.getAccountName()));
                textViewOTPIdentifier.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                binding.linearLayoutListCreatedBackup.addView(textViewOTPIdentifier);

                TextView textViewUID = new TextView(getContext());
                textViewUID.setText(String.format("Unique ID: %s", pickedSharedSecret.getUID()));
                textViewUID.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                binding.linearLayoutListCreatedBackup.addView(textViewUID);

                TextView textViewThreshold = new TextView(getContext());
                textViewThreshold.setText(String.format("Minimum number of backup needed to recover: %d", pickedSharedSecret.getThreshold()));
                textViewThreshold.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                binding.linearLayoutListCreatedBackup.addView(textViewThreshold);

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
                                SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.created_backup_shared_preferences_file), Context.MODE_PRIVATE);
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
                binding.linearLayoutListCreatedBackup.addView(buttonDeleteBackup);

                TextView divider = new TextView(getContext());
                divider.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                binding.linearLayoutListCreatedBackup.addView(divider);

                TextView textViewRecipients = new TextView(getContext());
                textViewRecipients.setText("Recipients:");
                textViewRecipients.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                binding.linearLayoutListCreatedBackup.addView(textViewRecipients);

                for (SharedSecretToRecover sharedSecret : sharedSecrets) {
                    TextView divider2 = new TextView(getContext());
                    divider2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 2);
                    binding.linearLayoutListCreatedBackup.addView(divider2);

                    TextView textViewRecipient = new TextView(getContext());
                    textViewRecipient.setText(String.format("    • %s", sharedSecret.getRecipient()));
                    textViewRecipient.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                    binding.linearLayoutListCreatedBackup.addView(textViewRecipient);

                    Button buttonShowShareSecret = new Button(getContext());
                    buttonShowShareSecret.setText("Show QR Code");
                    buttonShowShareSecret.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    buttonShowShareSecret.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // https://stackoverflow.com/questions/16036572/how-to-pass-values-between-fragments
                            Bundle bundle = new Bundle();
                            bundle.putString(ShowQRCodeFragment.MESSAGE, sharedSecret.getFormat().toString());

                            NavHostFragment.findNavController(ListCreatedBackupFragment.this)
                                    .navigate(R.id.action_ListCreatedBackupFragment_to_ShowQRCodeFragment, bundle);
                        }
                    });
                    binding.linearLayoutListCreatedBackup.addView(buttonShowShareSecret);

                    Button buttonCopyToClipboard = new Button(getContext());
                    buttonCopyToClipboard.setText("Copy To Clipboard");
                    buttonCopyToClipboard.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    buttonCopyToClipboard.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // https://stackoverflow.com/questions/19253786/how-to-copy-text-to-clip-board-in-android
                            ClipboardManager clipboard = (ClipboardManager) getSystemService(getContext(), ClipboardManager.class);
                            ClipData clip = ClipData.newPlainText("copy_of_backup", Util.getBeautifiedBackup(sharedSecret.getRecipient(), sharedSecret.getFormat().toString()));
                            clipboard.setPrimaryClip(clip);
                        }
                    });
                    binding.linearLayoutListCreatedBackup.addView(buttonCopyToClipboard);
                }

                TextView divider3 = new TextView(getContext());
                divider3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
                binding.linearLayoutListCreatedBackup.addView(divider3);
                i++;
            } catch (Exception e) {
                Log.v("ListCreatedBackupFragment", e.getMessage());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}