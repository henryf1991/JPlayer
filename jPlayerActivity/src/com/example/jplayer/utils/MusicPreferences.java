package com.example.jplayer.utils;

import com.example.jplayer.MaxPlayerActivity.PlayingMode;
import com.example.jplayer.fragments.PlayerFragment.PlaybackState;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class MusicPreferences {

	static final String CURRENT_GROUP_POS_KEY="group_pos_key";
	static final String CURRENT_NAV_POS_KEY="nav_pos_key";
	static final String PLAYBACKSTATE_KEY="playbackstate_key";
	static final String PLAYINGMODE_KEY="playingmode_key";
	static final String CURRENT_CHILD_POS_KEY="child_pos_key";
	static final String SHUFFLE_KEY="shuffle_key";
	
	public Context contxt;
	SharedPreferences prefs;
	public MusicPreferences(Context context) {
		// TODO Auto-generated constructor stub
		prefs=PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public int getGroupPos()
	{
		return prefs.getInt(CURRENT_GROUP_POS_KEY, 0);
	}
	public void setGroupPos(int groupPos)
	{
		SharedPreferences.Editor editor=prefs.edit();
		editor.putInt(CURRENT_GROUP_POS_KEY, groupPos);
		editor.commit();
	}
	public int getChildPos()
	{
		return prefs.getInt(CURRENT_CHILD_POS_KEY, 1);
	}
	public void setChildPos(int childPos)
	{
		SharedPreferences.Editor editor=prefs.edit();
		editor.putInt(CURRENT_CHILD_POS_KEY, childPos);
		editor.commit();
	}
	public int getNavPos()
	{
		return prefs.getInt(CURRENT_NAV_POS_KEY, -1);
	}
	public void setNavPos(int navPos)
	{
		SharedPreferences.Editor editor=prefs.edit();
		editor.putInt(CURRENT_NAV_POS_KEY, navPos);
		editor.commit();
	}
	public PlaybackState getPlayBackState() {
	    return PlaybackState.toPlayBackState(prefs.getString(PLAYBACKSTATE_KEY, PlaybackState.Stopped.toString()));
	}
	public void setPlayBackState(PlaybackState state) {
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PLAYBACKSTATE_KEY, state.toString());
	    editor.commit();
	}
	public PlayingMode getPlayingMode() {
	    return PlayingMode.toPlayingMode(prefs.getString(PLAYINGMODE_KEY, PlayingMode.RepeatOff.toString()));
	}
	public void setPlayPlayingMode(PlayingMode mode) {
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PLAYINGMODE_KEY, mode.toString());
	    editor.commit();
	}
	
	public boolean getShuffleMode()
	{
		return prefs.getBoolean(SHUFFLE_KEY, false);
	}
	
	public void setShuffleMode(boolean isShuffle)
	{
		SharedPreferences.Editor editor = prefs.edit();
	    editor.putBoolean(SHUFFLE_KEY, isShuffle);
	    editor.commit();
	}
}
