package com.xenoire.canvasstream;

import android.content.Intent;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField, emailField, passwordField;

    private Button registerButton;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setTitle("Register Canvas Stream");
        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        nameField = findViewById(R.id.editName);
        emailField = findViewById(R.id.editEmail);
        passwordField = findViewById(R.id.editPassword);

        registerButton = findViewById(R.id.buttonRegister);
        Toast.makeText(RegisterActivity.this, "Clicked",
                Toast.LENGTH_SHORT).show();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i("CLICKED", "CLICKEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEDDDDD");
                startRegister();
            }
        });

    }

    private void startRegister() {
        Toast.makeText(RegisterActivity.this, "Start Register",
                Toast.LENGTH_LONG).show();
        final String name = nameField.getText().toString();
        String email = emailField.getText().toString();
        String password = passwordField.getText().toString();
        Log.i("CLICKED", password);
        if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        String user_id = mAuth.getCurrentUser().getUid();
                        String email = emailField.getText().toString();
                        DatabaseReference current_user_db = mDatabase.child(swapString(email));
                        current_user_db.child("id").setValue(user_id);
                        current_user_db.child("nama").setValue(nameField.getText().toString());
                        current_user_db.child("email").setValue(emailField.getText().toString());
                        current_user_db.child("board");
                        Log.i("CLICKED", "is successs");
                        Intent main = new Intent(RegisterActivity.this,BoardList.class);
                        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(main);

                    }

                }
            });
        }
    }

    private String swapString(String email){
        email = email.replace('.', '0');
        email = email.replace('_', '1');
        email = email.replace('@', '2');
        Log.i("SWAP", email);
        return  email;
    }
}
