package com.pitmasteriq.qsmart.export;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pitmasteriq.qsmart.DataModel;
import com.pitmasteriq.qsmart.DataSource;
import com.pitmasteriq.qsmart.MyApplication;
import com.pitmasteriq.qsmart.Preferences;
import com.pitmasteriq.qsmart.R;
import com.pitmasteriq.qsmart.Temperature;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class SimpleExportActivity extends Activity
{
    private static final String DIRECTORY = "qSmart" + File.separator + "exports";

    private DataSource dataSource;
    private List<DataModel> data;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_export);
        getActionBar().setDisplayHomeAsUpEnabled(true);


        //Get new datasource object
        dataSource = new DataSource(getApplicationContext());

        Button export = (Button)findViewById(R.id.simple_export_export_button);
        Button clear = (Button)findViewById(R.id.simple_export_clear_data);
        TextView exportPath = (TextView)findViewById(R.id.simple_export_path);
        exportPath.setText(String.format(getString(R.string.export_path),
                Environment.getExternalStorageDirectory().getAbsolutePath(), DIRECTORY));


        export.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                exportData();
            }
        });


        clear.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clearData();
            }
        });
    }


    private void exportData()
    {
        if (dataSource.getNumberOfDataEntries() > 0)
        {
            String columnNames = "Date,Time,Pit Set,Pit Temp,Food 1 Temp,Food 2 Temp" + System.getProperty("line.separator");
            refreshData();  //get data from database and load it into the data list

            File outputFile = getOutputFile(); //get file to export data to

            try
            {
                FileWriter fileWriter = new FileWriter(outputFile);
                fileWriter.write(columnNames);
                fileWriter.write(getDataString());
                fileWriter.flush();
                fileWriter.close();

                Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show();

                clearData();
            } catch (IOException e)
            {
                e.printStackTrace();
                Toast.makeText(this, "Data exported failed", Toast.LENGTH_SHORT).show();
            }
        }
        else
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show();
    }

    private void refreshData()
    {
        data = dataSource.getAllData();
    }

    private void clearData()
    {
        if (dataSource.getNumberOfDataEntries() > 0)
            dataSource.clearData();
        else
            Toast.makeText(this, "No data to clear", Toast.LENGTH_SHORT).show();
    }

    private File getOutputFile()
    {
        Calendar c = Calendar.getInstance();
        String date = dateFormat.format(c.getTime());

        String filename = date.replace('/', '.');

        int fileIncrementer = 1;

        String externalStorage = Environment.getExternalStorageDirectory().getAbsolutePath();

        File outputDirectory = new File(externalStorage + File.separator + DIRECTORY );

        if(!outputDirectory.exists()){
            outputDirectory.mkdirs();
        }

        File outputFile = new File(externalStorage + File.separator + DIRECTORY + File.separator + filename+ ".csv");

        while (outputFile.exists())
        {
            outputFile = new File(externalStorage + File.separator + DIRECTORY
                    + File.separator + filename + "(" + fileIncrementer + ").csv");
            fileIncrementer++;
        }

        return outputFile;
    }

    private String getDataString()
    {
        boolean f = PreferenceManager.getDefaultSharedPreferences(MyApplication.getAppContext())
                .getBoolean(Preferences.TEMPERATURE_UNITS, true);

        String dataString = "";
        Log.e("TAG", "size" + data.size());

        for (DataModel d : data)
        {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(d.getDate());

            dataString += dateFormat.format(c.getTime()) + ",";
            dataString += timeFormat.format(c.getTime()) + ",";

            if (f) //export in fahrenheit
            {
                dataString += d.getPitSet() + ",";
                dataString += d.getPitTemp() + ",";
                dataString += d.getFood1Temp() + ",";
                dataString += d.getFood2Temp() + ",";
            }
            else //export in celsius
            {
                dataString += Temperature.f2c(d.getPitSet()) + ",";
                dataString += Temperature.f2c(d.getPitTemp()) + ",";
                dataString += Temperature.f2c(d.getFood1Temp()) + ",";
                dataString += Temperature.f2c(d.getFood2Temp()) + ",";
            }

            dataString += System.getProperty("line.separator");
        }

        return dataString;
    }
}
