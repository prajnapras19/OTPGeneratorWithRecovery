package com.example.otpgeneratorwithrecovery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;
import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.crypto.SharedSecret;
import com.example.otpgeneratorwithrecovery.databinding.FragmentRecoverOtpManualInputBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

public class RecoverOTPManualInputFragment extends Fragment {
    private FragmentRecoverOtpManualInputBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRecoverOtpManualInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        EditText editText = new EditText(getContext());
        editText.setHint("Input the backup");
        binding.linearLayoutInputBackups.addView(editText);
        binding.scrollViewBackups.fullScroll(ScrollView.FOCUS_DOWN);

        binding.buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText editText = new EditText(getContext());
                editText.setHint("Input the backup");
                binding.linearLayoutInputBackups.addView(editText);
            }
        });

        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    String[] shares = new String[binding.linearLayoutInputBackups.getChildCount()];
                    for (int i = 0; i < binding.linearLayoutInputBackups.getChildCount(); i++) {
                        shares[i] = Base32Wrapper.decodeStringToString(((EditText)binding.linearLayoutInputBackups.getChildAt(i)).getText().toString());
                    }

                    OTPSecret otpSecret = SharedSecret.recover(shares);

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
                NavHostFragment.findNavController(RecoverOTPManualInputFragment.this).navigateUp();
                return;
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}