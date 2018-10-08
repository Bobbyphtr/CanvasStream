package com.xenoire.canvasstream;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.DoubleToIntFunction;

public class MyCanvas extends View{

    List<Stroke> paths;
    SparseArray<Stroke> activePaths; //Mengambil stroke yang sedang di gambar
    /*
    Sparse Array : Dia seperti hashMap tetapi lebih efficient dan memory friendly
     */

    int color = 0;

    public MyCanvas(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paths = new ArrayList<>();
        activePaths = new SparseArray<>();
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (Stroke s : paths){
            if(s != null) {
                if(color == 0){
                    System.out.println("color = 0");
                    s.getPaint().setColor(Color.BLACK);
                } else {
                    s.getPaint().setColor(color);
                }
                System.out.println(s.getPaint().getColor());
                canvas.drawPath(s.getPath(), s.getPaint());
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Paint paint = new Paint();
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeJoin(Paint.Join.ROUND);
                paint.setStrokeCap(Paint.Cap.ROUND);
                paint.setStrokeWidth(15);
                Stroke stroke = new Stroke(paint);
                stroke.drawPath(x,y);
                System.out.println("Axtion Down Color : " + color);
                stroke.setColor(color);
                activePaths.append(0 ,stroke);
                paths.add(stroke);
                return true;
            case MotionEvent.ACTION_MOVE:
                for (int pointerId = 0; pointerId < event.getPointerCount(); pointerId++){
                    pointMove(x, y, pointerId);
                }
                break;
            case MotionEvent.ACTION_UP:
                //System.out.println("Action Up");
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    private void pointMove(float x, float y, int id){
        Stroke stroke = activePaths.get(id);
        if(stroke != null){
            stroke.drawPath(x, y);
        }
    }

    public void setColor(int color){
       this.color = color;
       System.out.println("Color : " + this.color);
    }
}
