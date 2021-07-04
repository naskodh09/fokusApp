package com.example.fokusapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.slider.Slider;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class TimerActivity extends AppCompatActivity
{
    private TextView countdownText,startStopText;
    private Button resetButton;

    private long study_time_in_millis = 120000;
    private long break_time_in_millis = 120000;
    private long time_left_millis = study_time_in_millis;
    private long end_time_millis;
    private long no_of_sessions;
    private boolean isTimerRunning = false;
    private boolean isFirstTime = true;
    private boolean isStudySession = true;
    private TextView studyBreakTimeText;

    private CountDownTimer countDownTimer;

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private boolean AM;
    private int DND;
    private boolean actionDND;
    private boolean actionAM;
    private long sessionDB;

    private Slider minutesSlider,breakSlider,sessionSlider;
    private TextView minutesTextBox, breakTextBox,sessionTextBox;


    int minutes;

    private LocalDate date;
    private LocalTime time;


    String prepareJsonStringResult = "";
    HttpURLConnection http;
    public String table;
    String urlForPosting = "https://fokusrestapi.herokuapp.com/sessions";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        preferences = getSharedPreferences("Pref",MODE_PRIVATE);
        editor = preferences.edit();

        countdownText = findViewById(R.id.countdown_text);
        startStopText = findViewById(R.id.countdown_text_start_pause);
        resetButton = findViewById(R.id.countdown_restart_button);

        minutesSlider = findViewById(R.id.studyMinutesSlider);
        breakSlider = findViewById(R.id.breakMinutesSlider);
        sessionSlider = findViewById(R.id.sessionsSlider);
        minutesTextBox = findViewById(R.id.studyMinutesBox);
        breakTextBox = findViewById(R.id.breakMinutesBox);
        sessionTextBox = findViewById(R.id.sessionMinutesBox);
        studyBreakTimeText=findViewById(R.id.countdown_activity_title);

        actionDND = false;
        actionAM = false;

        //Asking you to turn on non disturb and airplane when u first start
        startStopText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isTimerRunning){
                    PauseTimer();
                } else {
                    if (isFirstTime){
                        try {
                            DND = Settings.Global.getInt(getContentResolver(), "zen_mode");
                        } catch (Settings.SettingNotFoundException e) {
                            e.printStackTrace();
                        }
                        AM = Settings.System.getInt(getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
                        if (!actionDND) {
                            if (DND == 0) {
                                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(TimerActivity.this);
                                alertDialogBuilder.setMessage("Would you like to turn on Do Not Disturb?");
                                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
                                        startActivity(intent);
                                        actionDND = true;
                                    }
                                });
                                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        actionDND = true;
                                    }
                                });
                                androidx.appcompat.app.AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        }
                        if (!actionAM) {
                            if (!AM) {
                                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(TimerActivity.this);
                                alertDialogBuilder.setMessage("Would you like to turn on Airplane Mode?");
                                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        Intent intent = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                                        startActivity(intent);
                                        actionAM = true;
                                    }
                                });
                                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        actionAM = true;
                                    }
                                });
                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();
                            }
                        }

                        setData();
                    } else {
                        StartTimer();
                    }
                }
            }
        });


//Bottom navigation menu
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.timer);
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
                        return true;

                    case R.id.notes:
                        startActivity(new Intent(getApplicationContext(), NotesActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

        //Resets the time
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.clear().commit();
                time_left_millis = 0;
                break_time_in_millis = 0;
                study_time_in_millis = 0;
                no_of_sessions = 0;
                isFirstTime = true;
                isStudySession = true;
                isTimerRunning = false;
                countDownTimer.cancel();
                updateUI();
            }
        });

        updateUI();

    }

    //Sets the data for the app and for db as well
    private void setData() {
        isFirstTime = false;
        no_of_sessions = (long) sessionSlider.getValue();
        sessionDB=no_of_sessions;

        //The two lines behind will set the default values to the timer which are fetched from the slider
        //If they are uncomented comment the next two under them
        break_time_in_millis = (long) (breakSlider.getValue() * 60 * 1000);
        study_time_in_millis = (long) (minutesSlider.getValue() * 60 * 1000);

//        study_time_in_millis = 10000;
//        break_time_in_millis = 5000;
        time_left_millis = study_time_in_millis;
        StartTimer();
    }

