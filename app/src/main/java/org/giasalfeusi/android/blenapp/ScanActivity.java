package org.giasalfeusi.android.blenapp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.giasalfeusi.android.blen.DevicesList;
import org.giasalfeusi.android.blen.Orchestrator;
import org.giasalfeusi.android.blen.Utils;

import java.util.Observable;
import java.util.Observer;

public class ScanActivity extends AppCompatActivity implements Observer, AdapterView.OnItemClickListener, View.OnClickListener
{
    private final static String TAG = "BlueDevSAct";

    private int REQUEST_ENABLE_BT = 1;

    private boolean useBackground = true;

    /* M: Model */

    /* V: View */
    private ListView devicesListView;

    /* C: Controller/Adapter/Activity */
    private DeviceListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, String.format("onCreate %s", this));
        setContentView(R.layout.activity_main);

        /* Must be called before to ask to DeviceHost */
        Orchestrator.singleton().setContext(this); /* What a wrong if */
        ensureBle();

        long nextTick = Utils.getPrefLong(this, "nextTick");
        Log.w(TAG, String.format("nextTick is %d", nextTick));

//        this.sendBroadcast(new Intent("org.giasalfeusi.ble.gui"));

        DevicesList.singleton().addObserver(this);

        adapter = new DeviceListAdapter(this, DevicesList.singleton());
        devicesListView = (ListView) findViewById(R.id.deviceListViewId);
        devicesListView.setAdapter(adapter);
        devicesListView.setOnItemClickListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.startScanButton);
        fab.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, String.format("onResume %s", this));
        Orchestrator.singleton().setContext(this); /* What a wrong if */
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, String.format("onPause %s", this));
        Orchestrator.singleton().stopScan();
        Orchestrator.singleton().setContext(null);
 //       this.sendBroadcast(new Intent("org.giasalfeusi.ble.bg"));
    }

    @Override
    public void onClick(View view) {
        Snackbar.make(view, "Starting BLE scanning", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        startScan();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {

        BluetoothDevice o = (BluetoothDevice) devicesListView.getItemAtPosition(position);
        Toast.makeText(getBaseContext(), String.format("Connecting to %s", o.getName()),Toast.LENGTH_SHORT).show();
        Log.i(TAG, String.format("Connecting to %s@%s", o.getName(), o.getAddress()));
        Intent deviceIntent = new Intent(ScanActivity.this, DeviceActivity.class);
        deviceIntent.putExtra("deviceObject", o);
        Log.i(TAG, "deviceObject "+o.toString());
        startActivity(deviceIntent); // Take care: could arise pb accessing sto.activity!!!
    }

    private void ensureBle()
    {
        if (!Orchestrator.singleton().getDeviceHost().hasBluetoothLE(this)) {
            Toast.makeText(this, "This phone does not support BLE!",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                this.startScan();
            } else {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startScan() {
        if (!Orchestrator.singleton().getDeviceHost().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            Orchestrator.singleton().startScan();
        }
    }

    public void update(Observable o, Object arg)
    {
        /* New BluetoothDevice object is discovered */
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
