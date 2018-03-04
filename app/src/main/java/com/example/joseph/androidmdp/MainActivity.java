package com.example.joseph.androidmdp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.Set;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class MainActivity extends AppCompatActivity {



    BluetoothAdapter mBluetoothAdapter;
    BroadcastReceiver mReceiver;
    String deviceAddress;
    Handler mHandler;
    BluetoothService service;
    TextView messageTextView;
    Map mMap;
    int data[][] = new int[20][15];
    ImageButton grids[];
    GridLayout mapLayout;
    int robotLocation = 168;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView temp = (TextView) findViewById(R.id.chatBox);
        ImageButton up = (ImageButton) findViewById(R.id.up);
        ImageButton down = (ImageButton) findViewById(R.id.down);
        ImageButton left = (ImageButton) findViewById(R.id.left);
        ImageButton right = (ImageButton) findViewById(R.id.right);
        mMap = (Map) findViewById(R.id.map);
        mapLayout = (GridLayout) findViewById(R.id.map_grid);

        Rectangle rect = new Rectangle(this);
        grids = new ImageButton[300];

        for(int i = 0 ; i < 300 ; i++){

            final ImageButton btn = new ImageButton(this);
            btn.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));



            final int k = i;
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    btn.setBackgroundColor(Color.rgb(000, 255, 000));

                }
            });
            grids[i] = btn;
            mapLayout.addView(grids[i]);

        }


        int[][] data = new int[20][15];

        for(int i = 0; i < 20; i++){

            for(int j = 0; j < 15; j++) {

                data[i][j] = 0;

            }

        }

        data[3][13] = 1;
        data[3][14] = 1;

        updateMap(data);


        messageTextView = temp;



    deviceAddress = Constants.HARDWARE_ADDRESS;
    setupBluetooth();

        //setupBluetooth();

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                updateRobotLocation(robotLocation - 15);
                robotLocation -= 15;

                /*
                if(service != null)
                service.write(Constants.ACTION_FORWARD.getBytes());*/

            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateRobotLocation(robotLocation + 15);
                robotLocation += 15;
              /*  if(service != null)
                service.write(Constants.ACTION_REVERSE.getBytes());*/
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateRobotLocation(robotLocation - 1);
                robotLocation -= 1;


               /* if(service != null)
                service.write(Constants.ACTION_ROATE_LEFT.getBytes());*/

            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRobotLocation(robotLocation + 1);
                robotLocation += 1;



              /*  if(service != null)
                service.write(Constants.ACTION_ROATE_RIGHT.getBytes());*/

            }
        });







    }




    public void updateRobotLocation(int i){

        int center = i;
        int north = i - 15;
        int south = i + 15;
        int east = i + 1;
        int west = i - 1;
        int northwest = north - 1;
        int northeast = north + 1;
        int southwest = south - 1;
        int southeast = south + 1;


        grids[center].setBackgroundColor(Color.GREEN);
        grids[north].setBackgroundColor(Color.rgb(000, 255, 000));
        grids[south].setBackgroundColor(Color.rgb(000, 255, 000));
        grids[east].setBackgroundColor(Color.rgb(000, 255, 000));
        grids[west].setBackgroundColor(Color.rgb(000, 255, 000));
        grids[northwest].setBackgroundColor(Color.rgb(0, 0, 0));
        grids[northeast].setBackgroundColor(Color.rgb(0, 0, 0));
        grids[southwest].setBackgroundColor(Color.rgb(0, 0, 0));
        grids[southeast].setBackgroundColor(Color.rgb(0, 0, 0));







    }




    public void updateMap(int[][] data){



        mMap.updateMap(data);
        mMap.invalidate();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){

            case R.id.connect:
                startSearchDeviceActivity();
                break;


            case R.id.manual_input:
                manualInput();
                break;


            default:
                return super.onOptionsItemSelected(item);

        }

        return super.onOptionsItemSelected(item);


    }


    private void manualInput(){



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Input string")
                .setTitle("Manual input");



        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        builder.setView(input);
        builder.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                service.write(input.getText().toString().getBytes());

            }
        });


        AlertDialog dialog = builder.create();
        dialog.show();



    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        if (requestCode == Constants.ACTIVITY_RESULTS) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra(Constants.DEVICE_ADDRESS);

                deviceAddress = result;
                setupBluetooth();

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result

                deviceAddress = "1";
                setupBluetooth();


            }
        }


    }

    private void setLocationData(int locationData){

        int x = locationData/15;
        int y = locationData%15;

        data[x][y] = 1;


    }


    private void setLocationDataHex(String hex){
        String binary = "";


      /*  long hexa = Long.parseLong(hex , 16);
        String binary = Long.toBinaryString(hexa); */
        /*int n = 512 - binary.length();*/



        for(int i = 0 ; i < hex.length() ; i++){

            char c = hex.charAt(i);



            long hexlong = Long.parseLong(String.valueOf(c) , 16);
            binary = binary + Long.toBinaryString(hexlong);


        }

        int n = 512 - binary.length();


        for(int i = 0 ; i < n ; i++){


            binary = "0" + binary;

        }

        binary = binary.substring(211,binary.length()-1);


        for (int i = 0 ; i < binary.length() ; i++){

            char c = binary.charAt(i);
            if(c == '1'){

                setLocationData(i);
                grids[i].setBackgroundColor(Color.rgb(000, 255, 000));


            }

        }


        updateMap(data);
        mMap.invalidate();





    }






    private void startSearchDeviceActivity(){

        Intent i = new Intent(this, ListActivityBluetooth.class);
        startActivityForResult(i,Constants.ACTIVITY_RESULTS);
    }


    private void showPairedDevices(){
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
            }
        }
    }


    private void setupBluetooth(){


        //ask if bluetooth is enabled
       mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
/*
          mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Discovery has found a device. Get the BluetoothDevice
                    // object and its info from the Intent.
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                }
            }
        };*/



        //make device discoverable
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);




        // receiving message from connected bluetooth device
        Handler mHandler = new Handler(Looper.getMainLooper()){

            @Override
            public void handleMessage(Message msg) {



                String stuff = msg.getData().getString("Stuff" , "NOTHING");
                if(stuff.equals("NOTHING") != true)
                toast(stuff);

                byte[] bytes = msg.getData().getByteArray(Constants.SEND_TRANSMISSION);

                if(bytes != null) {
                    String ss = null;
                    try {
                        ss = new String(bytes , "ISO-8859-1");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    messageTextView.append(ss);

                   String s = new String(bytes);
                   if(s.startsWith("#update:")){


                       s = s.substring(s.indexOf(":")+1, s.length());
                        Log.i("AndroidMDP" , s);

                       while(true) {

                           int locationOfComma = s.indexOf(",");
                           String location = "";

                           if(locationOfComma != -1) {
                               location = s.substring(0, locationOfComma);
                               s=s.substring(locationOfComma+1,s.length());
                               setLocationData(Integer.valueOf(location));


                               grids[Integer.valueOf(location)].setBackgroundColor(Color.rgb(000, 255, 000));

                           }




                           Log.i("AndroidMDP" , location);

                           if(locationOfComma == -1){
                               break;
                           }

                       }

                       updateMap(data);
                       mMap.invalidate();


                   }

                    if(s.startsWith("#mass:")){

                       s = s.substring(s.indexOf(":")+1, s.length());
                       s = s.substring(0 , s.indexOf("/") );

                        setLocationDataHex(s);



                    }




                }




            }
        };




        //starts the bluetooth
        service = new BluetoothService(this,mHandler,mBluetoothAdapter , deviceAddress);
        service.startThreads();





    }




    private void toast(String stuff){

        Toast.makeText(this, stuff ,Toast.LENGTH_SHORT).show();

    }

}
