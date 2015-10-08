package com.pitmasteriq.qsmart;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Chris on 10/8/2015.
 */
public class DatabaseHelper extends SQLiteOpenHelper
{
    public static final String TABLE_DATA = "data";
    public static final String COL_ID = "_id";
    public static final String COL_DATE = "date";
    public static final String COL_TIME = "time";

    private static final String DATABASE_NAME = "data.db";
    private static final int DATABASE_VERSION = 1;


    private static final String DATABASE_CREATE = "create table "
            + TABLE_DATA + "("
            + COL_ID + " integer primary key autoincrement, "
            + COL_DATE + "text not null, "
            + COL_TIME + "text not null);";



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
