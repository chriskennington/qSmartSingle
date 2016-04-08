package com.pitmasteriq.qsmart;

import com.idevicesinc.sweetblue.BleDevice;

import java.util.HashMap;

/**
 * Created by Chris on 3/22/2016.
 */
public class ConnectionHandler
{
    public static final int STATE_DISCONNECTING = 0;
    public static final int STATE_DISCONNECTED = 1;
    public static final int STATE_UNINTENTIONAL_DISCONNECT = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_CONNECTED = 4;
    public static final int STATE_BONDING = 5;
    public static final int STATE_BONDED = 6;


    private static ConnectionHandler instance = null;

    private int state = 1;
    private HashMap<Long, Integer> connectionStateHistory = new HashMap<>();
    private BleDevice device = null;
    private long lastDataUpdateTime = 0L;

    public static ConnectionHandler get()
    {
        if (instance == null)
            instance = new ConnectionHandler();

        return instance;
    }

    private ConnectionHandler()
    {

    }

    public int getState() { return state; }
    public void setState(int state) { updateConnectionState(state); }
    public void setState(int state, BleDevice device) { updateConnectionState(state, device); }
    public boolean isConnected() { return (state > 2); }
    public String getAddress() { return device.getMacAddress(); }
    public BleDevice getDevice() { return device; }
    public void setLastDataUpdateTime(long time) { this.lastDataUpdateTime = time; }


    public void intentionalDisconnect()
    {
        clearConnectionHistory();
        device = null;
    }


    private void clearConnectionHistory()
    {
        connectionStateHistory.clear();
    }

    private void updateConnectionState(int state)
    {
        this.state = state;
        connectionStateHistory.put(System.currentTimeMillis(), state);
    }

    private void updateConnectionState(int state, BleDevice device)
    {
        this.state = state;
        this.device = device;
        connectionStateHistory.put(System.currentTimeMillis(), state);
    }
}
