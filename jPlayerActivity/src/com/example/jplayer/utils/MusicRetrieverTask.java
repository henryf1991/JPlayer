package com.example.jplayer.utils;


import com.example.jplayer.adapter.ExpandablePlayerAdapter;
import com.example.jplayer.interfaces.MusicRetrieveListener;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MusicRetrieverTask  extends AsyncTask<Integer, Void, ExpandablePlayerAdapter>{

	public MusicRetriever musicRetriever;
	public Context context;
	ExpandablePlayerAdapter expndPlayerAdapter;
	public ProgressDialog prgDialog;
	public MusicRetrieveListener musicRetrievelistener;
	
	
	public MusicRetrieverTask(Context context, MusicRetrieveListener musicRetrievelistener) {
		// TODO Auto-generated constructor stub
		this.context=context;
		this.musicRetriever=new MusicRetriever(this.context);
		this.musicRetrievelistener=musicRetrievelistener;
	
	}
	
	@Override
	protected void onPreExecute() {
		// TODO Auto-generated method stub
		//Log.d("INFO", "show progressbar");
		
		prgDialog=new ProgressDialog(this.context);
		prgDialog.setTitle("JPlayer");
		prgDialog.setMessage("Loading Music..");
		prgDialog.setCancelable(false);
		prgDialog.show();
		
	}
	
	
	@Override
	protected ExpandablePlayerAdapter doInBackground(Integer... params) {
		// TODO Auto-generated method stub
		expndPlayerAdapter=musicRetriever.retrieveMusicAdapter(params[0]);
		//this.expndPlayerAdapter=musicRetriever.retrieveMusic(MediaStore.Audio.Media.ALBUM);
		return expndPlayerAdapter;
	}
	@Override
    protected void onPostExecute(ExpandablePlayerAdapter expndAdapter) {
		try
		{
			prgDialog.dismiss();
		}
		catch (Exception e) {
			// TODO: handle exception
			Log.e("ERROR", e.getMessage());
		}
		musicRetrievelistener.onMusicRetrieveComplete(expndAdapter);
       Toast.makeText(this.context, "Completed", Toast.LENGTH_LONG).show();
    }


}
