package com.example.administrator.voicemonitor;

import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.provider.Settings;
import android.util.Log;

/**
 * Created by Administrator on 10/14/2017.
 */

public class AudioRecordUtil {

    private static AudioRecordUtil instance = null;

    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetVoiceRun = false;
    float voiceNum;
    Object mLock;
    long curTime;
    long nextTime;
    int textRate;

    private AudioRecordUtil() {
        mLock = new Object();
        voiceNum = 50;
        textRate = 10;
    }

    public static AudioRecordUtil getInstance() {
        if (null == instance) {
            instance = new AudioRecordUtil();
        }
        return instance;
    }

    public void getNoiseLevel() {

        if (mAudioRecord == null) {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                    AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        } else {
            if (!isGetVoiceRun) {
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
                return;
            }
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                while (isGetVoiceRun) {
                    //r是实际读取的数据长度，一般而言r会小于buffersize
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    // 将 buffer 内容取出，进行平方和运算
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    // 平方和除以数据总长度，得到音量大小。
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
                    Log.d(TAG, "分贝值:" + volume);
                    sendProress(volume);
                    if(volume > voiceNum){
                        if (System.currentTimeMillis() - curTime > 1000 * textRate) {
                            curTime = System.currentTimeMillis();
                            Log.d(TAG, "声音达标");
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction("com.sendSMS");
                            PApplication.getAppContext().sendBroadcast(intent);
                        }
                    }
                    // 大概一秒十次
                    synchronized (mLock) {
                        try {
                            mLock.wait(100);

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }).start();
    }

    private void sendProress(double volume) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("com.sendProgress");
        intent.putExtra("progress", volume);
        PApplication.getAppContext().sendBroadcast(intent);
    }
}
