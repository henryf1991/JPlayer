package com.example.jplayer.utils;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.jplayer.adapter.ExpandablePlayerAdapter;
import com.example.jplayer.model.Song;
import com.example.jplayer.model.SongHeader;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

public class MusicRetriever {
		
	public static final String TAG="MusicRetriever";
	public ContentResolver musicResolver;
	public Uri uri,groupUri;
	public String[] projections,groupProjections;
	public Cursor musicTypeCursor,musicCursor,musicGroupCursor;
	public Context context;
	public Song song;
	public SongHeader songHeader;
	public ArrayList<SongHeader> arrSongHeader;
	public ArrayList<Song> arrSong;
	public HashMap<SongHeader, ArrayList<Song>> musicMap;
	public ExpandablePlayerAdapter expndMusicAdapter;
	public String groupKey,selection,sortOrder;
	public boolean insertInMap=false, isFirstLoop=true;
	public enum MusicType{Album,Artist,Genres,Playlist,Folder};
	public MusicType musicType;
	public int row_count,curr_row;
	
	public static final int NAV_ALL=0;
	public static final int NAV_ARTIST=1;
	public static final int NAV_ALBUM=2;
	public static final int NAV_GENRE=3;
	public static final int NAV_FOLDER=4;
	public static final int NAV_PLAYLIST=5;
	public static final int NAV_UTUBE=6;
	
	public String AUDIO_ID;
	public String ALBUM_ID;
	public String TITLE;
	public String ALBUM;
	public String ARTIST;
	public String DURATION;
	public String DISPLAY_NAME;
	public String SONG_URI;
	public String DATE_ADDED;
	
	static final int AUDIO_ID_INDEX=0;
	static final int ALBUM_ID_INDEX=1;
	static final int TITLE_INDEX=2;
	static final int ALBUM_INDEX=3;
	static final int ARTIST_INDEX=4;
	static final int DURATION_INDEX=5;
	static final int DISPLAY_NAME_INDEX=6;
	static final int SONG_URI_INDEX=7;
	static final int DATE_ADDED_INDEX=8;
	
	public MusicRetriever(Context context) {
		// TODO Auto-generated constructor stub
		this.context=context;
		this.musicResolver=context.getContentResolver();
		
	} 
	
