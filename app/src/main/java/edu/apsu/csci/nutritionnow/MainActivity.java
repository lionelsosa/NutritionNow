package edu.apsu.csci.nutritionnow;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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

    List<foodItem> items = new ArrayList<>();
    List<foodItem> recipeItems = new ArrayList<>();
    String search_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ListView listView = (ListView) findViewById(R.id.results_View);
        listView.setOnItemClickListener(this);

    }

    private SearchResultsDownload downloadResults;

    public void searchButtonListener(View v){ //only button in activity
        //clear data
        items.clear();

        // get search query
        EditText searchText = findViewById(R.id.search_text);

        // build url
        Uri.Builder builder = Uri.parse("https://api.nal.usda.gov/fdc/v1/search").buildUpon();
        builder.appendQueryParameter("api_key", getResources().getString(R.string.api_key));
        builder.appendQueryParameter("generalSearchInput", searchText.getText().toString());
        search_url = builder.toString();
        downloadResults = new SearchResultsDownload();
        downloadResults.execute();
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        recipeItems.add(items.get(position));
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
                    String foodBrand = "";
                    if (item.has("brandOwner"))
                        foodBrand = item.getString("brandOwner");
                    else
                        foodBrand = null;
                    int foodId = item.getInt("fdcId");

                    //create new item
                    foodItem currentItem = new foodItem(foodDescription, foodBrand, foodId);
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
                if (items.get(i).brand != null)
                    results[i] = items.get(i).description + " by " + items.get(i).brand;
                else
                    results[i] = items.get(i).description + ", generic or unbranded";
            }
            ListView listView = (ListView) findViewById(R.id.results_View);
            ArrayAdapter<String> aa = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, results);
            listView.setAdapter(aa);
        }
    }

    private class foodItem {
        String description = "";
        String brand = "";
        int id;

        public foodItem(String descriptionReceived, String brandReceived, int idReceived) {
            description = descriptionReceived;
            brand = brandReceived;
            id = idReceived;
        }
    }

}
