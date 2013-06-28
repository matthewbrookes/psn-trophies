package com.brookes.psntrophies;

import android.content.Context;

import java.io.File;

public class DeleteImages {
    private Context context;
    public DeleteImages(Context context){
        this.context = context;
    }

    private boolean deleteDirectory(String path){
        boolean error = false; //Flag is changed if error occurs
        File file = new File(path); //Create file object from path
        if(file.exists()){
            File[] filesList = file.listFiles(); //List of all files/folders in directory
            for(int i=0; i< filesList.length; i++){
                if(filesList[i].isDirectory()){
                    if(deleteDirectory(filesList[i].getPath())){ //Recursively call function
                    }
                    else{
                        error = true; //Unable to successfully delete folder so change flag
                    }
                }
                else{
                    if(!filesList[i].delete()){ //If unable to delete file
                        error = true; //Change flag
                    }
                }
            }
            file.delete(); //Delete empty directory
        }
        else{
            error = true;
        }
        if(error){ //If error has occurred
            return false; //Report unsuccessful
        }
        else{
            return true;
        }
    }

    public boolean deleteImages(String path){
        if(deleteDirectory(path)){ //If successfully deleted everything in directory
            return true;
        }
        else{
            return false;
        }
    }
    public boolean deleteFile(String path){
        File file = new File(path); //Create file object from path
        if(file.exists()){
            if(file.delete()){ //If file successfully deleted
                return true; //Report successful
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }
    }
}
