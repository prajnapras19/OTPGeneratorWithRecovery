package com.example.otpgeneratorwithrecovery;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ScrollView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.otpgeneratorwithrecovery.client.Client;
import com.example.otpgeneratorwithrecovery.client.SharedBackup;
import com.example.otpgeneratorwithrecovery.crypto.OTPFriend;
import com.example.otpgeneratorwithrecovery.util.Base32Wrapper;
import com.example.otpgeneratorwithrecovery.crypto.OTPSecret;
import com.example.otpgeneratorwithrecovery.crypto.SharedSecret;
import com.example.otpgeneratorwithrecovery.databinding.FragmentCreateBackupBinding;
import com.example.otpgeneratorwithrecovery.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Create backup with SharedSecret class
 */
public class CreateBackupFragment extends Fragment {
    private FragmentCreateBackupBinding binding;
    private final static String CHOOSE_SECRET = "Choose OTP";
    private final static String THRESHOLD = "Threshold (default: number of recipients)";
    RecipientAdapter adapterRecipients;
    ArrayList<OTPFriend> friendArrayList;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentCreateBackupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("DefaultLocale")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // fill otp secrets
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> otpSecrets = sharedPref.getAll();

        ArrayList<String> otpIdentifiers = new ArrayList<>();
        otpIdentifiers.add(CreateBackupFragment.CHOOSE_SECRET);
        int i = 1;
        for (String k : Util.getSortedMapKey(otpSecrets)) {
            try {
                otpIdentifiers.add(String.format("%d. %s", i, new OTPSecret((String)otpSecrets.get(k)).getIdentifier()));
                i++;
            } catch (Exception e) {
                Log.v("CreateBackupFragment", e.getMessage());
            }
        }

