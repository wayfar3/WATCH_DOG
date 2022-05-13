package com.example.watchdog.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.watchdog.models.Stock;

import java.util.ArrayList;
import java.util.List;

public class DbHandler extends SQLiteOpenHelper {


    private static final int VERSION = 1;
    private static final String DB_NAME = "stockDatabase";
    private static final String STOCK_TABLE = "tbl_stock";
    private static final String ID = "id";
    private static final String SYMBOL = "symbol";
    private static final String WARNING = "warning";

    private static final String STATUS = "status";
    private static final String CREATE_STOCK_TABLE = "CREATE TABLE " + STOCK_TABLE + "(" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SYMBOL + " TEXT, "
            + WARNING + " INTEGER, "
            + STATUS + " INTEGER)";

    private SQLiteDatabase db;

    public DbHandler(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_STOCK_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + STOCK_TABLE);
        onCreate(db);
    }

    public void openDb() {
        db = this.getWritableDatabase();
    }


    @SuppressLint("Range")
    public List<Stock> getAllStock() {
        List<Stock> taskList = new ArrayList<>();
        Cursor curs = null;
        db.beginTransaction();
        try {
            curs = db.query(STOCK_TABLE, null, null, null, null, null, null);
            if (curs != null) {
                if (curs.moveToFirst()) {
                    do {
                        Stock s = new Stock();
                        s.setId(curs.getInt(curs.getColumnIndex(ID)));
                        s.setSymbol(curs.getString(curs.getColumnIndex(SYMBOL)));
                        s.setWarningPrice(curs.getDouble(curs.getColumnIndex(WARNING)));
                        s.setStatus(curs.getInt(curs.getColumnIndex(STATUS)));

                        taskList.add(s);

                    } while (curs.moveToNext());
                }
            }

        } finally {
            db.endTransaction();
            assert curs != null;
            curs.close();
        }
        return taskList;
    }

    public void insertStock(Stock stock) {
        ContentValues cv = new ContentValues();
        cv.put(SYMBOL, stock.getSymbol());
        cv.put(WARNING, stock.getWarningPrice());
        cv.put(STATUS, 0);
        db.insert(STOCK_TABLE, null, cv);
    }

    public void updateStock(int id, String symbol,Double warning) {
        ContentValues cv = new ContentValues();
        cv.put(SYMBOL, symbol);
        cv.put(WARNING,warning);
        db.update(STOCK_TABLE, cv, ID + "= ?", new String[]{String.valueOf(id)});
    }

    public void deleteStock(int id) {
        db.delete(STOCK_TABLE, ID + "= ?", new String[]{String.valueOf(id)});
    }

    public void updateStatus(int id, int status) {
        ContentValues cv = new ContentValues();
        cv.put(STATUS, status);
        db.update(STOCK_TABLE, cv, ID + "= ?", new String[]{String.valueOf(id)});
    }

}