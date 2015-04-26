package com.example.jplayer.services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.GetChars;
import android.util.Log;
import android.widget.Toast;

import com.example.jplayer.jPlayerActivity;
import com.example.jplayer.MaxPlayerActivity.PlayingMode;
import com.example.jplayer.interfaces.MusicFocusable;
import com.example.jplayer.model.Song;
import com.example.jplayer.utils.AudioFocusHelper;
import com.example.jplayer.utils.MusicRetrieverTask;


public class MusicService extends Service implements OnCompletionListener, OnPreparedListener,OnErrorListener, MusicFocusable{

	final static String TAG = "MusicService";
	
	static final String SONG_KEY="song_key";
	static final String BROADCAST_KEY="broadcast_key";
	private static MusicService instance = null;
	
	public static final String ACTION_TOGGLE_PLAYBACK = "com.example.jplayer.action.TOGGLE_PLAYBACK";
    public static final String ACTION_PLAY = "com.example.jplayer.action.PLAY";
    public static final String ACTION_PAUSE = "com.example.jplayer.action.PAUSE";
    public static final String ACTION_STOP = "com.example.jplayer.action.STOP";
    public static final String ACTION_PLAYNEXT = "com.example.jplayer.action.PLAYNEXT";
    public static final String ACTION_PLAYPREVIUOS = "com.example.jplayer.action.PLAYPREVIUOS";
    public static final String ACTION_URL = "com.example.jplayer.action.URL";
    
    public static final float DUCK_VOLUME = 0.1f;
    
    public MediaPlayer mPlayer = null;
    
    AudioFocusHelper mAudioFocusHelper = null;
	
    enum State {
        Stopped,    
        Preparing,  
        Playing,                      
        Paused     
    };    
    State mState = State.Stopped;

    enum PauseReason {
        UserRequest,  
        FocusLoss,    
    };
    PauseReason mPauseReason = PauseReason.UserRequest;

    enum AudioFocus {
        NoFocusNoDuck,    
        NoFocusCanDuck,   
        Focused           
    }
    AudioFocus mAudioFocus = AudioFocus.NoFocusNoDuck;
    
    final int NOTIFICATION_ID = 1;
    Notification notification;
    
    AudioManager mAudioManager;
    
    MusicRetrieverTask musicretrieveTask;
    
    ArrayList<Song> currentSongGroup;
    
    Song CURRENT_SONG;
    
    public int current_song_pos=1;   
    static final String CURRENT_CHILD_POS_KEY="child_pos_key"; 
    static final String CURRENT_SONG_GROUP_KEY="current_song_group";
       
    public int LAST_PLAYED_SONG_POS=current_song_pos+1;  
    public int NEXY_PLAYED_SONG_POS=current_song_pos-1;
    
    public PlayingMode playingmode=PlayingMode.RepeatOff;
    public boolean isSuffleOn=false;
    public int shuffleIndex=1;
    public ArrayList<Song> shuffleGroup;
    
    public static boolean isInstanceCreated() { 
        return instance != null; 
     }
    
    public class LocalBinder extends Binder {
        public MusicService getService() {
            return MusicService.this;
        }
    }

    public static MusicService getInstance()   
    {
    	return instance;
    }
	
