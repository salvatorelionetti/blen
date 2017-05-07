package org.giasalfeusi.android.blenapp;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.giasalfeusi.android.blen.CharacteristicDes;
import org.giasalfeusi.android.blen.CharacteristicValueType;
import org.giasalfeusi.android.blen.DeviceBook;
import org.giasalfeusi.android.blen.Orchestrator;
import org.giasalfeusi.android.blen.Utils;

import java.util.Observable;
import java.util.Observer;

public class DeviceActivity extends AppCompatActivity implements Observer, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {

    private final static String TAG = "BlueDevAct";

    /* M: Model Key */
    private BluetoothDevice deviceObj;
    private int newStatus;

    /* V: View */
    private ListView characteristicsListView;
    private View content_main2_view;

    /* C: Controller/Adapter/Activity */
    private DeviceAttrAdapter adapter;
    private FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Log.i(TAG, String.format("onCreate %s", this));

        /* Intent from previous activity */
        deviceObj = getIntent().getExtras().getParcelable("deviceObject");
        newStatus = BluetoothProfile.STATE_DISCONNECTED;

        /*
         * Before to interact with the model:
         * 1) set context
         * 2) observe device
         */
        Orchestrator.singletonInitialize(this); /* Not captured */
        Orchestrator.singleton().observeDevice(deviceObj, this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        content_main2_view = (View) findViewById(R.id.content_main2);

        adapter = new DeviceAttrAdapter(this, Orchestrator.singleton().getCharacteristicsList(deviceObj));

        characteristicsListView = (ListView) ((ViewGroup)content_main2_view).getChildAt(1);
        characteristicsListView.setAdapter(adapter);
        characteristicsListView.setOnItemClickListener(this);
        characteristicsListView.setOnItemLongClickListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        View headView = ((ViewGroup)content_main2_view).getChildAt(0);
        TextView textView = (TextView) headView.findViewById(R.id.firstLine);
        TextView desView = (TextView) headView.findViewById(R.id.secondLine);

        textView.setText(deviceObj.getName());
        desView.setText(deviceObj.getAddress());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, String.format("onResume %s", this));
        Orchestrator.singleton().observeDevice(deviceObj, this);
        Orchestrator.singleton().connectToDevice(this.getApplicationContext(), deviceObj);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, String.format("onPause %s", this));
        //sto.disconnectFromDevice(deviceObj);
        Orchestrator.singleton().noMoreObserveDevice(deviceObj, this);
    }

    public void update(Observable o, Object arg)
    {
        //java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
        //Toast.makeText(this, "Main2Act UPDATE CALLED!", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "update o " + o.toString());
        if (arg == null)
        {
            Log.i(TAG, "update arg <null>");
        } else {
            Log.i(TAG, "update arg " + arg.toString());

            if (arg instanceof Integer) {
                /* Connection status */
                newStatus = (int) arg;

                View rowView = ((ViewGroup)content_main2_view).getChildAt(0);
                final ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (newStatus) {
                            case BluetoothProfile.STATE_CONNECTED:
                                imageView.setImageResource(R.drawable.ic_bluetooth_connected_black_24dp);
                                Utils.setPref(DeviceActivity.this, "devName", deviceObj.getName());
                                Utils.setPref(DeviceActivity.this, "devAddr", deviceObj.getAddress());

                                /* Start bg T sampling */
                                Intent bgPollIntent = new Intent("org.giasalfeusi.ble.poll");
                                bgPollIntent.putExtra("deviceObject", deviceObj);
                                DeviceActivity.this.sendBroadcast(bgPollIntent);

                                break;
                            case BluetoothProfile.STATE_DISCONNECTED:
                                imageView.setImageResource(R.drawable.ic_bluetooth_black_24dp);
                                /* Grey out all entries */
                                if (adapter != null)
                                {
                                    adapter.lockEntries();
                                    adapter.notifyDataSetChanged();
                                }
                                break;
                            default:
                                Log.e(TAG, "STATE_OTHER "+Integer.valueOf(newStatus));
                                imageView.setImageResource(R.drawable.ic_bluetooth_searching_black_24dp);
                                /* Grey out all entries */
                                if (adapter != null)
                                {
                                    adapter.lockEntries();
                                    adapter.notifyDataSetChanged();
                                }
                                break;
                        }
                    }
                });
            } else if (arg instanceof BluetoothGattService) {
                /* Discovered a Service */
                final BluetoothGattService s = (BluetoothGattService) arg;

                if (adapter != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            } else if (arg instanceof BluetoothGattCharacteristic) {
                /* Char changed */
                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) arg;

                if (adapter != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onClick(View view) {
        String msg;

        switch (newStatus)
        {
            case BluetoothProfile.STATE_CONNECTING:
            case BluetoothProfile.STATE_CONNECTED:
                msg = "Start Disconnecting";
                Orchestrator.singleton().disconnectFromDevice(deviceObj);
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
            case BluetoothProfile.STATE_DISCONNECTED:
                msg = "Start Connecting";
                Orchestrator.singleton().connectToDevice(this.getApplicationContext(), deviceObj);
                break;
            default:
                msg = "Status is unknown " + newStatus;
                break;
        }

        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        Log.d(TAG, String.format("onItemClick pos %d", position));
        Orchestrator.singleton().readCharacteristic(deviceObj, Orchestrator.singleton().getCharacteristicsList(deviceObj).get(position));
        return;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                int position, long id)
    {
        BluetoothGattCharacteristic ch = Orchestrator.singleton().getCharacteristicsList(deviceObj).get(position);
        Log.d(TAG, String.format("onItemLongClick pos %d val %s", position, ch.getValue()));
        if (ch.getValue() == null)
        {
            Orchestrator.singleton().readCharacteristic(deviceObj, ch);
        } else {
            CharacteristicDes des = DeviceBook.singleton().getConfiguration().get(ch.getUuid().toString());
            if (des != null && des.getType() == CharacteristicValueType.BIT)
            {
                // Toggle
                byte[] val = ch.getValue();
                val[0] ^= 1;
                Log.d(TAG, String.format("onItemLongClick val %s -> %s", Utils.bytesToHex(ch.getValue()), Utils.bytesToHex(val)));
                Orchestrator.singleton().writeCharacteristic(deviceObj, ch, val);
            }
        }
        return true;
    }
}