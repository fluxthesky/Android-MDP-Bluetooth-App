package com.example.joseph.androidmdp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
    ImageView grids[];
    android.support.v7.widget.GridLayout mapLayout;
    int robotLocation = 168;
    TextView curStatus;
    int head = robotLocation - 15;
    int robotDirection = Constants.NORTH;
    int oldRobotLocation = -1;
    int MDF[] = new int[300];


    int waypoint = 0;








    ArrayList<Integer> robotHistory = new ArrayList<Integer>();
    ArrayList<Integer> exploredRobotHistory = new ArrayList<Integer>();


    boolean autoUpdate = false;
    boolean setRobot = false;
    boolean setWaypoint = false;



    Button manualUpBtn;
    ToggleButton autoUpdateToggle;
    ToggleButton autoUpdateToggleRobot;
    ToggleButton autoUpdateToggleWaypoint;



    String currentLocation = "000000000000000000000000000000000000000000000000000000000000000000000000000" , currentStatus;



    Thread autoUpdateThread;

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
        mapLayout = (android.support.v7.widget.GridLayout) findViewById(R.id.map_grid);

        Rectangle rect = new Rectangle(this);
        grids = new ImageView[300];

        curStatus = (TextView)findViewById(R.id.curStatus);

        manualUpBtn = (Button) findViewById(R.id.manualUpBtn);
        autoUpdateToggle = (ToggleButton) findViewById(R.id.autoUpdateToggle);
        autoUpdateToggleRobot = (ToggleButton) findViewById(R.id.robotUpBtn);
        autoUpdateToggleWaypoint = (ToggleButton) findViewById(R.id.waypointUpBtn);

        int[][] data = new int[20][15];

        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 15; j++) {
                data[i][j] = 0;
            }
        }

        data[3][13] = 1;
        data[3][14] = 1;

        updateMap(data);

        setupMap();


        initializeAutoUpdate();

        messageTextView = temp;

        deviceAddress = Constants.HARDWARE_ADDRESS;

        //setupBluetooth();

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  moveForward();
                drawRobot();
                robotLocation -= 15;*/
              oldRobotLocation = robotLocation;
              robotHistory.add(oldRobotLocation);

                switch (robotDirection){

                    case Constants.NORTH:
                        robotLocation-=15;
                        if(robotLocation<=14)
                        {

                            robotLocation+=15;

                        }
                        break;
                    case Constants.SOUTH:
                        robotLocation+=15;
                        if(robotLocation>=285)
                        {

                            robotLocation-=15;

                        }
                        break;
                    case Constants.EAST:
                        robotLocation+=1;
                        if((robotLocation + 1)%15 == 0)
                        {

                            robotLocation-=1;

                        }
                        break;
                    case Constants.WEST:
                        robotLocation-=1;

                        if(robotLocation%15 == 0)
                        {

                            robotLocation+=1;

                        }
                         break;


                }
                if(autoUpdate) {
                    drawRobot();
                    setRobotStatus("moving forward");
                }
                if(service != null)
                service.write(Constants.ACTION_FORWARD.getBytes());

            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*updateRobotLocation(robotLocation + 15 , Constants.SOUTH);
                robotLocation += 15; */


                oldRobotLocation = robotLocation;

                robotHistory.add(oldRobotLocation);

                switch (robotDirection){

                    case Constants.NORTH:
                        robotLocation+=15;
                        break;
                    case Constants.SOUTH:
                        robotLocation-=15;
                        break;
                    case Constants.EAST:
                        robotLocation-=1;
                        break;
                    case Constants.WEST:
                        robotLocation+=1;
                        break;


                }

                if(autoUpdate) {
                    drawRobot();
                    setRobotStatus("reversing");
                }


                if(service != null)
                service.write(Constants.ACTION_REVERSE.getBytes());
            }
        });

        left.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  turnLeft();
                drawRobot();
                robotLocation -= 1;*/

              switch (robotDirection){

                  case Constants.NORTH:
                       robotDirection = Constants.WEST;
                       break;
                  case Constants.SOUTH:
                      robotDirection = Constants.EAST;
                      break;
                  case Constants.EAST:
                      robotDirection = Constants.NORTH;
                      break;
                  case Constants.WEST:
                      robotDirection = Constants.SOUTH;
                      break;


              }
              if(autoUpdate) {
                  drawRobot();
                  setRobotStatus("turning left");
              }

               if(service != null)
                service.write(Constants.ACTION_ROATE_LEFT.getBytes());

            }
        });

        right.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* updateRobotLocation(robotLocation + 1 , Constants.EAST);
                robotLocation += 1;

