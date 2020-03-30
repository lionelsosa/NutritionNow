package edu.apsu.csci.nutritionnow;

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

    List<FoodItemExtended> recipeItems = new ArrayList<>();
    String search_url = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        ListView NL = findViewById(R.id.nutritionList);

        Intent intent = getIntent();
        recipeIDs = intent.getIntegerArrayListExtra("recipeItems");

        for (int i = 0; i < recipeIDs.size(); i++){
            getIngredient(recipeIDs.get(i));

        }
        //moved to function
        displayIngredients();
        /*
        ArrayAdapter<String> aa = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1,results);
        IL.setAdapter(aa);
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

        Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/" + id).buildUpon();
        builder.appendQueryParameter("api_key", "");
        search_url = builder.toString();

        downloadRecipeItem = new RecipeItemsDownload();
        downloadRecipeItem.execute();
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
                for (int i = 0; i < nutrients.length(); i++) {
                    JSONObject nutrient = nutrients.getJSONObject(i);
                    JSONObject nutrientData = nutrient.getJSONObject("nutrient");

                    //parse values
                    String nutrientName = nutrientData.getString("name");
                    String nutrientUnits = nutrientData.getString("unitName");

                    int nutrientAmount = nutrient.getInt("amount");


                    //assign value to nutrients wanted
                    switch (nutrientName){
                        case "Energy":
                            cals = nutrientAmount;
                            break;
                    }
                }

                recipeItems.add(new FoodItemExtended(description, cals, portionSize, portionUnits));

                connection.disconnect();



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
        String[] results = new String[recipeItems.size()];
        for (int i = 0; i < recipeItems.size(); i++ ){
            results[i] = recipeItems.get(i).description;
        }
        ListView IL = findViewById(R.id.ingredientList);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results);
        IL.setAdapter(aa);
    }

}
