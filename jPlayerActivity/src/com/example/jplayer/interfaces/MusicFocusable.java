package com.example.jplayer.interfaces;

public interface MusicFocusable {
    public void onGainedAudioFocus();
    public void onLostAudioFocus(boolean canDuck);
}
