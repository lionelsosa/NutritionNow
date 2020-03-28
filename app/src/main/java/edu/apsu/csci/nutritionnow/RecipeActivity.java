package edu.apsu.csci.nutritionnow;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class RecipeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        ListView IL = findViewById(R.id.ingredientList);
        ListView NL = findViewById(R.id.nutritionList);
    }
    @Override
    public void onBackPressed() {
        finish();
    }
}
