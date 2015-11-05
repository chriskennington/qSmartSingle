package com.pitmasteriq.qsmart.export;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import com.pitmasteriq.qsmart.R;

import java.util.ArrayList;
import java.util.List;

public class ColumnsFragment extends Fragment
{

    private String[] columnChoices = new String[]{"Pit Set", "Pit Temp", "Food 1 Temp", "Food 2 Temp"};
    private LinearLayout colContainer;
    private List<CheckBox> columns = new ArrayList<>();

    public ColumnsFragment(){}

    public static ColumnsFragment newInstance()
    {
        ColumnsFragment fragment = new ColumnsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_columns, container, false);

        colContainer = (LinearLayout)v.findViewById(R.id.columnContainer);

        addCheckBoxes();

        return v;
    }

    private void addCheckBoxes()
    {
        for (String s : columnChoices)
        {
            CheckBox c = new CheckBox(getActivity());
            c.setText(s);
            c.setTextSize(25);
            c.setButtonDrawable(R.drawable.checkbox_dark);
            columns.add(c);
            colContainer.addView(c);
        }
    }

    public List<CheckBox> getCheckboxes(){return columns;}
}