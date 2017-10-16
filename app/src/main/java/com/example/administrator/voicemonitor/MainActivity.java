package com.example.administrator.voicemonitor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Application;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {


    public Button startButton, sendMsgBtn;
    public boolean isOSVersionOver23;
    public EditText phoneNum;
    public EditText textMsg;
    public EditText volum;
    public EditText textRate;
    public ProgressBar proBar;
    public TextView tvOfVol;
    public String[] phoneNums;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= 23) {
            isOSVersionOver23 = true;
        } else {
            isOSVersionOver23 = false;
            checkPermission();
        }
        initViews();

        initDatas();

        registerReceiver(send, new IntentFilter("com.send"));
        registerReceiver(delivery, new IntentFilter("com.delivery"));
        registerReceiver(sendSms, new IntentFilter("com.sendSMS"));
        registerReceiver(sendProgress, new IntentFilter("com.sendProgress"));
    }

    private void initDatas() {

        if (PreferencesUtil.getString("phoneNum") != null) {
            phoneNum.setText(PreferencesUtil.getString("phoneNum"));
        }
        if (PreferencesUtil.getString("textMsg") != null) {
            textMsg.setText(PreferencesUtil.getString("textMsg"));
        }
        if (PreferencesUtil.getInt("volum") != -1) {
            volum.setText(String.valueOf(PreferencesUtil.getInt("volum")));
        }
        if (PreferencesUtil.getInt("textRate") != -1) {
            textRate.setText(String.valueOf(PreferencesUtil.getInt("textRate")));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(send);
        unregisterReceiver(delivery);
        unregisterReceiver(sendSms);
        unregisterReceiver(sendProgress);
    }

    private void initViews() {
        startButton = (Button) findViewById(R.id.btn_monitor);

        startButton.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {

                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                v.requestFocusFromTouch();


                if (TextUtils.isEmpty(phoneNum.getText().toString().trim())) {
                    Toast.makeText(MainActivity.this, "对方的手机号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Pattern p = Pattern.compile("[0-9]*");
                Matcher m ;
                String phonesString = phoneNum.getText().toString().trim().replace("，", ",");
                phoneNums = phonesString.split(",");
                for (int i = 0; i < phoneNums.length; i++) {
                    m = p.matcher(phoneNums[i].toString().trim());
                    if (!m.matches()) {
                        Toast.makeText(MainActivity.this, "手机号码只能为数字", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (TextUtils.isEmpty(textMsg.getText().toString().trim())) {
                    textMsg.setText("有声音响了");
                }

                m = p.matcher(volum.getText().toString());
                if (!m.matches()) {
                    Toast.makeText(MainActivity.this, "声音只能为数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                int volumNum = Integer.valueOf(volum.getText().toString());
                if (volumNum < 30 || volumNum > 100) {
                    Toast.makeText(MainActivity.this, "声音的范围为30 - 90", Toast.LENGTH_SHORT).show();
                    return;
                }
                m = p.matcher(textRate.getText().toString());
                if (!m.matches()) {
                    Toast.makeText(MainActivity.this, "频率只能为数字", Toast.LENGTH_SHORT).show();
                    return;
                }
                int txtRate = Integer.valueOf(textRate.getText().toString());
                if (txtRate < 2 || txtRate > 600) {
                    Toast.makeText(MainActivity.this, "频率范围为2 - 20", Toast.LENGTH_SHORT).show();
                    return;
                }
                saveValueToLocal(phoneNum.getText().toString().trim(), textMsg.getText().toString().trim(), volumNum, txtRate);
                AudioRecordUtil.getInstance().voiceNum = volumNum;
                AudioRecordUtil.getInstance().textRate = txtRate;
                if (isOSVersionOver23) {
                    if (checkPermission()) {
                        if (AudioRecordUtil.getInstance().isGetVoiceRun) {
                            AudioRecordUtil.getInstance().isGetVoiceRun = false;
                            startButton.setText("开始");
                            tvOfVol.setText("0");
                            proBar.setProgress(0);
                        } else {
                            AudioRecordUtil.getInstance().isGetVoiceRun = true;
                            startButton.setText("监听中...");
                            AudioRecordUtil.getInstance().getNoiseLevel();
                        }
                    }
                } else {
                    if (AudioRecordUtil.getInstance().isGetVoiceRun) {
                        AudioRecordUtil.getInstance().isGetVoiceRun = false;
                        startButton.setText("开始");
                        tvOfVol.setText("0");
                        proBar.setProgress(0);
                    } else {
                        AudioRecordUtil.getInstance().isGetVoiceRun = true;
                        startButton.setText("监听中...");
                        AudioRecordUtil.getInstance().getNoiseLevel();
                    }
                }
            }
        });

        sendMsgBtn = (Button) findViewById(R.id.btn_sendMsg);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {


            }
        });

        phoneNum = (EditText) findViewById(R.id.phoneNum);
        textMsg = (EditText) findViewById(R.id.textMsg);
        volum = (EditText) findViewById(R.id.volum);
        textRate = (EditText) findViewById(R.id.rate);
        View.OnFocusChangeListener onFocusChangeListener = new MyFocusChangeListener();
        phoneNum.setOnFocusChangeListener(onFocusChangeListener);
        textMsg.setOnFocusChangeListener(onFocusChangeListener);
        volum.setOnFocusChangeListener(onFocusChangeListener);
        textRate.setOnFocusChangeListener(onFocusChangeListener);

        proBar = (ProgressBar) findViewById(R.id.progressBar);
        proBar.setMax(100);
        proBar.setProgress(0);

        tvOfVol = (TextView) findViewById(R.id.tv_curvol);
    }

    private void saveValueToLocal(String phone, String text, int vol, int txtRate) {
        PreferencesUtil.putString("phoneNum", phone);
        PreferencesUtil.putString("textMsg", text);
        PreferencesUtil.putInt("volum", vol);
        PreferencesUtil.putInt("textRate", txtRate);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean  checkPermission()  {
        boolean checkOk = false;
        int permissionCheckRead = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int permissionSMS = ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if (permissionCheckRead == PackageManager.PERMISSION_GRANTED && permissionSMS == PackageManager.PERMISSION_GRANTED) {
            checkOk = true;
        } else {
            this.requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.SEND_SMS}, 1);
            checkOk = false;
        }
        return checkOk;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "请允许权限", Toast.LENGTH_SHORT);
            }
        } else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "请允许权限", Toast.LENGTH_SHORT);
            }
        }
    }

    BroadcastReceiver send = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if("com.send".equals(intent.getAction()))
                Toast.makeText(MainActivity.this, "SMS send  success",
                        Toast.LENGTH_LONG).show();
        }
    };
    BroadcastReceiver delivery = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if("com.delivery".equals(intent.getAction()))
                Toast.makeText(MainActivity.this, "SMS delivery success",
                        Toast.LENGTH_LONG).show();
        }
    };

    BroadcastReceiver sendSms = new BroadcastReceiver() {
        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (checkPermission()) {
                SmsManager manager = SmsManager.getDefault();
                Intent sent = new Intent();
                sent.setAction("com.send");
                PendingIntent sentIntent = PendingIntent.getBroadcast(MainActivity.this, 0, sent, 0);
                Intent delivery = new Intent();
                delivery.setAction("com.delivery");
                PendingIntent deliveryIntent = PendingIntent.getBroadcast(MainActivity.this, 0, delivery , 0);
                for(int i = 0; i< phoneNums.length; i++) {
                    manager.sendTextMessage(phoneNums[i].toString().trim(), null, textMsg.getText().toString(), sentIntent, deliveryIntent);
                }
            }
        }
    };

    BroadcastReceiver sendProgress = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double progressVal = intent.getDoubleExtra("progress",0);
            proBar.setProgress((int) progressVal);
            tvOfVol.setText((int) progressVal + "");
        }
    };


    private class MyFocusChangeListener implements View.OnFocusChangeListener {

        public void onFocusChange(View v, boolean hasFocus){

            if((v.getId() == R.id.phoneNum && !hasFocus) || (v.getId() == R.id.textMsg && !hasFocus)
                    || (v.getId() == R.id.volum && !hasFocus)
                    || (v.getId() == R.id.rate && !hasFocus )) {

                InputMethodManager imm =  (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

            }
        }
    }
}
