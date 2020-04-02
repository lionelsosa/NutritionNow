package edu.apsu.csci.nutritionnow;

// Team Members: Lionel Sosa Estrada, Joshua Foster, and Stephanie Escue

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Adding logo and title to action bar
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.ic_launcher_foreground);
        actionbar.setTitle(R.string.nutrition_now);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent goToSearchActivity = new Intent(this, SearchActivity.class);
        startActivity(goToSearchActivity);
    }
}
