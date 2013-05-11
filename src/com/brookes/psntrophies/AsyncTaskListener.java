package com.brookes.psntrophies;

import android.graphics.Bitmap;

public interface AsyncTaskListener {
	public void onProfileDownloaded(String profileXML); //Used by GetXML
	public void onGamesDownloaded(String gamesXML); //Used by GetXML
	public void onTrophiesDownloaded(String trophiesXML); //Used by GetXML
	public void onProfileImageDownloaded(Bitmap image); //Used by GetImage
	public void onGameImageDownloaded(String url, Bitmap image); //Used by GetImage
}
