package com.moor.im.ui.view.recordbutton;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.moor.im.R;
import com.moor.im.ui.view.recordbutton.AudioManager.AudioStateListener;


/**
 * Created by LongWei on 2015/6/11.
 */
public class AudioRecorderButton extends Button implements AudioStateListener{

    private static final int DISTANCE_Y_CANCEL = 50;

    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;
    private static final int STATE_WANT_TO_CANCEL = 3;

    private int mCurrentState = STATE_NORMAL;
    private boolean isRecording = false;

    private DialogManager mDialogManager;
    
    private AudioManager mAudioManager;

	protected float mTime;
	
	private boolean mReady;
    
    private static final int MSG_RECORDER_PREPARE = 0x11;
    private static final int MSG_VOICE_CHANGE = 0x12;
    private static final int MSG_DIALOG_DISMISS = 0x13;
    
    private RecorderFinishListener listener;
    
    private Runnable mGetVoiceLevelRunnable = new Runnable() {
		
		@Override
		public void run() {
			while(isRecording) {
				try {
					Thread.sleep(100);
					mTime += 0.1f;
					handler.sendEmptyMessage(MSG_VOICE_CHANGE);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};
	
	public interface RecorderFinishListener {
		void onRecordFinished(float mTime, String filePath);
	}

    public AudioRecorderButton(Context context) {
        this(context, null);
    }

    public AudioRecorderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDialogManager = new DialogManager(context);
        
        String dir = Environment.getExternalStorageDirectory() + "/m7_chat_recorder";
        
        mAudioManager = AudioManager.getInstance(dir);
        mAudioManager.setAudioStateListener(this);

        setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
            	System.out.println("进行了长按事件");
            	mReady = true;
                mAudioManager.prepareAudio();
                return false;
            }
        });
    }
    
    public void setRecordFinishListener(RecorderFinishListener listener) {
    	this.listener = listener;
    }
    
    private Handler handler = new Handler() {
    	

		public void handleMessage(android.os.Message msg) {
    		switch (msg.what) {
			case MSG_RECORDER_PREPARE:
				mDialogManager.showDialog();
                isRecording = true;
                new Thread(mGetVoiceLevelRunnable).start();
				break;
			case MSG_VOICE_CHANGE:
				mDialogManager.updateVoiceLevel(mAudioManager.getVoiceLevel(7));
				break;
			case MSG_DIALOG_DISMISS:
				mDialogManager.dismissDialog();
				break;

			default:
				break;
			}
    	};
    };
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int x = (int) event.getX();
        int y = (int) event.getY();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                changeState(STATE_RECORDING);
                
                break;
            case MotionEvent.ACTION_MOVE:

                if(isRecording) {
                    if(wanttocancel(x, y)) {
                        changeState(STATE_WANT_TO_CANCEL);
                    }else{
                        changeState(STATE_RECORDING);
                    }
                    
                }
                break;

            case MotionEvent.ACTION_UP:
            	
            	if(!mReady) {
            		reset();
            		return super.onTouchEvent(event);
            	}
            	if(!isRecording || mTime < 0.9) {
            		mDialogManager.tooShort();
            		mAudioManager.cancel();
            		handler.sendEmptyMessageDelayed(MSG_DIALOG_DISMISS, 1000);
            	}else if(mCurrentState == STATE_RECORDING) {
                    mDialogManager.dismissDialog();
                    
                    if(listener != null) {
                    	System.out.println("audiorecorederbutton中返回的mAudioManager.getCurrentFilePath()是："+mAudioManager.getCurrentFilePath());
                    	listener.onRecordFinished(mTime, mAudioManager.getCurrentFilePath());
                    }
                    mAudioManager.release();
                }else if(mCurrentState == STATE_WANT_TO_CANCEL) {
                    mDialogManager.dismissDialog();
                    mAudioManager.cancel();
                }

                reset();
                break;
        }

        return super.onTouchEvent(event);
    }

    /**
     * 恢复标志位
     */
    private void reset() {
        isRecording = false;
        mReady = false;
        mTime = 0;
        changeState(STATE_NORMAL);
    }

    private boolean wanttocancel(int x, int y) {
        if(x < 0 || x > getWidth()) {
            return true;
        }
        if(y < -DISTANCE_Y_CANCEL || y > getHeight() + DISTANCE_Y_CANCEL) {
            return true;
        }
        return false;
    }

    private void changeState(int state) {
        if(mCurrentState != state) {
            mCurrentState = state;
            switch (state) {
                case STATE_NORMAL:
                    setBackgroundResource(R.drawable.btn_recorder_normal);
                    setText(R.string.recorder_normal);
                    break;
                case STATE_RECORDING:
                    setBackgroundResource(R.drawable.btn_recorder_press);
                    setText(R.string.recorder_recording);
                    if(isRecording) {
                        mDialogManager.recording();
                    }
                    break;
                case STATE_WANT_TO_CANCEL:
                    setBackgroundResource(R.drawable.btn_recorder_press);
                    setText(R.string.recorder_want_cancel);
                    mDialogManager.wantToCancel();
                    break;
            }
        }
    }

	@Override
	public void wellPrepared() {
		handler.sendEmptyMessage(MSG_RECORDER_PREPARE);
	}
}
