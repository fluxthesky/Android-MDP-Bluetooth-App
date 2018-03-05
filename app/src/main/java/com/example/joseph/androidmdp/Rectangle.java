package com.example.joseph.androidmdp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by Joseph on 22/2/2018.
 */


 public class Rectangle extends View {

    ShapeDrawable rect;


    int x = 10;
    int y = 10;
    int width = 50;
    int height = 50;
    int space = 5;
    LayoutInflater inflater;


    public Rectangle(Context context,AttributeSet attributeSet) {

        super(context,attributeSet);





        rect = new ShapeDrawable(new RectShape());
        rect.getPaint().setColor(Color.rgb(255, 000, 000));
        rect.setBounds(x, y, x + width, y + height);


    }


    public Rectangle(Context context) {

        super(context);

         rect = new ShapeDrawable(new RectShape());
         rect.getPaint().setColor(Color.rgb(255, 000, 000));

        rect.setBounds(x, y, x + width, y + height);


    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {



        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        canvas.drawRect(30, 30, 80, 80, paint);
        paint.setStrokeWidth(0);
        paint.setColor(Color.CYAN);
        canvas.drawRect(33, 60, 77, 77, paint );
        paint.setColor(Color.YELLOW);
        canvas.drawRect(33, 33, 77, 60, paint );

    }
}
