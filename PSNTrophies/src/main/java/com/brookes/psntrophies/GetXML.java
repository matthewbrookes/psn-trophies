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

public class GetXML extends AsyncTask <String, Void, String> {
	private ProgressDialog mProgressDialog;
	private Context context;
	private AsyncTaskListener callback; //Will be called to tell activity download has finished
	private DownloadType downloadType;
	
	public GetXML(Context context){
		this.context = context;
		mProgressDialog = new ProgressDialog(this.context);
	    mProgressDialog.setMessage("Downloading data");
	    mProgressDialog.setIndeterminate(true); //Starts spinning wheel dialog
	    mProgressDialog.setCancelable(false);
	    this.callback = (AsyncTaskListener)context;
	}
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    mProgressDialog.show();
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
		mProgressDialog.dismiss();
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
			default:
				break;
		}
		
	}
	
}
