package org.giasalfeusi.android.blen;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Observer;
import java.util.UUID;

/**
 * Created by salvy on 17/03/17.
 */

public class Orchestrator {
    static final private String TAG = "STagOrch";
    static Orchestrator sto;

    private DeviceScanCallBack deviceScanCallBack;
    private DeviceHost deviceHost;
    private Context context; // AAARGH!
    private HashMap<BluetoothDevice, DeviceWithObservers> deviceWithObserversMap;

    static public Orchestrator singleton()
    {
        /* Ensure used objects are created too */
        DevicesList.singleton();
        DeviceBook.singleton();

        if (sto == null) {
            sto = new Orchestrator();
        }

        return sto;
    }

    private Orchestrator()
    {
        deviceScanCallBack = new DeviceScanCallBack();
        deviceWithObserversMap = new HashMap<BluetoothDevice, DeviceWithObservers>();
    }

    public void setContext(Context _context)
    {
        context = _context;
        if (deviceHost == null && _context != null) {
            deviceHost = new DeviceHost(_context);
        }
    }

    public DeviceHost getDeviceHost() { return deviceHost; }

    public void startScan()
    {
        deviceHost.startScan(deviceScanCallBack);
    }

    public void stopScan()
    {
        deviceHost.stopScan(deviceScanCallBack);
    }

    public void connectToDevice(BluetoothDevice blueDev)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null)
        {
            blueDev.connectGatt(context, false, dwo);
            stopScan();
        }
    }

    public void disconnectFromDevice(BluetoothDevice blueDev)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null)
        {
            if (dwo.getGattConnection() != null)
            {
                // .close() does not generate GattCallback
                dwo.getGattConnection().disconnect();
            }
        }
    }

    public List<BluetoothGattCharacteristic> getCharacteristicsList(BluetoothDevice blueDev)
    {
        List<BluetoothGattCharacteristic> ret = null;
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null) {
            ret = dwo.getCharacteristicsList();
        }
        return ret;
    }

    public void observeDevice(BluetoothDevice blueDev, Observer observer)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo == null)
        {
            dwo = new DeviceWithObservers(blueDev);
            deviceWithObserversMap.put(blueDev, dwo);
        }
        Log.i(TAG, String.format("addObserve %s<=%s:", blueDev.getAddress(), observer));
        dwo.addObserver(observer);
    }

    public void noMoreObserveDevice(BluetoothDevice blueDev, Observer observer)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null)
        {
            Log.i(TAG, String.format("noMoreObserve %s<=%s:", blueDev.getAddress(), observer));
            dwo.deleteObserver(observer);
            //deviceWithObserversMap.remove(blueDev);
        }
    }

    public boolean readCharacteristic(BluetoothDevice blueDev, BluetoothGattCharacteristic ch)
    {
        boolean ret = false;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null && dwo.getGattConnection() != null) {
            ret = dwo.getGattConnection().readCharacteristic(ch);
            if (!ret) {
                Log.e(TAG, String.format("readChar Failed for dev %s %s", blueDev.getAddress(), ch.getUuid()));
            }
        }

        return ret;
    }

    /* TODO: Add service as parameter? */
    public List<BluetoothGattCharacteristic> getCharacteristics(BluetoothDevice blueDev)
    {
        List<BluetoothGattCharacteristic> ret = null;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        Log.d(TAG, String.format("getChList dwo = %s", dwo));
        if (dwo != null /*&& dwo.getGattConnection() != null*/)
        {
            ret = dwo.getCharacteristicsList();
        }

        return ret;
    }

    public BluetoothGattCharacteristic getCharacteristic(BluetoothDevice blueDev, String uuid)
    {
        BluetoothGattCharacteristic ret = null;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        Log.d(TAG, String.format("getChar dwo = %s", dwo));
        if (dwo != null && dwo.getGattConnection() != null) {
            /* Lookup Characteristic */
            for (BluetoothGattCharacteristic gattCh: dwo.getCharacteristicsList())
            {
                if (gattCh.getUuid().compareTo(UUID.fromString(uuid)) == 0)
                {
                    ret = gattCh;
                }
            }
        }

        return ret;
    }

    public boolean readCharacteristic(BluetoothDevice blueDev, String uuid) {

        boolean ret = false;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null && dwo.getGattConnection() != null) {
            /* Lookup Characteristic */
            BluetoothGattCharacteristic gattCh = getCharacteristic(blueDev, uuid);

            if (gattCh != null) {
                ret = dwo.getGattConnection().readCharacteristic(gattCh);
                if (!ret) {
                    Log.e(TAG, String.format("readChar Failed for dev %s %s", blueDev.getAddress(), gattCh.getUuid()));
                }
            }
        }

        return ret;
    }

/*    public boolean readCharacteristics(BluetoothDevice blueDev, List<BluetoothGattCharacteristic> chs) {

        boolean ret = false;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null) {
            ret = dwo.readCharacteristics(chs);
            if (!ret) {
                Log.e(TAG, String.format("readChar Failed for dev %s %s", blueDev.getAddress(), ch.getUuid()));
            }
        }

        return ret;
    }*/

    public boolean writeCharacteristic(BluetoothDevice blueDev, BluetoothGattCharacteristic ch, byte[] val)
    {
        boolean ret = false;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null) {
            ch.setValue(val);
            ret = dwo.getGattConnection().writeCharacteristic(ch);

            if (!ret) {
                Log.e(TAG, String.format("writeChar Failed for dev %s %s", blueDev.getAddress(), ch.getUuid()));
            }
        }

        return ret;
    }

    public void serviceDiscovered(BluetoothDevice deviceObj, BluetoothGattService s)
    {
        for (BluetoothGattCharacteristic gattChar : s.getCharacteristics())
        {
            String uuid = gattChar.getUuid().toString();
            CharacteristicDes des = DeviceBook.singleton().getConfiguration().get(uuid);
            //Log.d(TAG, String.format("servDisc conf %s", conf)));
            if (des != null)
            {
                des.setCharacteristic(gattChar);
            }
        }
    }
}
