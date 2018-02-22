package com.example.joseph.androidmdp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;

/**
 * Created by Joseph on 9/2/2018.
 */

public class Map extends View {

    private ShapeDrawable mDrawable;
    private ShapeDrawable mDrawable2;
    private ShapeDrawable[][] shapes = new ShapeDrawable[20][16];
    int x = 10;
    int y = 10;
    int width = 50;
    int height = 50;
    int space = 5;
    public Map (Context context , AttributeSet attributeSet) {

        super(context, attributeSet);



        for (int i = 0; i < 20; i++) {

            if (i == 0) {
                y = 10;
            } else {

                y += height + space;
            }


            for (int j = 0; j < 15; j++) {
                shapes[i][j] = new ShapeDrawable(new RectShape());
                shapes[i][j].getPaint().setColor(Color.rgb(255, 000, 000));
                if (j == 0)
                    x = 10;
                else
                    x += width + space;
                shapes[i][j].setBounds(x, y, x + width, y + height);
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
                shapes[i][j].draw(canvas);
            }
        }
    }
}
