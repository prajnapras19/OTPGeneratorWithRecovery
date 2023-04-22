package com.example.otpgeneratorwithrecovery;

import android.annotation.SuppressLint;
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

import com.example.otpgeneratorwithrecovery.crypto.OTPFriend;
import com.example.otpgeneratorwithrecovery.databinding.FragmentListFriendBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.Map;

public class ListFriendFragment extends Fragment {
    private FragmentListFriendBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentListFriendBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Handler refreshHandler = new Handler();
        listFriend();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    listFriend();
                    refreshHandler.postDelayed(this, 1000);
                } catch (Exception e) {
                    // do nothing
                }
            }
        };
        refreshHandler.postDelayed(runnable, 1000);
    }

    @SuppressLint("DefaultLocale")
    public void listFriend() {
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.friends_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> friends = sharedPref.getAll();

        binding.linearLayoutList.removeAllViews();

        int i = 1;
        for (String k : Util.getSortedMapKey(friends)) {
            try {
                OTPFriend otpFriend = new OTPFriend((String) friends.get(k));
                TextView textViewName = new TextView(getContext());
                textViewName.setText(String.format("%d. Name: %s", i, otpFriend.getName()));
                textViewName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                binding.linearLayoutList.addView(textViewName);

                TextView textViewClientID = new TextView(getContext());
                textViewClientID.setText(String.format("Client ID: %s", otpFriend.getClientID()));
                textViewClientID.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                binding.linearLayoutList.addView(textViewClientID);

                Button buttonDelete = new Button(getContext());
                buttonDelete.setText("Delete");
                buttonDelete.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                buttonDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
                        alert.setTitle("Delete");
                        alert.setMessage("Are you sure you want to delete this from friend list?");
                        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.friends_shared_preferences_file), Context.MODE_PRIVATE);
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
                binding.linearLayoutList.addView(buttonDelete);

                TextView divider = new TextView(getContext());
                divider.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                binding.linearLayoutList.addView(divider);
                i++;
            } catch (Exception e) {
                Log.v("ListFriendFragment", e.getMessage());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}