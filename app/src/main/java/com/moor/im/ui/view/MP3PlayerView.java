package com.moor.im.ui.view;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moor.im.R;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by longwei on 2016/2/25.
 */
public class MP3PlayerView extends FrameLayout implements MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    private ImageView mp3player_iv_play;
    private TextView mp3player_tv_time;
    private SeekBar mp3player_seekbar;
    private RelativeLayout mp3player_rl;

    public MediaPlayer mediaPlayer;
    private Timer mTimer = new Timer();

    public void setUrlPath(String urlPath) {
        this.urlPath = urlPath;
    }

    private String urlPath;

    private boolean isPlaying = false;
    private boolean isPaused = false;


    public MP3PlayerView(Context context) {
        super(context);
    }

    public MP3PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.layout_mp3player, this);
        mp3player_rl = (RelativeLayout) findViewById(R.id.mp3player_rl);
        mp3player_iv_play = (ImageView) findViewById(R.id.mp3player_iv_play);
        mp3player_tv_time = (TextView) findViewById(R.id.mp3player_tv_time);
        mp3player_seekbar = (SeekBar) findViewById(R.id.mp3player_seekbar);
        mp3player_seekbar.setOnSeekBarChangeListener(new SeekBarChangeEvent());
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);// 设置媒体流类型
            mediaPlayer.setOnBufferingUpdateListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnErrorListener(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 每一秒触发一次
        mTimer.schedule(timerTask, 0, 1000);

        mp3player_iv_play.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isPlaying) {
                    if(urlPath != null && !"".equals(urlPath)) {
                        mp3player_iv_play.setImageResource(R.drawable.ic_pause_white_36dp);
                        isPlaying = true;
                        playUrl(urlPath);
                    }
                }else {
                    if(!isPaused) {
                        mp3player_iv_play.setImageResource(R.drawable.ic_play_arrow_white_36dp);
                        isPaused = true;
                        pause();
                    }else {
                        mp3player_iv_play.setImageResource(R.drawable.ic_pause_white_36dp);
                        isPaused = false;
                        play();
                    }

                }


            }
        });
    }

    // 计时器
    TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            if (mediaPlayer == null)
                return;
            if (mediaPlayer.isPlaying() && mp3player_seekbar.isPressed() == false) {
                handler.sendEmptyMessage(0); // 发送消息
            }
        }
    };

    Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            int position = mediaPlayer.getCurrentPosition();
            int duration = mediaPlayer.getDuration();
            if (duration > 0) {
                // 计算进度（获取进度条最大刻度*当前音乐播放位置 / 当前音乐时长）
                long pos = mp3player_seekbar.getMax() * position / duration;
                mp3player_seekbar.setProgress((int) pos);
                int musicTime = position / 1000;
                mp3player_tv_time.setText(musicTime / 60 + ":" + musicTime % 60);
            }
        }
    };

    public void play() {
        mediaPlayer.start();
    }

    public void playUrl(String url) {
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(url); // 设置数据源
            mediaPlayer.prepareAsync();
        }catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "录音已被删除，无法播放", Toast.LENGTH_SHORT).show();
        }
    }

    // 暂停
    public void pause() {
        mediaPlayer.pause();
    }

    // 停止
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // 播放准备
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        Log.e("mediaPlayer", "onPrepared");
    }

    // 播放完成
    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e("mediaPlayer", "onCompletion");
//        mp.stop();
        mp3player_iv_play.setImageResource(R.drawable.ic_play_arrow_white_36dp);
        mp3player_seekbar.setProgress(0);
        isPlaying = false;
    }

    /**
     * 缓冲更新
     */
    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        mp3player_seekbar.setSecondaryProgress(percent);
//        int currentProgress = mp3player_seekbar.getMax()
//                * mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration();
//        Log.e(currentProgress + "% play", percent + " buffer");
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(getContext(), "录音已被删除，无法播放", Toast.LENGTH_SHORT).show();
        mp3player_rl.setVisibility(GONE);
        stop();
        return true;
    }

    class SeekBarChangeEvent implements SeekBar.OnSeekBarChangeListener {
        int progress;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            // 原本是(progress/seekBar.getMax())*player.mediaPlayer.getDuration()
            this.progress = progress * mediaPlayer.getDuration()
                    / seekBar.getMax();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
            mediaPlayer.seekTo(progress);
        }

    }
}
