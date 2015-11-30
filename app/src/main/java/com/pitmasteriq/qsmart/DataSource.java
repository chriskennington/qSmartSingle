package com.pitmasteriq.qsmart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 11/2/2015.
 */
public class DataSource
{
    private SQLiteDatabase database;
    private DatabaseHelper dbHelper;

    public DataSource(Context context)
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

    public void storeDataString()
    {
        Device d = DeviceManager.get(MyApplication.getAppContext()).device();

        if (d == null)
            return;

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_DATE, System.currentTimeMillis());
        values.put(DatabaseHelper.COL_ADDR, d.getAddress());
        values.put(DatabaseHelper.COL_PIT_SET, d.config().pitSet().getRawTemp());
        values.put(DatabaseHelper.COL_PIT_TEMP, d.pitProbe().temperature().getRawTemp());
        values.put(DatabaseHelper.COL_FOOD1_TEMP, d.food1Probe().temperature().getRawTemp());
        values.put(DatabaseHelper.COL_FOOD2_TEMP, d.food2Probe().temperature().getRawTemp());

        long result = database.insert(DatabaseHelper.TABLE_DATA, null, values);
        if (result == -1)
            Log.e("TAG", "insert failed");
    }

    public void deleteDataString(int id)
    {
        long result = database.delete(DatabaseHelper.TABLE_DATA, DatabaseHelper.COL_ID + "=" + id, null);
    }

    public List<DataModel> getAllData()
    {
        List<DataModel> storedData = new ArrayList<>();

        String query = "SELECT * FROM " + DatabaseHelper.TABLE_DATA
                + " ORDER BY " + DatabaseHelper.COL_DATE + " DESC";

        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            DataModel sdm = cursorToDataModel(cursor);
            storedData.add(sdm);
            cursor.moveToNext();
        }

        cursor.close();
        return storedData;
    }

    public List<DataModel> getDataInRange(String address, long start, long end)
    {
        List<DataModel> storedData = new ArrayList<>();
        String query;


        if (address == null)
        {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_DATA
                    + " WHERE " + DatabaseHelper.COL_DATE + " >= " + start
                    + " AND " + DatabaseHelper.COL_DATE + " <= " + end
                    + " ORDER BY " + DatabaseHelper.COL_DATE + " DESC";
        }
        else
        {
            query = "SELECT * FROM " + DatabaseHelper.TABLE_DATA
                    + " WHERE " + DatabaseHelper.COL_DATE + " >= " + start
                    + " AND " + DatabaseHelper.COL_DATE + " <= " + end
                    + " AND " + DatabaseHelper.COL_ADDR + " = '" + address + "'"
                    + " ORDER BY " + DatabaseHelper.COL_DATE + " DESC";
        }

        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            DataModel sdm = cursorToDataModel(cursor);
            storedData.add(sdm);
            cursor.moveToNext();
        }

        cursor.close();
        return storedData;
    }

    private DataModel cursorToDataModel(Cursor cursor)
    {
        DataModel data = new DataModel();

        data.setId(cursor.getLong(0));
        data.setDate(Long.parseLong(cursor.getString(1)));
        data.setAddress(cursor.getString(2));
        data.setPitSet(cursor.getInt(3));
        data.setPitTemp(cursor.getInt(4));
        data.setFood1Temp(cursor.getInt(5));
        data.setFood2Temp(cursor.getInt(6));

        return data;
    }
}
