package edu.apsu.csci.nutritionnow;

// Team Members: Lionel Sosa Estrada, Joshua Foster, and Stephanie Escue

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    List<FoodItem> items = new ArrayList<>();
    ArrayList<Integer> recipeItems = new ArrayList<>();
    ArrayList<Integer> recipeItemsWeight = new ArrayList<>();

    String search_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_activity);

        // Adding logo and title to action bar
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayShowHomeEnabled(true);
        actionbar.setIcon(R.mipmap.ic_launcher_foreground);
        actionbar.setTitle(R.string.nutrition_now);

        ListView listView = (ListView) findViewById(R.id.results_View);
        listView.setOnItemClickListener(this);

    }


    private SearchResultsDownload downloadResults; //first API


    public void buttonListener(View v) { //only button in activity
        if (v == findViewById(R.id.search_button)) {
            //clear data
            items.clear();

            // get search query
            EditText searchText = findViewById(R.id.search_text);

            // build url
            Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/search").buildUpon();
            builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key) );
            builder.appendQueryParameter("generalSearchInput", searchText.getText().toString());
            builder.appendQueryParameter("includeDataTypeList", "Survey (FNDDS)");
            search_url = builder.toString();
            downloadResults = new SearchResultsDownload();
            downloadResults.execute();
        }

        if (v == findViewById(R.id.go_recipe_button)) {
            Intent intent = new Intent(getBaseContext(),RecipeActivity.class);
            intent.putIntegerArrayListExtra("recipeItems", recipeItems);
            intent.putIntegerArrayListExtra("recipeItemsWeight", recipeItemsWeight);
            startActivity(intent);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

        final int pos = position;

// get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.ingredient_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);

        final EditText userInput = (EditText) promptsView
                .findViewById(R.id.editTextDialogUserInput);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // get user input and set it to result
                                // edit text
                                recipeItemsWeight.add(Integer.parseInt(userInput.getText().toString()));
                                recipeItems.add(items.get(pos).id);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();


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



    //Superclass to hold food items, used to parse search results
    public class FoodItem {
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
}