        ArrayAdapter<String> adapterOTPSecrets = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, otpIdentifiers);
        adapterOTPSecrets.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.choiceCreateBackup.setAdapter(adapterOTPSecrets);

        // fill recipients
        SharedPreferences friendsSharedPref = getContext().getSharedPreferences(getString(R.string.friends_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> friends = friendsSharedPref.getAll();

        ArrayList<String> recipients = new ArrayList<>();
        friendArrayList = new ArrayList<>();
        i = 1;
        for (String k : Util.getSortedMapKey(friends)) {
            try {
                OTPFriend friend = new OTPFriend((String)friends.get(k));
                friendArrayList.add(friend);
                recipients.add(String.format("%d. %s (%s)", i, friend.getName(), friend.getClientID()));
                i++;
            } catch (Exception e) {
                Log.v("CreateBackupFragment", e.getMessage());
            }
        }

        adapterRecipients = new RecipientAdapter(getContext(), R.layout.checkbox_layout, recipients);
        binding.listViewRecipients.setAdapter(adapterRecipients);

        // fill threshold
        ArrayList<String> numbers = new ArrayList<>();
        numbers.add(CreateBackupFragment.THRESHOLD);
        for (int j = 0; j < recipients.size(); j++) {
            numbers.add(String.valueOf(j + 1));
        }
        ArrayAdapter<String> adapterThreshold = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, numbers);
        adapterThreshold.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.choiceThreshold.setAdapter(adapterThreshold);

        binding.buttonSubmitCreateBackup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValid()) {
                    try {
                        saveBackup();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Success");
                        builder.setMessage("Backup created successfully.");
                        AlertDialog alert = builder.create();
                        alert.show();
                        NavHostFragment.findNavController(CreateBackupFragment.this).navigateUp();
                    } catch (Exception e) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Error");
                        builder.setMessage(e.getMessage());
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Error");
                builder.setMessage("All recipients must be filled and secret must be chosen, please try again.");
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    class RecipientAdapter extends ArrayAdapter<String> {
        private final List<String> items;
        private boolean[] isCheckedList;
        private int layoutResource;

        public RecipientAdapter(Context context, int resource, List<String> items) {
            super(context, resource, items);
            this.items = items;
            this.layoutResource = resource;
            this.isCheckedList = new boolean[items.size()];
            for (int i = 0; i < items.size(); i++) {
                this.isCheckedList[i] = false;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(this.layoutResource, parent, false);
            }
            CheckBox checkBox = convertView.findViewById(R.id.checkbox);
            checkBox.setText(items.get(position));
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    isCheckedList[position] = isChecked;
                }
            });

            return convertView;
        }

        public String[] getCheckedItems() {
            List<String> res = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                if (isCheckedList[i]) {
                    res.add(items.get(i).split("\\. ")[1]);
                }
            }
            return res.toArray(new String[0]);
        }

        public Integer[] getCheckedNumbers() {
            List<Integer> res = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                if (isCheckedList[i]) {
                    res.add(Integer.valueOf(items.get(i).split(". ")[0]) - 1);
                }
            }
            return res.toArray(new Integer[0]);
        }
    }

    public boolean isValid() {
        boolean someFieldsAreEmpty = binding.choiceCreateBackup.getSelectedItem().toString().equals(CreateBackupFragment.CHOOSE_SECRET);
        someFieldsAreEmpty |= (adapterRecipients.getCheckedItems().length == 0);
        return !someFieldsAreEmpty;
    }

    public void saveBackup() throws Exception {
        int otpSecretNumber = Integer.valueOf(binding.choiceCreateBackup.getSelectedItem().toString().split(". ")[0]);
        OTPSecret otpSecret = null;
        SharedPreferences sharedPref = getContext().getSharedPreferences(getString(R.string.otp_secret_shared_preferences_file), Context.MODE_PRIVATE);
        Map<String, ?> otpSecrets = sharedPref.getAll();
        int j = 1;
        for (String k : Util.getSortedMapKey(otpSecrets)) {
            try {
                OTPSecret tmp = new OTPSecret((String)otpSecrets.get(k));
                if (j == otpSecretNumber) {
                    otpSecret = tmp;
                    break;
                }
                j++;
            } catch (Exception e) {
                // do nothing
            }
        }

        // sanity check
        if (otpSecret == null) {
            throw new Exception("OTP secret not found.");
        }

        // parse recipients
        String[] recipients = adapterRecipients.getCheckedItems();

        // parse threshold
        int threshold = 1;
        String thresholdString = binding.choiceThreshold.getSelectedItem().toString();
        if (thresholdString.equals(CreateBackupFragment.THRESHOLD)) {
            threshold = recipients.length;
        } else {
            threshold = Integer.valueOf(thresholdString);
        }

        // share secret
        String[] sharedSecret = SharedSecret.generate(otpSecret, recipients, threshold);

        // TODO: also send to server
        SharedBackup[] sharedBackups = new SharedBackup[recipients.length];
        Integer[] checkedNumbers = adapterRecipients.getCheckedNumbers();
        for (int i = 0; i < checkedNumbers.length; i++) {
            sharedBackups[i] = new SharedBackup(friendArrayList.get(checkedNumbers[i]), sharedSecret[i]);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Send To Server Status");
        builder.setMessage(Client.sendSharedBackup(getContext(), sharedBackups));
        AlertDialog alert = builder.create();
        alert.show();

        // encode for saving
        String[] savedSharedSecret = new String[sharedSecret.length];
        for (int i = 0; i < sharedSecret.length; i++) {
            savedSharedSecret[i] = Base32Wrapper.encodeStringToString(sharedSecret[i]);
        }

        SharedPreferences sharedPrefCreatedBackup = getContext().getSharedPreferences(getString(R.string.created_backup_shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefCreatedBackup.edit();
        editor.putString(Util.getNextSharedPreferenceID(sharedPrefCreatedBackup.getAll()), String.join("-", savedSharedSecret));
        editor.apply();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        adapterRecipients = null;
        friendArrayList = null;
    }
}