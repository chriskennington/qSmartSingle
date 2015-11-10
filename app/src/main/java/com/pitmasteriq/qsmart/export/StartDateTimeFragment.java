package com.pitmasteriq.qsmart.export;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.pitmasteriq.qsmart.DataModel;
import com.pitmasteriq.qsmart.DataSource;
import com.pitmasteriq.qsmart.DatePickerFragment;
import com.pitmasteriq.qsmart.R;

import java.util.Calendar;
import java.util.List;


public class StartDateTimeFragment extends Fragment
{
    private DataSource dataSource;
    private List<DataModel> data;

    private TextView startDate;
    private TimePicker startTime;

    public StartDateTimeFragment(){}

    public static StartDateTimeFragment newInstance()
    {
        StartDateTimeFragment fragment = new StartDateTimeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_date_time, container, false);

        startDate = (TextView)v.findViewById(R.id.export_start_date);

        startTime = (TimePicker)v.findViewById(R.id.export_start_time);
        startTime.setCurrentHour(0);
        startTime.setCurrentMinute(0);

        startDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openDatePickerFragment(0);
            }
        });


        dataSource = new DataSource(getActivity().getApplicationContext());
        refreshData();

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    private void openDatePickerFragment(int selector)
    {
        if (data.size() > 0)
        {
            FragmentTransaction ft = getFragmentManager().beginTransaction();

            DatePickerFragment frag = new DatePickerFragment();

            Bundle args = new Bundle();
            args.putInt("selector", selector);
            args.putLong("minDate", data.get(data.size()-1).getDate());
            args.putLong("maxDate", data.get(0).getDate());

            frag.setArguments(args);

            frag.show(ft, "dialog");
        }
        else
        {
            Toast.makeText(getActivity(), "No data avaialable", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshData()
    {
        dataSource.open();
        data = dataSource.getAllData();
        dataSource.close();
    }

    public void setDate(String s){startDate.setText(s);}

    public String getDate()
    {
        return startDate.getText().toString();
    }


    public long getDateTime()
    {

        String[] dates = startDate.getText().toString().split("/");
        Calendar c = Calendar.getInstance();

        //year month day hour min
        c.set(Integer.parseInt(dates[2]), Integer.parseInt(dates[0]), Integer.parseInt(dates[1]),
                startTime.getCurrentHour(), startTime.getCurrentMinute());

        return c.getTimeInMillis();
    }

}
