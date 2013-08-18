package com.brookes.psntrophies;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class Friend implements Parcelable{
    private String Username; //PSN name
    private String Avatar; //Holds URL to user's avatar
    private boolean online;
    private String game;
    private int Level; //Trophy Level determined by Sony
    private int[] Trophies = new int[4]; //Array of trophies in form (Platinum, Gold, Silver, Bronze)
    private int[] BackgroundColor = new int[3]; //Array of background color in form RGB
    private Bitmap image = null;

    public Friend(){}

    public Friend(Parcel in) { //Create parcel from object
        readFromParcel(in);
    }

    public String getUsername() {
        return Username;
    }
    public void setUsername(String username) {
        Username = username;
    }

    public String getAvatar() {
        return Avatar;
    }
    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public int getLevel() {
        return Level;
    }
    public void setLevel(int level) {
        Level = level;
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

    public boolean isOnline() {
        return online;
    }
    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getGame() {
        return game;
    }
    public void setGame(String game) {
        this.game = game;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // Writes variables to parcel
        dest.writeString(this.Username);
        dest.writeString(this.Avatar);
        dest.writeString(this.game);
        dest.writeByte((byte) (this.online ? 1 : 0));//if online == true, byte == 1
        dest.writeInt(this.Level);
        dest.writeIntArray(this.Trophies);
        dest.writeIntArray(this.BackgroundColor);
        dest.writeValue(this.image);
    }
    private void readFromParcel(Parcel in) {
        // Stores variables from parcel
        this.Username = in.readString();
        this.Avatar = in.readString();
        this.game = in.readString();
        this.online = in.readByte() == 1; //online == true if byte == 1
        this.Level = in.readInt();
        this.Trophies = in.createIntArray();
        this.BackgroundColor = in.createIntArray();
        this.image = (Bitmap) in.readValue(null);

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
