package com.example.otpgeneratorwithrecovery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.crypto.OTPURIFormat;
import com.example.otpgeneratorwithrecovery.databinding.FragmentOtpSecretManualInputBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple form to provide the case user want to input OTP secret manually.
 */
public class OTPSecretManualInputFragment extends Fragment {
    /**
     * references:
     * - https://code.tutsplus.com/tutorials/how-to-add-a-dropdown-menu-in-android-studio--cms-37860
     * - https://stackoverflow.com/questions/5787809/get-spinner-selected-items-text
     * - https://code.tutsplus.com/tutorials/android-essentials-creating-simple-user-forms--mobile-1758
     */
    private FragmentOtpSecretManualInputBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentOtpSecretManualInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // insert possible type to spinner
        // TODO: add support for hotp
        ArrayList<String> otpType = new ArrayList<>();
        otpType.add("totp");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, otpType);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.typeOtpSecretManualInput.setAdapter(adapter);

        binding.buttonSubmitOtpSecretManualInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Map<String, String> parameterMap = new HashMap<>();
                    parameterMap.put("secret", binding.secretOtpSecretManualInput.getText().toString());
                    parameterMap.put("issuer", binding.issuerOtpSecretManualInput.getText().toString());

                    OTPURIFormat format = new OTPURIFormat(
                            OTPSecret.PREFIX_OTP_SECRET,
                            binding.typeOtpSecretManualInput.getSelectedItem().toString(),
                            binding.accountNameOtpSecretManualInput.getText().toString(),
                            parameterMap);
                    OTPSecret secret = new OTPSecret(format);

                    SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Util.getNextSharedPreferenceID(sharedPref.getAll()), secret.getFormat().toString());
                    editor.apply();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Success");
                    builder.setMessage("Secret saved successfully for " + secret.getIdentifier());
                    AlertDialog alert1 = builder.create();
                    alert1.show();

                    NavHostFragment.findNavController(OTPSecretManualInputFragment.this).navigateUp();
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