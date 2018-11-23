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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ShareActivity extends AppCompatActivity {
    EditText userField;
    Button btnShare;
    DatabaseReference boardRef;
    String sharedUserId;
    String boardShared;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        userField = findViewById(R.id.userField);
        btnShare = findViewById(R.id.btnShare);

        Intent shareIntent = getIntent();
        if(shareIntent.getExtras()!=null) {
            boardShared = shareIntent.getStringExtra("Board");
        }

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userField.getText().toString().equalsIgnoreCase("")) {
                    sharedUserId = swapString(userField.getText().toString());
                    boardRef = FirebaseDatabase.getInstance().getReference("Users").child(sharedUserId).child("board");
                    boardRef.push().setValue(boardShared);
                    userField.setText("");
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
}
