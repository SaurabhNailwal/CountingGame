package com.example.saurmn.countinggame;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;


public class CountingGame extends ActionBarActivity {

    private CountingGameView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_couting_game);

        // create a new CountingGameView and add it to the RelativeLayout
        RelativeLayout layout = (RelativeLayout)findViewById(R.id.relativeLayout);
        view = new CountingGameView(this,layout);

        layout.addView(view,0);// add view to the layout
    }

    @Override
    public void onPause(){

        super.onPause();
        view.pause();
    }

    @Override
    public void onResume(){

        super.onResume();
        view.resume(this);
    }

}
