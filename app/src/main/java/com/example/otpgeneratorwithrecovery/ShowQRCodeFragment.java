package com.example.otpgeneratorwithrecovery;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.otpgeneratorwithrecovery.databinding.FragmentShowQrCodeBinding;
import com.example.otpgeneratorwithrecovery.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

/**
 * Class to show QR code
 */
public class ShowQRCodeFragment extends Fragment {
    /**
     * references:
     * - https://www.geeksforgeeks.org/how-to-generate-qr-code-in-android/
     * - https://stackoverflow.com/questions/8800919/how-to-generate-a-qr-code-for-an-android-application
     * - https://stackoverflow.com/questions/30515584/qr-encode-a-string-to-image-in-android-project-using-zxing
     */
    private FragmentShowQrCodeBinding binding;
    public final static String MESSAGE = "message";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentShowQrCodeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            Bitmap bm = Util.getQRCode(getArguments().getString(MESSAGE), 200);
            binding.imageViewShowQrCode.setImageBitmap(bm);
        } catch (Exception e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error");
            builder.setMessage("Something error happened.");
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}