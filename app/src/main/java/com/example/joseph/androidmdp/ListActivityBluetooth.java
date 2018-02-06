package com.example.joseph.androidmdp;

import android.app.Activity;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ListActivityBluetooth extends ListActivity {

    BluetoothAdapter mBluetoothAdapter;
    BroadcastReceiver mReceiver;
    ArrayList<AdapterItem> items = new ArrayList<AdapterItem>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bluetooth);



        // Register for broadcasts when a device is discovered.
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);





        final MyAdapter adapter = new MyAdapter(this,0 , items);



        setListAdapter(adapter);

        ListView list = getListView();
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent();
                i.putExtra(Constants.DEVICE_ADDRESS , items.get(position).device_address);
                setResult(Activity.RESULT_OK,i);
                finish();



            }
        });


        setupBluetooth();

        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.



                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address

                    Log.i("AndroidMDP", "onReceive: " + deviceName + " " + deviceHardwareAddress);

                    AdapterItem item = new AdapterItem(deviceName,deviceHardwareAddress);
                    items.add(item);
                    adapter.add(item);
                }

                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){

                    toast("Finished discvoering");
                    Log.i("AndroidMDP", "finished discovering of bluetooth");


                }
            }
        };


        this.registerReceiver(mReceiver, filter);

        toast("Starting discovery");
        boolean status = mBluetoothAdapter.startDiscovery();
        Log.i("AndroidMDP", "starting discovery "  + status );







    }



    private void toast(String stuff){

        Toast.makeText(this, stuff ,Toast.LENGTH_SHORT).show();

    }





    private void setupBluetooth(){


        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }




      /* Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);*/



    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        mBluetoothAdapter.cancelDiscovery();
        super.onDestroy();
    }

    private class AdapterItem{


        public String device_id;
        public String device_address;

        public AdapterItem(String device_id, String device_address) {
            this.device_id = device_id;
            this.device_address = device_address;
        }



    }


    private class MyAdapter extends ArrayAdapter<AdapterItem>{


        public MyAdapter(@NonNull Context context, int resource, @NonNull List<AdapterItem> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


            AdapterItem item = getItem(position);


            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_view, parent, false);
            }


            TextView tvName = (TextView) convertView.findViewById(R.id.text1);
            TextView tvHome = (TextView) convertView.findViewById(R.id.text2);

            tvName.setText(item.device_id);
            tvHome.setText(item.device_address);




            return convertView;
        }




    }







}
