package com.brookes.psntrophies;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class GamesAdapter extends BaseAdapter {
	 
    private ArrayList<Game> _data;
    Context _c;
    
    GamesAdapter (ArrayList<Game> data, Context c){
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
   
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
         View v = convertView;
         if (v == null)
         {
            LayoutInflater vi = (LayoutInflater)_c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_game, null);
         }
           //Assigns widgets to variables
           ImageView gameImage = (ImageView) v.findViewById(R.id.gameImage);
           TextView name = (TextView)v.findViewById(R.id.gameName);
           TextView totalTrophies = (TextView)v.findViewById(R.id.gameTrophiesAchievable);
           ImageView platformImage = (ImageView) v.findViewById(R.id.platformImage);
           TextView platinumLabel = (TextView)v.findViewById(R.id.platinumLabel);
           TextView goldLabel = (TextView)v.findViewById(R.id.goldLabel);
           TextView silverLabel = (TextView)v.findViewById(R.id.silverLabel);
           TextView bronzeLabel = (TextView)v.findViewById(R.id.bronzeLabel);
 
           Game game = _data.get(position);
           
           //Draws information in widgets
           name.setText(game.getTitle());
           totalTrophies.setText("" + game.getTotalTrophies());
           platinumLabel.setText("" + game.getTrophies()[0]);
           goldLabel.setText("" + game.getTrophies()[1]);
           silverLabel.setText("" + game.getTrophies()[2]);
           bronzeLabel.setText("" + game.getTrophies()[3]);
           gameImage.setImageBitmap(game.getBitmap());
           if(game.getPlatform().equalsIgnoreCase("psp2")){
        	   platformImage.setImageResource(R.drawable.vita);
           }
           else if(game.getPlatform().equalsIgnoreCase("ps3")){
        	   platformImage.setImageResource(R.drawable.ps3);
           }
             
        return v;
}
}