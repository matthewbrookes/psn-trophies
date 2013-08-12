package com.brookes.psntrophies;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.ExecutionException;

/**
 * Created by matt on 20/07/13.
 */
public class ServerAuthenticate {
    private Context mContext;
    private AuthenticatorListener callback;
    private ProgressDialog pDialog;

    public ServerAuthenticate(Context context){
        this.mContext = context;
        this.callback = (AuthenticatorListener)context;
    }

    public void authenticateUser(final String email, String pass){
        //This method attempts to retrieve the psn id for given arguments
        String username = "";

        //URI which will be downloaded
        final String uri = "http://psntrophies.net16.net/getPSNID.php?email=" + email + "&pass=" + pass;

        AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(mContext);
                pDialog.setIndeterminate(true);
                pDialog.setMessage("Authenticating " + email);
                pDialog.setCancelable(false);
                pDialog.show();
            }
            @Override
            protected String doInBackground(Void... voids) {
                //Downloads from URI
                HttpClient httpClient = new DefaultHttpClient();
                HttpContext localContext = new BasicHttpContext();
                HttpGet httpGet = new HttpGet(uri);

                String result = "";
                try {
                    HttpResponse response = httpClient.execute(httpGet, localContext);
                    HttpEntity entity = response.getEntity();
                    result = EntityUtils.toString(entity);
                } catch (Exception e) {
                    //TODO Add error message if unable to download data
                }
                return result;
            }
            protected void onPostExecute(String username){
                pDialog.cancel();
                if(username.contains("hosting24")){ //If extra content
                    int pos = username.indexOf("\r"); //Position of character following username
                    String finalUsername = username.substring(0, pos); //Retrieve username from string
                    username = finalUsername;
                }
                callback.onAccountAuthenticated(username);
            }
        };
        asyncTask.execute();
    }
}