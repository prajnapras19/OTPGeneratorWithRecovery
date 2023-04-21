package com.example.otpgeneratorwithrecovery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.otpgeneratorwithrecovery.databinding.FragmentRecoverOtpManualInputBinding;

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
        // TODO
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}