package com.example.otpgeneratorwithrecovery.client;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.example.otpgeneratorwithrecovery.R;
import com.example.otpgeneratorwithrecovery.util.AESWrapper;
import com.example.otpgeneratorwithrecovery.util.Util;

import org.apache.commons.codec.binary.Hex;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Perform operations related to the server.
 * references:
 * - https://www.digitalocean.com/community/tutorials/okhttp-android-example-tutorial
 * - https://guides.codepath.com/android/Using-OkHttp
 */
public class Client {
    private static final String SERVER = "server";

    public static String getBaseURL(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.client_identity_shared_preferences_file), Context.MODE_PRIVATE);
        return sharedPref.getString(Client.SERVER, "");
    }

    public static void setBaseURL(Context context, String baseURL) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.client_identity_shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Client.SERVER, baseURL);
        editor.apply();
    }

    public static void getSharedBackup(Context context, String clientID) {
        GetSharedBackupExecutor getExecutor = new GetSharedBackupExecutor(context);
        getExecutor.execute(Client.getBaseURL(context), clientID);
    }

    static class GetSharedBackupExecutor extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();
        Context context;

        public GetSharedBackupExecutor(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                Request.Builder builder = new Request.Builder();
                builder.url(String.format("%s/get/%s", params[0], params[1]));
                Request request = builder.build();
                Response response = client.newCall(request).execute();
                return response.body().string();
            } catch (Exception e){
                // do nothing
            }
            return "[]";
        }

        @Override
        protected void onPostExecute(String s) {
            // TODO
            System.out.println("yang didapatkan " + s);
        }
    }
}
