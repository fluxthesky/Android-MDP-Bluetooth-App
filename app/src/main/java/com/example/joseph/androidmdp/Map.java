package com.example.joseph.androidmdp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

/**
 * Created by Joseph on 9/2/2018.
 */

public class Map extends View {

    private ShapeDrawable mDrawable;
    private ShapeDrawable mDrawable2;
    private ShapeDrawable[][] shapes = new ShapeDrawable[20][15];
    Rectangle rect;

    private Rectangle[][] rectarr = new Rectangle[20][15];


    int x = 10;
    int y = 10;
    int width = 50;
    int height = 50;
    int space = 5;
    public Map (Context context , AttributeSet attributeSet) {

        super(context, attributeSet);


        rect = new Rectangle(context);
        rect.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("AndroidMDP" , "YES IT IS WORKING");
            }
        });

        rect.rect.setBounds(x, y, x + width, y + height);


        for (int i = 0; i < 20; i++) {

            if (i == 0) {
                y = 10;
            } else {

                y += height + space;
            }


            for (int j = 0; j < 15; j++) {

                rectarr[i][j] = new Rectangle(context);
                rectarr[i][j].rect.getPaint().setColor(Color.rgb(255, 000, 000));

                shapes[i][j] = new ShapeDrawable(new RectShape());
                shapes[i][j].getPaint().setColor(Color.rgb(255, 000, 000));
                if (j == 0)
                    x = 10;
                else
                    x += width + space;
                shapes[i][j].setBounds(x, y, x + width, y + height);
                rectarr[i][j].rect.setBounds(x, y, x + width, y + height);
                rectarr[i][j].setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.i("AndroidMDP" , " Works!");
                    }
                });
                rectarr[i][j].invalidate();
            }


        }
    }


    public void updateMap(int[][] data){


        for (int i = 0; i < 20; i++) {

            if (i == 0) {
                y = 10;
            } else {

                y += height + space;
            }


            for (int j = 0; j < 15; j++) {

                shapes[i][j] = new ShapeDrawable(new RectShape());
                if(data[i][j] == 1) {
                    shapes[i][j].getPaint().setColor(Color.rgb(255, 000, 000));
                }else{

                    shapes[i][j].getPaint().setColor(Color.rgb(000, 255, 000));
                }
                if (j == 0)
                    x = 10;
                else
                    x += width + space;
                shapes[i][j].setBounds(x, y, x + width, y + height);
            }


        }


    }





    @Override
    protected void onDraw(Canvas canvas) {
       for(int i=0;i<20;i++){
            for(int j=0;j<15;j++){
                rectarr[i][j].draw(canvas);
            }
        }


    }
}
