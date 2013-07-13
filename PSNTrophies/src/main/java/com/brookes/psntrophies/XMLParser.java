package com.brookes.psntrophies;

import android.text.Html;
import android.text.Spanned;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;


public class XMLParser{
	public Profile getProfile(String xmlString){ //Returns a Profile object from the XML string with Profile data
	Profile profile = null;
		profile = new Profile();
        String text = "";
		XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();
 
            parser.setInput(new StringReader(xmlString.replaceAll("&", "&amp;")));
 
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagname = parser.getName();
                switch (eventType) {
                case XmlPullParser.START_TAG:
                    break;
 
                case XmlPullParser.TEXT:
                    //Convert html encoding
                    String rawText = parser.getText();
                    Spanned escapedText = Html.fromHtml(rawText);
                    text = escapedText.toString();
                    break;
 
                case XmlPullParser.END_TAG:
                    if (tagname.equalsIgnoreCase("id")) {
                        profile.setName(text);
                    } else if (tagname.equalsIgnoreCase("level")) {
                        profile.setLevel(Integer.parseInt(text));
                    } else if (tagname.equalsIgnoreCase("aboutme")) {
                        profile.setAboutMe(text);
                    } else if (tagname.equalsIgnoreCase("avatar")) {
                        profile.setAvatar(text);
                    } else if (tagname.equalsIgnoreCase("progress")) {
                        profile.setProgress(Integer.parseInt(text));
                    } else if (tagname.equalsIgnoreCase("Platinum")) {
                    	profile.setPlatium(Integer.parseInt(text));
                    } else if (tagname.equalsIgnoreCase("Gold")) {
                    	profile.setGold(Integer.parseInt(text));
                    } else if (tagname.equalsIgnoreCase("Silver")) {
                    	profile.setSilver(Integer.parseInt(text));
                    } else if (tagname.equalsIgnoreCase("Bronze")) {
                    	profile.setBronze(Integer.parseInt(text));
                    } else if (tagname.equalsIgnoreCase("Plus")) {
                    	if(text.equalsIgnoreCase("1")){
                    		profile.setPlus(true);
                    	}
                    	else{
                    		profile.setPlus(false);
                    	}
                    } else if (tagname.equalsIgnoreCase("R")) {
                    	profile.setBackgroundRed(Integer.parseInt(text.replaceFirst("#", ""), 16)); // To convert hexadecimal to integer and remove #
                    } else if (tagname.equalsIgnoreCase("G")) {
                    	profile.setBackgroundGreen(Integer.parseInt(text.replaceFirst("#", ""), 16));
                    } else if (tagname.equalsIgnoreCase("B")) {
                    	profile.setBackgroundBlue(Integer.parseInt(text.replaceFirst("#", ""), 16));
                    }  
                    break;
 
                default:
                    break;
                }
                eventType = parser.next();
            }
 
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

	
	return profile;
}
	
	public ArrayList<Game> getPSNAPIGames(String xmlString){
		ArrayList<Game> games = new ArrayList<Game>();
		Game game = null;
		String text = "";
		XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();
 
            parser.setInput(new StringReader(xmlString.replaceAll("&", "&amp;")));
	        int eventType = parser.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            String tagname = parser.getName();
	            switch (eventType) {
		            case XmlPullParser.START_TAG:
		                if (tagname.equalsIgnoreCase("game")) {
		                    // create a new instance of Game
		                    game = new Game();
		                }
		                break;
		
		            case XmlPullParser.TEXT:
                        //Convert html encoding
                        String rawText = parser.getText();
                        Spanned escapedText = Html.fromHtml(rawText);
                        text = escapedText.toString();
		                break;
		
		            case XmlPullParser.END_TAG:
		            	if (tagname.equalsIgnoreCase("game")) {
                            //Calculate progress
                            float progress = 0;
                            progress += (game.getTrophies()[1] * 90); //Multiply gold trophies by score
                            progress += (game.getTrophies()[2] * 30); //Multiply silver trophies by score
                            progress += (game.getTrophies()[3] * 15); //Multiply bronze trophies by score

                            int totalPoints = game.getTotalPoints();
                            float progressPercent = (progress / totalPoints) * 100;
                            game.setProgress((int)progressPercent);

		                    // add Game object to list
		                    games.add(game);
		            	} else if (tagname.equalsIgnoreCase("id")) {
		                    game.setId(text);
		                } else if (tagname.equalsIgnoreCase("total")) {
		                    game.setTotalTrophies(Integer.parseInt(text));
		                } else if (tagname.equalsIgnoreCase("image")) {
		                    game.setImage(text);
		                } else if (tagname.equalsIgnoreCase("TotalTrophies")) {
		                    game.setTotalTrophies(Integer.parseInt(text));
		                } else if (tagname.equalsIgnoreCase("TotalPoints")) {
		                    game.setTotalPoints(Integer.parseInt(text));
		                } else if (tagname.equalsIgnoreCase("Platinum")) {
		                	game.setPlatium(Integer.parseInt(text));
		                } else if (tagname.equalsIgnoreCase("Gold")) {
		                	game.setGold(Integer.parseInt(text));
		                } else if (tagname.equalsIgnoreCase("Silver")) {
		                	game.setSilver(Integer.parseInt(text));
		                } else if (tagname.equalsIgnoreCase("Bronze")) {
		                	game.setBronze(Integer.parseInt(text));
		                } else if (tagname.equalsIgnoreCase("title")) {
		                	game.setTitle(text);
		                } else if (tagname.equalsIgnoreCase("platform")) {
		                	game.setPlatform(text);
		                } else if (tagname.equalsIgnoreCase("lastupdated")){
		                	game.setUpdated(text);
		                }
		                break;
		
	            	default:
	            		break;
	            }
            eventType = parser.next();
        }
        

    } catch (XmlPullParserException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
		return games;
		
	}
	
	public ArrayList<Trophy> getPSNAPITrophies(String xmlString){
		ArrayList<Trophy> trophies = new ArrayList<Trophy>();
		Trophy trophy = null;
		String text = "";
		XmlPullParserFactory factory = null;
        XmlPullParser parser = null;
        
        try {
            factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newPullParser();
            //Here we give our file object in the form of a stream to the parser
            parser.setInput(new StringReader(xmlString.replaceAll("&", "&amp;")));
	        int eventType = parser.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            String tagname = parser.getName();
	            switch (eventType) {
		            case XmlPullParser.START_TAG:
		                if (tagname.equalsIgnoreCase("trophy")) {
		                    // create a new instance of Trophy
		                    trophy = new Trophy();
		                }
		                break;
		
		            case XmlPullParser.TEXT:
                        //Convert html encoding
		                String rawText = parser.getText();
                        Spanned escapedText = Html.fromHtml(rawText);
                        text = escapedText.toString();
                        break;
		
		            case XmlPullParser.END_TAG:
		            	if (tagname.equalsIgnoreCase("trophy")) {
		                    // add trophy object to list
		                    trophies.add(trophy);
		            	} else if (tagname.equalsIgnoreCase("id")) {
		                    trophy.setId(Integer.parseInt(text));
		                } else if (tagname.equalsIgnoreCase("trophydate")) {
		                	if(text.contains("false") || text.contains("true")){
		                		trophy.setDateEarned("");
		                	}
		                	else{
		                		trophy.setDateEarned(text);
		                	}
		                } else if (tagname.equalsIgnoreCase("image")) {
		                    trophy.setImage(text);
		                } else if (tagname.equalsIgnoreCase("title")) {
		                	trophy.setTitle(text);
		                } else if (tagname.equalsIgnoreCase("description")) {
		                	trophy.setDescription(text);
		                } else if (tagname.equalsIgnoreCase("DisplayDate")) {
		                	trophy.setDisplayDate(text);
		                } else if (tagname.equalsIgnoreCase("hidden")) {
		                	if(text.equals("true")){
		                		trophy.setHidden(true);
		                	}
		                	else{
		                		trophy.setHidden(false);
		                	}
		                } else if (tagname.equalsIgnoreCase("type")){
		                	if(text.equalsIgnoreCase("platinum")){
		                		trophy.setType(Trophy.Type.PLATINUM);
		                	} else if(text.equalsIgnoreCase("gold")){
		                		trophy.setType(Trophy.Type.GOLD);
		                	} else if(text.equalsIgnoreCase("silver")){
		                		trophy.setType(Trophy.Type.SILVER);
		                	} else if(text.equalsIgnoreCase("bronze")){
		                		trophy.setType(Trophy.Type.BRONZE);
		                	}
		                }
		                break;
		
	            	default:
	            		break;
	            }
            eventType = parser.next();
        }
        

    } catch (XmlPullParserException e) {
        e.printStackTrace();
    } catch (IOException e) {
        e.printStackTrace();
    }
		return trophies;
		
	}
}

