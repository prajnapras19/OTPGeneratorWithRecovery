package com.example.otpgeneratorwithrecovery;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.otpgeneratorwithrecovery.databinding.FragmentShowQrCodeBinding;
import com.example.otpgeneratorwithrecovery.util.Util;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.io.ByteArrayOutputStream;

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

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            Bitmap bm = Util.getQRCode(getArguments().getString(MESSAGE), 200);
            binding.imageViewShowQrCode.setImageBitmap(bm);

            Button buttonShareQRCode = new Button(getContext());
            buttonShareQRCode.setText("Share");
            buttonShareQRCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            buttonShareQRCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // https://stackoverflow.com/questions/7661875/how-to-use-share-image-using-sharing-intent-to-share-images-in-android
                    String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bm, "qr_code_backup", null);
                    Uri uri = Uri.parse(path);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "Share"));
                }
            });
            binding.linearLayoutShowQrCode.addView(buttonShareQRCode);
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