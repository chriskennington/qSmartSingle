package com.pitmasteriq.qsmartsingle;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Chris on 8/26/2015.
 */
public class TextEditorDialog extends DialogFragment
{
    public static final int DEVICE_NAME = 100;
    public static final int FOOD1_NAME = 101;
    public static final int FOOD2_NAME = 102;

    private DeviceManager deviceManager;

    public TextEditorDialog(){}

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        deviceManager = DeviceManager.get(getActivity().getApplication());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_text_editor_dialog, container, false);
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        final int selector = getArguments().getInt("selector");


        TextView title = (TextView) v.findViewById(R.id.text_editor_title);
        final EditText value = (EditText)v.findViewById(R.id.text_editor_value);
        Button cancel = (Button)v.findViewById(R.id.text_editor_cancel);
        Button ok = (Button)v.findViewById(R.id.text_editor_ok);

        title.setText(getArguments().getString("title"));

        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dismiss();
            }
        });

        ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switch (selector)
                {
                    case DEVICE_NAME:
                        if(value.getText().length() > 0)
                            deviceManager.device().setDefinedName(value.getText().toString());
                        else
                            deviceManager.device().setDefinedName(deviceManager.device().getName());
                        break;
                    case FOOD1_NAME:
                        if(value.getText().length() > 0)
                            deviceManager.device().food1Probe().setName(value.getText().toString());
                        else
                            deviceManager.device().food1Probe().setName("Food 1");
                        break;

                    case FOOD2_NAME:
                        if(value.getText().length() > 0)
                            deviceManager.device().food2Probe().setName(value.getText().toString());
                        else
                            deviceManager.device().food2Probe().setName("Food 2");
                        break;
                }
                deviceManager.saveDevice();
                dismiss();
            }
        });

        return v;
    }
}
