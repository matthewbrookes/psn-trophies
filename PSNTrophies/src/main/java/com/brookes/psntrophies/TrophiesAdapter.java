package com.brookes.psntrophies;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class TrophiesAdapter extends BaseAdapter {
	 
    private ArrayList<Trophy> _data;
    Context _c;
    
    TrophiesAdapter (ArrayList<Trophy> data, Context c){
        _data = data;
        _c = c;
    }
   
    public int getCount() {
        // TODO Auto-generated method stub
        return _data.size();
    }
    
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return _data.get(position);
    }
 
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }
   
    @SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
         View v = convertView;
         if (v == null)
         {
            LayoutInflater vi = (LayoutInflater)_c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_trophy, null);
         }
           Object chosenLayout = v.getTag();
           
           //Assigns widgets to variables
           ImageView trophyImage = (ImageView) v.findViewById(R.id.trophyImage);
           TextView name = (TextView)v.findViewById(R.id.trophyName);
           TextView earnedLabel = (TextView)v.findViewById(R.id.trophyDateLabel);
           TextView description = (TextView)v.findViewById(R.id.trophyDescription);
           ImageView trophyTypeImage = (ImageView) v.findViewById(R.id.trophyTypeImage);
           TextView trophyDateEarned = (TextView)v.findViewById(R.id.trophyDateEarned);
 
           Trophy trophy = _data.get(position);
           
           //Draws information in widgets
           name.setText(trophy.getTitle());
           description.setText(trophy.getDescription());
           String date = trophy.getDateEarned();
           if(date.isEmpty()){ //If there is no value i.e. it hasn't been earned
        	   earnedLabel.setText("Not earned yet!");
        	   trophyDateEarned.setText("");
           }
           else{earnedLabel.setText("Earned: ");
        	   trophyDateEarned.setText(trophy.getDisplayDate());
           }
           trophyImage.setImageBitmap(trophy.getBitmap());
           
           if(chosenLayout == null){ //If it's the regular layout
        	   switch(trophy.getType()){
				case BRONZE:
					trophyTypeImage.setImageResource(R.drawable.bronze); //Use regular images
					break;
				case GOLD:
					trophyTypeImage.setImageResource(R.drawable.gold);
					break;
				case PLATINUM:
					trophyTypeImage.setImageResource(R.drawable.platinum);
					break;
				case SILVER:
					trophyTypeImage.setImageResource(R.drawable.silver);
					break;
				default:
					break;
        	   }
           }
           
           else if(chosenLayout.equals("large_layout")){ //If it's the large layout
	           switch(trophy.getType()){
					case BRONZE:
						trophyTypeImage.setImageResource(R.drawable.bronze100); //Use large images
						break;
					case GOLD:
						trophyTypeImage.setImageResource(R.drawable.gold100);
						break;
					case PLATINUM:
						trophyTypeImage.setImageResource(R.drawable.platinum100);
						break;
					case SILVER:
						trophyTypeImage.setImageResource(R.drawable.silver100);
						break;
					default:
						break;
	           }
           }
        return v;
}
}