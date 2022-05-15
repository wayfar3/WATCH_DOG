package com.example.watchdog.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchdog.AddNewTask;
import com.example.watchdog.Constant;
import com.example.watchdog.MainActivity;
import com.example.watchdog.R;
import com.example.watchdog.models.Stock;
import com.example.watchdog.utils.DbHandler;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.ViewHolder> {

    private List<Stock> stockList;
    private final MainActivity activity;
    private final DbHandler db;

    public StockAdapter(DbHandler db, MainActivity activity) {
        this.activity = activity;
        this.db = db;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        return new ViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        db.openDb();

        final Stock item = stockList.get(position);

        holder.tvSymbol.setText(item.getSymbol());
        holder.tvShortName.setText(item.getShortName());
        holder.tvWarning.setText(String.valueOf(item.getWarningPrice()));
        holder.tvLast.setText(String.valueOf(item.getLastPrice()));
        if (item.getType() == Stock.LESS) {
            holder.tvType.setText(Constant.LESS);
            holder.tvType.setTextColor(ContextCompat.getColor(activity, R.color.warning_dark));
        } else {
            holder.tvType.setText(Constant.GREATER);
            holder.tvType.setTextColor(ContextCompat.getColor(activity, R.color.success_dark));
        }
    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

    public Context getContext() {
        return activity;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setTasks(List<Stock> stockList) {
        this.stockList = stockList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        Stock item = stockList.get(position);
        db.deleteStock(item.getId());
        stockList.remove(position);
        notifyItemRemoved(position);

        activity.startTrackingService();
    }

    public void editItem(int position) {
        Stock item = stockList.get(position);
        Bundle bundle = new Bundle();
        bundle.putSerializable("stock", item);
        bundle.putInt("id", item.getId());
        AddNewTask fragment = new AddNewTask(activity.getStockDex());
        fragment.setArguments(bundle);
        fragment.show(activity.getSupportFragmentManager(), AddNewTask.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSymbol;
        TextView tvWarning;
        TextView tvLast;
        TextView tvType;
        TextView tvShortName;


        ViewHolder(View view) {
            super(view);
            tvSymbol = view.findViewById(R.id.stackSymbol);
            tvWarning = view.findViewById(R.id.stackWarning);
            tvLast = view.findViewById(R.id.stackLast);
            tvType = view.findViewById(R.id.stackType);
            tvShortName = view.findViewById(R.id.stackShortName);
        }
    }
}
