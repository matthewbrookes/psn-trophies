package com.brookes.psntrophies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class SortList {
	public ArrayList<Game> sortRecent(ArrayList<Game> old){
		ArrayList<Game> newList = old;
		if(old.size() > 1){
			while(!isSortedRecent(newList)){
				for(int i=0;i<newList.size()-1;i++){
					if(newList.get(i).getOrderPlayed() > newList.get(i+1).getOrderPlayed()){
						Collections.swap(newList, i, i+1);
					}
				}
			}
		}
		return newList;
	}
	public boolean isSortedRecent(ArrayList<Game> list){
		for(int i=0;i<list.size()-1;i++){
			if(list.get(i).getOrderPlayed() > list.get(i+1).getOrderPlayed()){
				return false;
			}
		}
		return true;
	}
	
	public ArrayList<Game> sortAlphabetical(ArrayList<Game> old){
		ArrayList<Game> newList = old;
		Collections.sort(newList, new Comparator<Game>() {
		    public int compare(Game result1, Game result2) {
		        return result1.getTitle().toUpperCase(Locale.US).compareTo(result2.getTitle().toUpperCase(Locale.US));
		    }
		});
		return newList;
	}
	
	public ArrayList<Game> sortPlatform(ArrayList<Game> old){
		ArrayList<Game> ps3List = new ArrayList<Game>();
		ArrayList<Game> psvitaList = new ArrayList<Game>();
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