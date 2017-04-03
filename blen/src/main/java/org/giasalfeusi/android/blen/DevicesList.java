package org.giasalfeusi.android.blen;


import android.bluetooth.BluetoothDevice;
import android.util.Log;

//import org.giasalfeusi.blewithbeaconlib.ObservableAllPublic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Observer;

/**
 * Created by salvy on 10/03/17.
 */


/* Unfortunately java don't support multiple inheritance nor
 * it define an interface for Observable.
 * Moreover w.o. multiple inheritance it's hard to work with
 * protected visibility on {clear, set}Changed methods.
 */
/*interface ObservableImplInterface extends ObservableInterface
{
/*    void clearChanged();
    void setChanged();
    boolean hasChanged();* /
    void notifyObservers(Object arg);
    void notifyObservers();
}*/

/* Singleton */
public class DevicesList<B> extends ArrayList<BluetoothDevice> {

    private static final String TAG = "DevList";

    private static DevicesList devicesList = null;

    private ObservableAllPublic observable = null;

    /* Why constructor is required by singleton initialization? */
    public DevicesList(Collection<BluetoothDevice> c)
    {
        super(c);
        observable = new ObservableAllPublic();
    }

    public static DevicesList singleton()
    {
        if (devicesList == null) {

            BluetoothDevice[] _devices = {};
            devicesList = new DevicesList(Arrays.asList(_devices));
        }
        return devicesList;
    }

    public void replaceAll(Collection<BluetoothDevice> elements)
    {
        Log.i(TAG, String.format("RelpaceAllWith(%s)", elements.toString()));
        this.clear();
        this.addAll(elements);
    }

    @Override
    public boolean add(BluetoothDevice object)
    {
        boolean ret;

        ret = super.add(object);
        if (ret)
        {
            observable.setChanged();
            observable.notifyObservers(object);
        }

        return ret;
    }

    /* Proxy part */
    public void addObserver(Observer observer) {
        observable.addObserver(observer);
    }

    public void delObserver(Observer observer) {
        observable.deleteObserver(observer);
    }
}