package edu.apsu.csci.nutritionnow;

// Team Members: Lionel Sosa Estrada, Joshua Foster, and Stephanie Escue

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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

public class RecipeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    ArrayList<Integer> recipeIDs = new ArrayList<>();
    ArrayList<Integer> recipeWeights = new ArrayList<>();

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
        recipeWeights = intent.getIntegerArrayListExtra("recipeItemsWeight");

        ListView listView = (ListView) findViewById(R.id.ingredientList);
        listView.setOnItemClickListener(this);

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

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        final int pos = position;

        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove ingredient");
        builder.setMessage("Do you want to remove " + recipeItems.get(position).description + " from the recipe?");

        // Set the alert dialog yes button click listener
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                recipeItems.remove(pos);
                displayIngredients();
            }
        });

        // Set the alert dialog no button click listener
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Toast.makeText(getApplicationContext(),
                //        "Food was not added",Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        // Display the alert dialog on interface
        dialog.show();


    }
    private class RecipeItemsDownload extends AsyncTask<Void, Void, List> {

        @Override
        protected List doInBackground(Void... voids) {

            try {
                int cals=0;
                int protein=0;
                int fiber=0;
                int fat=0;
                int carbs=0;
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

                    portionSize = recipeWeights.get(i);
                    portionUnits = "grams";


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
                                cals = nutrientAmount * portionSize / 100;
                                break;
                            case "Protein":
                                protein = nutrientAmount;
                                break;
                            case "Total lipid (fat)":
                                fat = nutrientAmount;
                                break;
                            case "Carbohydrate, by difference":
                                carbs = nutrientAmount;
                                break;
                            case "Fiber, total dietary":
                                fiber = nutrientAmount;
                                break;
                        }
                    }

                    recipeItems.add(new FoodItemExtended(description, cals, portionSize, portionUnits, protein, fat, carbs, fiber));

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

            displayIngredients();

        }
    }
    public class FoodItemExtended {
        String description;
        int calories;
        int grams;
        String units;
        int Protein;
        int Fat;
        int Carbs;
        int Fiber;

        public FoodItemExtended(){
            description = "";
            calories = 0;
            grams = 0;
            units = "";
        }

        public FoodItemExtended(String des, int cals, int gs, String us, int pro, int fats, int carbs, int fiber){
            description = des;
            calories = cals;
            grams = gs;
            units = us;
            Protein = pro;
            Fat = fats;
            Carbs = carbs;
            Fiber = fiber;
        }
    }
    public void displayIngredients(){

        results = new String[recipeItems.size()];
        String[] nutrients = new String[5];

        int cals = 0;
        int pros = 0;
        int fat = 0;
        int carbs = 0;
        int fib = 0;
        //String[] results = {"one","two","three","four", "five"};
        //String[] results = new String[recipeIDs.size()];

        for (int i = 0; i < recipeItems.size(); i++ ){
            results[i] = recipeItems.get(i).description;
            cals+=recipeItems.get(i).calories;
            pros+=recipeItems.get(i).Protein;
            fat+=recipeItems.get(i).Fat;
            carbs+=recipeItems.get(i).Carbs;
            fib+=recipeItems.get(i).Fiber;
        }

        nutrients[0]= "Energy: " + cals + " kcals";
        nutrients[1]="Fat: "+fat + " grams";
        nutrients[2]="Protein: "+pros+ " grams";
        nutrients[3]="Carbohydrates: "+carbs+ " grams";
        nutrients[4]="Fiber: "+fib+ " grams";


        ListView NL = findViewById(R.id.nutritionList);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplication(),android.R.layout.simple_list_item_1,nutrients);
        NL.setAdapter(adapter);

        ListView IL = findViewById(R.id.ingredientList);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, results);
        IL.setAdapter(aa);
    }

}
