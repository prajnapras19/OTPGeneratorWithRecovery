package com.example.otpgeneratorwithrecovery;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.crypto.Base32Wrapper;
import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.crypto.OTPURIFormat;
import com.example.otpgeneratorwithrecovery.crypto.SharedSecret;
import com.example.otpgeneratorwithrecovery.databinding.FragmentCreateBackupBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.ArrayList;
import java.util.Map;

/**
 * Create backup with SharedSecret class
 */
public class CreateBackupFragment extends Fragment {
    private FragmentCreateBackupBinding binding;
    private final static String CHOOSE_SECRET = "Choose secret";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCreateBackupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);

        Map<String, ?> otpSecrets = sharedPref.getAll();

        ArrayList<String> otpIdentifiers = new ArrayList<>();
        otpIdentifiers.add(CreateBackupFragment.CHOOSE_SECRET);
        int i = 1;
        for (String k : Util.getSortedMapKey(otpSecrets)) {
            try {
                otpIdentifiers.add(String.format("%d. %s", i, new OTPSecret((String)otpSecrets.get(k)).getIdentifier()));
                i++;
            } catch (Exception e) {
                Log.v("CreateBackupFragment", e.getMessage());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, otpIdentifiers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.choiceCreateBackup.setAdapter(adapter);

        // initialize with one recipient (full secret)
        EditText editText = new EditText(getContext());
        editText.setHint("Recipient 1");
        binding.linearLayoutRecipientsCreateBackup.addView(editText);
        binding.scrollViewCreateBackupFragment.fullScroll(ScrollView.FOCUS_DOWN);

        binding.buttonAddRecipientsCreateBackup.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onClick(View view) {
                // add threshold field if recipients >= 2
                if (binding.linearLayoutRecipientsCreateBackup.getChildCount() == 1) {
                    EditText editText = new EditText(getContext());
                    editText.setHint(String.format("Threshold (default: number of recipients)", binding.linearLayoutRecipientsCreateBackup.getChildCount() + 1));
                    binding.linearLayoutThresholdCreateBackup.addView(editText);
                }

                EditText editText = new EditText(getContext());
                editText.setHint(String.format("Recipient %d", binding.linearLayoutRecipientsCreateBackup.getChildCount() + 1));
                binding.linearLayoutRecipientsCreateBackup.addView(editText);

                binding.scrollViewCreateBackupFragment.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        binding.buttonSubmitCreateBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    try {
                        saveBackup();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Success");
                        builder.setMessage("Backup created successfully.");
                        AlertDialog alert = builder.create();
                        alert.show();
                        NavHostFragment.findNavController(CreateBackupFragment.this).navigateUp();
                    } catch (Exception e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Error");
                        builder.setMessage(e.getMessage());
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Error");
                builder.setMessage("All recipients must be filled and secret must be chosen, please try again.");
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public boolean isValid() {
        boolean someFieldsAreEmpty = binding.choiceCreateBackup.getSelectedItem().toString().equals(CreateBackupFragment.CHOOSE_SECRET);
        for (int i = 0; i < binding.linearLayoutRecipientsCreateBackup.getChildCount(); i++) {
            someFieldsAreEmpty |= ((EditText)binding.linearLayoutRecipientsCreateBackup.getChildAt(i)).getText().toString().equals("");
        }
        return !someFieldsAreEmpty;
    }

    public void saveBackup() throws Exception {
        int otpSecretNumber = Integer.valueOf(binding.choiceCreateBackup.getSelectedItem().toString().split(". ")[0]);
        OTPSecret otpSecret = null;
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> otpSecrets = sharedPref.getAll();
        int j = 1;
        for (String k : Util.getSortedMapKey(otpSecrets)) {
            try {
                OTPSecret tmp = new OTPSecret((String)otpSecrets.get(k));
                if (j == otpSecretNumber) {
                    otpSecret = tmp;
                    break;
                }
                j++;
            } catch (Exception e) {
                // do nothing
            }
        }

        // sanity check
        if (otpSecret == null) {
            throw new Exception("OTP secret not found.");
        }

        // parse recipients
        String[] recipients = new String[binding.linearLayoutRecipientsCreateBackup.getChildCount()];
        for (int i = 0; i < binding.linearLayoutRecipientsCreateBackup.getChildCount(); i++) {
            recipients[i] = ((EditText)binding.linearLayoutRecipientsCreateBackup.getChildAt(i)).getText().toString();
        }

        // parse threshold
        int threshold = 0;
        if (recipients.length > 1) {
            String thresholdString = ((EditText)binding.linearLayoutThresholdCreateBackup.getChildAt(0)).getText().toString();
            if (thresholdString.equals("")) {
                threshold = recipients.length;
            } else {
                threshold = Integer.valueOf(thresholdString);
            }
        }

        // share secret
        String[] sharedSecret = SharedSecret.generate(otpSecret, recipients, threshold);

        // encode for saving
        String[] savedSharedSecret = new String[sharedSecret.length];
        for (int i = 0; i < sharedSecret.length; i++) {
            savedSharedSecret[i] = Base32Wrapper.encodeStringToString(sharedSecret[i]);
        }

        SharedPreferences sharedPrefCreatedBackup = getContext().getSharedPreferences(getString(R.string.created_backup_shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefCreatedBackup.edit();
        editor.putString(Util.getNextSharedPreferenceID(sharedPrefCreatedBackup.getAll()), String.join("-", savedSharedSecret));
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}