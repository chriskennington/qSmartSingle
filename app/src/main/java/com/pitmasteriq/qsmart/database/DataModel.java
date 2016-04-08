package com.pitmasteriq.qsmart.database;

/**
 * Created by Chris on 11/2/2015.
 */
public class DataModel
{
    private long id;
    private long date;
    private String address;
    private int pitSet;
    private int pitTemp;
    private int food1Temp;
    private int food2Temp;

    public long getId() {return id;}
    public void setId(long id) {this.id = id;}

    public long getDate() {return date;}
    public void setDate(long date) {this.date = date;}

    public String getAddress() {return address;}
    public void setAddress(String address){this.address = address;}

    public int getFood1Temp(){return food1Temp;}
    public void setFood1Temp(int food1Temp) {this.food1Temp = food1Temp;}

    public int getFood2Temp(){return food2Temp;}
    public void setFood2Temp(int food2Temp){this.food2Temp = food2Temp;}

    public int getPitSet(){return pitSet;}
    public void setPitSet(int pitSet){this.pitSet = pitSet;}

    public int getPitTemp(){return pitTemp;}
    public void setPitTemp(int pitTemp){this.pitTemp = pitTemp;}
}
