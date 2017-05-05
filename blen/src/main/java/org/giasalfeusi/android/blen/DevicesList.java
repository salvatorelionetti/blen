package org.giasalfeusi.android.blen;


import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Observer;

//import org.giasalfeusi.blewithbeaconlib.ObservableAllPublic;

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

    private ObservableAllPublic observable = null;

    /* Dependency inject */
    private DeviceHost deviceHost = null;

    public DevicesList(Collection<BluetoothDevice> c, DeviceHost dh)
    {
        super(c);
        observable = new ObservableAllPublic();
        deviceHost = dh;
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
            Log.i(TAG, String.format("add(%s) notifying", object));
            observable.setChanged();
            observable.notifyObservers(object);

            deviceHost.notifyChanged(object);
        }

        return ret;
    }

    public void stateChanged(BluetoothDevice object)
    {
        if (super.contains(object))
        {
            observable.setChanged();
            observable.notifyObservers(object);
        }
    }

    /* Proxy part */
    public void addObserver(Observer observer)
    {
        Log.i(TAG, String.format("addObserver %s", observer));
        observable.addObserver(observer);
    }

    public void delObserver(Observer observer)
    {
        Log.i(TAG, String.format("delObserver %s", observer));
        observable.deleteObserver(observer);
    }
}