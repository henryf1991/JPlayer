package com.example.jplayer.fragments;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.example.jplayer.MaxPlayerActivity;
import com.example.jplayer.MaxPlayerActivity.PlayingMode;
import com.example.jplayer.R;
import com.example.jplayer.adapter.ExpandablePlayerAdapter;
import com.example.jplayer.interfaces.MusicRetrieveListener;
import com.example.jplayer.model.Song;
import com.example.jplayer.model.SongHeader;
import com.example.jplayer.services.MusicService;
import com.example.jplayer.utils.MusicPreferences;
import com.example.jplayer.utils.MusicRetrieverTask;
import com.example.jplayer.utils.MusicUtil;
import com.example.jplayer.utils.PlaylistHelper;


public class PlayerFragment extends SherlockFragment implements MusicRetrieveListener{
	
	public static final String TAG="PlayerFragment";
	public static Context context;
	static final String SONG_KEY="song_key";
	static final String CURRENT_GROUP_POS_KEY="group_pos_key";
	static final String CURRENT_NAV_POS_KEY="nav_pos_key";
	static final String CURRENT_CHILD_POS_KEY="child_pos_key";
	static final String CURRENT_SONG_GROUP_KEY="current_song_group";
	static final String CURRENT_PLAYBACKSTATE_KEY="current_playbackstate_key";
	static final String CURRENT_PLAYINGMODE_KEY="current_playingmode_key";
	static final String BROADCAST_KEY="broadcast_key";
	static final String CURRENT_SONG_KEY="current_song_key";
    
    public int current_group_pos=0;
    public int current_child_pos=1;
    public static Song current_song;
      
    public enum PlaybackState{Paused,Playing,Stopped;
    						public static PlaybackState toPlayBackState (String myEnumString) {
					        try {
					            return valueOf(myEnumString);
					        } catch (Exception ex) {
					        	Log.d("ERROR", ex.getMessage());
					            return Stopped;
					        }
    					}};
    public PlaybackState playbackstate=PlaybackState.Stopped;
	
    public ExpandableListView expndplayerlist;
	public ExpandablePlayerAdapter expndPlayerAdapter;
	public ArrayList<Song> currentSongGroup;
	public ImageView miniPlayerPlayPause,miniPlayerPrevious,miniPlayerNext;
	static ImageView miniPlayerAlbumArt;
	public static TextView miniPlayerSongName;
	public RelativeLayout miniPlayer;
	public int nav_pos=3;
	public boolean isStartNew=false;;
	public MusicPreferences prefs;
	public MessageReciever reciever;
	public MusicService musicservice;
	public boolean isShuffleOn=false;
	
	public static PlayerFragment newInstance()
	{
		PlayerFragment fragment=new PlayerFragment();
	    return fragment;
	}
	
	public PlayerFragment() {
		// TODO Auto-generated constructor stub
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.player_main, container, false);   
     
        context=getActivity();
        isStartNew=true;
        prefs=new MusicPreferences(getActivity());
		nav_pos=prefs.getNavPos();
		nav_pos=nav_pos==-1?3:nav_pos;	
		current_group_pos=prefs.getGroupPos();
		current_group_pos=current_group_pos==-1?0:current_group_pos;
        
        // Setting the ListView
		int width=MusicUtil.getWidth(getActivity());
        expndplayerlist = (ExpandableListView) rootView.findViewById(R.id.expndPlaylist);
        expndplayerlist.setIndicatorBounds(width - MusicUtil.GetPixelFromDips(getActivity(),50), width - MusicUtil.GetPixelFromDips(getActivity(),10));
        expndplayerlist.setOnChildClickListener(childClickListener);
        expndplayerlist.setOnItemLongClickListener(childLongClickListener);
        prepareListData(nav_pos);    
        
        //miniplayer
        miniPlayer=(RelativeLayout)rootView.findViewById(R.id.miniPlayer);
        miniPlayerAlbumArt=(ImageView)rootView.findViewById(R.id.miniPlayerCover);
        miniPlayerAlbumArt.setOnClickListener(miniPlayerCoverListener);
        miniPlayerSongName=(TextView)rootView.findViewById(R.id.miniPlayerSong);
        miniPlayerPlayPause=(ImageView)rootView.findViewById(R.id.miniPlayerPlayPause);
        miniPlayerPlayPause.setOnClickListener(playPauseListener);
        miniPlayerPrevious=(ImageView)rootView.findViewById(R.id.miniPlayerPrevious);
        miniPlayerNext=(ImageView)rootView.findViewById(R.id.miniPlayerNext);
        miniPlayerPrevious.setOnClickListener(previousNextListener);
        miniPlayerNext.setOnClickListener(previousNextListener);
        
