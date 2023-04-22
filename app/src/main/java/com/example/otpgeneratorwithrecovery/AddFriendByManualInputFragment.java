package com.example.otpgeneratorwithrecovery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.crypto.OTPFriend;
import com.example.otpgeneratorwithrecovery.databinding.FragmentAddFriendByManualInputBinding;
import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;
import com.example.otpgeneratorwithrecovery.util.Util;

public class AddFriendByManualInputFragment extends Fragment {
    private FragmentAddFriendByManualInputBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentAddFriendByManualInputBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    OTPFriend friend = new OTPFriend(Base32Wrapper.decodeStringToString(binding.friendClientIdentity.getText().toString()));
                    friend.setName(binding.name.getText().toString());

                    SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.friends_shared_preferences_file), Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(Util.getNextSharedPreferenceID(sharedPref.getAll()), friend.toString());
                    editor.apply();

                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Success");
                    builder.setMessage(String.format("Friend %s added successfully", friend.getName()));
                    AlertDialog alert = builder.create();
                    alert.show();
                    NavHostFragment.findNavController(AddFriendByManualInputFragment.this).navigateUp();
                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Error");
                    builder.setMessage(e.getMessage());
                    AlertDialog alert = builder.create();
                    alert.show();
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