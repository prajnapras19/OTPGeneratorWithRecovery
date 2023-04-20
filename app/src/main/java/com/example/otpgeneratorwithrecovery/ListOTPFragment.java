package com.example.otpgeneratorwithrecovery;

import android.app.AlertDialog;
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

import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.databinding.FragmentListOtpBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.Map;

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
        Handler refreshHandler = new Handler();
        listOTP();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    listOTP();
                    refreshHandler.postDelayed(this, 1000);
                } catch (Exception e) {
                    // do nothing
                }
            }
        };
        refreshHandler.postDelayed(runnable, 1000);
    }

    public void listOTP() {
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> otpSecrets = sharedPref.getAll();

        binding.linearLayoutListOtp.removeAllViews();

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

                Button buttonDeleteSecret = new Button(getContext());
                buttonDeleteSecret.setText("Delete");
                buttonDeleteSecret.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                buttonDeleteSecret.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Delete");
                        alert.setMessage("Are you sure you want to delete this secret?");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
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
                binding.linearLayoutListOtp.addView(buttonDeleteSecret);

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
