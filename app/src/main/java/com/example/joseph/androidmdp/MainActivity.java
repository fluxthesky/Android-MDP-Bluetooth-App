package com.example.joseph.androidmdp;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.DialogInterface.*;

public class MainActivity extends AppCompatActivity {


    DrawerLayout mDrawerLayout;
    NavigationView mNavigationView;
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
    int robotLocation = 271;
    TextView curStatus;
    TextView timerText;
    int timerSecond = 0;
    int timerMinute = 0;
    Timer mTimer;


    int head = robotLocation - 15;
    int robotDirection = Constants.NORTH;
    int oldRobotLocation = -1;
    Thread t;
    int MDF[] = new int[300];
    IncomingMessageDialog icd;


    String testString = "F8007000E00000000000000000000000000000000000000000000000000000000007000E001F";
    boolean testStringOnce = true;

    String fullyExplored = "FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF";

    boolean exploring = false;


    int waypoint = 0;

    boolean once = true;


    boolean initialStart = true;








    ArrayList<Integer> robotHistory = new ArrayList<Integer>();
    ArrayList<Integer> exploredRobotHistory = new ArrayList<Integer>();


    boolean autoUpdate = true;
    boolean setRobot = false;
    boolean setWaypoint = false;



    Button manualUpBtn;
    ToggleButton autoUpdateToggle;
    ToggleButton autoUpdateToggleRobot;
    ToggleButton autoUpdateToggleWaypoint;
    Button startExpBtn;



    String mapMDF = Constants.BLANK_MAP , currentStatus;





    ImageButton up;
    ImageButton down;
    ImageButton left;
    ImageButton right;

    Thread autoUpdateThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView temp = (TextView) findViewById(R.id.chatBox);
         up = (ImageButton) findViewById(R.id.up);
         down = (ImageButton) findViewById(R.id.down);
         left = (ImageButton) findViewById(R.id.left);
         right = (ImageButton) findViewById(R.id.right);
        mMap = (Map) findViewById(R.id.map);
        timerText = (TextView) findViewById(R.id.timer);

        timerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopTimer();
            }
        });

        mapLayout = (android.support.v7.widget.GridLayout) findViewById(R.id.map_grid);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);

        icd = new IncomingMessageDialog(this);




        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){


                    case R.id.set_waypoint:
                        if(!item.isChecked()){
                            item.setChecked(true);

                            setWaypoint = true;
                            setRobot = false;
                            autoUpdateToggleRobot.setChecked(false);
                        }else{
                            setWaypoint = false;
                            item.setChecked(false);
                        }
                        break;
                    case R.id.set_robot:
                        if(!item.isChecked()){
                            item.setChecked(true);

                            setRobot = true;
                            setWaypoint = false;
                            autoUpdateToggleWaypoint.setChecked(false);
                        }else{
                            setRobot = false;
                            item.setChecked(false);

                        }
                        break;

                    case R.id.auto_update:
                        if(!item.isChecked()){
                            item.setChecked(true);
                            manualUpBtn.setVisibility(View.GONE);
                            autoUpdate = true;
                            if(!t.isAlive())
                                t.start();
                        }else{
                            manualUpBtn.setVisibility(View.VISIBLE);
                            autoUpdate = false;
                            item.setChecked(false);

                        }
                        break;

                    case R.id.start_exploration:
                        item.setChecked(false);
                        startExploration();
                        break;

                    case R.id.clear_map:


                        clearMap();
                        break;

                    case R.id.fastest_path:

                        item.setChecked(false);
                        startFastestPath();
                        break;




                }

                 mDrawerLayout.closeDrawers();

                return true;
            }
        });


        Rectangle rect = new Rectangle(this);
        grids = new ImageView[300];

        curStatus = (TextView)findViewById(R.id.curStatus);

        manualUpBtn = (Button) findViewById(R.id.manualUpBtn);
        manualUpBtn.setVisibility(View.GONE);
        autoUpdateToggle = (ToggleButton) findViewById(R.id.autoUpdateToggle);
        autoUpdateToggleRobot = (ToggleButton) findViewById(R.id.robotUpBtn);
        autoUpdateToggleWaypoint = (ToggleButton) findViewById(R.id.waypointUpBtn);

        startExpBtn = (Button)findViewById(R.id.startExpBtn);

        int[][] data = new int[20][15];

        for(int i = 0; i < 20; i++){
            for(int j = 0; j < 15; j++) {
                data[i][j] = 0;
            }
        }

        data[3][13] = 1;
        data[3][14] = 1;

        updateMap(data);

       // setupMap();

        IntentFilter filter1 = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);


        mReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                    switch(state) {
                        case BluetoothAdapter.STATE_OFF:

                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:

                            Toast.makeText(MainActivity.this,"Bluetooth turned off!" , Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothAdapter.STATE_ON:

                            setupBluetooth();
                            break;
                        case BluetoothAdapter.STATE_TURNING_ON:

                            break;
                    }

                }
            }
        };


        registerReceiver(mReceiver, filter1);



        initializeAutoUpdate();

        messageTextView = temp;

        deviceAddress = getDeviceAddress();

       // deviceAddress = Constants.HARDWARE_ADDRESS;

       // deviceAddress = "40:E2:30:C7:30:C8";

        setupBluetooth();

        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              /*  moveForward();
                drawRobot();
                robotLocation -= 15;*/
            /*  oldRobotLocation = robotLocation;
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
                }*/
                if(service != null)
                service.write(Constants.ACTION_FORWARD.getBytes());

            }
        });
        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                /*updateRobotLocation(robotLocation + 15 , Constants.SOUTH);
                robotLocation += 15; */


           /*     oldRobotLocation = robotLocation;

                robotHistory.add(oldRobotLocation);

                switch (robotDirection){

                    case Constants.NORTH:
                        robotLocation+=15;
                        if(robotLocation>=285)
                        {

                            robotLocation-=15;

                        }
                        break;
                    case Constants.SOUTH:
                        robotLocation-=15;
                        if(robotLocation<=14)
                        {

                            robotLocation+=15;

                        }
                        break;
                    case Constants.EAST:
                        robotLocation-=1;
                        if(robotLocation%15 == 0)
                        {

                            robotLocation+=1;

                        }
                        break;
                    case Constants.WEST:
                        robotLocation+=1;
                        if((robotLocation + 1)%15 == 0)
                        {

                            robotLocation-=1;

                        }
                        break;


                }

                if(autoUpdate) {
                    drawRobot();
                    setRobotStatus("reversing");
                }
*/

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
/*
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

              */

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
/*
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

                */
                if(service != null)
                service.write(Constants.ACTION_ROATE_RIGHT.getBytes());
            }
        });

        startExpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startExploration();
            }
        });
    }


    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //uses free form text input
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //Puts a customized message to the prompt

        startActivityForResult(intent, 123);
    }



    @Override
    protected void onDestroy() {


        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    public void initializeAutoUpdate(){
         t = new Thread(){
            @Override
            public void run(){
                while(!isInterrupted()){
                    try{
                        Thread.sleep(2000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(autoUpdate && !exploring) {
                                   // setLocationDataHex(mapMDF);
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

       // mapMDF = currentStatus = "";
        currentStatus = "";
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
                    setWaypoint = false;
                    autoUpdateToggleWaypoint.setChecked(false);
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
                    setRobot = false;
                    autoUpdateToggleRobot.setChecked(false);
                }else{
                    setWaypoint = false;
                }
            }
        });

        manualUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawRobot();

                setLocationDataHex(mapMDF);
                setRobotStatus(currentStatus);
             }
        });
    }


    public void redrawRobot(){

    }

    public void drawStartingAndEnding(){
        grids[2].setBackgroundColor(Color.GRAY);
        grids[1].setBackgroundColor(Color.GRAY);
        grids[0].setBackgroundColor(Color.GRAY);
        grids[17].setBackgroundColor(Color.GRAY);
        grids[16].setBackgroundColor(Color.GRAY);
        grids[15].setBackgroundColor(Color.GRAY);
        grids[32].setBackgroundColor(Color.GRAY);
        grids[31].setBackgroundColor(Color.GRAY);
        grids[30].setBackgroundColor(Color.GRAY);

        grids[299].setBackgroundColor(Color.GRAY);
        grids[298].setBackgroundColor(Color.GRAY);
        grids[297].setBackgroundColor(Color.GRAY);
        grids[284].setBackgroundColor(Color.GRAY);
        grids[283].setBackgroundColor(Color.GRAY);
        grids[282].setBackgroundColor(Color.GRAY);
        grids[269].setBackgroundColor(Color.GRAY);
        grids[268].setBackgroundColor(Color.GRAY);
        grids[267].setBackgroundColor(Color.GRAY);
    }


    public void drawMap(){







       /* grids[14].setBackgroundColor(Color.GRAY);
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
        grids[257].setBackgroundColor(Color.GRAY);*/


        grids[2].setBackgroundColor(Color.GRAY);
        grids[1].setBackgroundColor(Color.GRAY);
        grids[0].setBackgroundColor(Color.GRAY);
        grids[17].setBackgroundColor(Color.GRAY);
        grids[16].setBackgroundColor(Color.GRAY);
        grids[15].setBackgroundColor(Color.GRAY);
        grids[32].setBackgroundColor(Color.GRAY);
        grids[31].setBackgroundColor(Color.GRAY);
        grids[30].setBackgroundColor(Color.GRAY);

        grids[299].setBackgroundColor(Color.GRAY);
        grids[298].setBackgroundColor(Color.GRAY);
        grids[297].setBackgroundColor(Color.GRAY);
        grids[284].setBackgroundColor(Color.GRAY);
        grids[283].setBackgroundColor(Color.GRAY);
        grids[282].setBackgroundColor(Color.GRAY);
        grids[269].setBackgroundColor(Color.GRAY);
        grids[268].setBackgroundColor(Color.GRAY);
        grids[267].setBackgroundColor(Color.GRAY);






        setLocationDataHex(mapMDF);


    }







    @Override
    protected void onStart() {
        super.onStart();

       // setupMap();

    }

    public void startFastestPathExploration(){

        {


            if(service != null) {
                service.write("#sfp".getBytes());
                curStatus.setText("Exploring");
                exploring = true;
            }else{

                Toast.makeText(this, "Start fastest path exploration failed!", Toast.LENGTH_SHORT).show();


            }

        }
    }


    public void startFastestPath(){
        {


            if(service != null) {
                service.write("#sfp".getBytes());
                curStatus.setText("Fastest path");
                exploring = true;
                setupTimer();
            }else{

                Toast.makeText(this, "Start exploration failed!", Toast.LENGTH_SHORT).show();


            }

        }



    }


    public void startExploration(){


        if(service != null) {
            service.write("#se".getBytes());
            curStatus.setText("Exploring");
            exploring = true;
            setupTimer();
        }else{

            Toast.makeText(this, "Start exploration failed!", Toast.LENGTH_SHORT).show();


        }

    }

    public void clearMap(){

        for(int i = 0 ; i < 300 ; i++){
            grids[i].setBackgroundColor(Color.rgb(166,170,178));
        }

        MDF = new int[300];

        robotLocation = 16;
        oldRobotLocation = 16;

drawStartingAndEnding();
drawRobot();

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
                       /* if (service != null)
                            service.write(s.getBytes());
*/
                        drawRobot();
                    }

                }

                if(setWaypoint){


                    waypoint = k;
                    grids[waypoint].setBackgroundColor(Color.YELLOW);
                    String s = "#wp:" + locationToCoordinate(waypoint);
                    Log.i("AndroidMDP" , s);
                    if(service != null)
                    service.write(s.getBytes());





                }










            }
        });

        btn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                startVoiceRecognitionActivity();
                return false;
            }
        });
        grids[i] = btn;
        grids[i].setBackgroundColor(Color.rgb(166,170,178));
        grids[i].setImageResource(R.layout.rectangle);

        GridLayout.LayoutParams params = new GridLayout.LayoutParams();

        params.setGravity(Gravity.FILL);


        //nexus 7
        //params.height = 45;
        //params.width
            //
            // v20


        int w = mapLayout.getWidth();
        int h = mapLayout.getHeight();

            params.height = h / 20;
            params.width = w / 15;
       // Log.i("AndroidMDP" , String.valueOf(w)  + " value " + String.valueOf(h));








        grids[i].setLayoutParams(params);

        mapLayout.addView(grids[i]);
    }


      /*  grids[14].setBackgroundColor(Color.GRAY);
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
*/


        grids[2].setBackgroundColor(Color.GRAY);
        grids[1].setBackgroundColor(Color.GRAY);
        grids[0].setBackgroundColor(Color.GRAY);
        grids[17].setBackgroundColor(Color.GRAY);
        grids[16].setBackgroundColor(Color.GRAY);
        grids[15].setBackgroundColor(Color.GRAY);
        grids[32].setBackgroundColor(Color.GRAY);
        grids[31].setBackgroundColor(Color.GRAY);
        grids[30].setBackgroundColor(Color.GRAY);

        grids[299].setBackgroundColor(Color.GRAY);
        grids[298].setBackgroundColor(Color.GRAY);
        grids[297].setBackgroundColor(Color.GRAY);
        grids[284].setBackgroundColor(Color.GRAY);
        grids[283].setBackgroundColor(Color.GRAY);
        grids[282].setBackgroundColor(Color.GRAY);
        grids[269].setBackgroundColor(Color.GRAY);
        grids[268].setBackgroundColor(Color.GRAY);
        grids[267].setBackgroundColor(Color.GRAY);


        robotDirection = Constants.NORTH;


        oldRobotLocation = 16;
        robotLocation = 16;
        drawRobot();

    }



    public void updateRobot2(String s){

        int x = Integer.valueOf(s.substring(0,s.indexOf(",")));
        int y = Integer.valueOf(s.substring(s.indexOf(",")+1  , s.lastIndexOf(",")) );


        int direction = Integer.valueOf(s.substring(s.lastIndexOf(",")+1));

        Log.i("AndroidMDP" , String.valueOf(x) + " " + String.valueOf(y) + " " + String.valueOf(direction));


       // y = 19 - y;
        int location =  ( y * 15 ) + x;



        if(oldRobotLocation == -1){

            robotLocation = location;
            oldRobotLocation = robotLocation;


        }

        else {
            oldRobotLocation = robotLocation;
            robotLocation = location;
        }
        robotDirection = direction;
        robotHistory.add(oldRobotLocation);

        drawRobot();


    }


    public void updateRobot(String s){


        Log.i("AndroidMDP" , "Update robot called");


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
        //x = 19 - x;


        return  y + "," + x ;
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
            int oldNorth = oldCenter + 15;
            int oldSouth = oldCenter - 15;
            int oldEast = oldCenter + 1;
            int oldWest = oldCenter - 1;
            int oldNorthwest = oldNorth - 1;
            int oldNortheast = oldNorth + 1;
            int oldSouthwest = oldSouth - 1;
            int oldSoutheast = oldSouth + 1;



            grids[oldCenter].setBackgroundColor(Color.rgb(166,170,178));
            grids[oldNorth].setBackgroundColor(Color.rgb(166,170,178));
            grids[oldSouth].setBackgroundColor(Color.rgb(166,170,178));
            grids[oldEast].setBackgroundColor(Color.rgb(166,170,178));
            grids[oldWest].setBackgroundColor(Color.rgb(166,170,178));
            grids[oldNorthwest].setBackgroundColor(Color.rgb(166,170,178));
            grids[oldNortheast].setBackgroundColor(Color.rgb(166,170,178));
            grids[oldSouthwest].setBackgroundColor(Color.rgb(166,170,178));
            grids[oldSoutheast].setBackgroundColor(Color.rgb(166,170,178));



        }






