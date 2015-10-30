package com.pitmasteriq.qsmart;

import android.content.Intent;

/**
 * Created by Chris on 10/28/2015.
 */
public class ParameterEditedEvent
{
    int resultCode;
    Intent data;

    public ParameterEditedEvent(int resultCode, Intent data)
    {
        this.resultCode = resultCode;
        this.data = data;
    }

    public int resultCode(){return resultCode;}
    public Intent data(){return data;}
}
