package com.xenoire.canvasstream;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    MyCanvas myCanvas;
    ImageButton black, red;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        black = findViewById(R.id.black);
        red = findViewById(R.id.red);

        black.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myCanvas.setColor(Color.BLACK);
            }
        });

        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myCanvas.setColor(Color.rgb(255, 0,0));
            }
        });

        myCanvas = new MyCanvas(this, null);
        //setContentView(myCanvas);
    }
}
