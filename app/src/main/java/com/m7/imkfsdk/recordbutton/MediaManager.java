package com.m7.imkfsdk.recordbutton;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

import java.io.IOException;

public class MediaManager {
	
	private static MediaPlayer mMediaPlayer;
	private static boolean isPause;

	public static void playSound(String filePath,
			OnCompletionListener onCompletionListener) {
		if(filePath == null && "".equals(filePath)) {
			return;
		}
		if(mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnErrorListener(new OnErrorListener() {
				
				@Override
				public boolean onError(MediaPlayer mp, int what, int extra) {
					mMediaPlayer.reset();
					return false;
				}
			});
		}else {
			mMediaPlayer.reset();
		}
		
		try {
			mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mMediaPlayer.setOnCompletionListener(onCompletionListener);
			mMediaPlayer.setDataSource(filePath);
			mMediaPlayer.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mMediaPlayer.start();
	}
	
	public static void pause() {
		if(mMediaPlayer != null && mMediaPlayer.isPlaying()) {
			mMediaPlayer.pause();
			isPause = true;
		}
	}
	public static void resume() {
		if(mMediaPlayer != null && isPause) {
			mMediaPlayer.start();
			isPause = false;
		}
	}
	public static void relese() {
		if(mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

}
