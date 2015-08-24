package com.pitmasteriq.qsmartsingle;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Chris on 5/11/2015.
 */
public class MessageDialog extends DialogFragment
{
    public static MessageDialog newInstance(String title, String message)
    {
        MessageDialog frag = new MessageDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        frag.setArguments(args);
        return frag;
    }

    public static MessageDialog newInstance(String title, String message, String btnOk)
    {
        MessageDialog frag = new MessageDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putString("btnOk", btnOk);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        String btnOk = getArguments().getString("btnOk");

        String button = "OK";

        if( btnOk != null )
            button = btnOk;

        return new AlertDialog.Builder(getActivity())
            .setTitle(getArguments().getString("title"))
            .setMessage(getArguments().getString("message"))
            .setCancelable(false)
            .setPositiveButton(button,
                    new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int whichButton)
                        {
                            // Positive button clicked
                            dismiss();
                        }
                    }
            )
            .create();
    }
}
