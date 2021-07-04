package com.example.fokusapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class NotesActivity extends AppCompatActivity
{
    ImageView add;
    NoteAdapter adapter;
    public  static ListView listView;
    public static ArrayList<AddNote> noteList = new ArrayList<>();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    String subject;
    String title;
    String note;

    String url = "https://fokusrestapi.herokuapp.com/notes";

    String prepareJsonStringResult = "";
    HttpURLConnection http;


    public String urlForGetting = String.format("https://fokusrestapi.herokuapp.com/notes?id=%s", UserId.instance.get());
    public String table;
    public String[] specificTable;

    volatile List<String> resultsArray = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        preferences = getSharedPreferences("Pref",MODE_PRIVATE);
        editor = preferences.edit();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        add = findViewById(R.id.image_view_add);

        subject = getIntent().getStringExtra("keySubject");
        title = getIntent().getStringExtra("keyTitle");
        note = getIntent().getStringExtra("keyNote");

        filterNotes();

        startJsonGetTask();

        setUpList();

        //Sending the information of the note to the database
        startJsonPostTask();
        setUpOnclickListener();


        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent addNotePage = new Intent(NotesActivity.this, NoteDetailActivity.class);
                startActivity(addNotePage);
            }
        });

        //deleting note
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                final int item = position;

                new AlertDialog.Builder(NotesActivity.this)
                        .setTitle("Deleting note")
                        .setMessage("Do you want to delete this note?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                noteList.remove(item);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("no", null)
                        .show();
                return true;
            }
        });



        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.notes);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
            {
                switch (menuItem.getItemId())
                {
                    case R.id.statistics:
                        startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.schedule:
                        startActivity(new Intent(getApplicationContext(), CalendarActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.timer:
                        startActivity(new Intent(getApplicationContext(), TimerActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.notes:

                        return true;

                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    private void setUpData(String subject, String title, String theNote) {
        if(subject == null && title == null && theNote == null){

        }else {
            AddNote note = new AddNote(subject, title, theNote);
            noteList.add(note);
        }
    }

    private void setUpList() {
        listView = (ListView) findViewById(R.id.notes_list_view);

        adapter = new NoteAdapter(getApplicationContext(),0, noteList);
        listView.setAdapter(adapter);
    }

    private void setUpOnclickListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                AddNote noteSelected = (AddNote) (listView.getItemAtPosition(position));
                Intent goToDetail = new Intent(NotesActivity.this, GetDetails.class);
                goToDetail.putExtra("theTitle", noteSelected.getTitle());
                goToDetail.putExtra("theNote", noteSelected.getNoteTaken());
                goToDetail.putExtra("theSubject", noteSelected.getSubject());
                //goToDetail.putExtra("noteIndex", position);
                startActivity(goToDetail);

            }
        });
    }


    private void filterNotes(){
        SearchView searchView = (SearchView) findViewById(R.id.search_notes);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                ArrayList<AddNote> filteredNotes = new ArrayList<>();
                for(AddNote notes : noteList){
                    if(notes.getSubject().toLowerCase().contains(newText.toLowerCase())){
                        filteredNotes.add(notes);
                    }
                }
                NoteAdapter theAdapter = new NoteAdapter(getApplicationContext(), 0, filteredNotes);
                listView.setAdapter(theAdapter);
                return false;
            }
        });
    }




    //Asynctask running on a different thread that posts the users email to the database
    public class JsonPostTask extends AsyncTask<String, String, String> {

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

                prepareJsonNoteString(subject, title, note, UserId.instance.get());

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

    void startJsonPostTask(){
        NotesActivity.JsonPostTask task = new NotesActivity.JsonPostTask();
        task.execute(url);
    }



//-------------------------------------------------------------------------------------ASYNCTASK FOR GETTING THE USER ID FROM THE DATABASE------------------------------------------------------

    //METHOD FOR START THE GETTING ASYNCTASK
    private void startJsonGetTask() {
        NotesActivity.JsonGetTask task = new NotesActivity.JsonGetTask();
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

        @Override
        protected void onPostExecute(List<String> result) {
            super.onPostExecute(result);
            Log.d("msg", "executedPostExecute");

            Log.d("TAG", "value of result:" + String.valueOf(result));

            int x = 0;
            int z = 0;
            int i = 1;

            while((x + 1 < result.size()) && (z + 1 < result.size()) && (i + 1 < result.size())){
                subject = result.get(i);
                subject = subject.substring(1, subject.length() - 1);
                x = i + 1;
                z = i + 2;
                title = result.get(x);
                title = title.substring(1, title.length() - 1);
                note = result.get(z);
                note = note.substring(1, note.length() - 1);
                setUpData(subject, title, note);
                setUpList();
                i+=5;
            }
        }
    }
}