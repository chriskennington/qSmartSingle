package com.pitmasteriq.qsmart;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class ScanFragment extends DialogFragment
{
    private ScanClickListener listener;

    private List<String> addresses;

    private ListView list;
    private RowAdapter rowAdapter;

    public ScanFragment(){}

    public interface ScanClickListener
    {
        void onScanClick(String address);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scan, container, false);
        this.getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        addresses = ScannedDevices.get().refreshList();
        String currentAddress = getArguments().getString("address");

        if(currentAddress != null )
            addresses.remove(currentAddress);

        TextView noDevices = (TextView) v.findViewById(R.id.available_devices_none);
        if( addresses.size() > 0  )
            noDevices.setVisibility(View.GONE);

        list = (ListView)v.findViewById(R.id.available_devices_list);
        rowAdapter = new RowAdapter(getActivity(), addresses);
        list.setAdapter(rowAdapter);
        list.setOnItemClickListener(onClick);


        return v;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (ScanClickListener) activity;
        } catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement InfoFragmentListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        listener = null;
    }


    private AdapterView.OnItemClickListener onClick = new AdapterView.OnItemClickListener()
    {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, final int position, long id)
        {
            listener.onScanClick(addresses.get(position));
            getDialog().dismiss();
        }
    };

    private class RowAdapter extends ArrayAdapter<String>
    {
        private final Context context;
        private final List<String> a;

        public RowAdapter(Context context, List<String> addresses)
        {
            super(context, R.layout.row_available_devices, addresses);

            this.context = context;
            this.a = addresses;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            //Create inflater
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            convertView = inflater.inflate(R.layout.row_available_devices, parent, false);

            TextView name = (TextView) convertView.findViewById(R.id.available_devices_row_name);
            TextView addr = (TextView) convertView.findViewById(R.id.available_devices_row_address);

            String s = a.get(position).replace(":", "");
            String n = "pmIQ-IQ130-" + s.substring(s.length()-4);

            name.setText(n);
            addr.setText(a.get(position));

            return convertView;
        }
    }




}
