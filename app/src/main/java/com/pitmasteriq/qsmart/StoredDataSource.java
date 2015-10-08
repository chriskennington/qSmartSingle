package com.pitmasteriq.qsmart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Chris on 10/8/2015.
 */
public class StoredDataSource
{
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;
    private String[] allColumns = {DatabaseHelper.COL_ID, DatabaseHelper.COL_DATE, DatabaseHelper.COL_TIME};

    public StoredDataSource(Context context)
    {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() throws SQLException
    {
        database = dbHelper.getWritableDatabase();
    }

    public void close()
    {
        dbHelper.close();
    }

    //TODO finish added data to table and models!
    public void createDataString(List<Short> rawData)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat stf = new SimpleDateFormat("HH:mm");
        Date date = new Date();

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_DATE, sdf.format(date));
        values.put(DatabaseHelper.COL_TIME, stf.format(date));

        long result = database.insert(DatabaseHelper.TABLE_DATA, null, values);
    }

    public void deleteDataString(int id)
    {
        long result = database.delete(DatabaseHelper.TABLE_DATA, DatabaseHelper.COL_ID + "=" + id, null);
    }

    public List<StoredDataModel> getAllData()
    {
        List<StoredDataModel> storedData = new ArrayList<>();

        Cursor cursor = database.query(DatabaseHelper.TABLE_DATA, allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            StoredDataModel sdm = cursorToDataModel(cursor);
            storedData.add(sdm);
            cursor.moveToNext();
        }

        cursor.close();
        return storedData;
    }

    private StoredDataModel cursorToDataModel(Cursor cursor)
    {
        StoredDataModel data = new StoredDataModel();

        data.setId(cursor.getLong(0));
        data.setDate(cursor.getString(1));
        data.setTime(cursor.getString(2));

        return data;
    }
}
