package com.pitmasteriq.qsmart;

import android.content.Intent;

/**
 * Created by Chris on 5/11/2015.
 */
public interface DialogResultListener
{
    void onDialogResult(int requestCode, int resultCode, Intent data);
}
