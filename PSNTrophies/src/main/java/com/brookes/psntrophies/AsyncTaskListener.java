package com.brookes.psntrophies;

import android.graphics.Bitmap;

public interface AsyncTaskListener {
	public void onProfileDownloaded(String profileXML); //Used by GetXML
	public void onPSNGamesDownloaded(String gamesXML); //Used by GetXML
	public void onPSNTrophiesDownloaded(String trophiesXML); //Used by GetXML
    public void onFriendsDownloaded(String friendsXML); //Used by GetXML
	public void onProfileImageDownloaded(String url, Bitmap image); //Used by GetImage
	public void onGameImageDownloaded(String url, Bitmap image); //Used by GetImage
}
