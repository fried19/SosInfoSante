package com.andsomore.sosinfosante;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WelcomeActivity extends AppCompatActivity implements View.OnClickListener {
    public static  boolean isAppRunning;
    private Button btConnexion;
    private TextView tvRegister;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        InitViews();
        btConnexion.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
    }

    private void InitViews() {
        btConnexion = findViewById(R.id.btConnexion);
        tvRegister = findViewById(R.id.tvRegister);
    }

    @Override
    public void onClick(View v) {
        if (v == btConnexion){
            startActivity(new Intent(this,LoginActivity.class));
        }
        if(v == tvRegister){
            startActivity(new Intent(this,EnregistrementActivity.class));

        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppRunning = false;
    }
}
