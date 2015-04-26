package com.example.jplayer.model;

public class Playlist {
	long playlist_id;
	String playlist_name;
	
	public Playlist(long playlist_id,String playlist_name)
	{
		this.playlist_id=playlist_id;
		this.playlist_name=playlist_name;
	}

	public long getPlaylist_id() {
		return playlist_id;
	}

	public void setPlaylist_id(long playlist_id) {
		this.playlist_id = playlist_id;
	}

	public String getPlaylist_name() {
		return playlist_name;
	}

	public void setPlaylist_name(String playlist_name) {
		this.playlist_name = playlist_name;
	}
	
	
}
