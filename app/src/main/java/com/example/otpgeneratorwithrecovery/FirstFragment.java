package com.example.otpgeneratorwithrecovery;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.databinding.FragmentFirstBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

public class FirstFragment extends Fragment {
    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    class ButtonAndActionDTO {
        public Button button;
        public int action;
        public ButtonAndActionDTO(Button button, int action) {
            this.button = button;
            this.action = action;
        }
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButtonAndActionDTO[] buttonAndActionList = new ButtonAndActionDTO[]{
                new ButtonAndActionDTO(binding.buttonFirstToOtpSecretQrCodeScanner, R.id.action_FirstFragment_to_OTPSecretQRCodeScannerFragment),
                new ButtonAndActionDTO(binding.buttonFirstToOtpSecretManualInput, R.id.action_FirstFragment_to_OTPSecretManualInputFragment),
                new ButtonAndActionDTO(binding.buttonFirstToListOtp, R.id.action_FirstFragment_to_ListOTPFragment),
                new ButtonAndActionDTO(binding.buttonFirstToCreateBackup, R.id.action_FirstFragment_to_CreateBackupFragment),
                new ButtonAndActionDTO(binding.buttonFirstToListCreatedBackup, R.id.action_FirstFragment_to_ListCreatedBackupFragment),
                new ButtonAndActionDTO(binding.buttonFirstToSaveSharedBackupQrCode, R.id.action_FirstFragment_to_SaveSharedBackupQRCodeFragment),
                new ButtonAndActionDTO(binding.buttonFirstToSaveSharedBackupManualInput, R.id.action_FirstFragment_to_SaveSharedBackupManualInputFragment),
                new ButtonAndActionDTO(binding.buttonFirstToListSharedBackup, R.id.action_FirstFragment_to_ListSharedBackupFragment),
                new ButtonAndActionDTO(binding.buttonFirstToRecoverOtpByScanQrCode, R.id.action_FirstFragment_to_RecoverOTPByScanQRCodeFragment),
                new ButtonAndActionDTO(binding.buttonFirstToRecoverOtpManualInput, R.id.action_FirstFragment_to_RecoverOTPManualInputFragment),
                new ButtonAndActionDTO(binding.buttonFirstToShowClientIdentity, R.id.action_FirstFragment_to_ShowClientIdentityFragment),
                new ButtonAndActionDTO(binding.buttonFirstToAddFriendByScanQrCode, R.id.action_FirstFragment_to_AddFriendByScanQRCodeFragment),
                new ButtonAndActionDTO(binding.buttonFirstToAddFriendByManualInput, R.id.action_FirstFragment_to_AddFriendByManualInputFragment),
                new ButtonAndActionDTO(binding.buttonFirstToListFriend, R.id.action_FirstFragment_to_ListFriendFragment),
        };

        for (ButtonAndActionDTO dto : buttonAndActionList) {
            dto.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    NavHostFragment.findNavController(FirstFragment.this)
                            .navigate(dto.action);
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}