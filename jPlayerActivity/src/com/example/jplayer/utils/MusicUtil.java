package com.example.jplayer.utils;

import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.util.Log;

import com.example.jplayer.R;
import com.example.jplayer.imageloaderhelper.MemoryCache;

public class MusicUtil {
	
	static final String TAG="MusicUtil";
	
	private static final BitmapFactory.Options sBitmapOptionsCache = new BitmapFactory.Options();
	private static final int BETTER_QUALITY_ALBUMART=-2;
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static MemoryCache cache=new MemoryCache();

    static {
        sBitmapOptionsCache.inPreferredConfig = Bitmap.Config.RGB_565;
        sBitmapOptionsCache.inDither = false;
    }
    
	public static String milliSecondsToTimer(String millisecString) {
		long milliseconds = Long.parseLong(millisecString);
		String finalTimerString = "";
		String secondsString = "";

		// Convert total duration into time
		int hours = (int) (milliseconds / (1000 * 60 * 60));
		int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
		int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
		// Add hours if there
		if (hours > 0) {
			finalTimerString = hours + ":";
		}

		// Prepending 0 to seconds if it is one digit
		if (seconds < 10) {
			secondsString = "0" + seconds;
		} else {
			secondsString = "" + seconds;
		}

		finalTimerString = finalTimerString + minutes + ":" + secondsString;

		// return timer string
		return finalTimerString;
	}
	
	public static  Bitmap getArtworkQuick(Context context, long album_id, int w, int h) {
        //w -= 1;
    	
    	Bitmap bitmap=cache.get(album_id);
    	if(bitmap!=null)
    	{
    		return bitmap;
    	}
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        ParcelFileDescriptor fd = null;
        try {
                fd = res.openFileDescriptor(uri, "r");
                int sampleSize = 1;
                sBitmapOptionsCache.inJustDecodeBounds = true;
                BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, sBitmapOptionsCache);
                int nextWidth = sBitmapOptionsCache.outWidth >> 1;
                int nextHeight = sBitmapOptionsCache.outHeight >> 1;
                while (nextWidth>w && nextHeight>h) {
                    sampleSize <<= 1;
                    nextWidth >>= 1;
                    nextHeight >>= 1;
                }

                sBitmapOptionsCache.inSampleSize = sampleSize;
                sBitmapOptionsCache.inJustDecodeBounds = false;
                Bitmap b = BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, sBitmapOptionsCache);

                if (b != null) {
                    if (sBitmapOptionsCache.outWidth != w || sBitmapOptionsCache.outHeight != h) {
                        Bitmap tmp = Bitmap.createScaledBitmap(b, w, h, true);
                        if (tmp != b) b.recycle();
                        b = tmp;
                    }
                }
                cache.put(album_id, b);
                return b;
            } catch (FileNotFoundException e) {
            	Log.d("ERROR", e.getMessage());
            	Bitmap default_bmp=BitmapFactory.decodeResource(context.getResources(), R.drawable.fallback_cover);
            	cache.put(album_id, default_bmp);
            	return default_bmp;
            	
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
        
    }
	
	public static Bitmap getAlbumArt(Context context, long album_id, int w, int h)
	{
		Bitmap bitmap=cache.get(album_id*BETTER_QUALITY_ALBUMART);
    	if(bitmap!=null)
    	{
    		Log.d(TAG, "got it "+album_id);
    		return bitmap;
    	}
        ContentResolver res = context.getContentResolver();
        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
        ParcelFileDescriptor fd = null;
        try {
                fd = res.openFileDescriptor(uri, "r");
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false;
                options.inDither = false;
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap b=BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, options);
                cache.put(album_id*BETTER_QUALITY_ALBUMART, b);
                return b;
                
                
            } catch (FileNotFoundException e) {
            	Log.d("ERROR", e.getMessage());
            	Bitmap default_bmp=BitmapFactory.decodeResource(context.getResources(), R.drawable.fallback_cover);
            	cache.put(album_id*BETTER_QUALITY_ALBUMART, default_bmp);
            	return default_bmp;
            	
            } finally {
                try {
                    if (fd != null)
                        fd.close();
                } catch (IOException e) {
                }
            }
	}
	
	public static int GetPixelFromDips(Context context,float pixels) {
	    // Get the screen's density scale 
	    final float scale = context.getResources().getDisplayMetrics().density;
	    // Convert the dps to pixels, based on density scale
	    return (int) (pixels * scale + 0.5f);
	}
	
	public static int getWidth(Activity activity)
	{
		DisplayMetrics metrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;  
        return width;
	}
}
