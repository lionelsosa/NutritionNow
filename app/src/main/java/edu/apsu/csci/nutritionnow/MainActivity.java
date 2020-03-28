package edu.apsu.csci.nutritionnow;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener{

    List<FoodItem> items = new ArrayList<>();
    List<FoodItemExtended> recipeItems = new ArrayList<>();
    String search_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.results_View);
        listView.setOnItemClickListener(this);

    }

    private SearchResultsDownload downloadResults; //first API
    private RecipeItemsDownload downloadRecipeItem; //second API

    public void buttonListener(View v) { //only button in activity
        if (v == findViewById(R.id.search_button)) {
            //clear data
            items.clear();

            // get search query
            EditText searchText = findViewById(R.id.search_text);

            // build url
            Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/search").buildUpon();
            builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key));
            builder.appendQueryParameter("generalSearchInput", searchText.getText().toString());
            builder.appendQueryParameter("includeDataTypeList", "Survey (FNDDS)");
            search_url = builder.toString();
            downloadResults = new SearchResultsDownload();
            downloadResults.execute();
        }

        if (v == findViewById(R.id.go_recipe_button)) {
            //switch activity here
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        final int pos = position;
        // Build an AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Confirm your choice");
        builder.setMessage("Add " + items.get(position).description + "(ID " + items.get(position).id + ") to the recipe?");

        // Set the alert dialog yes button click listener
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //parse extra fields from second API
                // build url
                Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/" + items.get(pos).id).buildUpon();
                builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key));
                search_url = builder.toString();
                downloadRecipeItem = new RecipeItemsDownload();
                downloadRecipeItem.execute();


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

    private class SearchResultsDownload extends AsyncTask<Void, Void, List> {

        @Override
        protected List doInBackground(Void... voids) {

            try {
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

                //Array to hold the results
                List<String> foodResults = new ArrayList<>();

                //Parse root object
                JSONObject rootObject = new JSONObject(jsonData.toString());
                //Parse foods
                JSONArray foods = rootObject.getJSONArray("foods");
                //parse individual results
                for (int i = 0; i < foods.length(); i++) {
                    JSONObject item = foods.getJSONObject(i);

                    //parse values
                    String foodDescription = item.getString("description");
                    int foodId = item.getInt("fdcId");

                    //create new item
                    FoodItem currentItem = new FoodItem(foodDescription, foodId);
                    items.add(currentItem);
                }
                connection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return items;
        }

        @Override
        protected void onPostExecute(List item) {
            String[] results = new String[items.size()];
            for (int i = 0; i < items.size(); i++ ){
                    results[i] = items.get(i).description;
            }
            ListView listView = (ListView) findViewById(R.id.results_View);
            ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, results);
            listView.setAdapter(aa);
        }
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
                recipeItems.add(new FoodItemExtended(new FoodItem(description, 0), cals, portionSize, portionUnits));

                connection.disconnect();

            } catch (MalformedURLException e) {
                Log.e("URL", e.toString());
            } catch (IOException e) {
                Log.e("IO", e.toString());
            } catch (JSONException e) {
                Log.e("JSON", e.toString());
            }

            return items;
        }

        @Override
        protected void onPostExecute(List item) {
         /*   String[] results = new String[items.size()];
            for (int i = 0; i < items.size(); i++ ){
                results[i] = items.get(i).description;
            }
            ListView listView = (ListView) findViewById(R.id.results_View);
            ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, results);
            listView.setAdapter(aa);*/

         FoodItemExtended tempFood = recipeItems.get(recipeItems.size() - 1);
            Toast.makeText(getApplicationContext(),
                    "Added " + tempFood.grams + " " + tempFood.units + " of " + tempFood.description + " worth " + tempFood.calories + " calories", Toast.LENGTH_SHORT).show();


             /*Toast.makeText(getApplicationContext(),
                     "Items in recipe: " + recipeItems.size(), Toast.LENGTH_SHORT).show();*/



        }
    }


    //Superclass to hold food items, used to parse search results
    private class FoodItem {
        String description = "";
        int id;

        public FoodItem(){
            description = "";
            id = 0;
        }

        public FoodItem(String descriptionReceived, int idReceived) {
            description = descriptionReceived;
            id = idReceived;
        }
    }

    //subclass to pass selected results to the recipe
    private class FoodItemExtended extends FoodItem {
        int calories;
        int grams;
        String units;

        public FoodItemExtended(){
            calories = 0;
            grams = 0;
            units = "";
        }

        public FoodItemExtended(FoodItem foodItem, int cals, int gs, String us){
            description = foodItem.description;
            id = foodItem.id;
            calories = cals;
            grams = gs;
            units = us;
        }

        public FoodItemExtended(FoodItem foodItem){
            description = foodItem.description;
            id = foodItem.id;
            calories = 0;
            grams = 0;
            units = "";
        }

    }

}
