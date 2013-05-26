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

public class GetImage extends AsyncTask <String, Void, Bitmap> {
	private ProgressDialog mProgressDialog;
	private Context context;
	private AsyncTaskListener callback; //Will be called to tell activity download has finished
	private DownloadType downloadType;
	private String uri = "";

	public GetImage(Context context){
		this.context = context;
		mProgressDialog = new ProgressDialog(this.context);
	    mProgressDialog.setMessage("Downloading image");
	    mProgressDialog.setIndeterminate(true); //Starts spinning wheel dialog
	    this.callback = (AsyncTaskListener)context;
	}
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	    mProgressDialog.show();
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
		mProgressDialog.dismiss();
		switch(downloadType){
			case PROFILE:
				callback.onProfileImageDownloaded(resultOfComputation); //Returns the profile
				break;
			case GAMESTROPHIES:
				callback.onGameImageDownloaded(uri, resultOfComputation);
				break;
			default:
				break;
			}
	}
	
}
