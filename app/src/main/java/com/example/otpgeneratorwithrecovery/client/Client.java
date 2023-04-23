package com.example.otpgeneratorwithrecovery.client;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;

import com.example.otpgeneratorwithrecovery.R;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Perform operations related to the server.
 * references:
 * - https://www.digitalocean.com/community/tutorials/okhttp-android-example-tutorial
 * - https://guides.codepath.com/android/Using-OkHttp
 */
public class Client {
    private static final String SERVER = "server";
    private static final String NOT_OK = "NOT OK";

    public static String getBaseURL(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.client_identity_shared_preferences_file), Context.MODE_PRIVATE);
        return sharedPref.getString(Client.SERVER, "-");
    }

    public static void setBaseURL(Context context, String baseURL) {
        SharedPreferences sharedPref = context.getSharedPreferences(context.getResources().getString(R.string.client_identity_shared_preferences_file), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Client.SERVER, baseURL);
        editor.apply();
    }

    public static String checkHealth(String baseURL) {
        CheckHealthExecutor executor = new CheckHealthExecutor();
        try {
            executor.execute(baseURL).get();
            return executor.getResult();
        } catch (Exception e) {
            return Client.NOT_OK;
        }
    }

    static class CheckHealthExecutor extends AsyncTask<String, Void, Void> {
        OkHttpClient client = new OkHttpClient();
        String result = Client.NOT_OK;

        @Override
        protected Void doInBackground(String... params) {
            try {
                Request.Builder builder = new Request.Builder();
                builder.url(String.format("%s/_healthcheck", params[0]));
                Request request = builder.build();
                Response response = client.newCall(request).execute();
                result = response.body().string();
            } catch (Exception e){
                result = Client.NOT_OK;
            }
            return (Void)null;
        }

        public String getResult() {
            return result;
        }
    }

    public static String sendSharedBackup(Context context, SharedBackup[] sharedBackups) {
        SendSharedBackupExecutor executor = new SendSharedBackupExecutor();
        try {
            JSONArray ja = new JSONArray();
            for (SharedBackup sharedBackup : sharedBackups) {
                ja.put(sharedBackup.getJSONObject());
            }
            executor.execute(getBaseURL(context), ja.toString()).get();
            return executor.getResult();
        } catch (Exception e) {
            return SendSharedBackupExecutor.FAILED_TO_SEND_SHARED_BACKUP;
        }
    }

    static class SendSharedBackupExecutor extends AsyncTask<String, Void, Void> {
        OkHttpClient client = new OkHttpClient();
        public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        public static final String FAILED_TO_SEND_SHARED_BACKUP = "Failed to send shared backup to server";
        public static final String BACKUP_SENT_SUCCESSFULLY = "Backup sent successfully to server";
        String result = FAILED_TO_SEND_SHARED_BACKUP;

        @Override
        protected Void doInBackground(String... params) {
            try {
                Request.Builder builder = new Request.Builder();
                builder.url(String.format("%s/insert", params[0]));
                RequestBody body = RequestBody.create(JSON, params[1]);
                Request request = builder.post(body).build();
                Response response = client.newCall(request).execute();
                if (response.code() == 204) {
                    result = BACKUP_SENT_SUCCESSFULLY;
                }
            } catch (Exception e){
                // do nothing
            }
            return (Void)null;
        }

        public String getResult() {
            return result;
        }
    }

    public static void getSharedBackup(Context context, String clientID) {
        GetSharedBackupExecutor executor = new GetSharedBackupExecutor(context);
        executor.execute(Client.getBaseURL(context), clientID);
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
