package dawson.dawsondangerousclub;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class WeatherActivity extends AppCompatActivity {

    EditText cityInput;
    Spinner countrySelector;
    TextView forecastDisplay;
    ArrayList<String> countries;

    String selectedCity ="montreal";
    String countryCode ="ca";

    private static final String API_KEY = "818cbaf6cb7daa55c791ff656317de47";
    private static final String WEATHER_URL = "http://api.openweathermap.org/data/2.5/forecast?";
    private static final double KELVIN = 273.15;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        cityInput = (EditText) findViewById(R.id.cityET);
        countrySelector = (Spinner) findViewById(R.id.countrySpinner);
        forecastDisplay = (TextView)findViewById(R.id.forecastTV);

        getCountriesFromFile();

        //attach countres to spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.spinner_item, countries);
        countrySelector.setAdapter(adapter);

        //set defaults
        countrySelector.setSelection(39);

        // first check to see if we can get on the network
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()){
            new getForecast().execute();

        } else {
            forecastDisplay.setText("No network connection available.");
        }




    }

    /**
     * Read json file with all the countries and their ISO 3166 country codes . Puts them into an arraylist.
     */
    public void getCountriesFromFile(){

        countries = new ArrayList<String>();

        try {
            InputStream is = getResources().openRawResource(R.raw.country_and_codes);
            String jsonTxt = IOUtils.toString(is, "UTF-8");

            JSONArray json = new JSONArray(jsonTxt);

            for(int country=0; country < 249; country++ ){

                JSONObject countryData = (JSONObject) json.get(country);
                String countryCode = countryData.getString("Code");
                String countryName = countryData.getString("Name");
                countries.add(countryCode + "|" + countryName);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This Async task gets the forecast via an open weather API.
     * It receives JSON data which is deciphered and displayed to the user.
     */
    private class getForecast extends AsyncTask<Nullable, Void, String> {

        @Override
        protected String doInBackground(Nullable... nullables) {
            try {

                String url = "http://api.openweathermap.org/data/2.5/forecast?q=montreal,CA&appid=818cbaf6cb7daa55c791ff656317de47";

                //String url = WEATHER_URL + "q=" + selectedCity + "," + countryCode + "&appid=" + API_KEY;

                return fetchForecastJSON(url);

            } catch (IOException e) {

                e.printStackTrace();
                return "Unable to retrieve web page. URL may be invalid.";
            }

        }

        protected void onPostExecute(String result) {

            if(result == "Invalid request."){

            }else {
                //temperatureTextView.setText(result);
                //Toast.makeText(getApplicationContext(), "Finished", Toast.LENGTH_SHORT).show();
                Log.i("json", result);
               // forecastDisplay.setText(result);
                cityInput.setText(result);
            }


        }

    }

    /**
     * GET request to the weather API, returns JSON.
     *
     * @param myurl
     * @return weather JSON
     * @throws IOException
     */
    private String fetchForecastJSON(String myurl) throws IOException {
        InputStream is = null;

        HttpURLConnection conn = null;
        URL url = new URL(myurl);
        try {
            // create and open the connection
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();

            int response = conn.getResponseCode();

            if (response != HttpURLConnection.HTTP_OK)
                return "Invalid request.";

            // get the stream for the data from the website
            is = conn.getInputStream();
            // read the stream, returns String
            return convertInputStreamToString(is);

        } catch (IOException e) {
            throw e;
        } finally {
            /*
			 * Make sure that the InputStream is closed after the app is
			 * finished using it.
			 * Make sure the connection is closed after the app is finished using it.
			 */
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
                if (conn != null)
                    try {
                        conn.disconnect();
                    } catch (IllegalStateException ignore) {
                    }
            }
        }
    }

    /**
     * Converts the input stream returned form the website into a readable JSON string.
     *
     * @param stream
     * @return JSON String
     * @throws IOException
     */
    public String convertInputStreamToString(InputStream stream) throws IOException {

        String buildStr = "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(
                stream, "iso-8859-1"), 8);
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }

        //get json string
        String weatherDataJson = sb.toString();

        //reset string builder
        sb.setLength(0);

        try {

            //parse json text
            JSONObject jsonObj = new JSONObject(weatherDataJson);

            //retrieve the number of forecasts "cnt"(count) provided by the API
            int forecasts =  jsonObj.getInt("cnt");
            //get the all the forecasts
            JSONArray forecastList = jsonObj.getJSONArray("list");

            //iterate through each forecast and retrieve relevant data
            for(int forecast=0; forecast < forecasts; forecast++){

                JSONObject weatherObj = (JSONObject) forecastList.get(forecast);
                JSONObject weatherTemperature = weatherObj.getJSONObject("main");
                JSONArray weatherDetails = weatherObj.getJSONArray("weather");

                String forecastDate = weatherObj.getString("dt_txt");
                String description =  weatherDetails.getJSONObject(0).getString("description");
                String icon = weatherDetails.getJSONObject(0).getString("icon");
                String temperature = Integer.toString( (int)Math.round(weatherTemperature.getDouble("temp") - KELVIN)) + "°C";

                description  = description.substring(0, 1).toUpperCase() + description.substring(1);

                //format found in json
                SimpleDateFormat apiFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date parsedDate = apiFormat.parse(forecastDate);
                //desired format
                SimpleDateFormat appFormat= new SimpleDateFormat("EEEE MMMM dd ha");

                Log.i("forecast", appFormat.format(parsedDate)+ " " + description + " " +  temperature + " -"+ icon);

                // amalgamate data to be deciphered in async task post execute, "|" separates each weather forecast
                sb.append(appFormat.format(parsedDate)+ " " + description + " " +  temperature + " $"+ icon + "|");

            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException("JSON data invalid");
        }
        return sb.toString();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            //open About app activity
            case R.id.about:
                Intent openAbout = new Intent(getApplicationContext(),
                        AboutActivity.class);
                startActivity(openAbout);
                return true;
            //Launches Dawson Computer Science web page
            case R.id.dawson:
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.dawsoncollege.qc.ca/computer-science-technology/"));
                startActivity(i);
                return true;
            //Open last viewed quote
            case R.id.settings:
                //open settings activity
                Intent openSettings = new Intent(getApplicationContext(),
                        SettingsActivity.class);
                startActivity(openSettings);
                return true;
            default:
                return false;
        }

    }

}
