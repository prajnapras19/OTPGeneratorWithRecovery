package com.example.otpgeneratorwithrecovery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.created_backup_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> createdBackups = sharedPref.getAll();

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

                TextView textViewThreshold = new TextView(getContext());
                textViewThreshold.setText(String.format("Minimum number of backup needed to recover: %d", pickedSharedSecret.getThreshold()));
                textViewThreshold.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                binding.linearLayoutListCreatedBackup.addView(textViewThreshold);

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
                }

                TextView divider3 = new TextView(getContext());
                divider3.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
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