	public ExpandablePlayerAdapter retrieveMusicAdapter(int key)
	{
		switch (key) {
		
		case NAV_ALL :
			AUDIO_ID=MediaStore.Audio.Media._ID;
			ALBUM_ID=MediaStore.Audio.Media.ALBUM_ID;
			TITLE=MediaStore.Audio.Media.TITLE;
			ALBUM=MediaStore.Audio.Media.ALBUM;
			ARTIST=MediaStore.Audio.Media.ARTIST;
			DURATION=MediaStore.Audio.Media.DURATION;
			DISPLAY_NAME=MediaStore.Audio.Media.DISPLAY_NAME;
			SONG_URI=MediaStore.Audio.Media.DATA;
			DATE_ADDED=MediaStore.Audio.Media.DATE_ADDED;
			return getAllSongs();
			
		case NAV_ARTIST:
			//Retrieve according to artist
			Log.d("Test", "in case 1");
			
			AUDIO_ID=MediaStore.Audio.Media._ID;
			ALBUM_ID=MediaStore.Audio.Media.ALBUM_ID;
			TITLE=MediaStore.Audio.Media.TITLE;
			ALBUM=MediaStore.Audio.Media.ALBUM;
			ARTIST=MediaStore.Audio.Media.ARTIST;
			DURATION=MediaStore.Audio.Media.DURATION;
			DISPLAY_NAME=MediaStore.Audio.Media.DISPLAY_NAME;
			SONG_URI=MediaStore.Audio.Media.DATA;
			DATE_ADDED=MediaStore.Audio.Media.DATE_ADDED;
			return getAbumArtistQuery(ARTIST);
			
		case NAV_ALBUM:
			//Retrieve according to album
			Log.d("Test", "in case 3");
			
			AUDIO_ID=MediaStore.Audio.Media._ID;
			ALBUM_ID=MediaStore.Audio.Media.ALBUM_ID;
			TITLE=MediaStore.Audio.Media.TITLE;
			ALBUM=MediaStore.Audio.Media.ALBUM;
			ARTIST=MediaStore.Audio.Media.ARTIST;
			DURATION=MediaStore.Audio.Media.DURATION;
			DISPLAY_NAME=MediaStore.Audio.Media.DISPLAY_NAME;
			SONG_URI=MediaStore.Audio.Media.DATA;
			DATE_ADDED=MediaStore.Audio.Media.DATE_ADDED;
			return getAbumArtistQuery(ALBUM);
	        
		case NAV_GENRE:
			Log.d("Test", "in case 4");
			
			AUDIO_ID=MediaStore.Audio.Genres.Members._ID;
			ALBUM_ID=MediaStore.Audio.Genres.Members.ALBUM_ID;
			TITLE=MediaStore.Audio.Genres.Members.TITLE;
			ALBUM=MediaStore.Audio.Genres.Members.ALBUM;
			ARTIST=MediaStore.Audio.Genres.Members.ARTIST;
			DURATION=MediaStore.Audio.Genres.Members.DURATION;
			DISPLAY_NAME=MediaStore.Audio.Genres.Members.DISPLAY_NAME;
			SONG_URI=MediaStore.Audio.Genres.Members.DATA;
			DATE_ADDED=MediaStore.Audio.Genres.Members.DATE_ADDED;
			
			groupUri=MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI;
			groupProjections=new String[]{MediaStore.Audio.Genres._ID,MediaStore.Audio.Genres.NAME};
			sortOrder=MediaStore.Audio.Genres.NAME;		
			musicType = MusicType.Genres;			
			return getGenresPlaylistQuery(groupUri,groupProjections,sortOrder,musicType);
			
		case NAV_FOLDER:
			Log.d("Test", "in case 5");	
			
			AUDIO_ID=MediaStore.Audio.Media._ID;;
			ALBUM_ID=MediaStore.Audio.Media.ALBUM_ID;
			TITLE=MediaStore.Audio.Media.TITLE;
			ALBUM=MediaStore.Audio.Media.ALBUM;
			ARTIST=MediaStore.Audio.Media.ARTIST;
			DURATION=MediaStore.Audio.Media.DURATION;
			DISPLAY_NAME=MediaStore.Audio.Media.DISPLAY_NAME;
			SONG_URI=MediaStore.Audio.Media.DATA;
			DATE_ADDED=MediaStore.Audio.Media.DATE_ADDED;
			musicType = MusicType.Folder;
			return getAbumArtistQuery(SONG_URI);
			
		case NAV_PLAYLIST:
			AUDIO_ID=MediaStore.Audio.Playlists.Members._ID;
			ALBUM_ID=MediaStore.Audio.Playlists.Members.ALBUM_ID;
			TITLE=MediaStore.Audio.Playlists.Members.TITLE;
			ALBUM=MediaStore.Audio.Playlists.Members.ALBUM;
			ARTIST=MediaStore.Audio.Playlists.Members.ARTIST;
			DURATION=MediaStore.Audio.Playlists.Members.DURATION;
			DISPLAY_NAME=MediaStore.Audio.Playlists.Members.DISPLAY_NAME;
			SONG_URI=MediaStore.Audio.Playlists.Members.DATA;
			DATE_ADDED=MediaStore.Audio.Playlists.Members.DATE_ADDED;
			
			groupUri=MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
			groupProjections=new String[]{MediaStore.Audio.Playlists._ID,MediaStore.Audio.Playlists.NAME};
			sortOrder=MediaStore.Audio.Playlists.NAME;			
			musicType = MusicType.Playlist;		
			return getPopularMusic(getGenresPlaylistQuery(groupUri,groupProjections,sortOrder,musicType));
			
		default:
			return null;
		}
	}
	
