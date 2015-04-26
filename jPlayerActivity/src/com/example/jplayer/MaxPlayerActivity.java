package com.example.jplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jplayer.fragments.PlayerFragment.PlaybackState;
import com.example.jplayer.model.Song;
import com.example.jplayer.services.MusicService;
import com.example.jplayer.utils.MusicPreferences;
import com.example.jplayer.utils.MusicUtil;

public class MaxPlayerActivity extends Activity {

	static final String TAG="MaxPlayerActivity";
	
	static final String CURRENT_SONG_GROUP_KEY="current_song_group";
	static final String CURRENT_PLAYBACKSTATE_KEY="current_playbackstate_key";
	static final String CURRENT_PLAYINGMODE_KEY="current_playingmode_key";
	static final String SONG_KEY="song_key";
	static final String BROADCAST_KEY="broadcast_key";
	static final String CURRENT_SONG_KEY="current_song_key";
	
	public PlaybackState playbackstate=PlaybackState.Stopped;	
	public static ImageView cover;
	public static SeekBar seekbar;
	public ImageView playpause,previous,next,playingmodeBtn,shuffleBtn;
	public static TextView songName,currentTime,totalTime,songArtist;
	public Song current_song;
	public MessageReciever reciever;
	public static Context context;
	public ArrayList<Song> currentSongGroup;
	public MusicPreferences prefs;
	public MusicService musicService;
	public static Handler handler=new Handler();
	public Runnable seekbarrunnable;
	public int seekToPostion=0;
	public ImageView maxPlayerControlBtn,maxPlayerBackBtn;
	public AudioManager audioManager;
	public enum PlayingMode {RepeatAll,RepeatCurrent,RepeatOff;
	public static PlayingMode toPlayingMode (String myEnumString) {
        try {
            return valueOf(myEnumString);
        } catch (Exception ex) {
        	Log.d("ERROR", ex.getMessage());
            return RepeatOff;
        }
	}};
	public PlayingMode playingmode = PlayingMode.RepeatOff;
	public boolean isShuffleOn=false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) setContentView(R.layout.maxplayer_layout_landscape);
			else setContentView(R.layout.maxplayer_layout);
		
		musicService=MusicService.getInstance();		
		context=MaxPlayerActivity.this;
		prefs=new MusicPreferences(MaxPlayerActivity.this);
		current_song=getIntent().getExtras().getParcelable(CURRENT_SONG_KEY);
		currentSongGroup=getIntent().getParcelableArrayListExtra(CURRENT_SONG_GROUP_KEY);
		reciever=new MessageReciever();
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		
		cover=(ImageView)findViewById(R.id.maxPlayerCover);
		songName=(TextView)findViewById(R.id.maxPlayerSong);
		songArtist=(TextView)findViewById(R.id.maxPlayerSongArtist);
		totalTime=(TextView)findViewById(R.id.maxPlayerTotalTime);
		playpause=(ImageView)findViewById(R.id.maxPlayerPlayPause);
		playpause.setOnClickListener(playPauseListener);
		previous=(ImageView)findViewById(R.id.maxPlayerPrevious);
		next=(ImageView)findViewById(R.id.maxPlayerNext);
		previous.setOnClickListener(previousNextListener);
		next.setOnClickListener(previousNextListener);
		seekbar=(SeekBar)findViewById(R.id.maxPlayerSeekbar);
		seekbar.setOnSeekBarChangeListener(seekbarchangelistener);
		currentTime=(TextView)findViewById(R.id.maxPlayerCurrentTime);
		playingmodeBtn=(ImageView)findViewById(R.id.maxPlayerPlayingMode);
		playingmodeBtn.setOnClickListener(playingModeListener);
		shuffleBtn=(ImageView)findViewById(R.id.maxPlayerShuffle);
		shuffleBtn.setOnClickListener(playingModeListener);
		maxPlayerControlBtn=(ImageView)findViewById(R.id.maxPlayerVolumneControl);
		maxPlayerControlBtn.setOnClickListener(volumeControlListener);
		maxPlayerBackBtn=(ImageView)findViewById(R.id.maxPlayerBackButton);
		maxPlayerBackBtn.setOnClickListener(backPressListener);
		
		
	} 
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		LocalBroadcastManager.getInstance(this).registerReceiver(reciever, new IntentFilter(BROADCAST_KEY));
		if(MusicService.isInstanceCreated())
			setMaxPlayer(currentSongGroup.get(musicService.getChildPos()));
		updateSeekbar(musicService.getPlayerPosition());
		playbackstate=prefs.getPlayBackState();
		if(playbackstate==PlaybackState.Paused)playpause.setImageResource(android.R.drawable.ic_media_play);
		if(playbackstate==PlaybackState.Playing)
		{
			startSeeKbarRunnable();
		}
		isShuffleOn=prefs.getShuffleMode();
		initShuffle(isShuffleOn);
		playingmode=prefs.getPlayingMode();
		initPlayingMode(playingmode);
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();		
		handler.removeCallbacks(seekbarrunnable);
		prefs.setPlayBackState(playbackstate);
		prefs.setPlayPlayingMode(playingmode);
		prefs.setShuffleMode(isShuffleOn);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(reciever);
	}
	
	
	OnClickListener playPauseListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!(playbackstate==PlaybackState.Stopped))
			{			
				change_playbacktoggle();				
				startService(new Intent(MusicService.ACTION_TOGGLE_PLAYBACK));
			}				
		}
	};
	
	OnClickListener previousNextListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.maxPlayerPrevious:
				if(playbackstate==PlaybackState.Paused) change_playbacktoggle();
				startService(new Intent(MusicService.ACTION_PLAYPREVIUOS));
				break;
			case R.id.maxPlayerNext:
				if(playbackstate==PlaybackState.Paused) change_playbacktoggle();
				startService(new Intent(MusicService.ACTION_PLAYNEXT));
				break;
			default:
				break;
			}
		}
	};
	
	SeekBar.OnSeekBarChangeListener seekbarchangelistener=new OnSeekBarChangeListener() {
		
		@Override
		public void onStopTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			musicService.updateProgress(seekToPostion);
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar arg0) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void onProgressChanged(SeekBar arg0, int position, boolean arg2) {
			// TODO Auto-generated method stub
			seekToPostion=position;
		}
		
	};
	
	OnClickListener playingModeListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v.getId()==R.id.maxPlayerPlayingMode)
			{
				switch (playingmode) {
				case RepeatOff:
					playingmode=PlayingMode.RepeatCurrent;
					isShuffleOn=false;
					initShuffle(isShuffleOn);
					Toast.makeText(MaxPlayerActivity.this, "Repeating Currrent Song", Toast.LENGTH_SHORT).show();
					initPlayingMode(playingmode);
					break;
				case RepeatCurrent:
					playingmode=PlayingMode.RepeatAll;
					isShuffleOn=false;
					initShuffle(isShuffleOn);
					Toast.makeText(MaxPlayerActivity.this, "Repeating All Songs", Toast.LENGTH_SHORT).show();
					initPlayingMode(playingmode);
					break;
				case RepeatAll:
					playingmode=PlayingMode.RepeatOff;
					Toast.makeText(MaxPlayerActivity.this, "Repeat is off", Toast.LENGTH_SHORT).show();
					initPlayingMode(playingmode);
					break;
				default:
					break;
				}
				isShuffleOn=false;
				initShuffle(isShuffleOn);
				
			}
			else if(v.getId()==R.id.maxPlayerShuffle)
			{
				if(!isShuffleOn)
				{
					isShuffleOn=true;
					Toast.makeText(MaxPlayerActivity.this, "Shuffle is on", Toast.LENGTH_SHORT).show();
					initShuffle(isShuffleOn);					
				}
				else
				{
					isShuffleOn=false;
					Toast.makeText(MaxPlayerActivity.this, "Shuffle is off", Toast.LENGTH_SHORT).show();
					initShuffle(isShuffleOn);
				}
			}
		}
	};
	
	OnClickListener volumeControlListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
		}
	};
	OnClickListener backPressListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			finish();
		}
	};
	
	public void initPlayingMode(PlayingMode mode)
	{
		switch (mode) {
		case RepeatOff:
			musicService.setPlayingmode(playingmode);
			playingmodeBtn.setImageResource(R.drawable.repeat_inactive);
			break;
		case RepeatCurrent:
			musicService.setPlayingmode(playingmode);
			playingmodeBtn.setImageResource(R.drawable.repeat_current_active);
			break;
		case RepeatAll:
			musicService.setPlayingmode(playingmode);
			playingmodeBtn.setImageResource(R.drawable.repeat_active);
			break;
		default:
			break;
		}
	}
	
	public void initShuffle(boolean isShuf)
	{
		musicService.toggleShufflemode(isShuffleOn);
		if(isShuf) shuffleBtn.setImageResource(R.drawable.shuffle_active);
		else shuffleBtn.setImageResource(R.drawable.shuffle_inactive);
	}
	
	public static void setMaxPlayer(Song song)
	{
		songName.setText(song.getTitle());
		songArtist.setText(song.getArtist());
		cover.setImageBitmap(MusicUtil.getAlbumArt(context, song.getAlbumID(), cover.getWidth(), cover.getHeight()));
		totalTime.setText(MusicUtil.milliSecondsToTimer(song.getDuration()));
		seekbar.setMax(Integer.parseInt(song.getDuration()));
	}
	
	public static void updateSeekbar(int progress)
	{
		seekbar.setProgress(progress); 
		currentTime.setText(MusicUtil.milliSecondsToTimer(String.valueOf(progress)));
	}
	
	public void startSeeKbarRunnable()
	{
		
		seekbarrunnable=new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub.
				updateSeekbar(musicService.getPlayerPosition());
				handler.postDelayed(this, 1000);
			}
		};
		seekbarrunnable.run();
	}
	
	public void change_playbacktoggle()
	{
		playbackstate=playbackstate==PlaybackState.Paused?PlaybackState.Playing:PlaybackState.Paused;
		if(playbackstate==PlaybackState.Paused)
			handler.removeCallbacks(seekbarrunnable);
		else
			startSeeKbarRunnable();			
		playpause.setImageResource(playbackstate==PlaybackState.Paused?android.R.drawable.ic_media_play:android.R.drawable.ic_media_pause);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_max_player, menu);
		return true;
	}
	private class MessageReciever extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			setMaxPlayer( (Song)intent.getExtras().getParcelable(SONG_KEY));
		}
		
	}

}
