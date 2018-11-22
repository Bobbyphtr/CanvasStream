package com.xenoire.canvasstream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        userField = findViewById(R.id.userField);
        btnShare = findViewById(R.id.btnShare);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!userField.getText().toString().equalsIgnoreCase("")) {
                    sharedUserId = userField.getText().toString();
                    boardRef = FirebaseDatabase.getInstance().getReference("Users").child(sharedUserId).child("board");
                    boardRef.push().setValue("Tes");//Isi value diganti board-board yang dimiliki user yang sedang login
                    userField.setText("");
                    Toast.makeText(ShareActivity.this,"Boards Successfully Shared!",Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(ShareActivity.this,"Input the USER ID!",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
