package com.example.otpgeneratorwithrecovery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.otpgeneratorwithrecovery.databinding.FragmentRecoverOtpByScanQrCodeBinding;

public class RecoverOTPByScanQRCodeFragment extends Fragment {
    private FragmentRecoverOtpByScanQrCodeBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentRecoverOtpByScanQrCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // TODO
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}