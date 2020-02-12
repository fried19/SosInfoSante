package com.andsomore.sosinfosante;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.tabs.TabLayout;

public class IncidentActivity extends AppCompatActivity implements View.OnClickListener {
    private TabLayout tabLayout;
    public ViewPager viewPager;
    private  View icBAck;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incident);
        InitViews();
        icBAck.setOnClickListener(this);
        ViewPageAdapter adapter = new ViewPageAdapter(getSupportFragmentManager());
        //Ajout de fragments
        adapter.AddFragment(new NouveauIncidentFragmentActivity(),"NOUVEAU");
        adapter.AddFragment(new ListeIncidentFragmentActivity(),"LISTE DES INCIDENCES");
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager,true);
    }

    private void InitViews() {
        tabLayout= findViewById(R.id.tabLayout);
        viewPager=findViewById(R.id.viewpager);
        icBAck = findViewById(R.id.ivBack);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onClick(View v) {
        if(v==icBAck){
            onBackPressed();
        }
    }


}