        if(getActivity().getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE) adjustMiniPlayer(3.5f, 1.5f);
        reciever=new MessageReciever();
        
        return rootView;
    
	}
	
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(MusicService.isInstanceCreated())musicservice=MusicService.getInstance();
		if(!isStartNew && currentSongGroup!=null)
		{
			setMiniPlayer(currentSongGroup.get(musicservice.getChildPos()));
			playbackstate=prefs.getPlayBackState();
			if(playbackstate!=PlaybackState.Stopped)miniPlayerPlayPause.setImageResource(playbackstate==PlaybackState.Paused?android.R.drawable.ic_media_play:android.R.drawable.ic_media_pause);
		}
	    LocalBroadcastManager.getInstance(getActivity()).registerReceiver(reciever, new IntentFilter(BROADCAST_KEY));
	    isShuffleOn=prefs.getShuffleMode();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(reciever);
		prefs.setShuffleMode(isShuffleOn);
		prefs.setGroupPos(current_group_pos);
		prefs.setPlayBackState(playbackstate);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);	   	    
	    int width=MusicUtil.getWidth(getActivity());
        expndplayerlist.setIndicatorBounds(width - MusicUtil.GetPixelFromDips(getActivity(),50), width - MusicUtil.GetPixelFromDips(getActivity(),10));       
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	adjustMiniPlayer(3.5f, 1.5f);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
        	adjustMiniPlayer(4, 1);
        }
	}
	
	public void adjustMiniPlayer(float weightList,float weightMiniPlayer)
	{
		LinearLayout.LayoutParams lpList=(LinearLayout.LayoutParams)expndplayerlist.getLayoutParams();
	    lpList.weight=weightList;
	    expndplayerlist.setLayoutParams(lpList);
	    LinearLayout.LayoutParams lpminiplayer=(LinearLayout.LayoutParams)miniPlayer.getLayoutParams();
	    lpminiplayer.weight=weightMiniPlayer;
	    miniPlayer.setLayoutParams(lpminiplayer);
	}
	
	private void prepareListData(int navItem)
	{
		new MusicRetrieverTask(getActivity(), PlayerFragment.this).execute(navItem);		
	}	
	 
	@Override
	public void onMusicRetrieveComplete(
			ExpandablePlayerAdapter expndPlayerAdapter) {
		// TODO Auto-generated method stub
		this.expndPlayerAdapter=expndPlayerAdapter;
		this.expndplayerlist.setAdapter(expndPlayerAdapter);
		if(MusicService.isInstanceCreated() && isStartNew)
        {
			playbackstate=prefs.getPlayBackState();
        	currentSongGroup=expndPlayerAdapter.getAllChildFromGroup((SongHeader)expndPlayerAdapter.getGroup(current_group_pos));
        	//getActivity().startService(new Intent(MusicService.ACTION_SENDCHILD));
        	setMiniPlayer(currentSongGroup.get(musicservice.getChildPos()));
        	if(playbackstate!=PlaybackState.Stopped)miniPlayerPlayPause.setImageResource(playbackstate==PlaybackState.Paused?android.R.drawable.ic_media_play:android.R.drawable.ic_media_pause);
        }
		isStartNew=false;
	}
	
	ExpandableListView.OnChildClickListener childClickListener=new OnChildClickListener() {
		
		@Override
		public boolean onChildClick(ExpandableListView arg0, View v, int groupPosition,
				int childPosition, long id) {
			// TODO Auto-generated method stub
			if(childPosition==0)
			{
				setPlayingMode(groupPosition);
				return true;
			}
			setPlayer(groupPosition, childPosition);
			return true;
		}
	};
	
	OnItemLongClickListener childLongClickListener=new OnItemLongClickListener() {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			// TODO Auto-generated method stub
			if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	            int groupPosition = ExpandableListView.getPackedPositionGroup(id);
	            int childPosition = ExpandableListView.getPackedPositionChild(id);
	            if(childPosition==0){return false;}
	            showDialog(groupPosition, childPosition);
	            return true;
	        }
			return false;
		}
	};
	
	OnClickListener playPauseListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!(playbackstate==PlaybackState.Stopped))
			{			
				change_playbacktoggle();				
				getActivity().startService(new Intent(MusicService.ACTION_TOGGLE_PLAYBACK));
			}				
		}
	};
	
	OnClickListener previousNextListener=new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(!(playbackstate==PlaybackState.Stopped))
			{
				switch (v.getId()) {
				case R.id.miniPlayerPrevious:
					current_child_pos=current_child_pos==1?currentSongGroup.size()-1:(current_child_pos-1);
					if(playbackstate==PlaybackState.Paused) change_playbacktoggle();
					getActivity().startService(new Intent(MusicService.ACTION_PLAYPREVIUOS));
					break;
				case R.id.miniPlayerNext:
					current_child_pos=current_child_pos==currentSongGroup.size()-1?1:(current_child_pos+1);
					if(playbackstate==PlaybackState.Paused) change_playbacktoggle();
					getActivity().startService(new Intent(MusicService.ACTION_PLAYNEXT));
					break;
				default:
					break;
				}
			}
		}
	};
	
	OnClickListener miniPlayerCoverListener=new OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			if(playbackstate!=PlaybackState.Stopped)
			{
				Intent in=new Intent(getActivity(),MaxPlayerActivity.class);
				in.putExtra(CURRENT_SONG_KEY, current_song);
				in.putExtra(CURRENT_SONG_GROUP_KEY, currentSongGroup);
				in.putExtra(CURRENT_PLAYBACKSTATE_KEY, playbackstate.toString());
				startActivity(in);
			}
		}
	};
	
	public void setPlayer(int groupPosition,int childPosition)
	{
		if(playbackstate==PlaybackState.Paused)change_playbacktoggle();
		current_group_pos=groupPosition;
		current_child_pos=childPosition;
		playbackstate=PlaybackState.Playing;
		currentSongGroup=expndPlayerAdapter.getAllChildFromGroup((SongHeader)expndPlayerAdapter.getGroup(groupPosition));
		setMiniPlayer(currentSongGroup.get(childPosition));
		playSong(childPosition);
	}
	
	public void change_playbacktoggle()
	{
		playbackstate=playbackstate==PlaybackState.Paused?PlaybackState.Playing:PlaybackState.Paused;
		miniPlayerPlayPause.setImageResource(playbackstate==PlaybackState.Paused?android.R.drawable.ic_media_play:android.R.drawable.ic_media_pause);
	}
	
	public void setPlayingMode(int groupPosition)
	{
		if (current_group_pos==groupPosition) {
			if(!isShuffleOn)
			{
				isShuffleOn=true;
				Toast.makeText(getActivity(), "Shuffle started", Toast.LENGTH_LONG).show();
				musicservice.toggleShufflemode(isShuffleOn);
			}
			else
			{
				isShuffleOn=false;
				Toast.makeText(getActivity(), "Shuffle stopped", Toast.LENGTH_LONG).show();
				musicservice.toggleShufflemode(isShuffleOn);
			}
		}
		else
		{
			Toast.makeText(getActivity(), "No song playing from this group", Toast.LENGTH_LONG).show();
		}
	}
	
	public void playSong(int childPosition)
	{
		Intent serviceIntent=new Intent(MusicService.ACTION_URL);
		serviceIntent.putExtra(CURRENT_CHILD_POS_KEY, childPosition);
		serviceIntent.putParcelableArrayListExtra(CURRENT_SONG_GROUP_KEY, currentSongGroup);
		this.getActivity().startService(serviceIntent);
	}
	
	public void showDialog(final int groupPos,final int childPos)
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
		final Song song=expndPlayerAdapter.getAllChildFromGroup((SongHeader)expndPlayerAdapter.getGroup(groupPos)).get(childPos);
		builder.setTitle(song.getTitle()); 
		builder.setItems(R.array.dialog_items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {	
				switch (which) {
				case 0:
					setPlayer(groupPos, childPos);
					break;
				case 1:
					shareSong(groupPos, childPos);
					break;
				case 2:
					PlaylistHelper.showAddToPlaylistDialog(getActivity(), song);
				default:
					break;
				}
			}
		});
		builder.show();
	}

	public static void setMiniPlayer(Song song)
	{
		current_song=song;
		miniPlayerAlbumArt.setImageBitmap(MusicUtil.getArtworkQuick(context, song.getAlbumID(), 40, 40));
		miniPlayerSongName.setText(song.getTitle());
	}
	
	
	public void shareSong(int groupPos,int childPos)
	{
	    Intent share = new Intent(Intent.ACTION_SEND);
	    share.setType("audio/*");
	    String songToShare=expndPlayerAdapter.getAllChildFromGroup((SongHeader)expndPlayerAdapter.getGroup(groupPos)).get(childPos).getSongUri();
	    Log.d(TAG, songToShare);
	    Uri UritoShare=Uri.parse(songToShare);
	    share.putExtra(Intent.EXTRA_STREAM, UritoShare);
	    startActivity(Intent.createChooser(share, "Share Music"));
	}
	
	private class MessageReciever extends BroadcastReceiver
	{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Toast.makeText(context, "got messag in fragment", Toast.LENGTH_LONG).show();
			setMiniPlayer( (Song)intent.getExtras().getParcelable(SONG_KEY));
		}
		
	}
	
}
