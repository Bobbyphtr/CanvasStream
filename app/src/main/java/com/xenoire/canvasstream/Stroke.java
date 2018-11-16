package com.xenoire.canvasstream;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

/*
Class ini tujuannya untuk menampung path. Jadi 1 canvas bisa punya path yang berbeda
dengan attribute yang berbeda pula.
 */
public class Stroke {

    private List<Point> points = new ArrayList<Point>();
    private int color;
    int strokeWidth;


    public Stroke(){};

    public Stroke(int color, int strokeWidth) {

        this.color = color;
        this.strokeWidth = strokeWidth;

    }

    public  void  addPoint(int x, int y){
        Point p = new Point(x, y);
        points.add(p);
    }

    public List<Point> getPoints(){
        return points;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
