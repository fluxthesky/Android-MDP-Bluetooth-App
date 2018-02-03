package com.example.joseph.androidmdp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Joseph on 2/2/2018.
 */



public class BluetoothService extends Thread{
    private final BluetoothServerSocket mmServerSocket;

    private UUID MY_UUID  = UUID.fromString("000001101-0000-1000-8000-00805F9B34FB");
    Context context;
    Handler handler;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothServerSocket tmp = null;
    private String TAG = "AndroidMDPBluetooth";

    BluetoothService(Context context, Handler handler, BluetoothAdapter adapter){

        this.context = context;
        this.handler = handler;
        mBluetoothAdapter = adapter;
        try (BluetoothServerSocket bluetoothService = tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BluetoothService", MY_UUID)) {
        }
        catch (IOException e){
            Log.e(TAG, "Socket's listen() method failed", e);

        }
        mmServerSocket = tmp;




    }


    public void run() {
        BluetoothSocket socket = null;
        // Keep listening until exception occurs or a socket is returned.
        while (true) {
            try {
                socket = mmServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.

                Toast.makeText(context,"Socket accepted!" ,Toast.LENGTH_SHORT).show();

                Thread newThread = new Thread(new Runnable() {

                    @Override
                    public void run() {

                        while(true){

                            Log.e(TAG, "Thread running");
                        }

                    }


                });


                try {
                    mmServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }







}
