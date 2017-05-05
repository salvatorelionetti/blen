package org.giasalfeusi.android.blen;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * Created by salvy on 20/03/17.
 */

/* BluetoothDevice is final! */
public class DeviceWithObservers extends BluetoothGattCallback {
    private final String TAG = "DevWithObs";

    private ObservableAllPublic observable = null;

    private BluetoothDevice obj = null;

    private List<BluetoothGattCharacteristic> characteristicsList;

    private DeviceHost deviceHost = null;

    private BluetoothGatt mGatt = null;

    private Integer connStatus = null; /* Unknown */

    public DeviceWithObservers(BluetoothDevice _obj, DeviceHost dh)
    {
        obj = _obj;
        observable = new ObservableAllPublic();
        characteristicsList = new ArrayList<BluetoothGattCharacteristic>();
        deviceHost = dh;
        connStatus = null;
    }

    public BluetoothDevice getBluetoothDevice()
    {
        return obj;
    }

    public Integer getConnectionStatus()
    {
        return connStatus;
    }

    public void addChars(BluetoothGattService gattService)
    {
        Log.i(TAG, String.format("S%s", gattService.getUuid()));

        for (BluetoothGattCharacteristic gattChar : gattService.getCharacteristics())
        {
            Log.i(TAG, String.format(" %s", gattChar.getUuid()));

            for (BluetoothGattDescriptor des : gattChar.getDescriptors())
            {
                Log.i(TAG, String.format(" >%s", des.getUuid()));
            }
        }
        characteristicsList.addAll(gattService.getCharacteristics());
    }

    /* Sometimes
    04-07 19:59:03.875 32594-32686/org.giasalfeusi.android.blenapp D/BluetoothGatt: onClientConnectionState() - status=133 clientIf=6 device=24:71:89:BE:F7:07
    04-07 19:59:03.875 32594-32686/org.giasalfeusi.android.blenapp I/DevWithObs: onConnStateChange Status 133, newState 0, gatt android.bluetooth.BluetoothGatt@1458f58e
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.i(TAG, String.format("onConnStateChange Status %d, newState %d, gatt %s", status, newState, gatt));

        if (status == BluetoothGatt.GATT_SUCCESS) {
            mGatt = gatt;
        }

        connStatus = newState;

        observable.setChanged();
        observable.notifyObservers(Integer.valueOf(newState));
        deviceHost.notifyChanged(newState);

            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "STATE_CONNECTED");
                    characteristicsList.clear();// = new ArrayList<BluetoothGattCharacteristic>();
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "STATE_DISCONNECTED");
                    mGatt = null;
                    //characteristicsList.clear();
                    break;
                default:
                    Log.e(TAG, "STATE_OTHER "+Integer.valueOf(newState));
                    //characteristicsList.clear();
                    mGatt = null;
            }

/*        } else {
            Log.e(TAG, "onConnStateChange Failed!");
            // #define  GATT_ERROR                          0x85
            if (status == 0x85)
            { // called when we disconnect (es stand-by), device is owered down, then exit from stand-by and retry connection
              // Also gatt pointer does change!
                if (gatt != null)
                {
                    gatt.close();
                }

            }
        }*/
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt _gatt, int status)
    {
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            mGatt = _gatt;
            List<BluetoothGattService> services = mGatt.getServices();
            Log.i("GATT.onServDiscovered", services.toString());

            // First add services to the DeviceBook
            for (BluetoothGattService gattService: services)
            {
                Orchestrator.singleton().serviceDiscovered(mGatt.getDevice(), gattService);
            }

            // Notify all service once, in the case user is interested in it
            observable.setChanged();
            observable.notifyObservers(services);
            deviceHost.notifyChanged(services);

            // Then notify to all interested users
            for (BluetoothGattService gattService: services)
            {
                addChars(gattService);
                observable.setChanged();
                observable.notifyObservers(gattService);
                deviceHost.notifyChanged(gattService);
            }
        }
        else
        {
            mGatt = null;
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic
                                             characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, String.format("onCharRead %s => %s", characteristic.getUuid(), Utils.bytesToHex(characteristic.getValue())));
            observable.setChanged();
            observable.notifyObservers(characteristic);
            deviceHost.notifyChanged(characteristic);
        } else {
            Log.w(TAG, "onCharRead failed: received: " + status);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic
                                              characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, String.format("onCharWrite %s => %s", characteristic.getUuid(), Utils.bytesToHex(characteristic.getValue())));
            observable.setChanged();
            observable.notifyObservers(characteristic);
            deviceHost.notifyChanged(characteristic);
        } else {
            Log.w(TAG, "onCharWrite failed: received: " + status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, String.format("onCharChanged %s %s ", Utils.bytesToHex(characteristic.getValue()), characteristic.getUuid().toString()));
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status)
    {
        Log.d(TAG, String.format("onRemoteRssi(%d, %d)", rssi, status));
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            Rssi rssiObj = new Rssi(rssi);
            observable.setChanged();
            observable.notifyObservers(rssiObj);
            deviceHost.notifyChanged(rssiObj);
        }
        else
        {
            Log.e(TAG, String.format("onReadRemRssi status %d", status));
        }
    }

    public BluetoothGatt getGattConnection()
    {
        Log.d(TAG, String.format("gattConn is %s", mGatt));
        return mGatt;
    }

    public List<BluetoothGattCharacteristic> getCharacteristicsList() { return characteristicsList;}

    public boolean readCharacteristics(List<BluetoothGattCharacteristic> chs) {
        return false;
    }

    /* Proxy part */
    public void addObserver(Observer o)
    {
        Log.i(TAG, "addObs: " + o.toString());
        observable.addObserver(o);
    }

    public void deleteObserver(Observer o)
    {
        Log.i(TAG, "delObs: " + o.toString());
        observable.deleteObserver(o);
    }

    public List<BluetoothGattCharacteristic> cloneCharacteristicList()
    {
        /* Avoid concurrent modification */
        List<BluetoothGattCharacteristic> clonedGattChList;
        clonedGattChList = (List<BluetoothGattCharacteristic>) ((ArrayList<BluetoothGattCharacteristic>) this.getCharacteristicsList()).clone();

        return clonedGattChList;
    }
}