//Starts the timer and does the logic on the inside
    private void StartTimer() {

        isTimerRunning = true;
        startStopText.setText("Pause");
        end_time_millis = System.currentTimeMillis() + time_left_millis;
        countDownTimer = new CountDownTimer(time_left_millis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time_left_millis = millisUntilFinished;
                updateUI();
            }

            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFinish() {
                isTimerRunning = false;
                if (no_of_sessions > 1) {
                    if (isStudySession) {
                        time_left_millis = break_time_in_millis;
                        isStudySession = false;
                    } else {
                        no_of_sessions = no_of_sessions - 1;
                        isStudySession = true;
                        time_left_millis = study_time_in_millis;
                    }
                    updateUI();
                    StartTimer();
                } else {
                    editor.clear().commit();
                    sendToDb(study_time_in_millis,break_time_in_millis,sessionDB);
                    if(minutes<1){
                        Log.d("msg", "Minutes is 0");
                    }
                    else{
                        startJsonPostTask();
                    }
                    time_left_millis = 0;
                    break_time_in_millis = 0;
                    study_time_in_millis = 0;
                    no_of_sessions = 0;
                    isFirstTime = true;
                    isStudySession = true;
                    isTimerRunning = false;
                    updateUI();
                    studyBreakTimeText.setText("Study Finished");
                }
            }
        }.start();
    }

    private void updateUI() {

        if (!isFirstTime){
            sessionTextBox.setVisibility(View.GONE);
            minutesTextBox.setVisibility(View.GONE);
            breakTextBox.setVisibility(View.GONE);
            sessionSlider.setVisibility(View.GONE);
            breakSlider.setVisibility(View.GONE);
            minutesSlider.setVisibility(View.GONE);
            resetButton.setVisibility(View.VISIBLE);
        } else {
            sessionTextBox.setVisibility(View.VISIBLE);
            minutesTextBox.setVisibility(View.VISIBLE);
            breakTextBox.setVisibility(View.VISIBLE);
            sessionSlider.setVisibility(View.VISIBLE);
            breakSlider.setVisibility(View.VISIBLE);
            minutesSlider.setVisibility(View.VISIBLE);
            resetButton.setVisibility(View.GONE);
        }

        //Update study/BreakTime
        if(isStudySession==true)
        {
            studyBreakTimeText.setText("Study Time");
        }
        else {
            studyBreakTimeText.setText("Break Time");
        }

        if (isTimerRunning){
            startStopText.setText("Pause");
        } else {
            startStopText.setText("Start");
        }

//Displaying the values in the timer properly not in miliseconds but in minutes and seconds
        int minutes = (int) (time_left_millis / 1000) / 60;
        int seconds = (int) (time_left_millis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        countdownText.setText(timeLeftFormatted);
    }

    //Pausing the timer
    private void PauseTimer() {
        countDownTimer.cancel();
        isTimerRunning = false;
        updateUI();
    }

    //Getting  a lot of values so i dont use services
    @Override
    protected void onStart() {
        super.onStart();
        isTimerRunning = preferences.getBoolean("TimerRunning",false);
        isFirstTime = preferences.getBoolean("isFirstTime",false);
        isStudySession = preferences.getBoolean("isStudySession", false);
        time_left_millis = preferences.getLong("TimesLeft",0);
        end_time_millis = preferences.getLong("EndTime",0);
        study_time_in_millis = preferences.getLong("StudyTime",0);
        break_time_in_millis = preferences.getLong("BreakTime",0);
        no_of_sessions = preferences.getLong("NoSessions",0);
        sessionDB=preferences.getLong("SessionDB",0);

        if (isTimerRunning){
            time_left_millis = end_time_millis - System.currentTimeMillis();
            if (time_left_millis > 1000) {
                StartTimer();
            } else {
                isTimerRunning = false;
                updateUI();
            }
        } else {
            if (no_of_sessions > 1) {
                updateUI();
            }
        }
    }

    //Passing a lot of values so i dont use services
    @Override
    protected void onStop() {
        super.onStop();
        editor.putBoolean("TimerRunning",isTimerRunning);
        editor.putBoolean("isFirstTime",isFirstTime);
        editor.putBoolean("isStudySession",isStudySession);
        editor.putLong("TimesLeft",time_left_millis);
        editor.putLong("EndTime", end_time_millis);
        editor.putLong("BreakTime", break_time_in_millis);
        editor.putLong("StudyTime", study_time_in_millis);
        editor.putLong("NoSessions",no_of_sessions);
        editor.putLong("SessionDB",sessionDB);
        editor.apply();
    }

    //Using this to send data to db upwards in onFinish()
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendToDb(long studyDB, long breakDB, long sessionDB)
    {
       long totalTime = studyDB*sessionDB + breakDB*(sessionDB-1);
       minutes = (int) (totalTime / 1000) / 60;
       date = LocalDate.now();
       time = LocalTime.now();
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

                prepareJsonSessionString(String.valueOf(date), String.valueOf(time), String.valueOf(minutes), UserId.instance.get());

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
        TimerActivity.JsonPostTask task = new TimerActivity.JsonPostTask();
        task.execute(urlForPosting);
    }
}
