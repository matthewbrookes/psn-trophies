package com.brookes.psntrophies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class SortList {
	public ArrayList<Game> sortRecent(ArrayList<Game> old){
		ArrayList<Game> newList = old;
		if(old.size() > 1){ //If there are more than one games
			while(!isSortedRecent(newList)){ //If it's not already sorted according to date
				for(int i=0;i<newList.size()-1;i++){ //Reduced by one iteration to prevent index out of range error
					if(Integer.parseInt(newList.get(i).getUpdated()) < Integer.parseInt(newList.get(i+1).getUpdated())){ //If they need swapping
						Collections.swap(newList, i, i+1);
					}
				}
			}
		}
		return newList;
	}
	public boolean isSortedRecent(ArrayList<Game> list){
		for(int i=0;i<list.size()-1;i++){ //Iterates over the list until penultimate game
			if(Integer.parseInt(list.get(i).getUpdated()) < Integer.parseInt(list.get(i+1).getUpdated())){ //If they're in the wrong order
				return false;
			}
		}
		//Will only reach this point if the list is in order
		return true;
	}
	
	public ArrayList<Game> sortAlphabetical(ArrayList<Game> old){
		ArrayList<Game> newList = old;
		Collections.sort(newList, new Comparator<Game>() { //New comparator will check if they are in correct alphabetical sequence
		    public int compare(Game result1, Game result2) {
		        return result1.getTitle().toUpperCase(Locale.UK).compareTo(result2.getTitle().toUpperCase(Locale.UK)); //Uses UK Locale so i become I unlike in Turkish
		    }
		});
		return newList;
	}
	
	public ArrayList<Game> sortPlatform(ArrayList<Game> old){
		ArrayList<Game> ps3List = new ArrayList<Game>(); //List will hold only ps3 games
		ArrayList<Game> psvitaList = new ArrayList<Game>(); //List will hold only psvita games
		ArrayList<Game> finalList = new ArrayList<Game>();
		for(int i=0;i<old.size();i++){
			if(old.get(i).getPlatform().equals("psp2")){
				//If it's a Vita game
				psvitaList.add(old.get(i));
			}
			else{
				//If it's a PS3 game
				ps3List.add(old.get(i));
			}
		}
		for(int i=0;i<ps3List.size();i++){
			finalList.add(ps3List.get(i));
		}
		for(int i=0;i<psvitaList.size();i++){
			finalList.add(psvitaList.get(i));
		}
		return finalList;
	}
}