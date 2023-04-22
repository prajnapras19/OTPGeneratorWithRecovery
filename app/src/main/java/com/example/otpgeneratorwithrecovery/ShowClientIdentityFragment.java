package com.example.otpgeneratorwithrecovery;

import static androidx.core.content.ContextCompat.getSystemService;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
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

import com.example.otpgeneratorwithrecovery.crypto.OTPFriend;
import com.example.otpgeneratorwithrecovery.databinding.FragmentShowClientIdentityBinding;
import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;
import com.example.otpgeneratorwithrecovery.util.Util;

public class ShowClientIdentityFragment extends Fragment {
    private FragmentShowClientIdentityBinding binding;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentShowClientIdentityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String clientID = Util.getClientID(getContext());
        byte[] clientSecret = Util.getClientSecretBytes(getContext());

        binding.textviewClientId.setText(String.format("Client ID: %s", clientID));
        binding.textviewClientId.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);

        try {
            OTPFriend clientIdentity = new OTPFriend(clientID, clientSecret);
            Bitmap bm = Util.getQRCode(clientIdentity.toString(), 200);
            binding.imageViewShowQrCode.setImageBitmap(bm);

            Button buttonShareQRCode = new Button(getContext());
            buttonShareQRCode.setText("Share");
            buttonShareQRCode.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            buttonShareQRCode.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // https://stackoverflow.com/questions/7661875/how-to-use-share-image-using-sharing-intent-to-share-images-in-android
                    String path = MediaStore.Images.Media.insertImage(getContext().getContentResolver(), bm, "qr_code_identity", null);
                    Uri uri = Uri.parse(path);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/jpeg");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    startActivity(Intent.createChooser(intent, "Share"));
                }
            });
            binding.linearLayoutShowQrCode.addView(buttonShareQRCode);

            Button buttonCopyToClipboard = new Button(getContext());
            buttonCopyToClipboard.setText("Copy To Clipboard");
            buttonCopyToClipboard.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            buttonCopyToClipboard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // https://stackoverflow.com/questions/19253786/how-to-copy-text-to-clip-board-in-android
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(getContext(), ClipboardManager.class);
                    ClipData clip = ClipData.newPlainText("copy_of_identity", Base32Wrapper.encodeStringToString(clientIdentity.toString()));
                    clipboard.setPrimaryClip(clip);
                }
            });
            binding.linearLayoutShowQrCode.addView(buttonCopyToClipboard);
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