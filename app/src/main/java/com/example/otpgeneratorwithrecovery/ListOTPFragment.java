package com.example.otpgeneratorwithrecovery;

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

import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.databinding.FragmentListOtpBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ListOTPFragment  extends Fragment {
    private FragmentListOtpBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentListOtpBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            @Override
            public void run() {
                // TODO
            }
        }, 0, 1000);

        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> otpSecrets = sharedPref.getAll();

        for (String k : Util.getSortedMapKey(otpSecrets)) {
            try {
                OTPSecret otpSecret = new OTPSecret((String) otpSecrets.get(k));
                TextView textViewOTPIdentifier = new TextView(getContext());
                textViewOTPIdentifier.setText(otpSecret.getIdentifier());
                textViewOTPIdentifier.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                binding.linearLayoutListOtp.addView(textViewOTPIdentifier);

                TextView textViewOTP = new TextView(getContext());
                textViewOTP.setText(otpSecret.getOTP());
                textViewOTP.setTextSize(TypedValue.COMPLEX_UNIT_SP, 36);
                binding.linearLayoutListOtp.addView(textViewOTP);

                TextView textViewOTPRemainingTime = new TextView(getContext());
                textViewOTPRemainingTime.setText(String.format("Time remaining until next OTP: %s seconds", Util.getRemainingOTPTime(otpSecret.getPeriod())));
                textViewOTPRemainingTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                binding.linearLayoutListOtp.addView(textViewOTPRemainingTime);

                TextView divider = new TextView(getContext());
                divider.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                binding.linearLayoutListOtp.addView(divider);
            } catch (Exception e) {
                Log.v("ListOTPFragment", e.getMessage());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}