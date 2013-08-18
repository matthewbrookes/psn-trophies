package com.brookes.psntrophies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class FriendsAdapter extends BaseAdapter {

    private ArrayList<Friend> _data;
    Context _c;

    FriendsAdapter(ArrayList<Friend> data, Context c){
        _data = data;
        _c = c;
    }
   
    public int getCount() {
        return _data.size();
    }
    
    public Object getItem(int position) {
        return _data.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
   
    public View getView(int position, View convertView, ViewGroup parent) {
         View v = convertView;
         if (v == null)
         {
            LayoutInflater vi = (LayoutInflater)_c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.list_item_friend, null);
         }
        //Assigns widgets to variables
        ImageView friendImage = (ImageView) v.findViewById(R.id.friendImage);
        TextView name = (TextView)v.findViewById(R.id.friendName);
        TextView trophyLevel = (TextView)v.findViewById(R.id.trophyLevel);
        TextView platinumLabel = (TextView)v.findViewById(R.id.platinumLabel);
        TextView goldLabel = (TextView)v.findViewById(R.id.goldLabel);
        TextView silverLabel = (TextView)v.findViewById(R.id.silverLabel);
        TextView bronzeLabel = (TextView)v.findViewById(R.id.bronzeLabel);
        TextView presence = (TextView)v.findViewById(R.id.presence);
        TextView game = (TextView)v.findViewById(R.id.game);
 
        Friend friend = _data.get(position);

        //Draws information in widgets
        name.setText(friend.getUsername());
        trophyLevel.setText("" + friend.getLevel());
        platinumLabel.setText("" + friend.getTrophies()[0]);
        goldLabel.setText("" + friend.getTrophies()[1]);
        silverLabel.setText("" + friend.getTrophies()[2]);
        bronzeLabel.setText("" + friend.getTrophies()[3]);
        if(friend.getImage() != null){ //If image has been downloaded
            friendImage.setImageBitmap(friend.getImage());
        }
        else{
            friendImage.setImageResource(R.drawable.defaultavatar);
        }
        if(friend.isOnline()){
            presence.setText("Online");
        }
        else{
            presence.setText("Offline");
        }
        if(friend.getGame().equalsIgnoreCase("null")){
            game.setText("");
        }
        else{
            game.setText(friend.getGame());
        }
             
        return v;
    }
}