package org.giasalfeusi.android.blen;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.List;

/**
 * Created by salvy on 10/03/17.
 */

public class DeviceScanCallBack extends ScanCallback
{
    private final String TAG = "DeviceScanCallBack";

    private DeviceHost deviceHost = null;

    public DeviceScanCallBack(DeviceHost dh)
    {
        deviceHost = dh;
    }

    private DeviceHost getDeviceHost()
    {
        return deviceHost;
    }

    private void gotResult(ScanResult res)
    {
        BluetoothDevice blueDev = res.getDevice();

        if (!getDeviceHost().getDevicesList().contains(blueDev))
        {
            String bdName = String.format("%s@%s", blueDev.getName(), blueDev.getAddress());
            Log.i(TAG, String.format("Adding to Devices!!! %s@%s", blueDev.getName(), blueDev.getAddress()));
            getDeviceHost().getDevicesList().add(blueDev);
            getDeviceHost().notifyChanged(res);
        }
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result)
    {
        //Log.i(TAG, String.format("onScanRes(%s,%s)", String.valueOf(callbackType), result.toString()));
        gotResult(result);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results)
    {
        //Log.i(TAG, String.format("onBatchScanRes(%s)", results));
        for (ScanResult result : results)
        {
            gotResult(result);
        }
    }

    @Override
    public void onScanFailed(int errorCode)
    {
        Log.e(TAG, String.format("onScanFailed: error code %d", errorCode));
        getDeviceHost().notifyChanged(new ScanFailed(errorCode));
    }
}

