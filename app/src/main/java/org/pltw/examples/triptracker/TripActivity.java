package org.pltw.examples.triptracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

public class TripActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_trip);


        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(R.id.tripFragmentContainer);

        if (fragment==null) {
            fragment = new TripFragment();
            manager.beginTransaction()
                    .add(R.id.tripFragmentContainer, fragment)
                    .commit();
        }
    }
}
