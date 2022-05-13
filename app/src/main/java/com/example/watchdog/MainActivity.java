package com.example.watchdog;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchdog.adapter.StockAdapter;
import com.example.watchdog.interfaces.DialogCloseListener;
import com.example.watchdog.models.Stock;
import com.example.watchdog.services.TrackingService;
import com.example.watchdog.utils.DbHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DialogCloseListener {
    public static final String ACTIVITY_FINISH = "main_activity_finish";
    private DbHandler db;
    private StockAdapter adapter;
    private List<Stock> stockList;
    BroadcastReceiver receiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(ACTIVITY_FINISH)){
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Objects.requireNonNull(getSupportActionBar()).hide();

        IntentFilter filter=new IntentFilter();
        filter.addAction(ACTIVITY_FINISH);
        registerReceiver(receiver,filter);

        db = new DbHandler(this);
        db.openDb();

        stockList = new ArrayList<>();

        RecyclerView taskRecyclerView = findViewById(R.id.tasksRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new StockAdapter(db, this);
        taskRecyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerItemTouchHelper(adapter, this));
        itemTouchHelper.attachToRecyclerView(taskRecyclerView);

        stockList = db.getAllStock();
        Collections.reverse(stockList);

        adapter.setTasks(stockList);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        startTrackingService();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void handleDialogClose(DialogInterface dialog) {
        stockList = db.getAllStock();
        Collections.reverse(stockList);
        adapter.setTasks(stockList);
        adapter.notifyDataSetChanged();

        startTrackingService();
    }

    public void startTrackingService() {
        Intent serviceIntent = new Intent(this, TrackingService.class);
        startService(serviceIntent);
    }
}