package com.hayanesh.feeder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.example.fontometrics.Fontometrics;

public class MainActivity extends AppCompatActivity {

    private TextView f_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        f_title =(TextView)findViewById(R.id.title);
        f_title.setTypeface(Fontometrics.back_black(this));
    }
}
