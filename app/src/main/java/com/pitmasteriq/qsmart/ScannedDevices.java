package com.pitmasteriq.qsmart;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 7/27/2015.
 */
public class ScannedDevices
{
    private static ScannedDevices instance = null;
    private static boolean hasNew = false;

    private static List<Address> addresses;


    public static ScannedDevices get()
    {
        if (instance == null)
            instance = new ScannedDevices();

        return instance;
    }

    public ScannedDevices()
    {
        addresses = new ArrayList<>();
    }

    public void addressDiscovered(String address)
    {
        hasNew = true;
        addresses.add(new Address(address, System.currentTimeMillis()));
    }

    public void addressRediscovered(String address)
    {
        int index = findIndex(address);
        if (index != -1)
            addresses.get(index).updateTime(System.currentTimeMillis());
        else
        {
            hasNew = true;
            addresses.add(new Address(address, System.currentTimeMillis()));
        }
    }

    public void addressUndiscovered(String address)
    {
        int index = findIndex(address);
        if (index != -1)
            addresses.remove(index);
    }

    public List<String> refreshList()
    {
        List<Address> toRemove = new ArrayList<>();

        for (Address a : addresses)
            if (a.shouldBeRemoved())
                toRemove.add(a);

        addresses.removeAll(toRemove);

        List<String> toReturn = new ArrayList<>();

        for (Address a : addresses)
            toReturn.add(a.getAddress());


        return toReturn;
    }

    private int findIndex(String address)
    {
        for(Address a : addresses)
        {
            if(a.getAddress().equals(address))
                return addresses.indexOf(a);
        }
        return -1;
    }

    public void clearHasNew(){ hasNew = false; }

    public boolean hasNew()
    {
        return hasNew;
    }


    private class Address
    {
        private static final long TIMEOUT = 5000;

        private String address;
        private long time;

        public Address(String address, long time)
        {
            this.address = address;
            this.time = time;
        }

        public String getAddress() { return address; }

        public void updateTime(long time) { this.time = time; }
        public boolean shouldBeRemoved() { return (System.currentTimeMillis() - time) > TIMEOUT; }
    }
}
