package com.example.fokusapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.example.fokusapplication.CalendarActivity.selectedDate;

public class EventActivity extends AppCompatActivity {
    public ListView eventListView;
    TextView eventDateTV;
    EventAdapter eventAdapter;


    public String urlForGetting = String.format("https://fokusrestapi.herokuapp.com/events?id=%s", UserId.instance.get());
    public String table;
    public String[] specificTable;

    volatile List<String> resultsArray = new ArrayList<String>();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);

        Log.d("msg", "Started oncreate");
        eventListView = findViewById(R.id.eventListView);
        eventDateTV = findViewById(R.id.eventDateTV);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
        String date = selectedDate.format(formatter);
        eventDateTV.setText( date);
        setEventAdapter();

        startJsonGetTask();

        //deleting event
        eventListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int item = position;

                new AlertDialog.Builder(EventActivity.this)
                        .setTitle("Deleting event")
                        .setMessage("Do you want to delete this event?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Event getItem = Event.eventsList.get(item);
                                Event.eventsList.remove(item);
                                eventAdapter.remove(getItem);
                                eventAdapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("no", null)
                        .show();
                return true;
            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setEventAdapter();
    }

    private void setEventAdapter() {

        ArrayList<Event> dailyEvents = Event.eventsForDate(selectedDate);
        eventAdapter = new EventAdapter(getApplicationContext(), dailyEvents);
        eventListView.setAdapter(eventAdapter);
    }



//-------------------------------------------------------------------------------------ASYNCTASK FOR GETTING THE USER ID FROM THE DATABASE------------------------------------------------------

    //METHOD FOR START THE GETTING ASYNCTASK
    private void startJsonGetTask() {
        EventActivity.JsonGetTask task = new EventActivity.JsonGetTask();
        task.execute(urlForGetting);
    }


    //ASYNCTASK FOR GETTING FROM THE API
    public class JsonGetTask extends AsyncTask<String, List<String>, List<String>> implements com.example.fokusapplication.JsonGetTask {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d("msg", "ExecutePreExecute");
        }

        @Override
        protected List<String> doInBackground(String... params) {

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

            return resultsArray;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            Log.i("msg", "AsyncTask cancelled");
        }

        @Override
        protected void onProgressUpdate(List<String>... progressArray) {
            super.onProgressUpdate(progressArray);

            Log.d("msg", String.valueOf(progressArray[0]));
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            Log.d("msg", "executedPostExecute");

            Log.d("TAG", "value of result:" + String.valueOf(result));

            int x = 0;
            int z = 0;
            int i = 1;

            while(i + 1 < result.size()){
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-d");
                String date = result.get(i);
                selectedDate = LocalDate.parse(date, formatter);
                i+=5;
                Log.d("msg", "Date value is:" + String.valueOf(selectedDate));
            }
        }
    }
}