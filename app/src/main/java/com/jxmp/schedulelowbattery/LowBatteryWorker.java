package com.jxmp.schedulelowbattery;

import android.content.Context;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;


import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Properties;

import java.io.*;
import java.net.InetAddress;
import java.util.Properties;
import java.util.Date;

public class LowBatteryWorker extends Worker {
    boolean isOn;
    public LowBatteryWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
        isOn = params.getInputData().getBoolean("isOn", false);
    }

    @NonNull
    @NotNull
    @Override
    public Result doWork() {
        MainActivity.SetLowBatteryMode(getApplicationContext(), isOn);
        MainActivity.StartLowBatteryWorker(getApplicationContext());
        return Result.success();
    }

}

