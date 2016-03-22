package com.pitmasteriq.qsmart;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 11/2/2015.
 */
public class DataSource
{
    private static final int ROWS_TO_SAVE = 1440;

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
        open();

        Device d = null;

        try
        {
           d = DeviceManager.get(MyApplication.getAppContext()).device();
        }
        catch(NullDeviceException e){e.printStackTrace();}

        if (d == null)
            return;

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_DATE, System.currentTimeMillis());
        values.put(DatabaseHelper.COL_ADDR, d.getAddress());

        if (d.config().pitSet().getRawTemp() != 999)
            values.put(DatabaseHelper.COL_PIT_SET, d.config().pitSet().getRawTemp());
        else
            values.put(DatabaseHelper.COL_PIT_SET, 0);

        if (d.pitProbe().temperature().getRawTemp() != 999)
            values.put(DatabaseHelper.COL_PIT_TEMP, d.pitProbe().temperature().getRawTemp());
        else
            values.put(DatabaseHelper.COL_PIT_TEMP, 0);

        if (d.food1Probe().temperature().getRawTemp() != 999)
            values.put(DatabaseHelper.COL_FOOD1_TEMP, d.food1Probe().temperature().getRawTemp());
        else
            values.put(DatabaseHelper.COL_FOOD1_TEMP, 0);

        if (d.food2Probe().temperature().getRawTemp() != 999)
            values.put(DatabaseHelper.COL_FOOD2_TEMP, d.food2Probe().temperature().getRawTemp());
        else
            values.put(DatabaseHelper.COL_FOOD2_TEMP, 0);

        if (getNumberOfDataRows() < ROWS_TO_SAVE)
        {
            Console.d("Database: ADDING ROW");

            long result = database.insert(DatabaseHelper.TABLE_DATA, null, values);

            if (result == -1)
                Console.e("Database: Insert failed");
        }
        else
        {
            Console.d("Database: UPDATING ROW");

            int result = database.update(DatabaseHelper.TABLE_DATA, values, DatabaseHelper.COL_DATE + "= (SELECT min(" + DatabaseHelper.COL_DATE + ") FROM "+ DatabaseHelper.TABLE_DATA +")", null);
            if (result == 0)
                Console.e("Database: update failed");
        }

        close();
    }

    public void clearData()
    {
        open();

        String query = "DELETE FROM " + DatabaseHelper.TABLE_DATA;
        database.execSQL(query);

        close();
    }

    public List<DataModel> getAllData()
    {
        open();

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

        close();
        return storedData;
    }

    public List<DataModel> getDataInRange(String address, long start, long end)
    {
        open();

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

        close();
        return storedData;
    }

    private int getNumberOfDataRows()
    {
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_DATA;
        Cursor cursor = database.rawQuery(query, null);

        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public int getNumberOfDataEntries()
    {
        open();
        int count = getNumberOfDataRows();
        close();

        return count;
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
