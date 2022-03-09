package com.jxmp.schedulelowbattery;

import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import android.provider.Settings;
import android.text.SpannableString;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    TextView serverState;
    EditText textOpenHour;
    EditText textOpenMinute;
    EditText textCloseHour;
    EditText textCloseMinute;
    final static String WORK_NAME = "LowBatteryManager";
    final static String ENABLE_TAG = "enable";
    final static String DISABLE_TAG = "disable";
    final static int DEFAULT_OPEN_HOUR = 0;
    final static int DEFAULT_OPEN_MINUTE = 0;
    final static int DEFAULT_CLOSE_HOUR = 8;
    final static int DEFAULT_CLOSE_MINUTE = 0;
    static int openHour = DEFAULT_OPEN_HOUR;
    static int openMinute = DEFAULT_OPEN_MINUTE;
    static int closeHour = DEFAULT_CLOSE_HOUR;
    static int closeMinute = DEFAULT_CLOSE_MINUTE;
    static boolean isStarted = false;

    Timer timer;
    TimerTask task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        serverState = findViewById(R.id.ServerState);
        textOpenHour = findViewById(R.id.editTextStartHour);
        textOpenMinute = findViewById(R.id.editTextStartMinute);
        textCloseHour = findViewById(R.id.editTextEndHour);
        textCloseMinute = findViewById(R.id.editTextEndMinute);
        LoadSettings();
        RegistEvent();
        SetServerStateInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        timer = new Timer();
        task = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        SetServerStateInfo();
                    }
                });
            }
        };
        timer.schedule(task, 0, 1000);
    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    private void RegistEvent() {
        final Context context = getApplicationContext();
        textOpenHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && Integer.parseInt(s.toString()) > 23) {
                    Editable e = (Editable) s;
                    e.delete(start, start + count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                SaveSettings();
            }
        });
        textOpenMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && Integer.parseInt(s.toString()) > 59) {
                    Editable e = (Editable) s;
                    e.delete(start, start + count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                SaveSettings();
            }
        });
        textCloseHour.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && Integer.parseInt(s.toString()) > 23) {
                    Editable e = (Editable) s;
                    e.delete(start, start + count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                SaveSettings();
            }
        });
        textCloseMinute.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0 && Integer.parseInt(s.toString()) > 59) {
                    Editable e = (Editable) s;
                    e.delete(start, start + count);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                SaveSettings();
            }
        });
        Button startBtn = findViewById(R.id.buttonstart);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStarted = true;
                SaveStartedSettings();
                StartLowBatteryWorker(context);
            }
        });
        Button stopBtn = findViewById(R.id.buttonstop);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStarted = false;
                SaveStartedSettings();
                CancleWorker(context);
            }
        });
    }

    private void SetServerStateInfo() {
        try {
            serverState.setText("未开启");
            ListenableFuture<List<WorkInfo>> workInfosByTag = WorkManager.getInstance(getApplicationContext()).getWorkInfosForUniqueWork(WORK_NAME);
            for (WorkInfo info : workInfosByTag.get()) {
                WorkInfo.State state = info.getState();
                switch (state) {
                    case ENQUEUED:
                        if (info.getTags().contains(ENABLE_TAG)) {
                            serverState.setText(openHour + ":" + openMinute + " 开启省电模式");
                        } else if (info.getTags().contains(DISABLE_TAG)) {
                            serverState.setText(closeHour + ":" + closeMinute + " 关闭省电模式");
                        }

                        break;
                    case RUNNING:
                        if (info.getTags().contains(ENABLE_TAG)) {
                            serverState.setText("正在开启省电模式");
                        } else if (info.getTags().contains(DISABLE_TAG)) {
                            serverState.setText("正在关闭省电模式");
                        }

                        break;
                    case SUCCEEDED:
                        break;
                    case FAILED:
                        break;
                    case BLOCKED:
                        break;
                    case CANCELLED:
                        break;
                }
            }
        } catch (ExecutionException e) {

        } catch (InterruptedException e) {

        }
    }

    private void SaveStartedSettings()
    {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("isStarted", isStarted);
        editor.apply();
    }

    private void SaveSettings() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();

        try {
            openHour = Integer.parseInt(String.valueOf(textOpenHour.getText()));
            openMinute = Integer.parseInt(String.valueOf(textOpenMinute.getText()));
            closeHour = Integer.parseInt(String.valueOf(textCloseHour.getText()));
            closeMinute = Integer.parseInt(String.valueOf(textCloseMinute.getText()));
        } catch (NumberFormatException e) {

        }

        editor.putInt("openHour", openHour);
        editor.putInt("openMinute", openMinute);
        editor.putInt("closeHour", closeHour);
        editor.putInt("closeMinute", closeMinute);
        editor.putBoolean("isStarted", isStarted);
        editor.apply();
        CancleWorker(getApplicationContext());
    }

    private void LoadSettings() {
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        openHour = sharedPref.getInt("openHour", DEFAULT_OPEN_HOUR);
        openMinute = sharedPref.getInt("openMinute", DEFAULT_OPEN_MINUTE);
        closeHour = sharedPref.getInt("closeHour", DEFAULT_CLOSE_HOUR);
        closeMinute = sharedPref.getInt("closeMinute", DEFAULT_CLOSE_MINUTE);
        isStarted = sharedPref.getBoolean("isStarted", false);

        RefreshUI();
    }

    private void RefreshUI() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textOpenHour.setText(new SpannableString(openHour + ""), TextView.BufferType.SPANNABLE);
                textOpenMinute.setText(new SpannableString(openMinute + ""), TextView.BufferType.SPANNABLE);
                textCloseHour.setText(new SpannableString(closeHour + ""), TextView.BufferType.SPANNABLE);
                textCloseMinute.setText(new SpannableString(closeMinute + ""), TextView.BufferType.SPANNABLE);
            }
        });
    }

    public static void CancleWorker(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }

    public static void SetLowBatteryMode(Context context, boolean isOn)
    {
        int p = -1;
        try {
            p = Settings.Global.getInt(context.getContentResolver(), "low_power");
            if((!isOn && p != 0) || (isOn && p == 0)) {
                Settings.Global.putInt(context.getContentResolver(), "low_power",  isOn ? 1 : 0);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void StartLowBatteryWorker(Context context) {

        Calendar now = Calendar.getInstance();
        int nowHour = now.get(Calendar.HOUR_OF_DAY);
        int nowMinute = now.get(Calendar.MINUTE);

        boolean inOpenTime = false;
        for(int i = openHour; true; i++) {
            int h = i % 24;
            if(nowHour == h) {
                if (openHour == closeHour && h == openHour) {
                    if(openMinute <= nowMinute && nowMinute < closeMinute) {
                        inOpenTime = true;
                        break;
                    } else {
                        break;
                    }
                } else {
                    if(h == openHour) {
                        if(nowMinute >= openMinute)
                        {
                            inOpenTime = true;
                            break;
                        }
                        else {
                            break;
                        }
                    }else if(h == closeHour) {
                        if(nowMinute < closeMinute) {
                            inOpenTime = true;
                            break;
                        } else {
                            break;
                        }
                    } else {
                        inOpenTime = true;
                        break;
                    }
                }

            }
            if(h == closeHour)
            {
                break;
            }
        }
        boolean isOn = !inOpenTime;

        SetLowBatteryMode(context, inOpenTime);

        Data data = new Data.Builder().putBoolean("isOn", isOn).build();
        int h = isOn ? openHour : closeHour;
        int m = isOn ? openMinute : closeMinute;

        Calendar dst = Calendar.getInstance();
        dst.set(Calendar.HOUR_OF_DAY, h);
        dst.set(Calendar.MINUTE, m);
        dst.set(Calendar.SECOND, 0);
        dst.set(Calendar.MILLISECOND, 0);
        long duration = dst.getTimeInMillis() - now.getTimeInMillis();
        if(duration < 0) {
            dst.add(Calendar.DAY_OF_YEAR, 1);
            duration = dst.getTimeInMillis() - now.getTimeInMillis();
        }

        OneTimeWorkRequest req = new OneTimeWorkRequest.Builder(LowBatteryWorker.class).setInputData(data).addTag(isOn ? ENABLE_TAG : DISABLE_TAG).setInitialDelay(duration, TimeUnit.MILLISECONDS).build();
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.REPLACE, req);
    }
}