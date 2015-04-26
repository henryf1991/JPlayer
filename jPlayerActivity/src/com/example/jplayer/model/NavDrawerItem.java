package com.example.jplayer.model;

public class NavDrawerItem {
    
    private String title;
    private int icon;
    private int musicKey;
     
    

	public NavDrawerItem(){}
 
    public NavDrawerItem(String title, int icon,int musicKey){
        this.title = title;
        this.icon = icon;
        this.musicKey = musicKey;
    }
     
     
    public int getMusicKey() {
		return musicKey;
	}

	public void setMusicKey(int musicKey) {
		this.musicKey = musicKey;
	}

	public String getTitle(){
        return this.title;
    }
     
    public int getIcon(){
        return this.icon;
    }
     
  
     
    public void setTitle(String title){
        this.title = title;
    }
     
    public void setIcon(int icon){
        this.icon = icon;
    }
    
    
     

}
