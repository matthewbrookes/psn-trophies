package com.brookes.psntrophies;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Game implements Parcelable{
	private String id; //Unique Game ID
	private String image; //Holds URL to game's image
	private String title; //Holds game's title
	private int ProgresstoCompletion; //Stored as integer but used as percentage
	private int[] Trophies = new int[4]; //Array of trophies in form (Platinum, Gold, Silver, Bronze)
	private int trophiesAchievable; //Number of trophies available
	private String platform; //ps3 or psp2(Vita)
	private String updated; //Holds time of last update
	private Bitmap imageBitmap;
	
	public Game(){}
	public Game(Parcel in) { //Create parcel from object
		readFromParcel(in);
	}
	

	public String getTitle(){
		return this.title;
	}
	public void setTitle(String name){
		this.title = name;
	}
	
	public String getImage(){
		return this.image;
	}
	public void setImage(String url){
		this.image = url;
	}
	
	public String getId(){
		return this.id;
	}
	public void setId(String id){
		this.id = id;
	}
	
	public int getProgress(){
		return this.ProgresstoCompletion;
	}
	public void setProgress(int progress){
		this.ProgresstoCompletion = progress;
	}
	
	public int[] getTrophies(){
		return Trophies;
	}
	public void setTrophies(int[] trophies){
		this.Trophies = trophies;
	}
	public void setPlatium(int platinum){
		this.Trophies[0] = platinum; //Manually modifies Trophies array to add platinum trophies to 1st index
	}
	public void setGold(int gold){
		this.Trophies[1] = gold;
	}
	public void setSilver(int silver){
		this.Trophies[2] = silver;
	}
	public void setBronze(int bronze){
		this.Trophies[3] = bronze;
	}
	public int getTrophiesEarnt(){
		int total = 0;
		for(int i=0;i<Trophies.length;i++){
			total += Trophies[i]; //Adds together all the trophies
		}
		return total;
	}
	
	public int getTotalTrophies(){
		return this.trophiesAchievable;
	}
	public void setTotalTrophies(int total){
		this.trophiesAchievable = total;
	}
	
	public String getPlatform(){
		return this.platform;
	}
	public void setPlatform(String platform){
		this.platform = platform;
	}
	
	public String getUpdated(){
		return this.updated;
	}
	public void setUpdated(String time){
		this.updated = time;
	}
	
	public Bitmap getBitmap(){
		return this.imageBitmap;
	}
	public void setBitmap(Bitmap bitmap){
		this.imageBitmap = bitmap;
	}
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// Writes variables to parcel
		dest.writeString(this.id);
		dest.writeString(this.image);
		dest.writeString(this.platform);
		dest.writeString(this.title);
		dest.writeInt(this.trophiesAchievable);
		dest.writeInt(this.ProgresstoCompletion);
		dest.writeIntArray(this.Trophies);
		dest.writeValue(this.imageBitmap);
	}
	private void readFromParcel(Parcel in) {
		// Stores variables from parcel
		this.id = in.readString();
		this.image = in.readString();
		this.platform = in.readString();
		this.title = in.readString();
		this.trophiesAchievable = in.readInt();
		this.ProgresstoCompletion = in.readInt();
		this.Trophies = in.createIntArray();
		this.imageBitmap = (Bitmap) in.readValue(null);
		
	}
	
	@SuppressWarnings("rawtypes")
	public static final Parcelable.Creator CREATOR =
	    	new Parcelable.Creator() {
	            public Game createFromParcel(Parcel in) {
	                return new Game(in);
	            }
	 
	            public Game[] newArray(int size) {
	                return new Game[size];
	            }
	        };
}
