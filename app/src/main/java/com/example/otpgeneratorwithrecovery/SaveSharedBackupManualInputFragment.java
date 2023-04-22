package com.example.otpgeneratorwithrecovery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.crypto.SharedSecretToRecover;
import com.example.otpgeneratorwithrecovery.databinding.FragmentSaveSharedBackupManualInputBinding;
import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;
import com.example.otpgeneratorwithrecovery.util.Util;

public class SaveSharedBackupManualInputFragment extends Fragment {
    private FragmentSaveSharedBackupManualInputBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSaveSharedBackupManualInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonSubmitSharedBackupManualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    SharedSecretToRecover sharedSecret = new SharedSecretToRecover(Base32Wrapper.decodeStringToString(binding.sharedBackupManualInput.getText().toString()));

                    SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.shared_backup_shared_preferences_file), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Util.getNextSharedPreferenceID(sharedPref.getAll()), sharedSecret.toString());
                    editor.apply();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Success");
                    builder.setMessage("Backup saved successfully for " + sharedSecret.getIdentifier());
                    AlertDialog alert1 = builder.create();
                    alert1.show();

                    NavHostFragment.findNavController(SaveSharedBackupManualInputFragment.this).navigateUp();
                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error");
                    builder.setMessage(e.getMessage());
                    AlertDialog alert1 = builder.create();
                    alert1.show();
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}