package com.pitmasteriq.qsmart;

/**
 * Created by Chris on 10/27/2015.
 */
public class FragmentResponseEvent
{
    public static final int CONNECT_TO_ADDRESS = 1;
    public static final int APPLICATION_CLOSE = 2;

    private int type = 0;
    private String stringData = "";



    public FragmentResponseEvent(int type)
    {
        this.type = type;
    }

    public FragmentResponseEvent(int type, String data)
    {
        this.type = type;
        this.stringData = data;
    }

    public int type(){return type;}
    public String stringData(){return stringData;}
}