/*

    for (int i = 0; i < robotHistory.size(); i++) {


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

        grids[oldCenter].setBackgroundColor(Color.WHITE);
        grids[oldNorth].setBackgroundColor(Color.WHITE);
        grids[oldSouth].setBackgroundColor(Color.WHITE);
        grids[oldEast].setBackgroundColor(Color.WHITE);
        grids[oldWest].setBackgroundColor(Color.WHITE);
        grids[oldNorthwest].setBackgroundColor(Color.WHITE);
        grids[oldNortheast].setBackgroundColor(Color.WHITE);
        grids[oldSouthwest].setBackgroundColor(Color.WHITE);
        grids[oldSoutheast].setBackgroundColor(Color.WHITE);

        exploredRobotHistory.add(robotHistory.get(i));


        //   }

    }


else if (i == 0 || i == 1 || i == 2 || i == 15 || i == 16 || i == 17
                        || i == 30 || i == 31 || i == 32 || i == 297 || i == 298 || i == 299
                        || i == 282 || i == 283 || i == 284 || i == 267 || i == 268 || i == 269){
                    grids[i].setBackgroundColor(Color.GRAY);



*/


        int oldCenter = oldRobotLocation;
        int oldNorth = oldCenter + 15;
        int oldSouth = oldCenter - 15;
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
        grids[oldSoutheast].setBackgroundColor(Color.WHITE);



        drawStartingAndEnding();


        int center = robotLocation;
        int north = center + 15;
        int south = center - 15;
        int east = center + 1;
        int west = center - 1;
        int northwest = north - 1;
        int northeast = north + 1;
        int southwest = south - 1;
        int southeast = south + 1;


        if((center <= 299 && center >= 284) != true && (center <= 0 && center >= 14) != true) {



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
                    grids[center + 15].setBackgroundColor(Color.BLUE);
               /* if(oldRobotLocation != robotLocation){

                    grids[oldRobotLocation+14].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation+15].setBackgroundColor(Color.RED);
                    grids[oldRobotLocation+16].setBackgroundColor(Color.RED);

                }*/
                    break;
                case Constants.SOUTH:
                    grids[center - 15].setBackgroundColor(Color.BLUE);
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


            case R.id.configure_hardware_device:
                configureDeviceAddres();
                break;

            case R.id.view_incoming_messages:
                icd.showDialog();
                break;




            default:
                return super.onOptionsItemSelected(item);

        }

        return super.onOptionsItemSelected(item);


    }


    private void showReceivedData(){


    }


    private void configureDeviceAddres(){



        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Manual input");

       /* View view = getLayoutInflater().inflate(R.layout.my_alert_dialog,null);

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
        builder.setView(view);*/




       final EditText mEditText = new EditText(this);
       mEditText.setText(getDeviceAddress());




        builder.setPositiveButton("OK", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                storeDeviceAddress(mEditText.getText().toString());

            }
        });
        builder.setView(mEditText);
        AlertDialog dialog = builder.create();
        dialog.show();
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


        if (requestCode == 123 && resultCode == RESULT_OK) {
            // Fill the list view with the strings the recognizer thought it could have heard
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            if(matches.contains("start exploration") || matches.contains("explore")){

                startExploration();

            }
            else if(matches.contains("move forward") || matches.contains("forward")){


                up.performClick();

            }
            else if(matches.contains("rotate left") || matches.contains("left")){


                left.performClick();

            }
            else if(matches.contains("rotate right") || matches.contains("right")){

                right.performClick();

            }
            else if(matches.contains("move back") || matches.contains("back") || matches.contains("reverse")){

                down.performClick();

            }
            for(int i = 0;i<20;i++){
                if(matches.contains("move forward "+i+" times") || matches.contains("forward "+i+" times")){
                    for(int j=i;j>0;j--){
                        up.performClick();
                    }
                }else if(matches.contains("move back "+i+" times") || matches.contains("back "+i+" times") || matches.contains("reverse "+i+" times")){
                    for(int j=i;j>0;j--){
                        down.performClick();
                    }
                }else if(matches.contains("rotate right "+i+" times") || matches.contains("right "+i+" times")){
                    for(int j=i;j>0;j--){
                        right.performClick();
                    }
                }else if(matches.contains("rotate left "+i+" times") || matches.contains("left "+i+" times")){
                    for(int j=i;j>0;j--){
                        left.performClick();
                    }
                }
            }






            //Turn on or off bluetooth here
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

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

    private String convertMDFFormat(String hex){



        String hex1 = hex.substring(0,15);
        String hex2 = hex.substring(15,30);
        String hex3 = hex.substring(30,45);
        String hex4 = hex.substring(45,60);
        String hex5 = hex.substring(60,75);
        String hex6 = hex.substring(75,90);
        String hex7 = hex.substring(90,105);
        String hex8 = hex.substring(105,120);
        String hex9 = hex.substring(120,135);
        String hex10 = hex.substring(135,150);
        String hex11 = hex.substring(150,165);
        String hex12 = hex.substring(165,180);
        String hex13 = hex.substring(180,195);
        String hex14 = hex.substring(195,210);
        String hex15 = hex.substring(210,225);
        String hex16 = hex.substring(225,240);
        String hex17 = hex.substring(240,255);
        String hex18 = hex.substring(255,270);
        String hex19 = hex.substring(270,285);
        String hex20 = hex.substring(285);
        Log.i("AndroidMDP" , "hex1 " + hex1);
        Log.i("AndroidMDP" , "hex2 " + hex2);
        Log.i("AndroidMDP" , "hex3 " + hex3);
        Log.i("AndroidMDP" , "hex4 " + hex4);
        Log.i("AndroidMDP" , "hex5 " + hex5);
        Log.i("AndroidMDP" , "hex6 " + hex6);
        Log.i("AndroidMDP" , "hex7 " + hex7);
        Log.i("AndroidMDP" , "hex8 " + hex8);
        Log.i("AndroidMDP" , "hex9 " + hex9);
        Log.i("AndroidMDP" , "hex10 " + hex10);
        Log.i("AndroidMDP" , "hex11 " + hex11);
        Log.i("AndroidMDP" , "hex12 " + hex12);
        Log.i("AndroidMDP" , "hex13 " + hex13);
        Log.i("AndroidMDP" , "hex14" + hex14);
        Log.i("AndroidMDP" , "hex15 " + hex15);
        Log.i("AndroidMDP" , "hex16 " + hex16);
        Log.i("AndroidMDP" , "hex17 " + hex17);
        Log.i("AndroidMDP" , "hex18 " + hex18);
        Log.i("AndroidMDP" , "hex19 " + hex19);
        Log.i("AndroidMDP" , "hex20 " + hex20);





        hex = hex20 + hex19 + hex18 + hex17 + hex16 + hex15 + hex14
                +hex13 + hex12 + hex11 + hex10 + hex9 + hex8 + hex7 + hex6
                +hex5 + hex4 + hex3 + hex2 + hex1;





        return hex;

    }

    private void setExplored(String hex){
        String binary = "";
        String zeros = "";

       // hex = hex.substring(0,75);




        for(int i = 0 ; i < hex.length() ; i++){
            char c = hex.charAt(i);
            //long hexlong = Long.parseLong(String.valueOf(c) , 16);
            //binary = binary + Long.toBinaryString(hexlong);
            binary = binary + HexToBinary.HexToBinary(String.valueOf(c));
        }

        Log.i("AndroidMDP" , "binary setExplored is " + binary);
        Log.i("AndroidMDP" , "Length of setExplored binary is " + binary.length());


        if(binary.length()>300) {
            binary = binary.substring(1);
            binary = binary.substring(1);
            binary = binary.substring(0, binary.length() - 1);
            binary = binary.substring(0, binary.length() - 1);
        }

      /*  if(binary.length()<300){

            int numOfPads = 300 - binary.length();
            zeros = "";
            for(int i = 0 ; i < numOfPads; i++){

                zeros  = "0"+ zeros;


            }

        }*/

      //  binary = zeros + binary;
       // binary = convertMDFFormat(binary);




     /*   int n = 512 - binary.length();
        for(int i = 0 ; i < n ; i++){
            binary = "0" + binary;
        }

        binary = binary.substring(211,binary.length()-1);*/


     //1 is for explored
        // 0 unexplored


        //unexplored portions


     /*   for(int i = 0 ; i <MDF.length; i++){

            if(MDF[i] == 1){

                if(i != 14 && i != 13 && i != 12 && i != 29 && i != 28 && i != 27
                        && i != 44 && i != 43 && i != 42 && i != 285 && i != 286 && i != 287
                        && i != 270 && i != 271 && i != 272 && i != 255 && i != 256 && i != 257
                        ) {


                    grids[i].setBackgroundColor(Color.rgb(166, 170, 178));
                }

            }

        }*/


        //set explored



        for (int i = 0 ; i < binary.length() ; i++){
            char c = binary.charAt(i);
            if(c == '1'){



               /* if(i != 14 && i != 13 && i != 12 && i != 29 && i != 28 && i != 27
                        && i != 44 && i != 43 && i != 42 && i != 285 && i != 286 && i != 287
                        && i != 270 && i != 271 && i != 272 && i != 255 && i != 256 && i != 257
                        )*/

                   // setLocationData(i);
                if(MDF[i] == 0){
                    MDF[i] = 1;}
                if(i != 0 && i != 1 && i != 2 && i != 15 && i != 16 && i != 17
                        && i != 30 && i != 31 && i != 32 && i != 297 && i != 298 && i != 299
                        && i != 282 && i != 283 && i != 284 && i != 267 && i != 268 && i != 269
                        ) {

                    if(MDF[i] == 1) {
                        grids[i].setBackgroundColor(Color.WHITE);
                    }
                }



               /* else if (i == 0 || i == 1 || i == 2 || i == 15 || i == 16 || i == 17
                        || i == 30 || i == 31 || i == 32 || i == 297 || i == 298 || i == 299
                        || i == 282 || i == 283 || i == 284 || i == 267 || i == 268 || i == 269){
                    grids[i].setBackgroundColor(Color.GRAY);

                }*/
            }

            else{

                MDF[i] = 0;


            }
        }

        updateMap(data);
//        setupMap();
        //      drawRobot();
    }




    private void setLocationDataHex(String hex){

        //for obstacles

        mapMDF = hex;

        String zeros = "";
      /*  if(hex.length()>75) {
            hex = hex.substring(0, 75);
        }*/
        String binary = "";



        for(int i = 0 ; i < hex.length() ; i++){
            char c = hex.charAt(i);
            //long hexlong = Long.parseLong(String.valueOf(c) , 16);
            //binary = binary + Long.toBinaryString(hexlong);
            binary = binary + HexToBinary.HexToBinary(String.valueOf(c));
        }

        Log.i("AndroidMDP" , "binary is " + binary);
        Log.i("AndroidMDP" , "Length of binary is " + binary.length());


      //  binary = binary.substring(1);
       // binary = binary.substring(1);

        if(binary.length() > 300) {
            binary = binary.substring(0, binary.length() - 1);
            binary = binary.substring(0, binary.length() - 1);
            binary = binary.substring(0, binary.length() - 1);
            binary = binary.substring(0, binary.length() - 1);

        }
        Log.i("AndroidMDP" , "binary after conversation is " + binary);
        Log.i("AndroidMDP" , "Length of binary after conversation is " + binary.length());




     /*   int n = 512 - binary.length();
        for(int i = 0 ; i < n ; i++){
            binary = "0" + binary;
        }

        binary = binary.substring(211,binary.length()-1);*/


/*
        if(binary.length()<300){

            int numOfPads = 300 - binary.length();
            zeros = "";
            for(int i = 0 ; i < numOfPads; i++){

                zeros  = "0"+ zeros;


            }

        }
*/
      //  binary = binary + zeros;

        //binary = convertMDFFormat(binary);




        Log.i("AndroidMDP" , "binary after convertMDFFormat is " + binary);
        Log.i("AndroidMDP" , "Length of binary after coconvertMDFFormatnversation is " + binary.length());


        int p = 0;

        for(int i = 0  ; i <MDF.length; i++){

            if(MDF[i] > 0){

               /* if(i != 0 && i != 1 && i != 2 && i != 15 && i != 16 && i != 17
                        && i != 30 && i != 31 && i != 32 && i != 297 && i != 298 && i != 299
                        && i != 282 && i != 283 && i != 284 && i != 267 && i != 268 && i != 269
                        ){
                grids[i].setBackgroundColor(Color.WHITE);
                }*/
                if(p<binary.length()) {
                    char c = binary.charAt(p);
                    if (c == '1') {

                        grids[i].setBackgroundColor(Color.BLACK);
                        MDF[i] = 2;
                    } else {

                    }
                }
                p++;
            }

        }

        //   12/4/2018 commented
/*
        for (int i = 0 ; i < binary.length() ; i++){
            char c = binary.charAt(i);
            if(c == '1'){
              //  setLocationData(i);
                MDF[i] = 1;
                grids[i].setBackgroundColor(Color.BLACK);
            }

            else{

                MDF[i] = 0;


            }
        }
*/


        updateMap(data);
//        setupMap();
  //      drawRobot();
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);



        if(once) {



            int w = mapLayout.getWidth();
            int h = mapLayout.getHeight();
            Log.i("AndroidMDP" , String.valueOf(w)  + " value onWindowFocus " + String.valueOf(h));

            setupMap();
            //setExplored(fullyExplored);
          // setLocationDataHex("00000000000700820004020F000208A41E000004604040808000000000000010002000400080");
          //  setLocationDataHex("0000000700C0100B80106A0E000004408081010000000000000020004000800100");
        }
        once = false;


    }

    private void setupBluetooth(){
        //ask if bluetooth is enabled
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }



        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){



            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

        }


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
                    icd.setMessage(ss);

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
                            mapMDF = s;

                            if(autoUpdate) {
                                setLocationDataHex(s);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    if (s.startsWith("#mdf1:")){


                        {



                            s = s.substring(s.indexOf(":")+1, s.length());
                            s = s.substring(0 , s.indexOf("/") );
                            mapMDF = s;

                            if(autoUpdate) {
                                if(testStringOnce){
                                    setExplored(testString);
                                    testStringOnce = false;
                                }else {
                                    setExplored(s);
                                }
                                drawRobot();
                            }
                        }


                    }


                    if(s.startsWith("#mdf2:")){


                        {



                       s = s.substring(s.indexOf(":")+1, s.length());
                        s = s.substring(0 , s.indexOf("/") );
                                mapMDF = s;

                                if(autoUpdate) {
                                    setLocationDataHex(s);
                                    drawRobot();
                                }
                            }


                        }

                    if(s.startsWith("#status:")){
                        s = s.substring(s.indexOf(":")+1, s.length());
                        s = s.substring(0, s.indexOf("/"));
                        s = s.toLowerCase();
                        currentStatus = s;
                    }


                    if(s.startsWith("#setrobot:")){

                        if(initialStart){
                            setRobot = true;
                        }

                        s = s.substring(s.indexOf(":")+1, s.length()-1);
                        s = s.substring(0 , s.indexOf("/") );
                        updateRobot2(s);
                        initialStart = false;
                        setRobot = false;

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
        exploring=false;
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


    private void storeDeviceAddress(String mString){
        SharedPreferences mSharedPreferences = getSharedPreferences("buttonString", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("hardware_address", mString);
        mEditor.apply();
    }



    private void storeString(String mString){
        SharedPreferences mSharedPreferences = getSharedPreferences("buttonString", MODE_PRIVATE);
        SharedPreferences.Editor mEditor = mSharedPreferences.edit();
        mEditor.putString("mString", mString);
        mEditor.apply();
    }

    private String getDeviceAddress(){


        SharedPreferences mSharedPreferences = getSharedPreferences("buttonString",MODE_PRIVATE);
        String mSavedString = mSharedPreferences.getString("hardware_address",Constants.HARDWARE_ADDRESS);
        return mSavedString;
    }

    private String getString(){
        SharedPreferences mSharedPreferences = getSharedPreferences("buttonString",MODE_PRIVATE);
        String mSavedString = mSharedPreferences.getString("mString","Old String");
        return mSavedString;
    }




    private void stopTimer(){
        if(mTimer != null) {
            mTimer.cancel();
            timerSecond = 0;
            timerMinute = 0;
        }
    }

    private void setupTimer(){




        TimerTask tt = new TimerTask() {
            @Override
            public void run() {

                timerSecond++;
                if(timerSecond == 60){
                    timerMinute++;
                    timerSecond = 0;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        timerText.setText(timerMinute + "m" + timerSecond + "s");

                    }
                });

            }
        };

        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(tt,0,1000);




    }



}
