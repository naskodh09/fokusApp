package com.example.fokusapplication;

import androidx.appcompat.app.AppCompatActivity;


import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public String url;
    public String table;
    public String[] specificTable;

    List<String> resultsArray = new ArrayList<String>();

    Button btnHit;
    Button postButton;
    TextView txtJson;

    HttpURLConnection http;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        btnHit = (Button) findViewById(R.id.btnHit);
//        postButton = (Button) findViewById(R.id.btnHit);
//        txtJson = (TextView) findViewById(R.id.tvJsonItem);

    }

//    public void onClick(View v) {
//        url = "https://fokusrestapi.herokuapp.com/notesUpdate";
//
//        new JsonPostTask().execute(url);
//    }

    public class JsonGetTask extends AsyncTask<String, String, Object> {

        protected Object doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                String line = "";

                StringBuilder jsonString = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    jsonString.append(line+"\n");
                    Log.d("Response: ", "> " + line);

                }

                JsonArray jsonArray = new JsonParser().parse(jsonString.toString()).getAsJsonArray();

                int arraySize = jsonArray.size();

                int first = url.toString().indexOf("/");
                int second = url.toString().indexOf("/", first + 1);
                int third = url.toString().indexOf("/", second + 1);

                table = url.toString().substring(third + 1);
                Log.d("msg", "table is:" + table);

                if(table.indexOf("=") < 0)
                {
                    Log.d("msg", "getting all");
                }
                else
                {
                    specificTable = table.split("=");
                    Log.d("msg", String.format("specific table is %s", specificTable[0]));
                }


                if (table.compareTo("accounts") == 0 || (specificTable != null && specificTable[0].compareTo("accounts?email") == 0))
                {
                    Log.d("msg", "reached accounts");
                    for (int counter = 0; counter < arraySize; counter++)
                    {
                        JsonObject jsonObject = jsonArray.get(counter).getAsJsonObject();

                        String email = jsonObject.get("email").toString();
                        Integer id = jsonObject.get("id").getAsInt();

                        resultsArray.add(email);
                        resultsArray.add(id.toString());
                    }
                }
                else if (table.compareTo("quotes") == 0 || (specificTable != null && specificTable[0].compareTo("quotes?id") == 0))
                {
                    for (int counter = 0; counter < arraySize; counter++)
                    {
                        JsonObject jsonObject = jsonArray.get(counter).getAsJsonObject();

                        Integer id = jsonObject.get("id").getAsInt();
                        String content = jsonObject.get("content").toString();
                        Integer account_id = jsonObject.get("account_id").getAsInt();


                        resultsArray.add(id.toString());
                        resultsArray.add(content);
                        resultsArray.add(account_id.toString());
                    }
                }
                else if (table.compareTo("notes") == 0 || (specificTable != null && specificTable[0].compareTo("notes?id") == 0))
                {
                    for (int counter = 0; counter < arraySize; counter++)
                    {
                        JsonObject jsonObject = jsonArray.get(counter).getAsJsonObject();

                        Integer id = jsonObject.get("id").getAsInt();
                        String subject = jsonObject.get("subject").toString();
                        String title = jsonObject.get("title").toString();
                        String content = jsonObject.get("content").toString();
                        Integer account_id = jsonObject.get("account_id").getAsInt();

                        resultsArray.add(id.toString());
                        resultsArray.add(subject);
                        resultsArray.add(title);
                        resultsArray.add(content);
                        resultsArray.add(account_id.toString());
                    }
                }
                else if (table.compareTo("events") == 0 || (specificTable != null && specificTable[0].compareTo("events?id") == 0))
                {
                    for (int counter = 0; counter < arraySize; counter++)
                    {
                        JsonObject jsonObject = jsonArray.get(counter).getAsJsonObject();

                        Integer id = jsonObject.get("id").getAsInt();
                        String date = jsonObject.get("date").toString();
                        String time = jsonObject.get("time").toString();
                        String title = jsonObject.get("title").toString();
                        Integer account_id = jsonObject.get("account_id").getAsInt();

                        resultsArray.add(id.toString());
                        resultsArray.add(date);
                        resultsArray.add(time);
                        resultsArray.add(title);
                        resultsArray.add(account_id.toString());
                    }
                }
                else if (table.compareTo("sessions") == 0 || (specificTable != null && specificTable[0].compareTo("sessions?id") == 0))
                {
                    for (int counter = 0; counter < arraySize; counter++)
                    {
                        JsonObject jsonObject = jsonArray.get(counter).getAsJsonObject();

                        Integer id = jsonObject.get("id").getAsInt();
                        String date = jsonObject.get("date").toString();
                        String time = jsonObject.get("time").toString();
                        Integer duration = jsonObject.get("duration").getAsInt();
                        Integer account_id = jsonObject.get("account_id").getAsInt();

                        resultsArray.add(id.toString());
                        resultsArray.add(date);
                        resultsArray.add(time);
                        resultsArray.add(duration.toString());
                        resultsArray.add(account_id.toString());
                    }
                }

                return resultsArray;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                resultsArray.clear();

                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }




    public class JsonPostTask extends AsyncTask<String, String, String> {

        String prepareJsonStringResult = "";

        protected String prepareJsonAccountString(String email)
        {
            prepareJsonStringResult = String.format("{\"email\": \"%s\"}", email);
            Log.d("msg", prepareJsonStringResult);

            return prepareJsonStringResult;
        }

        protected String prepareJsonQuoteString(String content, String accountId)
        {
            prepareJsonStringResult = String.format("{\"content\": \"%s\", \"account_id\": \"%s\"}",content, accountId);

            return prepareJsonStringResult;
        }

        protected String prepareJsonNoteString(String subject, String title, String content, String accountId)
        {
            prepareJsonStringResult = String.format("{\"subject\": \"%s\", \"title\": \"%s\", \"content\": \"%s\", \"account_id\": \"%s\"}",subject, title, content, accountId);

            return prepareJsonStringResult;
        }

        protected String prepareJsonEventString(String date, String time, String title, String accountId)
        {
            prepareJsonStringResult = String.format("{\"date\": \"%s\", \"time\": \"%s\", \"title\": \"%s\", \"account_id\": \"%s\"}",date, time, title, accountId);

            return prepareJsonStringResult;
        }

        protected String prepareJsonSessionString(String date, String time, String duration, String accountId)
        {
            prepareJsonStringResult = String.format("{\"date\": \"%s\", \"time\": \"%s\", \"duration\": \"%s\", \"account_id\": \"%s\"}",date, time, duration, accountId);

            return prepareJsonStringResult;
        }

        protected String prepareJsonUpdateNotesString(String id, String content)
        {
            prepareJsonStringResult = String.format("{\"id\": \"%s\", \"content\": \"%s\"}",id, content);

            return prepareJsonStringResult;
        }

        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);

                http = (HttpURLConnection)url.openConnection();
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Content-Type", "application/json;utf-8");

               prepareJsonAccountString("signIn@mail.com");

                byte[] jsonBytes = prepareJsonStringResult.getBytes("utf-8");
                http.getOutputStream().write(jsonBytes, 0 , jsonBytes.length);

                Log.d("msg", "posted" + http.getResponseMessage() + http.getResponseCode() + http.getRequestMethod());

            }
            catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            } finally
            {
                if (http != null)
                {
                    http.disconnect();
                }
            }

            String postConfirmation = "Posted";

            return postConfirmation;
        }

        @Override
        protected void onPostExecute(String postConfirmation) {
            super.onPostExecute(postConfirmation);
        }
    }
}

