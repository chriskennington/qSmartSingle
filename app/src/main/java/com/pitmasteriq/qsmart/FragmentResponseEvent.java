package com.pitmasteriq.qsmart;

/**
 * Created by Chris on 10/27/2015.
 */
public class FragmentResponseEvent
{
    enum Type
    {
        CONNECTION, CLOSE
    }

    public static final int CONNECT_TO_ADDRESS = 1;
    public static final int APPLICATION_CLOSE = 2;

    private Type type;
    private String stringData = "";



    public FragmentResponseEvent(Type type)
    {
        this.type = type;
    }

    public FragmentResponseEvent(Type type, String data)
    {
        this.type = type;
        this.stringData = data;
    }

    public Type type(){return type;}
    public String stringData(){return stringData;}
}
