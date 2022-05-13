package com.example.watchdog.services;

import static com.example.watchdog.App.CHANNEL_ID;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.watchdog.MainActivity;
import com.example.watchdog.R;
import com.example.watchdog.models.Stock;
import com.example.watchdog.receivers.TrackingReceiver;
import com.example.watchdog.utils.DbHandler;
import com.example.watchdog.utils.StockCollection;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class TrackingService extends Service implements Runnable, StockCollection.ResponseListener {

    public static final String ACTION_SERVICE_STOP = "action_service_stop";
    public static final String ACTION_DATA_SEND = "action_data_send";

    private static final String TAG = TrackingService.class.getSimpleName();
    private static final int PERIOD = 10000;

    private Thread worker;
    private DbHandler db;
    private boolean running;
    private List<Stock> stockList;
    private StockCollection stockCollection;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "Start Tracking Service", Toast.LENGTH_SHORT).show();
        db = new DbHandler(this.getApplicationContext());
        stockCollection = new StockCollection(this);

        running = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        pullStocks();
        sendNotification(null, true);

        if (worker == null) {
            worker = new Thread(this);
            worker.start();
        }

        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        running = false;
        Toast.makeText(this, "Force Stop Tracking Service", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public synchronized void pullStocks() {
        //TODO: add stockList
        db.openDb();
        stockList = db.getAllStock();

        if (stockList.size() == 0)
            stopSelf();
    }

    public void sendNotification(String strDataIntent, boolean silent) {
        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pIntentActivity = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE);

        Intent actionIntent = new Intent(this, TrackingReceiver.class);
        actionIntent.setAction(ACTION_SERVICE_STOP);
        PendingIntent pIntentAction = PendingIntent.getBroadcast(this, 0, actionIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                //.setContentTitle("Watchdog")
                .setContentText(strDataIntent)
                .setContentIntent(pIntentActivity)
                .setSmallIcon(R.drawable.logo)
                .addAction(R.drawable.ic_baseline_close, "Exit", pIntentAction)
                .setSilent(silent)
                // set high priority for Heads Up Notification
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build();

        startForeground(1, notification);
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!running) break;
                stockCollection.collect(stockList, this);
                Thread.sleep(PERIOD);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResponse(List<Stock> stocks) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean alert = false;

        for (Stock s : stocks) {
            if (s.getStatus() == 1) continue;
            if (s.getWarningPrice() < s.getLastPrice()) continue;

            alert = true;
            stringBuilder.append(String.format("%s=%,.2f ", s.getSymbol(), s.getLastPrice()));
        }

        if (!alert) return;

        sendNotification(stringBuilder.toString(), false);
    }
}
