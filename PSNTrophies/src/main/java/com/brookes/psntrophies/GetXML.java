package com.brookes.psntrophies;



import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

public class GetXML extends AsyncTask <String, Void, String> {
	private AsyncTaskListener callback; //Will be called to tell activity download has finished
	private DownloadType downloadType;

    public GetXML(Context context){ //This constructor is used if called from an activity
        this.callback = (AsyncTaskListener)context;
    }
    public GetXML(Fragment fragment){ //This constructor is used if called from a fragment
        this.callback = (AsyncTaskListener)fragment;
    }
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	}
	@Override
	protected String doInBackground(String... params) {
		 //Downloads from URI
		 HttpClient httpClient = new DefaultHttpClient();
		 HttpContext localContext = new BasicHttpContext();
         HttpGet httpGet = new HttpGet(params[0]);
         if(params[0].contains("getProfile.php")){ //If we're downloading a profile
        	 downloadType = DownloadType.PROFILE;
         }
         else if(params[0].contains("getGames.php")){ 
        	 downloadType = DownloadType.PSNAPIGAMES;
         }
         else if(params[0].contains("getTrophies.php")){ 
        	 downloadType = DownloadType.PSNAPITROPHIES;
         }
         else if(params[0].contains("getFriends.php")){
             downloadType = DownloadType.FRIENDS;
         }
         String result = "";
         try {
               HttpResponse response = httpClient.execute(httpGet, localContext);
               HttpEntity entity = response.getEntity();
               result = EntityUtils.toString(entity);
               while (result.contains("403 - Forbidden")){
            	   Thread.sleep(100);
            	   response = httpClient.execute(httpGet, localContext);
                   entity = response.getEntity();
                   result = EntityUtils.toString(entity);
               }
         } catch (Exception e) {
        	 //TODO Add error message if unable to download data
         }
         return result;
	}	
	protected void onPostExecute(String resultOfComputation){
		switch(downloadType){
			case PROFILE:
				callback.onProfileDownloaded(resultOfComputation); //Returns the profile
				break;
			case PSNAPIGAMES:
				callback.onPSNGamesDownloaded(resultOfComputation);
				break;
			case PSNAPITROPHIES:
				callback.onPSNTrophiesDownloaded(resultOfComputation);
				break;
            case FRIENDS:
                callback.onFriendsDownloaded(resultOfComputation);
			default:
				break;
		}
		
	}
	
}
