package com.example.finalproject.Fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.finalproject.R;
import com.example.finalproject.Services.TimerService;

public class FragmentTimer extends Fragment {

    private NumberPicker numberPickerHours, numberPickerMinutes, numberPickerSeconds;
    private Button buttonStartTimer, buttonStopTimer;
    private TimerService timerService;
    private boolean isBound = false;

    public FragmentTimer() {}

    public static FragmentTimer newInstance() {
        return new FragmentTimer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_timer, container, false);

        numberPickerHours = rootView.findViewById(R.id.numberPickerHours);
        numberPickerMinutes = rootView.findViewById(R.id.numberPickerMinutes);
        numberPickerSeconds = rootView.findViewById(R.id.numberPickerSeconds);
        buttonStartTimer = rootView.findViewById(R.id.buttonStartTimer);
        buttonStopTimer = rootView.findViewById(R.id.buttonStopTimer);

        numberPickerHours.setMinValue(0);
        numberPickerHours.setMaxValue(23);
        numberPickerMinutes.setMinValue(0);
        numberPickerMinutes.setMaxValue(59);
        numberPickerSeconds.setMinValue(0);
        numberPickerSeconds.setMaxValue(59);

        buttonStartTimer.setOnClickListener(v -> startTimer());
        buttonStopTimer.setOnClickListener(v -> stopTimer());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent serviceIntent = new Intent(getActivity(), TimerService.class);
        getActivity().bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        getActivity().startService(serviceIntent);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isBound) {
            timerService.setListener(null);
            getActivity().unbindService(serviceConnection);
            isBound = false;
        }
    }
    private void startTimer() {
        int hours = numberPickerHours.getValue();
        int minutes = numberPickerMinutes.getValue();
        int seconds = numberPickerSeconds.getValue();
        long totalTimeMillis = (hours * 3600L + minutes * 60L + seconds) * 1000L;

        if (totalTimeMillis == 0) {
            Toast.makeText(getActivity(), "Please set a valid time", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent serviceIntent = new Intent(getActivity(), TimerService.class);
        getActivity().startService(serviceIntent);

        if (isBound) {
            timerService.startTimer(totalTimeMillis);
        }
    }
    private void stopTimer() {
        if (isBound) {
            timerService.stopTimer();
        }
    }
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
            timerService = binder.getService();
            isBound = true;

            timerService.setListener(new TimerService.TimerListener() {
                @Override
                public void onTimerTick(long millisUntilFinished) {
                    int seconds = (int) (millisUntilFinished / 1000);
                    numberPickerHours.setValue(seconds / 3600);
                    numberPickerMinutes.setValue((seconds % 3600) / 60);
                    numberPickerSeconds.setValue(seconds % 60);
                }
                @Override
                public void onTimerFinished() {
                    Toast.makeText(getActivity(), "Time's up!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };
}