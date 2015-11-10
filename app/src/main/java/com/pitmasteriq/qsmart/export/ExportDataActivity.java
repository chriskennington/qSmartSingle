package com.pitmasteriq.qsmart.export;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.pitmasteriq.qsmart.DataModel;
import com.pitmasteriq.qsmart.DataSource;
import com.pitmasteriq.qsmart.DatePickedListener;
import com.pitmasteriq.qsmart.MessageDialog;
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

public class ExportDataActivity extends ActionBarActivity implements ActionBar.TabListener, DatePickedListener
{
    private static final String DIRECTORY = "qSmart" + File.separator + "exports";

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    private StartDateTimeFragment startDateTimeFragment;
    private EndDateTimeFragment endDateTimeFragment;
    private ColumnsFragment columnsFragment;

    private List<CheckBox> columns;

    private DataSource dataSource;
    private List<DataModel> data;
    private String[]  dataTitles;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_data);

        dataTitles = getResources().getStringArray(R.array.exportValues);

        startDateTimeFragment = StartDateTimeFragment.newInstance();
        endDateTimeFragment = EndDateTimeFragment.newInstance();
        columnsFragment = ColumnsFragment.newInstance();

        dataSource = new DataSource(getApplicationContext());
        refreshData();

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);


        // Set up the action bar.
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(android.support.v7.app.ActionBar.NAVIGATION_MODE_TABS);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener()
        {
            @Override
            public void onPageSelected(int position)
            {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++)
        {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
    }



    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction)
    {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onDateSelected(int day, int month, int year, int selector)
    {
        switch (selector)
        {
            case 0:
                startDateTimeFragment.setDate(month + "/" + day + "/" + year);
                break;

            case 1:
                endDateTimeFragment.setDate(month + "/" + day + "/" + year);
                break;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter
    {

        public SectionsPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            switch (position)
            {
                case 0:
                    return startDateTimeFragment;
                case 1:
                    return endDateTimeFragment;
                case 2:
                    return columnsFragment;
            }
            return null;
        }

        @Override
        public int getCount()
        {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "Start Date/Time";
                case 1:
                    return "End Date/Time";
                case 2:
                    return "Values";
            }
            return null;
        }
    }

    public void graphData(View v)
    {
        columns = columnsFragment.getCheckboxes();

        //if we cant export the data, end attempt
        if (!canExportData())
            return;

        boolean[] values = new boolean[columns.size()];

        for (CheckBox c : columns)
        {
            if (c.isChecked())
                values[columns.indexOf(c)] = true;
            else
                values[columns.indexOf(c)] = false;
        }

        Intent i = new Intent(this, GraphActivity.class);
        i.putExtra("startTime", startDateTimeFragment.getDateTime());
        i.putExtra("endTime", endDateTimeFragment.getDateTime());
        i.putExtra("values", values);

        startActivity(i);
    }

    public void exportData(View v)
    {
        columns = columnsFragment.getCheckboxes();

        //if we cant export the data, end attempt
        if (!canExportData())
            return;

        refreshData(startDateTimeFragment.getDateTime(), endDateTimeFragment.getDateTime());

        File outputFile = getOutputFile();

        try
        {
            FileWriter fileWriter = new FileWriter(outputFile);
            fileWriter.write(getHeadersString());
            fileWriter.write(getDataString());
            fileWriter.flush();
            fileWriter.close();

            Toast.makeText(this, "File exported to " + outputFile.getAbsolutePath(),Toast.LENGTH_LONG).show();
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    private File getOutputFile()
    {
        String filename = startDateTimeFragment.getDate().replace('/', '.') + " - "
                + endDateTimeFragment.getDate().replace('/', '.');
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

    public void openOutputFolder(View v)
    {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath()
                + DIRECTORY);
        intent.setDataAndType(uri, "resource/folder");
        startActivity(intent);
    }

    private String getHeadersString()
    {
        String header = "Date,Time,";
        for (CheckBox c : columns)
            if (c.isChecked())
                header += c.getText() + ",";

        header += System.getProperty("line.separator");
        return header;
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
                if (columns.get(0).isChecked())
                    dataString += d.getPitSet() + ",";

                if (columns.get(1).isChecked())
                    dataString += d.getPitTemp() + ",";

                if (columns.get(2).isChecked())
                    dataString += d.getFood1Temp() + ",";

                if (columns.get(3).isChecked())
                    dataString += d.getFood2Temp() + ",";
            }
            else //export in celsius
            {
                if (columns.get(0).isChecked())
                    dataString += Temperature.f2c(d.getPitSet()) + ",";

                if (columns.get(1).isChecked())
                    dataString += Temperature.f2c(d.getPitTemp()) + ",";

                if (columns.get(2).isChecked())
                    dataString += Temperature.f2c(d.getFood1Temp()) + ",";

                if (columns.get(3).isChecked())
                    dataString += Temperature.f2c(d.getFood2Temp()) + ",";
            }

            dataString += System.getProperty("line.separator");
        }

        return dataString;
    }

    private void refreshData()
    {
        dataSource.open();
        data = dataSource.getAllData();
        dataSource.close();
    }

    private void refreshData(long start, long end)
    {
        dataSource.open();
        data = dataSource.getDataInRange(start, end);
        dataSource.close();
    }

    private boolean hasSelectedValues()
    {
        for (CheckBox c : columns)
            if (c.isChecked())
                return true;

        return false;
    }

    private boolean canExportData()
    {
        if (startDateTimeFragment.getDate().length() == 0)
        {
            MessageDialog md = MessageDialog.newInstance("Export Failed", "Please select a starting date");
            md.show(getFragmentManager(), "dialog");
            mViewPager.setCurrentItem(0, true);
            return false;
        }

        if (endDateTimeFragment.getDate().length() == 0)
        {
            MessageDialog md = MessageDialog.newInstance("Export Failed", "Please select an ending date");
            md.show(getFragmentManager(), "dialog");
            mViewPager.setCurrentItem(1, true);
            return false;
        }

        if (!hasSelectedValues())
        {
            MessageDialog md = MessageDialog.newInstance("Export Failed", "Please select values to export");
            md.show(getFragmentManager(), "dialog");
            return false;
        }

        return true;
    }


    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction){}
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction){}
}
