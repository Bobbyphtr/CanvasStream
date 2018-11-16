package com.xenoire.canvasstream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyCanvasView extends View{

    private Canvas mBuffer;
    private Bitmap mBitmap;
    private Paint mPaint;
    private Path mPath;
    private Paint mBitmapPaint = new Paint();
    private DatabaseReference ref;
    private ChildEventListener mListener;
    private int mLastX, mLastY;
    private float mScale;

    private Set<String> mOutstandingStrokes;
    private Stroke mCurrentStroke;

    private int mCanvasWidth;
    private int mCanvasHeight;

    private int strokeWidth;
    private int colorNow = Color.BLACK;

    private final int DEFAULT_COLOR = Color.BLACK;
    private final int DEFAULT_BG_COLOR = Color.GRAY;
    private final int DEFAULT_STROKE_WIDTH = 120;
    private static final float TOUCH_TOLERANCE = 4;
    private static final int PIXEL_SIZE = 8;

    public MyCanvasView(Context context, DatabaseReference ref) {
        this(context, null, ref, 1.0f);
    }

    public MyCanvasView(Context context, @Nullable AttributeSet attrs, DatabaseReference ref, int mCanvasWidth, int mCanvasHeight) {
        this(context, ref);
        this.setBackgroundColor(Color.GRAY);
        this.mCanvasWidth = mCanvasWidth;
        this.mCanvasHeight = mCanvasHeight;
    }

    public MyCanvasView(Context context, @Nullable AttributeSet attrs, DatabaseReference ref, float scale) {
        super(context, attrs);

        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE);

        mOutstandingStrokes = new HashSet<String>();
        mPath = new Path();

        this.ref = ref;
        this.mScale = scale;

        mListener = ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String name = dataSnapshot.getKey();
                if(!mOutstandingStrokes.contains(name)){
                    Stroke stroke = dataSnapshot.getValue(Stroke.class);
                    drawStroke(stroke, paintFromColor(stroke.getColor()));

                    invalidate();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(DEFAULT_COLOR);
        mPaint.setStyle(Paint.Style.STROKE);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);

    }

    private  Paint paintFromColor (int color){
        return paintFromColor(color, Paint.Style.STROKE);
    }

    private  Paint paintFromColor (int color, Paint.Style style){
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setColor(color);
        p.setStyle(style);
        return p;
    }

    private void drawStroke(Stroke stroke, Paint p){
        if(mBuffer != null){
            mBuffer.drawPath(getPathForPoints(stroke.getPoints(), mScale), p);
        }
    }

    private Path getPathForPoints(List<Point> points, float scale){
        Path path = new Path();
        scale = scale * PIXEL_SIZE;
        Point current = points.get(0);
        path.moveTo(Math.round(scale * current.x), Math.round(scale * current.y));
        Point next = null;
        for (int i = 1; i < points.size(); ++i) {
            next = points.get(i);
            path.quadTo(
                    Math.round(scale * current.x), Math.round(scale * current.y),
                    Math.round(scale * (next.x + current.x) / 2), Math.round(scale * (next.y + current.y) / 2)
            );
            current = next;
        }
        if (next != null) {
            path.lineTo(Math.round(scale * next.x), Math.round(scale * next.y));
        }
        return path;
    }

    // override onSizeChanged
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mScale = Math.min(1.0f * w / mCanvasWidth, 1.0f * h / mCanvasHeight);

        // your Canvas will draw onto the defined Bitmap
        mBitmap = Bitmap.createBitmap(Math.round(mCanvasWidth * mScale),
                Math.round(mCanvasHeight * mScale),
                Bitmap.Config.ARGB_8888);
        mBuffer = new Canvas(mBitmap);
        Log.i("AndroidDrawing", "onSizeChanged: created bitmap/buffer of "+
                mBitmap.getWidth()+"x"+mBitmap.getHeight());
    }

    public void clearAll(){
        mBitmap = Bitmap.createBitmap(mBitmap.getWidth(),
                mBitmap.getHeight(),
                Bitmap.Config.ARGB_8888);
        mBuffer = new Canvas(mBitmap);
        mCurrentStroke = null;
        mOutstandingStrokes.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.DKGRAY);
        canvas.drawRect(0, 0, mBitmap.getWidth(),
                mBitmap.getHeight(),
                paintFromColor(Color.WHITE, Paint.Style.FILL_AND_STROKE));
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        canvas.drawPath(mPath, mPaint);
    }

    private void touchStart(float x, float y) {
        mPath.reset();
        mPath.moveTo(x, y);
        mCurrentStroke = new Stroke(colorNow, DEFAULT_STROKE_WIDTH);
        mLastX = (int) x / PIXEL_SIZE;
        mLastY = (int) y / PIXEL_SIZE;
        mCurrentStroke.addPoint(mLastX, mLastY);
    }
    private void touchMove(float x, float y) {
            int x1 = (int) x / PIXEL_SIZE;
            int y1 = (int) y / PIXEL_SIZE;

            float dx = Math.abs(x1 - mLastX);
            float dy = Math.abs(y1 - mLastY);
            if (dx >= 1 || dy >= 1) {
                mPath.quadTo(mLastX * PIXEL_SIZE,
                        mLastY * PIXEL_SIZE,
                        ((x1 + mLastX) * PIXEL_SIZE) / 2,
                        ((y1 + mLastY) * PIXEL_SIZE) / 2);
                mLastX = x1;
                mLastY = y1;
                mCurrentStroke.addPoint(mLastX, mLastY);
            }
    }

    private void touchUp() {
       mPath.lineTo(mLastX * PIXEL_SIZE, mLastY * PIXEL_SIZE);
       mBuffer.drawPath(mPath,mPaint);
       mPath.reset();

       DatabaseReference strokeRef = ref.push();
       final String strokeName = strokeRef.getKey();
       mOutstandingStrokes.add(strokeName);

        // create a scaled version of the segment, so that it matches the size of the board
        Stroke stroke = new Stroke(mCurrentStroke.getColor(), DEFAULT_STROKE_WIDTH);
        for (Point point : mCurrentStroke.getPoints()){
            stroke.addPoint(Math.round(point.x / mScale), Math.round(point.y / mScale));
        }

        strokeRef.setValue(stroke, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e("AndroidDrawing", databaseError.toString());
                    throw databaseError.toException();
                }
                mOutstandingStrokes.remove(strokeName);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchStart(x,y);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                touchMove(x,y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                //System.out.println("Action Up");
                touchUp();
                invalidate();
                break;
        }
        return true;
    }

    public void cleanUp(){
        ref.removeEventListener(mListener);
    }

}
