package com.example.joseph.androidmdp;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

public class ListActivityBluetooth extends ListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_bluetooth);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list_item_view);
        setListAdapter(adapter);


    }
}
