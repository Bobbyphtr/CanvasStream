package com.xenoire.canvasstream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ShareActivity extends AppCompatActivity {
    EditText userField;
    Button btnShare;
    DatabaseReference boardRef, mRef;
    String emailUser;
    String boardShared;
    FirebaseAuth mAuth;
    Map<String, Object> metadata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        userField = findViewById(R.id.userField);
        btnShare = findViewById(R.id.btnShare);

        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        Intent shareIntent = getIntent();
        if(shareIntent.getExtras()!=null) {
            emailUser = shareIntent.getStringExtra("Email");
            boardShared = shareIntent.getStringExtra("Board");
        }

        getMetaDatas(boardShared);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userField.getText().toString().equalsIgnoreCase("")) {
                    emailUser = swapString(userField.getText().toString());
                    boardRef = FirebaseDatabase.getInstance().getReference("Users").child(swapString(emailUser)).child("boardmetas");
                    boardRef.child(boardShared).setValue(metadata);
                    userField.setText("");
                    Intent balik = new Intent(ShareActivity.this, DrawActivity.class);
                    balik.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    balik.putExtra("BOARD_ID", boardShared);
                    balik.putExtra("EMAIL", mAuth.getCurrentUser().getEmail());
                    startActivity(balik);
                    Toast.makeText(ShareActivity.this,"Boards Successfully Shared!",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ShareActivity.this,"Input the Email Field!",Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
    private String swapString(String email){
        email = email.replace('.', '0');
        email = email.replace('_', '1');
        email = email.replace('@', '2');
        Log.i("SWAP", email);
        return  email;
    }

    private void getMetaDatas(String boardShared){
        Log.i("CURRENT USER", mAuth.getCurrentUser().getEmail());
        mRef.child("Users").child(swapString(mAuth.getCurrentUser().getEmail())).child("boardmetas").child(boardShared).
                addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                metadata = (HashMap) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
