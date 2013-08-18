package com.brookes.psntrophies;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

public class GetImage extends AsyncTask <String, Void, Bitmap> {
	private AsyncTaskListener callback; //Will be called to tell activity download has finished
	private DownloadType downloadType;
	private String uri = "";

	public GetImage(Context context){ //This constructor is used if called from an activity
	    this.callback = (AsyncTaskListener)context;
	}
    public GetImage(Fragment fragment){ //This constructor is used if called from a fragment
        this.callback = (AsyncTaskListener)fragment;
    }
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	}
	@Override
	protected Bitmap doInBackground(String... params) {
		//Downloads from URI
		 HttpClient httpClient = new DefaultHttpClient();
		 HttpContext localContext = new BasicHttpContext();
         HttpGet httpGet = new HttpGet(params[0]);
         uri = params[0];
         if(params[0].contains("avatar")){ //If we're downloading a profile
        	 downloadType = DownloadType.PROFILE;
         }
         else if(params[0].contains("trophy/np/")){ //If we're downloading a game image
        	 downloadType = DownloadType.GAMESTROPHIES;
         }
         else if(params[0].contains("http://www.psnapi.com.ar/images/sony/newtrophies/")){ //Some trophy images come from here
        	 downloadType = DownloadType.GAMESTROPHIES;
         }
         else{
        	 downloadType = DownloadType.GAMESTROPHIES;
         }
         InputStream result = null;
         try {
               HttpResponse response = httpClient.execute(httpGet, localContext);
               HttpEntity entity = response.getEntity();
               result = entity.getContent();
         } catch (Exception e) {
        	 //TODO Add error message if unable to download data
         }
         final Bitmap bitmap = BitmapFactory.decodeStream(result); //Converts to Bitmap object
         
         return bitmap;
	}	
	protected void onPostExecute(Bitmap resultOfComputation){
		switch(downloadType){
			case PROFILE:
				callback.onProfileImageDownloaded(uri, resultOfComputation); //Returns the image
				break;
			case GAMESTROPHIES:
				callback.onGameImageDownloaded(uri, resultOfComputation);
				break;
			default:
				break;
			}
	}
	
}
