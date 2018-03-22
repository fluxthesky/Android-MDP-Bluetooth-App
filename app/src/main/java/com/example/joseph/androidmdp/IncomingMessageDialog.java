package com.example.joseph.androidmdp;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.widget.TextView;

/**
 * Created by Joseph on 22/3/2018.
 */

public class IncomingMessageDialog extends Dialog {

    Context context;
    TextView mTextView;
    String message;

    public IncomingMessageDialog(@NonNull Context context) {

        super(context);
        this.context = context;
        //this.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        this.setTitle("Incoming Messages");
        mTextView = new TextView(context);
        mTextView.setText("Hello world");
        this.setContentView(mTextView);

    }

    public void setMessage(String message) {
        mTextView.setText(message);
        //this.show();
    }



    public void showDialog() {

         this.show();


    }



}