   public void createMediaPlayerIfNeeded() {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            mPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
            mPlayer.setOnPreparedListener(this);
            mPlayer.setOnCompletionListener(this);
            mPlayer.setOnErrorListener(this);
        }
        else
            mPlayer.reset();
    }
    
    @Override
    public void onCreate() {
    	// TODO Auto-generated method stub
    	Log.d(TAG, "Service created");
    	instance = this;
    	 mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE); 
         if (android.os.Build.VERSION.SDK_INT >= 8)
             mAudioFocusHelper = new AudioFocusHelper(getApplicationContext(), this);
         else
             mAudioFocus = AudioFocus.Focused; // no focus feature, so we always "have" audio focus 
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
    	// TODO Auto-generated method stub
    	String action = intent.getAction();
        if (action.equals(ACTION_TOGGLE_PLAYBACK)) processTogglePlaybackRequest();
        else if (action.equals(ACTION_PLAY)) processPlayRequest();
        else if (action.equals(ACTION_PAUSE)) processPauseRequest();
        else if (action.equals(ACTION_PLAYPREVIUOS)) processPlayPreviuosRequest();
        else if (action.equals(ACTION_STOP)) processStopRequest();
        else if (action.equals(ACTION_PLAYNEXT)) processPlayNextRequest();
        else if (action.equals(ACTION_URL)) processAddRequest(intent);
        
        return START_NOT_STICKY; // Means we started the service, but don't want it to
                                 // restart in case it's killed.
    }
    
    public void processTogglePlaybackRequest() {
        if (mState == State.Paused || mState == State.Stopped) {
            processPlayRequest();
        } else {
            processPauseRequest();
        }
    }
    
    public void processPlayRequest() {       
    	tryToGetAudioFocus();
        if (mState == State.Stopped) {
            playNextSong(null);
        }
        else if (mState == State.Paused) {
            mState = State.Playing;
            configAndStartMediaPlayer();
        }

    }
    
    public void processPauseRequest() {
        if (mState == State.Playing) {
            mState = State.Paused;
            mPlayer.pause();
            relaxResources(false); // while paused, we always retain the MediaPlayer
            // do not give up audio focus
        }

    }

    public void processPlayPreviuosRequest() {
        if (mState == State.Playing || mState == State.Paused)
        	if(isSuffleOn)
        	{
        		shuffleIndex=shuffleIndex==1?shuffleGroup.size()-1:(shuffleIndex-1);
	        	tryToGetAudioFocus();
	        	playNextSong(shuffleGroup.get(shuffleIndex));
	        	sendMessage(shuffleGroup.get(shuffleIndex));
        	}
        	else
        	{
	        	current_song_pos=current_song_pos==1?currentSongGroup.size()-1:(current_song_pos-1);
	        	tryToGetAudioFocus();
	        	playNextSong(currentSongGroup.get(current_song_pos));
	        	sendMessage(currentSongGroup.get(current_song_pos));
        	}
    }

    public void processPlayNextRequest() {
        if (mState == State.Playing || mState == State.Paused) {
        	if(isSuffleOn)
        	{
        		shuffleIndex=shuffleIndex==shuffleGroup.size()-1?1:(shuffleIndex+1);
                tryToGetAudioFocus();
                playNextSong(shuffleGroup.get(shuffleIndex));
                sendMessage(shuffleGroup.get(shuffleIndex));
        	}
        	else
        	{
        		current_song_pos=current_song_pos==currentSongGroup.size()-1?1:(current_song_pos+1);
                tryToGetAudioFocus();
                playNextSong(currentSongGroup.get(current_song_pos));
                sendMessage(currentSongGroup.get(current_song_pos));
        	}
        	
        }
    }
    
    public void processStopRequest() {
        processStopRequest(false);
    }

    public void processStopRequest(boolean force) {
        if (mState == State.Playing || mState == State.Paused || force) {
            mState = State.Stopped;

            // let go of all resources...
            relaxResources(true);
            giveUpAudioFocus();

            // service is no longer necessary. Will be started again if needed.
            stopSelf();
        }
    }

    public void processAddRequest(Intent intent) {
    	
    	currentSongGroup=intent.getParcelableArrayListExtra(CURRENT_SONG_GROUP_KEY);
    	current_song_pos=intent.getIntExtra(CURRENT_CHILD_POS_KEY, 1);
        if (mState == State.Playing || mState == State.Paused || mState == State.Stopped) {
            tryToGetAudioFocus();
            playNextSong(currentSongGroup.get(current_song_pos));
        }
        
    }
     
    public int getChildPos()
    {
    	return current_song_pos;
    }
    
    public void configAndStartMediaPlayer() {
        if (mAudioFocus == AudioFocus.NoFocusNoDuck) {
            if (mPlayer.isPlaying()) mPlayer.pause();
            return;
        }
        else if (mAudioFocus == AudioFocus.NoFocusCanDuck)
            mPlayer.setVolume(DUCK_VOLUME, DUCK_VOLUME);  // we'll be relatively quiet
        else
            mPlayer.setVolume(1.0f, 1.0f); // we can be loud

        if (!mPlayer.isPlaying()) mPlayer.start();
    }
    
   public void tryToGetAudioFocus() {
        if (mAudioFocus != AudioFocus.Focused && mAudioFocusHelper != null
                        && mAudioFocusHelper.requestFocus())
            mAudioFocus = AudioFocus.Focused;
    }
   
   public void sendMessage(Song currSong)
   {
	   try {
		   Intent msgIntent=new Intent(BROADCAST_KEY);
		   msgIntent.putExtra(SONG_KEY, currSong);
		   LocalBroadcastManager.getInstance(this).sendBroadcast(msgIntent);
	} catch (Exception e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
   }
   
   public void playNextSong(Song songToPlay) {
	   mState = State.Stopped;
       relaxResources(false); // release everything except MediaPlayer
       try {
           if (songToPlay != null) {
               createMediaPlayerIfNeeded();
               mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
               mPlayer.setDataSource(songToPlay.getSongUri());
               setUpAsForeground(songToPlay.getTitle());
               mState = State.Preparing;          
               mPlayer.prepareAsync();
           }         
           
       }
       catch (IOException ex) {
           Log.e(TAG, "IOException playing next song: " + ex.getMessage());
           ex.printStackTrace();
       }
   }
   
   void setUpAsForeground(String text) {
       PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0,
               new Intent(getApplicationContext(), jPlayerActivity.class),
               PendingIntent.FLAG_UPDATE_CURRENT);
       notification = new Notification();
       notification.tickerText = text;
       notification.icon = android.R.drawable.ic_media_rew;
       notification.flags |= Notification.FLAG_ONGOING_EVENT;
       notification.setLatestEventInfo(getApplicationContext(), "JPlayer",
               text, pi);
       startForeground(NOTIFICATION_ID, notification);
   }
    
   public void relaxResources(boolean releaseMediaPlayer) {
       // stop being a foreground service
       stopForeground(true);

       // stop and release the Media Player, if it's available
       if (releaseMediaPlayer && mPlayer != null) {
           mPlayer.reset();
           mPlayer.release();
           mPlayer = null;
       }

   }

   public void giveUpAudioFocus() {
       if (mAudioFocus == AudioFocus.Focused && mAudioFocusHelper != null
                               && mAudioFocusHelper.abandonFocus())
           mAudioFocus = AudioFocus.NoFocusNoDuck;
   }

	@Override
	public void onGainedAudioFocus() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "gained audio focus.", Toast.LENGTH_SHORT).show();
        mAudioFocus = AudioFocus.Focused;

        // restart media player with new focus settings
        if (mState == State.Playing)
            configAndStartMediaPlayer();
	}

	@Override
	public void onLostAudioFocus(boolean canDuck) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "lost audio focus." + (canDuck ? "can duck" :
		            "no duck"), Toast.LENGTH_SHORT).show();
		        mAudioFocus = canDuck ? AudioFocus.NoFocusCanDuck : AudioFocus.NoFocusNoDuck;

		        // start/restart/pause media player with new focus settings
		        if (mPlayer != null && mPlayer.isPlaying())
		            configAndStartMediaPlayer();
	}

	@Override
	public boolean onError(MediaPlayer arg0, int what, int extra) {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(), "Media player error! Resetting.",
	            Toast.LENGTH_SHORT).show();
	        Log.e(TAG, "Error: what=" + String.valueOf(what) + ", extra=" + String.valueOf(extra));

	        mState = State.Stopped;
	        relaxResources(true);
	        giveUpAudioFocus();
	        return true; // true indicates we handled the error
	}

	@Override
	public void onPrepared(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		mState = State.Playing;
        configAndStartMediaPlayer();
	}

	@Override
	public void onCompletion(MediaPlayer arg0) {
		// TODO Auto-generated method stub
		if(isSuffleOn)
		{
			if(shuffleGroup==null)
				forceshuffle();
			playNextSong(shuffleGroup.get(shuffleIndex));
			sendMessage(shuffleGroup.get(shuffleIndex));
			shuffleIndex++;
		}
		else
		{
			switch (getPlayingmode()) {
			case RepeatAll:
				current_song_pos=current_song_pos==currentSongGroup.size()-1?1:(current_song_pos+1);
				if (current_song_pos==1)
					relaxResources(false);
				else
				{
					playNextSong(currentSongGroup.get(current_song_pos));
					sendMessage(currentSongGroup.get(current_song_pos));
				}
				break;
			case RepeatOff:
				relaxResources(false);
				break;
			case RepeatCurrent:
				playNextSong(currentSongGroup.get(current_song_pos));
				sendMessage(currentSongGroup.get(current_song_pos));
				break;
			default:
				playNextSong(null);
				break;
			}
		}
		
	}
	

	
	public PlayingMode getPlayingmode() {
		return playingmode;
	}

	public void setPlayingmode(PlayingMode playingmode) {
		this.playingmode = playingmode;
	}

	public void toggleShufflemode(boolean shufflemode) {
		this.isSuffleOn = shufflemode;
		if(!isSuffleOn)
		{
			shuffleGroup=null;
			shuffleIndex=1;
		}
	}

	public void forceshuffle()
	{
		shuffleGroup=new ArrayList<Song>();
		shuffleGroup=currentSongGroup;
		Collections.shuffle(shuffleGroup);
	}
	
	public int getRandom(int min,int max)
	{
		return min + (int)(Math.random() * ((max - min) + 1));
	}
	
	public int getPlayerPosition()
	{
		return mPlayer.getCurrentPosition();
	}
	
	public void updateProgress(int position)
	{
		if(mState==State.Playing || mState==State.Paused)
		mPlayer.seekTo(position);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		instance=null;
		mState = State.Stopped;
        relaxResources(true);
        giveUpAudioFocus();
	}

	@Override
	public IBinder onBind(Intent in) {
		// TODO Auto-generated method stub
		return null;
	}

}