*/

                switch (robotDirection){

                    case Constants.NORTH:
                        robotDirection = Constants.EAST;
                        break;
                    case Constants.SOUTH:
                        robotDirection = Constants.WEST;
                        break;
                    case Constants.EAST:
                        robotDirection = Constants.SOUTH;
                        break;
                    case Constants.WEST:
                        robotDirection = Constants.NORTH;
                        break;


                }
                if(autoUpdate) {
                    drawRobot();
                    setRobotStatus("turning right");
                }
                if(service != null)
                service.write(Constants.ACTION_ROATE_RIGHT.getBytes());
            }
        });
    }

    public void initializeAutoUpdate(){
        final Thread t = new Thread(){
            @Override
            public void run(){
                while(!isInterrupted()){
                    try{
                        Thread.sleep(500);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(autoUpdate) {
                                   // setLocationDataHex(currentLocation);
                                    setRobotStatus(currentStatus);
                                   // drawRobot();
                                }
                            }
                        });
                    }catch(InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        };

        currentLocation = currentStatus = "";
        autoUpdateToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    manualUpBtn.setVisibility(View.GONE);
                    autoUpdate = true;
                    if(!t.isAlive())
                        t.start();
                }else{
                    manualUpBtn.setVisibility(View.VISIBLE);
                    autoUpdate = false;
                }
            }
        });

        autoUpdateToggleRobot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setRobot = true;
                }else{
                    setRobot = false;
                }
            }
        });


        autoUpdateToggleWaypoint.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    setWaypoint = true;
                }else{
                    setWaypoint = false;
                }
            }
        });

        manualUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawRobot();

                setLocationDataHex(currentLocation);
                setRobotStatus(currentStatus);
             }
        });
    }


    public void redrawRobot(){





    }


    public void drawMap(){




        grids[14].setBackgroundColor(Color.GRAY);
        grids[13].setBackgroundColor(Color.GRAY);
        grids[12].setBackgroundColor(Color.GRAY);
        grids[29].setBackgroundColor(Color.GRAY);
        grids[28].setBackgroundColor(Color.GRAY);
        grids[27].setBackgroundColor(Color.GRAY);
        grids[44].setBackgroundColor(Color.GRAY);
        grids[43].setBackgroundColor(Color.GRAY);
        grids[42].setBackgroundColor(Color.GRAY);

        grids[285].setBackgroundColor(Color.GRAY);
        grids[286].setBackgroundColor(Color.GRAY);
        grids[287].setBackgroundColor(Color.GRAY);
        grids[270].setBackgroundColor(Color.GRAY);
        grids[271].setBackgroundColor(Color.GRAY);
        grids[272].setBackgroundColor(Color.GRAY);
        grids[255].setBackgroundColor(Color.GRAY);
        grids[256].setBackgroundColor(Color.GRAY);
        grids[257].setBackgroundColor(Color.GRAY);

        setLocationDataHex(currentLocation);


    }







    @Override
    protected void onStart() {
        super.onStart();
    }



    public void setupMap(){


        for(int i = 0 ; i < 300 ; i++){

        final ImageView btn = new ImageView(this);

        final int k = i;
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  btn.setBackgroundColor(Color.rgb(000, 000, 000));



                Log.i("AndroidMDP" , "Grid no " + String.valueOf(k));

                if(setRobot){








                    if((k%15 == 0)   || ((k + 1)%15 == 0)  || (k<=14) || (k>=285) )
                    {


                        Toast.makeText(MainActivity.this, "Invalid point!", Toast.LENGTH_SHORT).show();


                    }



                    else {

                        oldRobotLocation = robotLocation;
                        robotLocation = k;
                        String s = "robot start coordinate " + locationToCoordinate(robotLocation);
                        if (service != null)
                            service.write(s.getBytes());

                        drawRobot();
                    }

                }

                if(setWaypoint){


                    waypoint = k;
                    grids[waypoint].setBackgroundColor(Color.YELLOW);
                    String s = "coordinate " + locationToCoordinate(waypoint);
                    if(service != null)
                    service.write(s.getBytes());



                }










            }
        });
        grids[i] = btn;
        grids[i].setBackgroundColor(Color.WHITE);
        grids[i].setImageResource(R.drawable.square_cell);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();

        params.setGravity(Gravity.FILL);


        //nexus 7
        params.height = 45;
        params.width = 53;

        // v20
        //params.height = 80;
        //params.width = 88;

        grids[i].setLayoutParams(params);

        mapLayout.addView(grids[i]);
    }


        grids[14].setBackgroundColor(Color.GRAY);
        grids[13].setBackgroundColor(Color.GRAY);
        grids[12].setBackgroundColor(Color.GRAY);
        grids[29].setBackgroundColor(Color.GRAY);
        grids[28].setBackgroundColor(Color.GRAY);
        grids[27].setBackgroundColor(Color.GRAY);
        grids[44].setBackgroundColor(Color.GRAY);
        grids[43].setBackgroundColor(Color.GRAY);
        grids[42].setBackgroundColor(Color.GRAY);

        grids[285].setBackgroundColor(Color.GRAY);
        grids[286].setBackgroundColor(Color.GRAY);
        grids[287].setBackgroundColor(Color.GRAY);
        grids[270].setBackgroundColor(Color.GRAY);
        grids[271].setBackgroundColor(Color.GRAY);
        grids[272].setBackgroundColor(Color.GRAY);
        grids[255].setBackgroundColor(Color.GRAY);
        grids[256].setBackgroundColor(Color.GRAY);
        grids[257].setBackgroundColor(Color.GRAY);

        robotDirection = Constants.NORTH;
        oldRobotLocation = 144;
        robotLocation = 144;
        drawRobot();

    }

    public void updateRobot(String s){


        if(oldRobotLocation == -1){

            robotLocation = Integer.valueOf(s.substring(0,s.indexOf(",")));
            oldRobotLocation = robotLocation;


        }

        else {
            oldRobotLocation = robotLocation;
            robotLocation = Integer.valueOf(s.substring(0, s.indexOf(",")));
         }
        robotDirection = Integer.valueOf(s.substring(s.indexOf(",")+1));
        robotHistory.add(oldRobotLocation);

        drawRobot();





    }




    public String locationToCoordinate(int location){


        int x = location/15;
        int y = location%15;

        return  "(" + x + "," + y +  ")";






    }



    public void moveForward(){


        switch (robotDirection){

            case Constants.NORTH:






        }




    }


    public void drawRobot(){



        Log.i("AndroidMDP" , "old is " + String.valueOf(oldRobotLocation) + " new is " + String.valueOf(robotLocation));


     /*   int oldCenter = oldRobotLocation;
        int oldNorth = oldCenter - 15;
        int oldSouth = oldCenter + 15;
        int oldEast = oldCenter + 1;
        int oldWest = oldCenter - 1;
        int oldNorthwest = oldNorth - 1;
        int oldNortheast = oldNorth + 1;
        int oldSouthwest = oldSouth - 1;
        int oldSoutheast = oldSouth + 1;

        grids[oldCenter].setBackgroundColor(Color.RED);
        grids[oldNorth].setBackgroundColor(Color.RED);
        grids[oldSouth].setBackgroundColor(Color.RED);
        grids[oldEast].setBackgroundColor(Color.RED);
        grids[oldWest].setBackgroundColor(Color.RED);
        grids[oldNorthwest].setBackgroundColor(Color.RED);
        grids[oldNortheast].setBackgroundColor(Color.RED);
        grids[oldSouthwest].setBackgroundColor(Color.RED);
        grids[oldSoutheast].setBackgroundColor(Color.RED);*/


        if(setRobot){

            int oldCenter = oldRobotLocation;
            int oldNorth = oldCenter - 15;
            int oldSouth = oldCenter + 15;
            int oldEast = oldCenter + 1;
            int oldWest = oldCenter - 1;
            int oldNorthwest = oldNorth - 1;
            int oldNortheast = oldNorth + 1;
            int oldSouthwest = oldSouth - 1;
            int oldSoutheast = oldSouth + 1;

            grids[oldCenter].setBackgroundColor(Color.WHITE);
            grids[oldNorth].setBackgroundColor(Color.WHITE);
            grids[oldSouth].setBackgroundColor(Color.WHITE);
            grids[oldEast].setBackgroundColor(Color.WHITE);
            grids[oldWest].setBackgroundColor(Color.WHITE);
            grids[oldNorthwest].setBackgroundColor(Color.WHITE);
            grids[oldNortheast].setBackgroundColor(Color.WHITE);
            grids[oldSouthwest].setBackgroundColor(Color.WHITE);
            grids[oldSoutheast].setBackgroundColor(Color.WHITE  );



        }





        for (int i = 0 ; i < robotHistory.size(); i ++){


          //  if(exploredRobotHistory.contains(robotHistory.get(i)) != true) {


                int oldCenter = robotHistory.get(i);
                int oldNorth = oldCenter - 15;
                int oldSouth = oldCenter + 15;
                int oldEast = oldCenter + 1;
                int oldWest = oldCenter - 1;
                int oldNorthwest = oldNorth - 1;
                int oldNortheast = oldNorth + 1;
                int oldSouthwest = oldSouth - 1;
                int oldSoutheast = oldSouth + 1;

                grids[oldCenter].setBackgroundColor(Color.RED);
                grids[oldNorth].setBackgroundColor(Color.RED);
                grids[oldSouth].setBackgroundColor(Color.RED);
                grids[oldEast].setBackgroundColor(Color.RED);
                grids[oldWest].setBackgroundColor(Color.RED);
                grids[oldNorthwest].setBackgroundColor(Color.RED);
                grids[oldNortheast].setBackgroundColor(Color.RED);
                grids[oldSouthwest].setBackgroundColor(Color.RED);
                grids[oldSoutheast].setBackgroundColor(Color.RED);

                exploredRobotHistory.add(robotHistory.get(i));


         //   }

        }









        int center = robotLocation;
        int north = center - 15;
        int south = center + 15;
        int east = center + 1;
        int west = center - 1;
        int northwest = north - 1;
        int northeast = north + 1;
        int southwest = south - 1;
        int southeast = south + 1;


        if((center <= 299 && center >= 284) != true && (center <= 0 && center >= 14) != true) {

            drawMap();



            //draw the robot
            grids[center].setBackgroundColor(Color.GREEN);
            grids[north].setBackgroundColor(Color.rgb(000, 255, 000));
            grids[south].setBackgroundColor(Color.rgb(000, 255, 000));
            grids[east].setBackgroundColor(Color.rgb(000, 255, 000));
            grids[west].setBackgroundColor(Color.rgb(000, 255, 000));
            grids[northwest].setBackgroundColor(Color.DKGRAY);
            grids[northeast].setBackgroundColor(Color.DKGRAY);
            grids[southwest].setBackgroundColor(Color.DKGRAY);
            grids[southeast].setBackgroundColor(Color.DKGRAY);

            //draw the head

            switch (robotDirection) {

                case Constants.NORTH:
                    grids[center - 15].setBackgroundColor(Color.BLUE);
               /* if(oldRobotLocation != robotLocation){

                    grids[oldRobotLocation+14].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation+15].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation+16].setBackgroundColor(Color.RED);

                }*/
                    break;
                case Constants.SOUTH:
                    grids[center + 15].setBackgroundColor(Color.BLUE);
               /* if(oldRobotLocation != robotLocation){

                    grids[oldRobotLocation-14].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation-15].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation-16].setBackgroundColor(Color.RED);

                }*/
                    break;
                case Constants.EAST:
                    grids[center + 1].setBackgroundColor(Color.BLUE);
              /*  if(oldRobotLocation != robotLocation){

                    grids[oldRobotLocation-1].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation-16].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation+14].setBackgroundColor(Color.RED);

                }*/
                    break;
                case Constants.WEST:
                    grids[center - 1].setBackgroundColor(Color.BLUE);
              /*  if(oldRobotLocation != robotLocation){

                    grids[oldRobotLocation+1].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation-14].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation+15].setBackgroundColor(Color.RED);

                }*/
                    break;

            }

         }

    }

    public void updateRobotLocation(int i , int direction){

        int center = i;
        int north = i - 15;
        int south = i + 15;
        int east = i + 1;
        int west = i - 1;
        int northwest = north - 1;
        int northeast = north + 1;
        int southwest = south - 1;
        int southeast = south + 1;

        int x = 0 , y  = 0 , z = 0;

        grids[center].setBackgroundColor(Color.GREEN);
        grids[north].setBackgroundColor(Color.rgb(000, 255, 000));
        grids[south].setBackgroundColor(Color.rgb(000, 255, 000));
        grids[east].setBackgroundColor(Color.rgb(000, 255, 000));
        grids[west].setBackgroundColor(Color.rgb(000, 255, 000));
        grids[northwest].setBackgroundColor(Color.rgb(0, 0, 0));
        grids[northeast].setBackgroundColor(Color.rgb(0, 0, 0));
        grids[southwest].setBackgroundColor(Color.rgb(0, 0, 0));
        grids[southeast].setBackgroundColor(Color.rgb(0, 0, 0));

        switch(direction){
            case Constants.NORTH:
                y = center + 30;
                x =y - 1;
                z = y + 1;
                break;

            case Constants.SOUTH:
                y = center - 30;
                x = y - 1;
                z = y + 1;
                break;

            case Constants.EAST:
                y = center - 2;
                x = y + 15;
                z = y - 15;
                break;

            case Constants.WEST:
                y = center + 2;
                x = y + 15;
                z = y - 15;
                break;
        }
        grids[y].setBackgroundColor(Color.WHITE);
        grids[x].setBackgroundColor(Color.WHITE);
        grids[z].setBackgroundColor(Color.WHITE);
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
        builder.setMessage("Send string")
                .setTitle("Manual input");

        View view = getLayoutInflater().inflate(R.layout.my_alert_dialog,null);

        Button sendBtn = (Button)view.findViewById(R.id.sendBtn);
        Button saveBtn = (Button)view.findViewById(R.id.saveBtn);
        final EditText mEditText = (EditText)view.findViewById(R.id.mEditText);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                service.write(getString().toString().getBytes());
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storeString(mEditText.getText().toString());
            }
        });
        mEditText.setText(getString());
        builder.setView(view);

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

       /* int x = locationData/15;
        int y = locationData%15;

        data[x][y] = 1;*/

    }


    private void setLocationDataHex(String hex){
        String binary = "";

        for(int i = 0 ; i < hex.length() ; i++){
            char c = hex.charAt(i);
            //long hexlong = Long.parseLong(String.valueOf(c) , 16);
            //binary = binary + Long.toBinaryString(hexlong);
            binary = binary + HexToBinary.HexToBinary(String.valueOf(c));
        }

        Log.i("AndroidMDP" , "binary is " + binary);
        Log.i("AndroidMDP" , "Length of binary is " + binary.length());

     /*   int n = 512 - binary.length();
        for(int i = 0 ; i < n ; i++){
            binary = "0" + binary;
        }

        binary = binary.substring(211,binary.length()-1);*/

        for (int i = 0 ; i < binary.length() ; i++){
            char c = binary.charAt(i);
            if(c == '1'){
                setLocationData(i);
                MDF[i] = 1;
                grids[i].setBackgroundColor(Color.BLACK);
            }

            else{

                MDF[i] = 0;

            }
        }

        updateMap(data);
    }

    private void turnLeft(){

        switch(robotDirection){
            case Constants.NORTH:
                head = head + 14;
                robotDirection = Constants.WEST;
                break;

            case Constants.SOUTH:
                head = head - 14;
                robotDirection = Constants.EAST;
                break;

            case Constants.EAST:
                head = head - 16;
                robotDirection = Constants.NORTH;
                break;

            case Constants.WEST:
                head = head + 16;
                robotDirection = Constants.SOUTH;
                break;
        }
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



        //make device discoverable
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
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
                    Log.i("AndroidMDP" ,ss);

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


                    if(s.startsWith("{")){


                        try {
                            JSONObject jObject = new JSONObject(s);
                            s = jObject.getString("grid");
                            Log.i("AndroidMAP" , "s is " + s);
                       /* s = s.substring(s.indexOf(":")+1, s.length());
                        s = s.substring(0 , s.indexOf("/") );*/
                            currentLocation = s;

                            if(autoUpdate) {
                                setLocationDataHex(s);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    if(s.startsWith("#status:")){
                        s = s.substring(s.indexOf(":")+1, s.length());
                        s = s.substring(0, s.indexOf("/"));
                        s = s.toLowerCase();
                        currentStatus = s;
                    }
                    if(s.startsWith("#robotlocation:")){
                        s = s.substring(s.indexOf(":")+1, s.length()-1);
                        s = s.substring(0 , s.indexOf("/") );
                        updateRobot(s);
                    }

                }
            }
        };

        //starts the bluetooth
        service = new BluetoothService(this,mHandler,mBluetoothAdapter , deviceAddress);
        service.startThreads();
    }

    private void setRobotStatus(String s) {
        switch(s){
            case "exploring":
                curStatus.setText("Exploring");
                break;
            case "fastest path:":
                curStatus.setText("Exploring Fastest Path");
                break;
            case "turning left":
                curStatus.setText("Turning Left");
                break;
            case "turning right":
                curStatus.setText("Turning Right");
                break;
            case "moving forward":
                curStatus.setText("Moving Forward");
                break;
            case "reversing":
                curStatus.setText("Reversing");
                break;
            default:
                curStatus.setText("None");
        }
    }


    private void toast(String stuff){
        Toast.makeText(this, stuff ,Toast.LENGTH_SHORT).show();
    }

    private void storeString(String mString){
        SharedPreferences mSharedPreferences = getSharedPreferences("buttonString", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("mString", mString);
        mEditor.apply();
    }

    private String getString(){
        SharedPreferences mSharedPreferences = getSharedPreferences("buttonString",MODE_PRIVATE);
        String mSavedString = mSharedPreferences.getString("mString","Old String");
        return mSavedString;
    }
}
