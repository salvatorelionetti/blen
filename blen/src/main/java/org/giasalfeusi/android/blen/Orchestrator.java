package org.giasalfeusi.android.blen;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
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

    private DeviceHost deviceHost;
    private DeviceScanCallBack deviceScanCallBack;
    private HashMap<BluetoothDevice, DeviceWithObservers> deviceWithObserversMap;

    /* Required only once. Could be safely called multiple times */
    static public Orchestrator singletonInitialize(Context context)
    {
        if (sto == null)
        {
            sto = new Orchestrator(context);
        }

        return sto;
    }

    static public Orchestrator singleton()
    {
        return sto;
    }

    private Orchestrator(Context context)
    {
        /* Context used only to get the current ble adapter */
        deviceHost = new DeviceHost(context);
        deviceScanCallBack = new DeviceScanCallBack(deviceHost);
        deviceWithObserversMap = new HashMap<BluetoothDevice, DeviceWithObservers>();
    }

    public DeviceHost getDeviceHost()
    {
        return deviceHost;
    }

    public DeviceScanCallBack getDeviceScanCallBack()
    {
        return deviceScanCallBack;
    }

    public void startScan()
    {
        deviceHost.startScan(deviceScanCallBack);
    }

    public void stopScan()
    {
        deviceHost.stopScan(deviceScanCallBack);
    }

    public void connectToDevice(Context context, BluetoothDevice blueDev)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo == null)
        {
            dwo = new DeviceWithObservers(blueDev, getDeviceHost());
            deviceWithObserversMap.put(blueDev, dwo);
        }

        if (dwo != null)
        {
            Log.i(TAG, "connectToDev: connectGatt");
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
            dwo = new DeviceWithObservers(blueDev, getDeviceHost());
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

    /* TODO: Add service as parameter?
    public List<BluetoothGattCharacteristic> getCharacteristics(BluetoothDevice blueDev)
    {
        List<BluetoothGattCharacteristic> ret = null;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        Log.d(TAG, String.format("getChList dwo = %s", dwo));
        if (dwo != null *&& dwo.getGattConnection() != null*)
        {
            ret = dwo.getCharacteristicsList();
        }

        return ret;
    }*/

    public BluetoothGattCharacteristic getCharacteristic(BluetoothDevice blueDev, String uuid)
    {
        BluetoothGattCharacteristic ret = null;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        Log.d(TAG, String.format("getChar dwo = %s", dwo));
        if (dwo != null && dwo.getGattConnection() != null) {
            /* Lookup Characteristic */
            for (BluetoothGattCharacteristic gattCh: dwo.cloneCharacteristicList())
            {
                if (gattCh.getUuid().compareTo(UUID.fromString(uuid)) == 0)
                {
                    ret = gattCh;
                }
            }
        }

        return ret;
    }

    public byte[] readValue(String devAddr, String uuid)
    {
        byte[] ret = null;
        DeviceWithObservers dwo = deviceWithObserversMap.get(getDeviceWithAddress(devAddr));

        if (dwo != null)
        {
            /* Lookup Characteristic */
            BluetoothGattCharacteristic gattCh = getCharacteristic(dwo.getBluetoothDevice(), uuid);

            if (gattCh != null)
            {
                ret = gattCh.getValue();
            }
            else
            {
                Log.e(TAG, String.format("readValue: no characteristic found with id %s!", uuid));
            }
        }
        else
        {
            Log.e(TAG, "readValue: no device with addr "+devAddr);
        }

        return ret;
    }

    public boolean readCharacteristic(BluetoothDevice blueDev, String uuid)
    {
        boolean ret = false;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null && dwo.getGattConnection() != null)
        {
            /* Lookup Characteristic */
            BluetoothGattCharacteristic gattCh = getCharacteristic(blueDev, uuid);

            if (gattCh != null)
            {
                ret = dwo.getGattConnection().readCharacteristic(gattCh);
                if (!ret)
                {
                    Log.e(TAG, String.format("readChar Failed for dev %s %s", blueDev.getAddress(), gattCh.getUuid()));
                }
            }
            else
            {
                Log.e(TAG, String.format("readChar Failed for dev %s: char not yet present %s", blueDev.getAddress(), gattCh.getUuid()));
            }
        }
        else
        {
            Log.e(TAG, String.format("readChar no des found, dwo %s, gattConn %s", dwo, dwo.getGattConnection()));
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

    /* null mean Unknown */
    public Integer getConnectionStatus(BluetoothDevice deviceObj)
    {
        Integer ret = null;

        DeviceWithObservers dwo = deviceWithObserversMap.get(deviceObj);
        if (dwo != null)
        {
            ret = dwo.getConnectionStatus();
        }

        return ret;
    }

    public BluetoothDevice getDeviceWithAddress(String mac)
    {
        BluetoothDevice ret = null;

        for (HashMap.Entry<BluetoothDevice, DeviceWithObservers> entry : deviceWithObserversMap.entrySet()) {
            BluetoothDevice deviceObj = entry.getKey();
            DeviceWithObservers dwo = entry.getValue();

            if (mac !=null && deviceObj != null && deviceObj.getAddress()!=null && deviceObj.getAddress().equals(mac))
            {
                ret = deviceObj;
                break;
            }
        }

        return ret;
    }

    public Integer getConnectionStatus(String devAddr)
    {
        Integer ret = null;

        BluetoothDevice deviceObj = Orchestrator.singleton().getDeviceWithAddress(devAddr);
        ret = Orchestrator.singleton().getConnectionStatus(deviceObj);

        Log.i(TAG, String.format("getConnSt: deviceObj %s, connStatus %d/%s", deviceObj, ret == null ? 0 : ret.intValue(), ret));

        return ret;

    }

    public boolean isConnected(String devAddr)
    {
        Integer connStatus = getConnectionStatus(devAddr);

        return (connStatus != null && connStatus == BluetoothProfile.STATE_CONNECTED);
    }

    public BluetoothGatt gattConnection(String devAddr)
    {
        BluetoothGatt ret = null;
        DeviceWithObservers dwo = deviceWithObserversMap.get(getDeviceWithAddress(devAddr));

        if (dwo != null)
        {
            ret = dwo.getGattConnection();
        }

        return ret;
    }

    public boolean readRssi(String devAddr)
    {
        boolean ret = false;
        DeviceWithObservers dwo = deviceWithObserversMap.get(getDeviceWithAddress(devAddr));

        if (dwo != null)
        {
            ret = dwo.getGattConnection().readRemoteRssi();

            if (!ret)
            {
                Log.e(TAG, "readRssi: not requested!");
            }
        }
        else
        {
            Log.e(TAG, "readRssi: no device with addr "+devAddr);
        }

        return ret;
    }

    public boolean setNotification(String devAddr, String uuid)
    {
        boolean ret = false;
        BluetoothGattCharacteristic ch = getCharacteristic(getDeviceWithAddress(devAddr), uuid);

        Log.i(TAG, String.format("setNotification(%s,%s)", devAddr, uuid));
        if (ch != null)
        {
            //
        }
        else
        {
            Log.e(TAG, "setNotification: no device/characteristic found!"+devAddr+"/"+uuid);
        }

        return ret;
    }
}
