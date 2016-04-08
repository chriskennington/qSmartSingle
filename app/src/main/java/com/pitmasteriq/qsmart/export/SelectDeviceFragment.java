package com.pitmasteriq.qsmart.export;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.pitmasteriq.qsmart.database.DataModel;
import com.pitmasteriq.qsmart.database.DataSource;
import com.pitmasteriq.qsmart.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectDeviceFragment extends Fragment
{

    private DataSource dataSource;
    private List<DataModel> data;
    private List<String> addr = new ArrayList<String>();
    private Set<String> addresses = new HashSet<>();
    ArrayAdapter<String> adapter;

    private Spinner spinner;

    public SelectDeviceFragment(){}

    public static SelectDeviceFragment newInstance()
    {
        SelectDeviceFragment fragment = new SelectDeviceFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_select_device, container, false);

        spinner = (Spinner)v.findViewById(R.id.export_device_address);
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, addr);
        spinner.setAdapter(adapter);

        dataSource = new DataSource(getActivity().getApplicationContext());
        refreshData();

        buildListOfAddresses();
        addr.addAll(addresses);

        adapter.notifyDataSetChanged();

        return v;
    }

    private void buildListOfAddresses()
    {
        for (DataModel d : data)
        {
            addresses.add(d.getAddress());
        }
    }

    private void refreshData()
    {
        dataSource.open();
        data = dataSource.getAllData();
        dataSource.close();
    }

    public String getSelectedAddress()
    {
        return addr.get(spinner.getSelectedItemPosition());
    }
}
