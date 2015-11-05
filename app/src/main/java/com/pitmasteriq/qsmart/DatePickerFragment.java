package com.pitmasteriq.qsmart;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;

/**
 * Created by Chris on 11/2/2015.
 */
public class DatePickerFragment extends DialogFragment
{

    private DatePicker datePicker;
    private DatePickedListener listener;

    private int selector = -1;
    private long minDate;
    private long maxDate;


    //required empty constructor
    public DatePickerFragment(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        View v = inflater.inflate(R.layout.fragment_date_picker, container);

        selector = getArguments().getInt("selector");
        minDate = getArguments().getLong("minDate");
        maxDate = getArguments().getLong("maxDate");

        if (selector == 0)
            getDialog().setTitle("Select Start Date");

        if (selector == 1)
            getDialog().setTitle("Select End Date");

        datePicker = (DatePicker)v.findViewById(R.id.datePicker);
        datePicker.setMinDate(minDate);
        datePicker.setMaxDate(maxDate);

        Button select = (Button)v.findViewById(R.id.fragment_date_picker_select);
        select.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onDateSelected(datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear(), selector);
                dismiss();
            }
        });

        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (DatePickedListener) activity;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement listener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }
}
