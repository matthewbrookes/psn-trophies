package com.brookes.psntrophies;

import android.graphics.Bitmap;

public class Trophy {
	public enum Type {
		PLATINUM,
		GOLD,
		SILVER,
		BRONZE
	}
	
	private int id; //Trophy ID
	private String image; //Holds URL to trophy's image
	private String title;
	private String description;
	private String dateEarned;
	private String displayDate;
	private Type trophyType;
	private boolean hidden;
	private Bitmap imageBitmap;
	
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
	
	public String getDescription(){
		return this.description;
	}
	public void setDescription(String description){
		this.description = description;
	}
	
	public String getDateEarned(){
		return this.dateEarned;
	}
	public void setDateEarned(String date){
		this.dateEarned = date;
	}
	
	public String getDisplayDate(){
		return this.displayDate;
	}
	public void setDisplayDate(String date){
		this.displayDate = date;
	}
	
	public Type getType(){
		return this.trophyType;
	}
	public void setType(Type type){
		this.trophyType = type;
	}
	
	public Boolean isHidden(){
		return this.hidden;
	}
	public void setHidden(Boolean hidden){
		this.hidden = hidden;
	}
	
	public int getId(){
		return this.id;
	}
	public void setId(int id){
		this.id = id;
	}
	
	public Bitmap getBitmap(){
		return this.imageBitmap;
	}
	public void setBitmap(Bitmap bitmap){
		this.imageBitmap = bitmap;
	}
}
