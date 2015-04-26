package com.example.jplayer.model;

import android.os.Parcel;
import android.os.Parcelable;



public class Song implements Parcelable{

	private long audio_id;
	private String title;
	private String album;
	private String artist;
	private String duration;
	private String displayName;
	private String songUri;
	private String dateAdded;
	private long albumID;
	
	public Song(){
	 }
	 
	 public Song(Parcel source){
	  readFromParcel(source);
	 }
	
	public Song(long audio_id,long albumID,String title,String album,String artist,String duration,String displayName,String songUri,String date_added) {
		// TODO Auto-generated constructor stub
		this.audio_id=audio_id;
		this.albumID=albumID;
		this.title=title;
		this.album=album;
		this.artist=artist;
		this.duration=duration;
		this.displayName=displayName;
		this.songUri=songUri;
	}
	
	public static final Parcelable.Creator<Song> CREATOR =
			   new Parcelable.Creator<Song>(){

			    @Override
			    public Song createFromParcel(Parcel source) {
			     return new Song(source);
			    }

			    @Override
			    public Song[] newArray(int size) {
			     return new Song[size];
			    }
			 };
	
	public long getAudio_id() {
		return audio_id;
	}

	public void setAudio_id(long audio_id) {
		this.audio_id = audio_id;
	}

	public long getAlbumID() {
		return albumID;
	}

	public void setAlbumID(long albumID) {
		this.albumID = albumID;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAlbum() {
		return album;
	}
	public void setAlbum(String album) {
		this.album = album;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getSongUri() {
		return songUri;
	}
	public void setSongUri(String songUri) {
		this.songUri = songUri;
	}
	public String getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(String dateAdded) {
		this.dateAdded = dateAdded;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeLong(audio_id);
		dest.writeLong(albumID);
		dest.writeString(title);
		dest.writeString(album);
		dest.writeString(artist);
		dest.writeString(duration);
		dest.writeString(displayName);
		dest.writeString(songUri);
		dest.writeString(dateAdded);
	}
	
	public void readFromParcel(Parcel source){
		audio_id=source.readLong();
		albumID=source.readLong();
		title=source.readString();
		album=source.readString();
		artist=source.readString();
		duration=source.readString();
		displayName=source.readString();
		songUri=source.readString();
		dateAdded=source.readString();
	}
	
}
