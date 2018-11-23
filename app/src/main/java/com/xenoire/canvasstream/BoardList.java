package com.xenoire.canvasstream;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.xenoire.canvasstream.DrawActivity;
import com.xenoire.canvasstream.FirebaseListAdapter;
import com.xenoire.canvasstream.RegisterActivity;

import java.io.ObjectStreamException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class BoardList extends AppCompatActivity {

    ListView listView;
    ArrayAdapter<String> adapter;
    List<String> boardids;

    DatabaseReference mRef, mBoardsRef, mSegmentRef, mUserBoardRef;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ValueEventListener mConnectedListener;
    private FirebaseListAdapter<HashMap> mBoardListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_list);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null){
                    Intent loginIntent = new Intent(BoardList.this, LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                    finish();

                }
            }
        };

        setTitle("Board List");
        mRef = FirebaseDatabase.getInstance().getReference();
        mBoardsRef = mRef.child("boardmetas");
        mBoardsRef.keepSynced(true);
        mSegmentRef = mRef.child("boardsSegments");
        if(mAuth.getUid() != null){
            mUserBoardRef = mRef.child("Users").child(mAuth.getUid()).child("Boards");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);

        mConnectedListener = FirebaseDatabase.getInstance().getReference(".info/connected").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean connected = (Boolean) dataSnapshot.getValue();
                        if (connected) {
                            Toast.makeText(BoardList.this, "Connected to Firebase",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(BoardList.this, "Disconnected to Firebase",
                                    Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                }
        );

        final ListView boardList = (ListView) this.findViewById(R.id.boardList);
        mBoardListAdapter = new FirebaseListAdapter<HashMap>(mBoardsRef, HashMap.class, R.layout.board_element, this) {
            @Override
            protected void populateView(View v, HashMap model) {
                final View view = v;
                final String key = BoardList.this.mBoardListAdapter.getModelKey(model);
                mBoardsRef.child(key).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> boardValues = (Map<String, Object>) dataSnapshot.getValue();
                        ((TextView) view.findViewById(R.id.board_title)).setText((String) boardValues.get("boardName"));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        };

        boardList.setAdapter(mBoardListAdapter);
        boardList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openBoard(mBoardListAdapter.getModelKey(position));
            }
        });
        mBoardListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                boardList.setSelection(mBoardListAdapter.getCount() - 1);
            }
        });
    }

    private void createBoard() {
        // create a new board

        final String[] name = {""};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Board Name");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                name[0] = input.getText().toString();
                final DatabaseReference newBoardRef = mBoardsRef.push();
                Map<String, Object> newBoardValues = new HashMap<>();
                newBoardValues.put("createdAt", ServerValue.TIMESTAMP);
                Display display = getWindowManager().getDefaultDisplay();
                Point size = new Point();
                display.getSize(size);
                newBoardValues.put("boardName", name[0]);
                newBoardValues.put("width", size.x + 125);
                newBoardValues.put("height", size.y);
                newBoardRef.setValue(newBoardValues, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            System.out.println(databaseError.toString());
                            throw databaseError.toException();
                        } else {
                            // once the board is created, start a DrawingActivity on it
                            openBoard(newBoardRef.getKey());
                        }
                    }
                });
                DatabaseReference userBoard  = mUserBoardRef.push();
                userBoard.setValue(newBoardRef.getKey());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();


    }


    private void openBoard(String key) {
        System.out.println("Opening Board " + key);
        Toast.makeText(BoardList.this, "Opening board: " + key, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra("BOARD_ID", key);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_add:
                createBoard();
                break;

            case R.id.logout:
                mAuth.signOut();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.popup_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        return super.onContextItemSelected(item);
    }
}
