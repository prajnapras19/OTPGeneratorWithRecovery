package com.example.otpgeneratorwithrecovery;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.client.Client;
import com.example.otpgeneratorwithrecovery.databinding.FragmentSetBackupServerBinding;

public class SetBackupServerFragment extends Fragment {
    private FragmentSetBackupServerBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSetBackupServerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.setBackupServerInput.setHint(String.format("Backup Server (required, current: %s)", Client.getBaseURL(getContext())));

        binding.buttonCheckAvailability.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Status");
                    builder.setMessage(Client.checkHealth(binding.setBackupServerInput.getText().toString()));
                    AlertDialog alert = builder.create();
                    alert.show();
                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error");
                    builder.setMessage(e.getMessage());
                    AlertDialog alert = builder.create();
                    alert.show();
                }
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Client.setBaseURL(getContext(), binding.setBackupServerInput.getText().toString());

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Success");
                builder.setMessage("Backup server successfully set to " + Client.getBaseURL(getContext()));
                AlertDialog alert = builder.create();
                alert.show();

                NavHostFragment.findNavController(SetBackupServerFragment.this).navigateUp();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}