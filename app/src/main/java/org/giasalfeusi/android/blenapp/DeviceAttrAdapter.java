package org.giasalfeusi.android.blenapp;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.giasalfeusi.android.blen.CharacteristicDes;
import org.giasalfeusi.android.blen.DeviceBook;
import org.giasalfeusi.android.blen.Orchestrator;
import org.giasalfeusi.android.blen.Utils;
import org.giasalfeusi.android.blenapp.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by salvy on 20/03/17.
 */

public class DeviceAttrAdapter extends ArrayAdapter<BluetoothGattCharacteristic>
{
    static final private String TAG = "DevAttrAda";
    private final Context context;
    private List<BluetoothGattCharacteristic> values;
    private HashMap<String, CharacteristicDes> conf;
    private boolean entriesLocked;

    public DeviceAttrAdapter(Context context, List<BluetoothGattCharacteristic> values)
    {
        super(context, -1, values);
        this.context = context;
        this.values = values;
        this.conf = DeviceBook.singleton().getConfiguration();
        entriesLocked = false;
    }

    public void lockEntries()
    {
        entriesLocked = true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.device_list_row_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        TextView desView = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        //String bdAddress = ((BluetoothDevice) values.get(position))
        BluetoothGattCharacteristic ch = values.get(position);
        Log.d(TAG, String.format("getView val1 %s %s", Utils.bytesToHex(ch.getValue()), ch));
        String attrName = ch.getUuid().toString();
        String attrVal = "";
        if (ch.getValue() != null)
        {
            attrVal = Utils.bytesToHex(ch.getValue());
        }
        if (conf != null) {
            CharacteristicDes des = conf.get(ch.getUuid().toString());
            if (des != null) {
                attrName = des.getTitle();
                BluetoothGattCharacteristic ch2 = des.getCharacteristic();
                if (ch2 != null) {
                    attrVal = des.valueToString();
                    Log.d(TAG, String.format("getView val2 %s %s", Utils.bytesToHex(ch2.getValue()), ch2));
                }
                assert ch == ch2;
            }
        }
        textView.setText(attrName);
        desView.setText(attrVal);
        imageView.setImageResource(0);

//        rowView.setEnabled(!entriesLocked);
        if (entriesLocked)
        {
            rowView.setAlpha(.5f);
            rowView.setClickable(false);
        /*} else {
            rowView.setAlpha(1.0f);
            rowView.setClickable(true);*/
        }

        return rowView;
    }
}