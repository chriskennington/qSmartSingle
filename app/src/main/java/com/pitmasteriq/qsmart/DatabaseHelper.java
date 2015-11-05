package com.pitmasteriq.qsmart;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Chris on 11/2/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String TABLE_DATA = "data";
    public static final String COL_ID = "_id";
    public static final String COL_ADDR = "address";
    public static final String COL_DATE = "date";
    public static final String COL_PIT_SET = "pitset";
    public static final String COL_PIT_TEMP = "pittemp";
    public static final String COL_FOOD1_TEMP = "food1temp";
    public static final String COL_FOOD2_TEMP = "food2temp";

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;


    private static final String DATABASE_CREATE = "create table "
            + TABLE_DATA + "("
            + COL_ID + " integer primary key autoincrement, "
            + COL_DATE + " integer not null, "
            + COL_PIT_SET + " integer, "
            + COL_PIT_TEMP + " integer, "
            + COL_FOOD1_TEMP + " integer, "
            + COL_FOOD2_TEMP + " integer);";



    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        Log.w(DatabaseHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DATA);
        onCreate(db);
    }
}
