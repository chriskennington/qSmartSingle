package com.pitmasteriq.qsmart.service;

import android.content.Context;
import android.content.Intent;

import com.idevicesinc.sweetblue.BleDevice;
import com.pitmasteriq.qsmart.Console;
import com.pitmasteriq.qsmart.DeviceManager;
import com.pitmasteriq.qsmart.exception.ExceptionHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 3/31/2016.
 */
public class ParseDataRunnable implements Runnable
{
    private static final int TEMPERATURE_OFFSET = 145;
    private static final int NUMBER_OF_ALARM_BITS = 11;

    private ExceptionHelper exceptionHelper = ExceptionHelper.get();
    private DeviceManager deviceManager;

    private String lastFlagSequence = "";
    private Context context;

    private BleDevice device;
    private byte[] data;

    public ParseDataRunnable(Context c, BleDevice device, byte[] data, String lastFlagSequence)
    {
        this.device = device;
        this.data = data;
        this.lastFlagSequence = lastFlagSequence;
        this.context = c.getApplicationContext();

        deviceManager = DeviceManager.get(c.getApplicationContext());
    }

    @Override
    public void run()
    {
        short value;
        List<Short> values = new ArrayList<>();

        values.add((short) data[0]);
        values.add((short) data[1]);

        value = bytesToShort((byte) 0, data[2]);
        values.add((short) ((value == 0) ? 0 : value + TEMPERATURE_OFFSET));


        values.add(bytesToShort(data[3], data[4]));
        values.add(bytesToShort(data[5], data[6]));
        values.add(bytesToShort(data[7], data[8]));

        value = bytesToShort((byte) 0, data[10]);
        values.add((short) ((value == 0) ? 0 : value + TEMPERATURE_OFFSET));

        values.add(bytesToShort((byte) 0, data[11]));
        values.add(bytesToShort((byte) 0, data[12]));
        values.add((short) data[13]);
        values.add(bytesToShort((byte) 0, data[16]));

        value = bytesToShort((byte) 0, data[17]);
        values.add((short) ((value == 0) ? 0 : value + TEMPERATURE_OFFSET));

        values.add(bytesToShort((byte) 0, data[18]));

        value = bytesToShort((byte) 0, data[19]);
        values.add((short) ((value == 0) ? 0 : value + TEMPERATURE_OFFSET));

        values.add(getHighBits((short)data[9]));

        short flagBits = bytesToShort(data[14], data[15]);
        //convert short into boolean array by converting each bit in the
        //short from a 1 to true and 0 to false
        boolean bits[] = new boolean[NUMBER_OF_ALARM_BITS];

        String flagSequence = "";
        for (int i = NUMBER_OF_ALARM_BITS - 1; i >= 0; i--)
        {
            flagSequence += (flagBits & (1 << i));
            bits[i] = (flagBits & (1 << i)) != 0;
        }

        if (!flagSequence.equals(lastFlagSequence))
        {
            Console.d("Flag sequence doesn't match, updating flags");
            lastFlagSequence = flagSequence;
            for (int i = 0; i < bits.length; i++)
            {
                if (bits[i]) //flag is set
                {
                    if (!exceptionHelper.isExceptionActive(i))
                    {
                        exceptionHelper.activateException(i);
                        Console.d("activated " + exceptionHelper.getExceptionMessage(i));
                    }
                }
                else
                {
                    if (exceptionHelper.isExceptionActive(i))
                    {
                        exceptionHelper.deactivateException(i);
                        Console.d("deactivated " + exceptionHelper.getExceptionMessage(i));
                    }
                }
            }

            sendExcetionsUpdatedBroadcast();
        }

        if (values.get(3) == 999) // pit probe error
        {
            if (!exceptionHelper.isExceptionActive(11))
            {
                Console.d("activated " + exceptionHelper.getExceptionMessage(11));
                exceptionHelper.activateException(11);
            }
        }
        else
        {
            if (exceptionHelper.isExceptionActive(11))
            {
                Console.d("deactivated " + exceptionHelper.getExceptionMessage(11));
                exceptionHelper.deactivateException(11);
            }
        }

        deviceManager.updateValues(values);
    }

    private short getHighBits(short value)
    {
        int high = (value & 0XF0) >> 4;
        return (short) high;
    }

    private short bytesToShort(byte i, byte j)
    {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(j);
        bb.put(i);
        return bb.getShort(0);
    }

    private void sendExcetionsUpdatedBroadcast()
    {
        //sequence
        final Intent intent = new Intent(BluetoothService.EXCEPTIONS_UPDATED);
        intent.putExtra("sequence", lastFlagSequence);
        context.sendBroadcast(intent);
    }
}
