package com.xenoire.canvasstream;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;

/*
Class ini tujuannya untuk menampung path. Jadi 1 canvas bisa punya path yang berbeda
dengan attribute yang berbeda pula.
 */
public class Stroke {

    private Path path;
    private Paint paint;

    public Stroke(Paint paint) {
        this.paint = paint;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Paint getPaint() {
        return paint;
    }

    public void setPaint(Paint paint) {
        this.paint = paint;
    }

    public void drawPath(float x, float y){
        if(path == null){
            path = new Path();
            path.moveTo(x, y);
        } else {
            path.lineTo(x, y);
        }
    }

    public void setColor(int color){
        paint.setColor(color);
    }
}
