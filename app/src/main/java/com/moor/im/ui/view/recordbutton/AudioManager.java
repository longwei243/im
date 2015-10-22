package com.moor.im.ui.view.recordbutton;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.media.MediaRecorder;

public class AudioManager {

	private MediaRecorder mRecorder;
	private String mDir;
	private String mCurrentFilePath;
	
	private boolean isPrepared;
	
	private static AudioManager instance;
	
	private AudioStateListener listener;
	
	public interface AudioStateListener{
		void wellPrepared();
	}
	
	public void setAudioStateListener(AudioStateListener listener) {
		this.listener = listener;
	}
	
	private AudioManager(){}
	
	private AudioManager(String dir){
		this.mDir = dir;
	}
	
	public static AudioManager getInstance(String dir) {
		if(instance == null) {
			synchronized (AudioManager.class) {
				instance = new AudioManager(dir);
			}
		}
		return instance;
	}
	
	public void prepareAudio() {
		try {
			isPrepared = false;
			File dir = new File(mDir);
			if(!dir.exists()) {
				dir.mkdirs();
				
			}
			
			
				
			String fileName = generateFileName();
			File file = new File(dir, fileName);
			mCurrentFilePath = file.getAbsolutePath();
			System.out.println("录音文件路径是："+mCurrentFilePath);
			
			mRecorder = new MediaRecorder();
			mRecorder.setOutputFile(file.getAbsolutePath());
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			
			mRecorder.prepare();
			mRecorder.start();
			if(listener != null) {
				listener.wellPrepared();
			}
			isPrepared = true;
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String generateFileName() {
		
		return UUID.randomUUID().toString() + ".amr";
	}

	public int getVoiceLevel(int maxLevel) {
		if(isPrepared && mRecorder != null) {
			long a =  maxLevel * mRecorder.getMaxAmplitude();
			long b = a / 32768;
			int lev = (int)b + 1;
			return lev;
		}
		return 1;
	}
	
	public void release() {
		if(mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
			System.out.println("录音文件存起来了");
		}
	}
	
	public void cancel() {
		release();
		if(mCurrentFilePath != null) {
			File file = new File(mCurrentFilePath);
			file.delete();
			mCurrentFilePath = null;
			System.out.println("录音文件被删除了");
		}
	}

	public String getCurrentFilePath() {
		System.out.println("audiomanager中返回的mCurrentFilePath是："+mCurrentFilePath);
		return mCurrentFilePath;
	}
}
