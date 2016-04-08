package com.pitmasteriq.qsmart;


import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.utils.Interval;
import com.pitmasteriq.qsmart.service.BluetoothService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class ScanFragment extends DialogFragment
{
    private FragmentResponseListener listener;
    private static final int SCAN_TIME = 2000;
    private BleManager bleManager;

    private HashSet<String> addressSet = new HashSet<>();
    private ArrayList<String> addressList = new ArrayList<>();

    private ListView list;
    private RowAdapter rowAdapter;

    public ScanFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_scan, container, false);
        this.getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        bleManager = BleManager.get(getActivity().getApplicationContext());

        list = (ListView)v.findViewById(R.id.available_devices_list);
        rowAdapter = new RowAdapter(getActivity(), addressList);
        list.setAdapter(rowAdapter);
        list.setOnItemClickListener(onClick);

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        startScanning();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            listener = (FragmentResponseListener) activity;
        } catch (ClassCastException e)
        {

            throw new ClassCastException(activity.toString() + " must implement FragmentListener");
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
            Console.v("Clicked on " + addressList.get(position));
            listener.onFragmentResponse(new FragmentResponseEvent
                    (FragmentResponseEvent.Type.CONNECTION, addressList.get(position)));
            close();
        }
    };

    private void sendBroadcast(String address)
    {
        final Intent intent = new Intent(BluetoothService.ACTION_CONNECT);
        intent.putExtra("address", address);
        getActivity().sendBroadcast(intent);
    }

    private void close()
    {
        this.getDialog().dismiss();
    }

    private void startScanning()
    {
        bleManager.startScan(Interval.millis(SCAN_TIME), new BleManager.DiscoveryListener()
        {
            @Override
            public void onEvent(DiscoveryEvent e)
            {
                //if name does not match expression assume it is not an IQ and return
                if (!e.device().getName_native().toUpperCase().matches("[I][Q][A-F0-9]{4}"))
                    return;

                if (e.was(LifeCycle.DISCOVERED) || e.was(LifeCycle.REDISCOVERED))
                {
                    //if this is a device that is not already in the list
                    if (addressSet.add(e.device().getMacAddress()))
                    {
                        Console.i("discovered " + e.device().getMacAddress());
                        addressList.add(e.device().getMacAddress());

                        rowAdapter.notifyDataSetChanged();
                    }
                }

                if (e.was(LifeCycle.UNDISCOVERED))
                {
                    //remove this address if it was undiscovered
                    if (addressSet.remove(e.device().getMacAddress()))
                    {
                        Console.i("lost discovery of " + e.device().getMacAddress());
                        addressList.remove(e.device().getMacAddress());

                        rowAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

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
