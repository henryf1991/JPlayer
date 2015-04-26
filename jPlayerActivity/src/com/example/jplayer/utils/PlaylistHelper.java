package com.example.jplayer.utils;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import com.example.jplayer.model.Playlist;
import com.example.jplayer.model.Song;

public class PlaylistHelper {
	
	static final int PLAYLIST_ID_COLUMN=0;
	static final int PLAYLIST_NAME_COLUMN=1;
	
	static Context context;
	
	static Handler handler=new Handler()
	{
		public void handleMessage(Message msg) {
			Toast.makeText(context, msg.getData().getString("msg"), Toast.LENGTH_LONG).show();
		};
	};
		
	public static ArrayList<Playlist> retrievePlaylist(Context context)
	{
		ContentResolver mResolver=context.getContentResolver();
		Uri uri=MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;
		String[] projection = new String[]{MediaStore.Audio.Playlists._ID,MediaStore.Audio.Playlists.NAME};
		String sortOrder = MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER;
		ArrayList<Playlist> arrPlayList=new ArrayList<Playlist>();
		Playlist playlist;
		Cursor cursor=null;
		try
		{
			cursor=mResolver.query(uri, projection, null, null, sortOrder);
			if(cursor!=null && cursor.moveToFirst())			
			{
				do
				{
					playlist=new Playlist(cursor.getLong(PLAYLIST_ID_COLUMN), cursor.getString(PLAYLIST_NAME_COLUMN));
					arrPlayList.add(playlist);
				}while(cursor.moveToNext());
			}
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e("ERROR", e.getMessage());
		}
		finally
		{
			if(cursor!=null)
			{
				cursor.close();
			}
		}
		return arrPlayList;
	}
	
	public static boolean addToPlaylist(Context context,long playlist_id,long audio_id)
	{
		PlaylistHelper.context=context;
		ContentResolver mResolver=context.getContentResolver();
		String[] cols = new String[] {
                "count(*)"
        };
		Cursor cursor=null;
        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlist_id);
        cursor = mResolver.query(uri, cols, null, null, null);
        cursor.moveToFirst();
        final int base = cursor.getInt(0);
        cursor.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, (base + audio_id));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audio_id);
		return  mResolver.insert(uri, values)!=null?true:false;
	}
	
	public static boolean addToPlaylist(Context context,Uri playlistUri,long audio_id)
	{
		PlaylistHelper.context=context;
		ContentResolver mResolver=context.getContentResolver();
		String[] cols = new String[] {
                "count(*)"
        };
		Cursor cursor=null;
        cursor = mResolver.query(playlistUri, cols, null, null, null);
        cursor.moveToFirst();
        final int base = cursor.getInt(0);
        cursor.close();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, (base + audio_id));
        values.put(MediaStore.Audio.Playlists.Members.AUDIO_ID, audio_id);
		return  mResolver.insert(playlistUri, values)!=null?true:false;
	}
	
	public static String[] getPlayListName(ArrayList<Playlist> arrPlaylist)
	{
		String[] array=new String[arrPlaylist.size()+1];
		array[0]="new";
		for(int i=0;i<arrPlaylist.size();i++)
		{
			array[i+1]=arrPlaylist.get(i).getPlaylist_name();
		}
		return array;
	}
	
	public static void showAddToPlaylistDialog(final Context context,final Song song)
	{
		PlaylistHelper.context=context;
		AlertDialog.Builder builder=new AlertDialog.Builder(context);
		builder.setTitle("Add to Playlist");
		final ArrayList<Playlist> arrPlaylist=retrievePlaylist(context);
		builder.setItems(getPlayListName(arrPlaylist), new DialogInterface.OnClickListener() {	
			@Override
			public void onClick(DialogInterface arg0, int pos) {
				// TODO Auto-generated method stub
				final int position=pos;
				if(pos==0) createPlaylistDialog(context,song.getAudio_id());
				else
				{
					Thread thread=new Thread(new Runnable() {					
						@Override
						public void run() {
							// TODO Auto-generated method stub
							boolean suc=addToPlaylist(context,arrPlaylist.get(position-1).getPlaylist_id(), song.getAudio_id());
							if(suc) sendMessage("Song added to playlist");
							else sendMessage("Sorry could not add to playlist");
						}
					});
					thread.start();				
				}
				
			}
		});
		builder.show();
	}
	
	public static void createPlaylistDialog(final Context context, final long  audio_id)
	{
		PlaylistHelper.context=context;
		AlertDialog.Builder builder=new AlertDialog.Builder(context);
		builder.setTitle("Create Playlist");
		final EditText input = new EditText(context);
		builder.setView(input);
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			  final String playlistname = input.getText().toString();
			  if(audio_id==-1)
			  {
				  Thread thread=new Thread(new Runnable() {				
					@Override
					public void run() {
						// TODO Auto-generated method stub
						boolean suc=createPlaylist(context, playlistname)==null?false:true;
						if(suc) sendMessage("Playlist created");
						  else sendMessage("Playlist not created");
					}
				});
				  thread.start();
			  }
			  else
			  {
				  Thread thread=new Thread(new Runnable() {				
					@Override
					public void run() {
						// TODO Auto-generated method stub
						Uri playistUri=createPlaylist(context, playlistname);
						boolean suc=addToPlaylist(context, playistUri, audio_id);
						if(suc) sendMessage("Playlist created and song addded");
						else sendMessage("Playlist not created");
					}
				});
				  thread.start();
			  }			  
			  
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});

		builder.show();
		
	}
	
	public static Uri createPlaylist(Context context,String playlistname)
	{
		PlaylistHelper.context=context;
		ContentResolver mResolver=context.getContentResolver();
		ContentValues values=new ContentValues();
		values.put(MediaStore.Audio.Playlists.NAME, playlistname);
		values.put(MediaStore.Audio.Playlists.DATE_ADDED, System.currentTimeMillis());
		values.put(MediaStore.Audio.Playlists.DATE_MODIFIED, System.currentTimeMillis());
        return mResolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values);  
	}
	
	public static void sendMessage(String suc)
	{
		Message msg=handler.obtainMessage();
		Bundle b=new Bundle();
		b.putString("msg", suc);
		msg.setData(b);
		handler.sendMessage(msg);
	}
	

}
