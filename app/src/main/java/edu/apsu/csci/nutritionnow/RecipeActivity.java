package edu.apsu.csci.nutritionnow;

// Team Members: Lionel Sosa Estrada, Joshua Foster, and Stephanie Escue

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class RecipeActivity extends AppCompatActivity {

    ArrayList<Integer> recipeIDs = new ArrayList<>();
    //ArrayList<String> results = new ArrayList<>();
    List<FoodItemExtended> recipeItems = new ArrayList<>();
    String[]results;
    String search_url = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_activity);

        // Adding logo and title to action bar
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.ic_launcher_foreground);
        actionbar.setTitle(R.string.nutrition_now);

        Intent intent = getIntent();
        recipeIDs = intent.getIntegerArrayListExtra("recipeItems");


        downloadRecipeItem = new RecipeItemsDownload();
        downloadRecipeItem.execute();

/*
        Button Show_button = findViewById(R.id.showButton);
        Show_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //moved to function

            }
        });

 */
        Button back_button = findViewById(R.id.add_ingredient_button);
        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
        @Override
        public void onBackPressed() {
            finish();
        }

    private RecipeItemsDownload downloadRecipeItem; //second API

    private void getIngredient(int id){


    }

    public void parseIngredients(){


    }

    private class RecipeItemsDownload extends AsyncTask<Void, Void, List> {

        @Override
        protected List doInBackground(Void... voids) {

            try {
                int cals=0;
                String portionUnits = "";
                int portionSize = 0;

                for (int i = 0; i < recipeIDs.size(); i++) {
                    Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/" + recipeIDs.get(i)).buildUpon();
                    builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key));
                    search_url = builder.toString();


                    URL url = new URL(search_url);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                    InputStream is = connection.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);

                    StringBuilder jsonData = new StringBuilder();
                    String line;
                    //Read the data
                    while ((line = br.readLine()) != null) {
                        jsonData.append(line);
                    }


                    //Parse root object
                    JSONObject rootObject = new JSONObject(jsonData.toString());
                    //Parse nutrients
                    JSONArray nutrients = rootObject.getJSONArray("foodNutrients");
                    String description = rootObject.getString("description");

                    JSONArray nutrientInputs = rootObject.getJSONArray("inputFoods");
                    JSONObject nutrientInputFirst = nutrientInputs.getJSONObject(0);
                    portionSize = nutrientInputFirst.getInt("amount");
                    portionUnits = nutrientInputFirst.getString("unit");


                    //parse individual results
                    for (int x = 0; x < nutrients.length(); x++) {
                        JSONObject nutrient = nutrients.getJSONObject(x);
                        JSONObject nutrientData = nutrient.getJSONObject("nutrient");

                        //parse values
                        String nutrientName = nutrientData.getString("name");
                        String nutrientUnits = nutrientData.getString("unitName");

                        int nutrientAmount = nutrient.getInt("amount");


                        //assign value to nutrients wanted
                        switch (nutrientName) {
                            case "Energy":
                                cals = nutrientAmount;
                                break;
                        }
                    }

                    recipeItems.add(new FoodItemExtended(description, cals, portionSize, portionUnits));

                    connection.disconnect();
                }


            } catch (MalformedURLException e) {
                Log.e("URL", e.toString());
            } catch (IOException e) {
                Log.e("IO", e.toString());
            } catch (JSONException e) {
                Log.e("JSON", e.toString());
            }

            return recipeItems;
        }

        @Override
        protected void onPostExecute(List item) {

            Toast.makeText(getApplicationContext(), "Ingredients added:" + recipeItems.size(),Toast.LENGTH_SHORT).show();
            displayIngredients();

        }
    }
    public class FoodItemExtended {
        String description;
        int calories;
        int grams;
        String units;

        public FoodItemExtended(){
            description = "";
            calories = 0;
            grams = 0;
            units = "";
        }

        public FoodItemExtended(String des, int cals, int gs, String us){
            description = des;
            calories = cals;
            grams = gs;
            units = us;
        }
    }
    public void displayIngredients(){

        results = new String[recipeItems.size()];
        String[] nutritiants = new String[recipeItems.size()];

        int cals = 0;
        //String[] results = {"one","two","three","four", "five"};
        //String[] results = new String[recipeIDs.size()];

        for (int i = 0; i < recipeItems.size(); i++ ){
            results[i] = recipeItems.get(i).description;
            cals+=recipeItems.get(i).calories;
        }
        nutritiants[0]="Calories: "+cals;

        ListView NL = findViewById(R.id.nutritionList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(),android.R.layout.simple_list_item_1,nutritiants);
        NL.setAdapter(adapter);

        ListView IL = findViewById(R.id.ingredientList);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, results);
        IL.setAdapter(aa);
    }

}
