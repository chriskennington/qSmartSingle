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

public class EndDateTimeFragment extends Fragment
{

    private DataSource dataSource;
    private List<DataModel> data;

    private TextView endDate;
    private TimePicker endTime;

    public EndDateTimeFragment(){}

    public static EndDateTimeFragment newInstance()
    {
        EndDateTimeFragment fragment = new EndDateTimeFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_end_date_time, container, false);

        endDate = (TextView)v.findViewById(R.id.export_end_date);

        endTime = (TimePicker)v.findViewById(R.id.export_end_time);
        endTime.setCurrentHour(23);
        endTime.setCurrentMinute(59);

        endDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                openDatePickerFragment(1);
            }
        });

        dataSource = new DataSource(getActivity().getApplicationContext());
        refreshData();

        return v;
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

    public void setDate(String s){endDate.setText(s);}

    public String getDate()
    {
        return endDate.getText().toString();
    }

    public long getDateTime()
    {
        String[] dates = endDate.getText().toString().split("/");
        Calendar c = Calendar.getInstance();

        //year month day hour min
        c.set(Integer.parseInt(dates[2]), Integer.parseInt(dates[0]), Integer.parseInt(dates[1]),
                endTime.getCurrentHour(), endTime.getCurrentMinute());

        return c.getTimeInMillis();
    }
}
