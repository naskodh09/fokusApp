package com.example.fokusapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
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

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

//---------------------------------------------------------------------------------------DECLARATATION OF VARIABLES---------------------------------------------------------------------------

    int RC_SIGN_IN;
    GoogleSignInClient mGoogleSignInClient;


    String urlForPosting = "https://fokusrestapi.herokuapp.com/accounts";
    public String urlForGetting;
    public static String userEmail;
    public static String userId;

    String prepareJsonStringResult = "";
    HttpURLConnection http;
    public String table;
    public String[] specificTable;

    volatile List<String> resultsArray = new ArrayList<String>();

//------------------------------------------------------------------------------ONCREATE AND ONSTART OF THE SIGNINACTIVITY PAGE-----------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener((View.OnClickListener) this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

//-------------------------------------------------------------------------------------ASYNCTASK FOR POSTING THE USER EMAIL TO THE DATABASE------------------------------------------------------

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

                prepareJsonAccountString(userEmail);

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


    //METHOD FOR STARTING THE ASYNCTASK OF POSTING TO THE DATABASE
     void startJsonPostTask(){
        SignInActivity.JsonPostTask task = new SignInActivity.JsonPostTask();
        task.execute(urlForPosting);
     }

//-------------------------------------------------------------------------------------HANDLING THE GOOGLE SIGN IN PROCESS USING BUTTON--------------------------------------------------------


    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            updateUI(account);
            //Grabbing the users email from the google api
            userEmail = account.getEmail();
            UserEmail.instance.set(userEmail);
            //Setting the urlForGetting from the api based on the email of the signedIn User
            urlForGetting =  String.format("https://fokusrestapi.herokuapp.com/accounts?email=%s", UserEmail.instance.get());
            //Sending the users email to the database
            startJsonPostTask();
            //Getting the users id from the database
            startJsonGetTask();

            startActivity(new Intent(getApplicationContext(), TimerActivity.class));
        } catch (ApiException e) {
            updateUI(null);
        }
    }

    private void updateUI(Object o) {
    }


//-------------------------------------------------------------------------------------ASYNCTASK FOR GETTING THE USER ID FROM THE DATABASE------------------------------------------------------

    //METHOD FOR START THE GETTING ASYNCTASK
    private void startJsonGetTask() {
        JsonGetTask task = new JsonGetTask();
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

            userId = String.valueOf(result.get(1));

            UserId.instance.set(userId);
        }
    }
}