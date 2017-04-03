package org.giasalfeusi.android.blen;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

/**
 * Created by salvy on 03/04/17.
 */

public class DeviceBook {
    final static private String TAG = "DevBook";
    static private DeviceBook deviceBook;

    private HashMap<String, CharacteristicDes> configuration;

    static public DeviceBook singleton()
    {
        if (deviceBook == null)
        {
            deviceBook = new DeviceBook();
        }
        return deviceBook;
    }

    public DeviceBook()
    {
        configuration = new HashMap<String, CharacteristicDes>();
        configuration.put("00002a26-0000-1000-8000-00805f9b34fb", new CharacteristicDes("Firmware Revision String", CharacteristicValueType.STRING));
        configuration.put("00002a25-0000-1000-8000-00805f9b34fb", new CharacteristicDes("Serial Number String", CharacteristicValueType.STRING));
        configuration.put("00002a27-0000-1000-8000-00805f9b34fb", new CharacteristicDes("HW Revision String", CharacteristicValueType.STRING));
        configuration.put("00002a28-0000-1000-8000-00805f9b34fb", new CharacteristicDes("SW Revision String", CharacteristicValueType.STRING));
        configuration.put("00002a29-0000-1000-8000-00805f9b34fb", new CharacteristicDes("Manufacturer Name String", CharacteristicValueType.STRING));

        configuration.put("f000aa01-0451-4000-b000-000000000000", new CharacteristicDes("IR Temperature Data"));
        configuration.put("f000aa02-0451-4000-b000-000000000000", new CharacteristicDes("IR Temperature Config", CharacteristicValueType.BIT));
        configuration.put("f000aa03-0451-4000-b000-000000000000", new CharacteristicDes("IR Temperature Period"));
    }

    public HashMap<String, CharacteristicDes> getConfiguration() {
        return configuration;
    }
}
