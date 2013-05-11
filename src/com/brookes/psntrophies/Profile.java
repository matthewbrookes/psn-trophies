package com.brookes.psntrophies;

public class Profile {
	private String Username; //PSN name
	private String Avatar; //Holds URL to user's avatar
	private String AboutMe; //Holds short description of user
	private int Level; //Trophy Level determined by Sony
	private int ProgresstoNextLevel; //Stored as integer but used as percentage
	private int[] Trophies = new int[4]; //Array of trophies in form (Platinum, Gold, Silver, Bronze)
	private boolean PlaystationPlus; //Is the user a Plus member
	private int[] BackgroundColor = new int[3]; //Array of background color in form RGB
	
	public String getName(){
		return this.Username;
	}
	public void setName(String name){
		this.Username = name;
	}
	
	public String getAvatar(){
		return this.Avatar;
	}
	public void setAvatar(String avatar){
		this.Avatar = avatar;
	}
	
	public String getAboutMe(){
		return this.AboutMe;
	}
	public void setAboutMe(String aboutme){
		this.AboutMe = aboutme;
	}
	
	public int getLevel(){
		return this.Level;
	}
	public void setLevel(int level){
		this.Level = level;
	}
	
	public int getProgess(){
		return this.ProgresstoNextLevel;
	}
	public void setProgress(int progress){
		this.ProgresstoNextLevel = progress;
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
	public int getTotalTrophies(){
		int total = 0;
		for(int i=0;i<Trophies.length;i++){
			total += Trophies[i]; //Adds together all the trophies
		}
		return total;
	}
	
	public boolean isPlus(){
		return this.PlaystationPlus;
	}
	public void setPlus(boolean plus){
		this.PlaystationPlus = plus;
	}
	
	public int[] getBackgroundColor(){
		return this.BackgroundColor;
	}
	public void setBackgroundColor(int[] color){
		this.BackgroundColor = color;
	}
	public void setBackgroundRed(int red){
		this.BackgroundColor[0] = red; //Sets 1st index of array to red value specified
	}
	public void setBackgroundGreen(int green){
		this.BackgroundColor[1] = green;
	}
	public void setBackgroundBlue(int blue){
		this.BackgroundColor[2] = blue;
	}
}
