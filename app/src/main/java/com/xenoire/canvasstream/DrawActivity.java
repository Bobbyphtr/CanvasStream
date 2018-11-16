package com.xenoire.canvasstream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class DrawActivity extends AppCompatActivity {
    public static final String TAG = "AndroidDrawing";
    MyCanvasView myCanvasView;
    ImageButton black, red;

    DatabaseReference mStrokeRef, mMetadataRef, mFirebaseRef;
    ValueEventListener mConnectedListener;

    int mBoardWidth, mBoardHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String boardId = getIntent().getStringExtra("BOARD_ID");
        Log.i(TAG, "Adding DrawingView for boardId "+boardId);
        mStrokeRef = FirebaseDatabase.getInstance().getReference("boardstrokes").child(boardId);
        mMetadataRef = FirebaseDatabase.getInstance().getReference("boardmetas").child(boardId);
        mMetadataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(myCanvasView != null) {
                    ((ViewGroup) (myCanvasView).getParent()).removeView(myCanvasView);
                    myCanvasView.cleanUp();
                    myCanvasView = null;
                    Log.i(TAG, "Canvas view is not null");
                }
                Map<String, Object> boardValues = (Map<String, Object>) dataSnapshot.getValue();
                if(boardValues != null && boardValues.get("width") != null &&
                        boardValues.get("height") != null){
                    mBoardWidth = ((Long) boardValues.get("width")).intValue();
                    mBoardHeight = ((Long) boardValues.get("height")).intValue();

                    myCanvasView = new MyCanvasView(DrawActivity.this,null, mStrokeRef,
                            mBoardWidth, mBoardHeight);
                    Log.i(TAG, "new Canvas View");
                    setContentView(myCanvasView);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        mConnectedListener = FirebaseDatabase.getInstance().getReference(".info/connected").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean connected = (Boolean) dataSnapshot.getValue();
                        if (connected){
                            Toast.makeText(DrawActivity.this, "Connected to Firebase",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(DrawActivity.this, "Disconnected to Firebase",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Clean up our listener so we don't have it attached twice.
       FirebaseDatabase.getInstance().getReference(".info/connected").removeEventListener(mConnectedListener);
        if (myCanvasView != null) {
            myCanvasView.cleanUp();
        }
        //this.updateThumbnail(mBoardWidth, mBoardHeight, mSegmentsRef, mMetadataRef);


    }
}
