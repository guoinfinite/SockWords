package com.example.sockword;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;

public class ScreenListener {
    private Context context;
    private ScreenBroadcastReceiver screenBroadcastReceiver;
    private ScreenStateListener screenStateListener;


    public ScreenListener(Context context) {
        this.context = context;
        screenBroadcastReceiver = new ScreenBroadcastReceiver();
    }

    public interface ScreenStateListener {
        void onScreenOn();
        void onScreenOff();
        void onUserPresent();
    }

    private void getScreenState() {
        PowerManager manager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (manager.isScreenOn()) {
            if (screenStateListener != null) {
                screenStateListener.onScreenOn();
            }
        }
        else {
            if (screenStateListener != null) {
                screenStateListener.onScreenOff();
            }
        }
    }

    private class ScreenBroadcastReceiver extends BroadcastReceiver {
        private String action = null;

        @Override
        public void onReceive(Context context, Intent intent) {
            action = intent.getAction();
            if (Intent.ACTION_SCREEN_ON.equals(action)) {
                screenStateListener.onScreenOn();
            }
            else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                screenStateListener.onScreenOff();
            }
            else if (Intent.ACTION_USER_PRESENT.equals(action)) {
                screenStateListener.onUserPresent();
            }
        }
    }

    public void begin(ScreenStateListener listener) {
        screenStateListener = listener;
        registerListener();
        getScreenState();
    }

    private void registerListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        context.registerReceiver(screenBroadcastReceiver, filter);
    }

    public void unregisterListener() {
        context.unregisterReceiver(screenBroadcastReceiver);
    }
}
