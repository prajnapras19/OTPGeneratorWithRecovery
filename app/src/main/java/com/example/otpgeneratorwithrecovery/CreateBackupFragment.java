package com.example.otpgeneratorwithrecovery;

import android.annotation.SuppressLint;
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

import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
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
                // TODO
            }
        });
    }

    public boolean validate() {
        // TODO
        /*
        boolean someFieldsAreEmpty = binding.choiceCreateShares.getSelectedItem().toString().equals(getResources().getString(R.string.choose_secret));
        for (int i = 0; i < binding.recipientsLinearLayout.getChildCount(); i++) {
            someFieldsAreEmpty |= ((EditText)binding.recipientsLinearLayout.getChildAt(i)).getText().toString().equals("");
        }
        return !someFieldsAreEmpty;
         */
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}