	private ExpandablePlayerAdapter getAbumArtistQuery(String type)
	{
		uri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		projections=new String[]{AUDIO_ID,ALBUM_ID,TITLE,ALBUM,ARTIST,DURATION,DISPLAY_NAME,SONG_URI,DATE_ADDED};
		selection=MediaStore.Audio.Media.IS_MUSIC+"!=0";
		musicCursor=musicResolver.query(uri, projections, selection, null, type + " ASC");
		arrSongHeader=new ArrayList<SongHeader>();
		musicMap=new HashMap<SongHeader, ArrayList<Song>>();
		arrSong=new ArrayList<Song>();
		arrSong.add(0, null); //this is for adding header
		try
		{
			if(musicCursor!=null && musicCursor.moveToFirst())
			{
				row_count=musicCursor.getCount();
				curr_row=0;				
				do
				{
					curr_row++;	
					if(type.equals(SONG_URI))
					{
						String folder_path=musicCursor.getString(musicCursor.getColumnIndex(type));
						String folder_name=folder_path.substring(folder_path.substring(0, folder_path.lastIndexOf("/")).lastIndexOf("/")+1,folder_path.lastIndexOf("/"));																			
						Log.d(TAG, folder_name);
						songHeader=new SongHeader(folder_name, -1);
					}
					else
					{
						songHeader=new SongHeader(musicCursor.getString(musicCursor.getColumnIndex(type)), -1);
					}				
					if(!arrSongHeader.contains(songHeader))
					{
						if(isFirstLoop)
						{
							isFirstLoop=false;
						}
						else
						{
							
							musicMap.put(arrSongHeader.get(arrSongHeader.size()-1),arrSong );
							arrSong=new ArrayList<Song>();
							arrSong.add(0, null); //this is for adding header
							
						}
						songHeader.setAlbumid(musicCursor.getLong(ALBUM_ID_INDEX));
						arrSongHeader.add(songHeader);					
					}
					song=new Song(musicCursor.getLong(AUDIO_ID_INDEX),
							musicCursor.getLong(ALBUM_ID_INDEX),
							musicCursor.getString(TITLE_INDEX),
							musicCursor.getString(ALBUM_INDEX),
							musicCursor.getString(ARTIST_INDEX), 
							musicCursor.getString(DURATION_INDEX),  
							musicCursor.getString(DISPLAY_NAME_INDEX),
							musicCursor.getString(SONG_URI_INDEX),
							musicCursor.getString(DATE_ADDED_INDEX));
					
					arrSong.add(song);
					
					if(curr_row==row_count)
					{
						musicMap.put(songHeader,arrSong );
					}
					
					
				}while(musicCursor.moveToNext());
				
				expndMusicAdapter=new ExpandablePlayerAdapter(this.context, arrSongHeader, musicMap);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e("ERROR", e.getMessage());
		}
		finally
		{
			if(musicCursor!=null)
			{
				musicCursor.close();
			}
		}
		
		return expndMusicAdapter;
	}
	
	private ExpandablePlayerAdapter getGenresPlaylistQuery(Uri groupUri,String[] groupProjections,String sortOrder,MusicType musicType)
	{	
		musicGroupCursor=musicResolver.query(groupUri, groupProjections, null, null, sortOrder + " ASC");
		
		//Initialize the adapter constants
		arrSongHeader=new ArrayList<SongHeader>();
		musicMap=new HashMap<SongHeader, ArrayList<Song>>();
		arrSong=new ArrayList<Song>();
		
		
		try
		{
			if(musicGroupCursor!=null && musicGroupCursor.moveToFirst())
			{
				do
				{
					songHeader=new SongHeader(musicGroupCursor.getString(1), -1);
					arrSongHeader.add(songHeader);
					arrSong=getSongQuery(musicGroupCursor.getLong(0),musicType);
					arrSong.add(0, null);
					songHeader.setAlbumid(arrSong.get(1).getAlbumID());
					musicMap.put(songHeader, arrSong);
				}
				while(musicGroupCursor.moveToNext());
				
				
			}
			expndMusicAdapter=new ExpandablePlayerAdapter(this.context, arrSongHeader, musicMap);
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e("ERROR", e.getMessage());
		}
		finally
		{
			if(musicGroupCursor!=null)
			{
				musicGroupCursor.close();
			}
		}
		
		return expndMusicAdapter;
	}
	
	private ArrayList<Song> getSongQuery(long listId, MusicType musicType)
	{
		switch (musicType) {
		case Genres:
			uri=MediaStore.Audio.Genres.Members.getContentUri("external", listId);
			projections=new String[]{AUDIO_ID,ALBUM_ID,TITLE,ALBUM,ARTIST,DURATION,DISPLAY_NAME,SONG_URI,DATE_ADDED};
			selection=MediaStore.Audio.Genres.Members.IS_MUSIC+"!=0";
			break;
			
		case Playlist:
			uri=MediaStore.Audio.Playlists.Members.getContentUri("external", listId);
			projections=new String[]{AUDIO_ID,ALBUM_ID,TITLE,ALBUM,ARTIST,DURATION,DISPLAY_NAME,SONG_URI,DATE_ADDED};
			selection=MediaStore.Audio.Playlists.Members.IS_MUSIC+"!=0";
			break;

		default:
			break;
		}	
		musicCursor=musicResolver.query(uri, projections, selection, null, null);
		arrSong=new ArrayList<Song>();		
		try
		{ 
			if(musicCursor!=null && musicCursor.moveToFirst());
			{
				do
				{
					song=new Song(musicCursor.getLong(AUDIO_ID_INDEX),
							musicCursor.getLong(ALBUM_ID_INDEX),
							musicCursor.getString(TITLE_INDEX),
							musicCursor.getString(ALBUM_INDEX),
							musicCursor.getString(ARTIST_INDEX), 
							musicCursor.getString(DURATION_INDEX),  
							musicCursor.getString(DISPLAY_NAME_INDEX),
							musicCursor.getString(SONG_URI_INDEX),
							musicCursor.getString(DATE_ADDED_INDEX));
					arrSong.add(song);
					
				}while(musicCursor.moveToNext());
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e("ERROR", e.getMessage());
		}
		finally
		{
			if(musicCursor!=null)
			{
				musicCursor.close();
			}
		}
		
		return arrSong;
	}
	
	public ExpandablePlayerAdapter getPopularMusic(ExpandablePlayerAdapter expndPlayer)
	{
		AUDIO_ID=MediaStore.Audio.Media._ID;
		ALBUM_ID=MediaStore.Audio.Media.ALBUM_ID;
		TITLE=MediaStore.Audio.Media.TITLE;
		ALBUM=MediaStore.Audio.Media.ALBUM;
		ARTIST=MediaStore.Audio.Media.ARTIST;
		DURATION=MediaStore.Audio.Media.DURATION;
		DISPLAY_NAME=MediaStore.Audio.Media.DISPLAY_NAME;
		SONG_URI=MediaStore.Audio.Media.DATA;
		DATE_ADDED=MediaStore.Audio.Media.DATE_ADDED;
		
		ArrayList<SongHeader> tmpSongHeader=expndPlayer.getArrSongHeader();
		HashMap<SongHeader,ArrayList<Song>> tmpMusicMap=expndPlayer.getMusicMap();
		ArrayList<Song> tmpSong=new ArrayList<Song>();
		Uri tmpUri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		String[] tmpprojections=new String[]{AUDIO_ID,ALBUM_ID,TITLE,ALBUM,ARTIST,DURATION,DISPLAY_NAME,SONG_URI,DATE_ADDED};
		Cursor tmpmusicCursor=musicResolver.query(tmpUri, tmpprojections, null, null, DATE_ADDED + " DESC LIMIT 25");
		try
		{
			if(tmpmusicCursor!=null && tmpmusicCursor.moveToFirst())
			{
				songHeader=new SongHeader("Recently Added", -1);
				tmpSongHeader.add(songHeader);
				do
				{
					song=new Song(tmpmusicCursor.getLong(AUDIO_ID_INDEX),
							tmpmusicCursor.getLong(ALBUM_ID_INDEX),
							tmpmusicCursor.getString(TITLE_INDEX),
							tmpmusicCursor.getString(ALBUM_INDEX),
							tmpmusicCursor.getString(ARTIST_INDEX), 
							tmpmusicCursor.getString(DURATION_INDEX),  
							tmpmusicCursor.getString(DISPLAY_NAME_INDEX),
							tmpmusicCursor.getString(SONG_URI_INDEX),
							tmpmusicCursor.getString(DATE_ADDED_INDEX));
					tmpSong.add(song);
				}while(tmpmusicCursor.moveToNext());
			}
			tmpSong.add(0, null);
			songHeader.setAlbumid(tmpSong.get(1).getAlbumID());
			tmpMusicMap.put(songHeader, tmpSong);
			
			
			
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.d("ERROR", e.getMessage());
		}
		finally
		{
			if(tmpmusicCursor!=null)
			{
				tmpmusicCursor.close();
			}
		}
		return new ExpandablePlayerAdapter(this.context, tmpSongHeader, tmpMusicMap);
	}
	
	public ExpandablePlayerAdapter getAllSongs()
	{
		uri=MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		projections=new String[]{AUDIO_ID,ALBUM_ID,TITLE,ALBUM,ARTIST,DURATION,DISPLAY_NAME,SONG_URI,DATE_ADDED};
		selection=MediaStore.Audio.Media.IS_MUSIC+"!=0";
		musicCursor=musicResolver.query(uri, projections, selection, null, TITLE + " ASC");
		musicMap=new HashMap<SongHeader, ArrayList<Song>>();
		arrSongHeader=new ArrayList<SongHeader>();
		arrSong=new ArrayList<Song>();
		arrSong.add(0, null);
		try
		{
			if(musicCursor!=null && musicCursor.moveToFirst())
			{
				do
				{
					song=new Song(musicCursor.getLong(AUDIO_ID_INDEX),
							musicCursor.getLong(ALBUM_ID_INDEX),
							musicCursor.getString(TITLE_INDEX),
							musicCursor.getString(ALBUM_INDEX),
							musicCursor.getString(ARTIST_INDEX), 
							musicCursor.getString(DURATION_INDEX),  
							musicCursor.getString(DISPLAY_NAME_INDEX),
							musicCursor.getString(SONG_URI_INDEX),
							musicCursor.getString(DATE_ADDED_INDEX));
					arrSong.add(song);
				}while(musicCursor.moveToNext());
				songHeader=new SongHeader("All Songs", arrSong.get(1).getAlbumID());
				arrSongHeader.add(songHeader);
				musicMap.put(songHeader, arrSong);
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e("ERROR", e.getMessage());
		}
		finally
		{
			if(musicCursor!=null)
				musicCursor.close();
		}
		return new ExpandablePlayerAdapter(context, arrSongHeader, musicMap);
		
	}

}
