package com.motthoidecode.findplacesnearby;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import fragments.MapsFragment;

public class MapsActivity extends FragmentActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        MapsFragment mapsFragment = new MapsFragment();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_container,mapsFragment).commit();
    }
}
