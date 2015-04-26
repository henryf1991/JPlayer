package com.example.jplayer.model;



public class SongHeader {
	
	private String songHeader;
	private long albumid;
	
	public SongHeader(String songHeader,long albumid) {
		// TODO Auto-generated constructor stub
		this.songHeader=songHeader;
		this.albumid=albumid;
	}
		
	public String getSongHeader() {
		return songHeader;
	}
	public void setSongHeader(String songHeader) {
		this.songHeader = songHeader;
	}
	
	public long getAlbumid() {
		return albumid;
	}
	public void setAlbumid(long albumid) {
		this.albumid = albumid;
	}
	
	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		boolean result=false;
		//Log.d("Test","classes " + o.getClass().getSimpleName()+" "+getClass().getSimpleName());
		//Log.d("Test", "condition " + String.valueOf(o==null)+" "+String.valueOf( o.getClass()!=getClass()));
		
		if(o==null || o.getClass()!=getClass())
		{
			result = false;
		}
		else
		{
			SongHeader snghdr=(SongHeader)o;
			//Log.d("Test", this.songHeader +" = "+snghdr.songHeader);
			if(this.songHeader==snghdr.songHeader || this.getSongHeader().equals(snghdr.getSongHeader()))
			{
				result=true;
			}
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		int hash=3;
		hash=7 * hash+this.songHeader.hashCode();
		return hash;
	}

}
