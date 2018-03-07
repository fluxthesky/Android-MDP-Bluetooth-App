package com.example.joseph.androidmdp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Joseph on 2/2/2018.
 */



public class BluetoothService {



    private UUID MY_UUID  = UUID.fromString("000001101-0000-1000-8000-00805F9B34FB");
    Context context;
    Handler handler;
    BluetoothAdapter mBluetoothAdapter;
    String deviceAddress;

    private BluetoothServiceForConnect connectThread;
    private BluetoothServiceForListen listenThread;
    private ConnectedThread connectedThread;

    public BluetoothService(Context context, Handler handler, BluetoothAdapter adapter, String deviceAddress) {

        this.context = context;
        this.handler = handler;
        this.mBluetoothAdapter = adapter;
        this.deviceAddress = deviceAddress;

        connectThread = new BluetoothServiceForConnect();
        listenThread = new BluetoothServiceForListen();



    }

    private void toast(String stuff){

        Toast.makeText(context, stuff ,Toast.LENGTH_SHORT).show();

    }


    public void startThreads(){


        connectThread.start();
        listenThread.start();

    }


    public class BluetoothServiceForListen extends Thread{


        private final BluetoothServerSocket mmServerSocket;






        BluetoothServerSocket tmp = null;
        BluetoothSocket  tmp2 = null;
        private String TAG = "AndroidMDPBluetooth";
        String deviceAddress;

        BluetoothServiceForListen( ){




            try {

                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("BLUETOOTH_SERVICE_MDP", MY_UUID);



            } catch (IOException e) {
                e.printStackTrace();
            }
            mmServerSocket = tmp;


        }

        @Override
        public void run() {
            runListen();


        }



        public void runListen() {
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

                    Log.i("AndroidMDP" , "Connected on listen!");
                    Message msg = new Message();
                    Bundle bun = new Bundle();
                    bun.putString("Stuff", "Connected on listen");
                    msg.setData(bun);
                    handler.sendMessage(msg);
                    startConnectedThread(socket);


                    try {
                        mmServerSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
            }}






}

    public class BluetoothServiceForConnect extends Thread{
        BluetoothSocket mmServerSocketForConnect;



        final BluetoothDevice mmDevice;
        BluetoothDevice tmpDevice;

        BluetoothSocket  tmp2 = null;


        private String TAG = "AndroidMDPBluetooth";



        BluetoothServiceForConnect(){



            if(deviceAddress.equals("1") != true) {
                tmpDevice = mBluetoothAdapter.getRemoteDevice(deviceAddress);
            }

            try {

                if(deviceAddress.equals("1") != true) {
                    tmp2 = tmpDevice.createRfcommSocketToServiceRecord(MY_UUID);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }

            if(deviceAddress.equals("1") != true) {
                mmDevice = tmpDevice;
                mmServerSocketForConnect = tmp2;
            }else{
                mmDevice = null;
            }

        }


        @Override
        public void run() {


            if(deviceAddress.equals("1") != true) {
                runConnect();
            }
        }




        public void runConnect(){

            try {
                mmServerSocketForConnect.connect();
                Log.i("AndroidMDP" , "Connected on run!");
                Message msg = new Message();
                Bundle bun = new Bundle();
                bun.putString("Stuff", "Connected on run");
                msg.setData(bun);
                handler.sendMessage(msg);

                startConnectedThread(mmServerSocketForConnect);




            } catch (IOException e) {
                e.printStackTrace();
            }


        }




    }

    public void write(byte[] bytes){


        connectedThread.write(bytes);



    }

    private void startConnectedThread(BluetoothSocket mmServerSocketForConnect){


        connectedThread = new ConnectedThread(mmServerSocketForConnect);
        connectedThread.start();

        Message msg = new Message();
        Bundle bun = new Bundle();
        bun.putString("Stuff", "connectedThread started!");
        msg.setData(bun);
        handler.sendMessage(msg);


    }


    public class ConnectedThread extends Thread {

        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;
        private byte[] mmBuffer;
        Boolean running = true;


        public ConnectedThread(BluetoothSocket socket) {

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e("AndroidMDP", "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e("AndroidMDP", "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;



        }


        public void write(byte[] bytes){

            try {
                mmOutStream.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }


        @Override
        public void run() {

            mmBuffer = new byte[1024];
            int numBytes;


            while(running){

                try {
                    numBytes = mmInStream.read(mmBuffer);
                    Message msg = new Message();
                    Bundle bun = new Bundle();
                    bun.putByteArray(Constants.SEND_TRANSMISSION , mmBuffer);
                    msg.setData(bun);

                    handler.sendMessage(msg);



                } catch (IOException e) {
                    e.printStackTrace();
                    running = false;
                    resetConnection();
                }


            }



        }

        private void resetConnection() {
            Log.d("Bluetooth Stream","Closing");
            if(mmInStream!=null){
                try {
                    mmInStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mmInStream = null;
            }
            if(mmOutStream!=null){
                try {
                    mmOutStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mmOutStream = null;
            }
            if(mmSocket!=null){
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mmSocket = null;
            }
            Log.d("Bluetoth Stream","Closed");
        }
    }






}
