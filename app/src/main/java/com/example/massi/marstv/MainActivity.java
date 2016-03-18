package com.example.massi.marstv;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        Log.e("MainActivity", "HERE");
        Intent startServiceIntent = new Intent(getApplicationContext(), PostMetrics.class);
        getApplicationContext().startService(startServiceIntent);
        finish();
    }

}
