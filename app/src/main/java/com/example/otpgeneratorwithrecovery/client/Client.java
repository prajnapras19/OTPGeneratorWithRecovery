package com.example.otpgeneratorwithrecovery.client;


import android.os.AsyncTask;

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
    private static final String BASE_URL = "http://REDACTED";

    public static void Get(String clientID) {
        GetExecutor getExecutor = new GetExecutor();
        getExecutor.execute(Client.BASE_URL, clientID);
    }

    static class GetExecutor extends AsyncTask<String, Void, String> {
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... params) {
            Request.Builder builder = new Request.Builder();
            builder.url(String.format("%s/get/%s", params[0], params[1]));
            Request request = builder.build();

            try {
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
