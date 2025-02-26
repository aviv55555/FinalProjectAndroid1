package com.example.finalproject.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.example.finalproject.R;

public class TimerService extends Service {
    private static final String CHANNEL_ID = "TimerServiceChannel";
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 0;
    private MediaPlayer mediaPlayer;
    private final IBinder binder = new LocalBinder();
    private boolean isRunning = false;
    private TimerListener listener;
    public interface TimerListener {
        void onTimerTick(long millisUntilFinished);
        void onTimerFinished();
    }
    public class LocalBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Timer Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    public void startTimer(long millis) {
        if (isRunning) return;

        timeLeftInMillis = millis;
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                if (listener != null) {
                    listener.onTimerTick(millisUntilFinished);
                }
                updateNotification("Time left: " + millisUntilFinished / 1000 + " sec");
            }
            @Override
            public void onFinish() {
                isRunning = false;
                playAlarmSound();
                if (listener != null) {
                    listener.onTimerFinished();
                }
                stopForeground(true);
                stopSelf();
            }
        }.start();
        isRunning = true;
        startForeground(1, createNotification("Timer is running..."));
    }
    public long getTimeLeft() {
        return timeLeftInMillis;
    }
    public boolean isTimerRunning() {
        return isRunning;
    }
    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            isRunning = false;
        }
        stopForeground(true);
        stopSelf();
    }
    public void setListener(TimerListener listener) {
        this.listener = listener;
        if (isRunning && listener != null) {
            listener.onTimerTick(timeLeftInMillis);
        }
    }
    private Notification createNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Cooking Timer")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_timer)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
    private void updateNotification(String contentText) {
        Notification notification = createNotification(contentText);
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(1, notification);
        }
    }
    private void playAlarmSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.timer_sound);
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(mp -> {
            mediaPlayer.release();
            mediaPlayer = null;
        });
    }
}