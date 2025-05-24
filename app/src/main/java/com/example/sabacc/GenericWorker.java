package com.example.sabacc;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class GenericWorker extends Worker {
    public static final String KEY_TASK_NAME = "task_name";
    public static final String KEY_SLEEP_DURATION = "sleep_duration";
    public static final String KEY_LOG_TAG = "log_tag";
    public GenericWorker(@NonNull Context context, @NonNull WorkerParameters workerParameters){
        super(context, workerParameters);
    }

    @NonNull
    @Override
    public Result doWork() {
        String taskName = getInputData().getString(KEY_TASK_NAME);
        long sleepDuration = getInputData().getLong(KEY_SLEEP_DURATION, 1000);
        String tag = getInputData().getString(KEY_LOG_TAG);

        try{
            Thread.sleep(sleepDuration);
            String time = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                time = DateTimeFormatter.ofPattern("HH:mm:ss").format(LocalTime.now());
            }
            Data output = new Data.Builder().putString("result", time).build();
            return Result.success(output);
        } catch(InterruptedException e){
            Log.e(tag, taskName + " failed", e);
            return Result.failure();
        }
    }
}
