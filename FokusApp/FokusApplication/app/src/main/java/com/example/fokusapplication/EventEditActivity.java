package com.example.fokusapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Locale;

import static com.example.fokusapplication.CalendarActivity.selectedDate;

public class EventEditActivity extends AppCompatActivity
{
    private EditText eventNameET;
    private TextView eventDateTV;

    private LocalTime time;
    //private static DateTimeFormatter formatter;

    LocalTime localTime;
    LocalDate localDate;
    String eventName;

    String urlForPosting = "https://fokusrestapi.herokuapp.com/events";
    String prepareJsonStringResult = "";
    HttpURLConnection http;

    Button timeButton, dateButton;
    int hour, minute;

    DatePickerDialog datePickerDialog;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);
        initWidgets();
        time = LocalTime.now();
        eventDateTV.setText(dateOfToday());
        //eventTimeTV.setText("Time: " + formattedTime(time));

        initDatePicker();


    }

    private String dateOfToday()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        month = month + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        return makeDateString(day,month, year);
    }

    private void initDatePicker()
    {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day)
            {
                month = month + 1;
                String date = makeDateString(day, month, year);
                dateButton.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        datePickerDialog = new DatePickerDialog(this, dateSetListener, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());
    }

    private String makeDateString(int day, int month, int year)
    {
        return  day +" " +getMonthFormat(month) +" " +year;
    }

    private String getMonthFormat(int month)
    {
        if(month == 1)
            return "jan";
        if(month == 2)
            return "Feb";
        if(month == 3)
            return "Mar";
        if(month == 4)
            return "Apr";
        if(month == 5)
            return "May";
        if(month == 6)
            return "Jun";
        if(month == 7)
            return "Jul";
        if(month == 8)
            return "Aug";
        if(month == 9)
            return "Sep";
        if(month == 10)
            return "Oct";
        if(month == 11)
            return "Nov";
        if(month == 12)
            return "Dec";
        //default
        return "Jan";
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formattedDate(LocalDate date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        return date.format(formatter);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String formattedTime(LocalTime time)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
        return time.format(formatter);
    }

    private void initWidgets()
    {
        eventNameET = findViewById(R.id.eventNameET);
        eventDateTV = findViewById(R.id.eventDateTV);
        //eventTimeTV = findViewById(R.id.eventTimeTV);
        timeButton = findViewById(R.id.eventTimeButton);
        dateButton = findViewById(R.id.eventDateTV);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void saveEventAction(View view)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMM yyyy");
        String date = dateButton.getText().toString();

        localTime = LocalTime.parse(timeButton.getText().toString());
        //String date = formattedDate(LocalDate.parse(dateButton.getText().toString()));
        localDate = LocalDate.parse(date, formatter);

        eventName = eventNameET.getText().toString();
        Event newEvent = new Event(eventName, localDate, localTime);
        startJsonPostTask();
        Event.eventsList.add(newEvent);
        finish();
    }


    public void popTimeClicker(View view)
    {
        TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener()
        {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute)
            {
                hour = selectedHour;
                minute = selectedMinute;
                timeButton.setText(String.format(Locale.getDefault(), "%02d:%02d", hour,minute));
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, onTimeSetListener, hour, minute, true);
        timePickerDialog.show();
    }

    public void setDatePicker(View view)
    {
        datePickerDialog.show();
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

        @RequiresApi(api = Build.VERSION_CODES.O)
        protected String doInBackground(String... params) {

            try {
                URL url = new URL(params[0]);

                http = (HttpURLConnection)url.openConnection();
                http.setRequestMethod("POST");
                http.setDoOutput(true);
                http.setRequestProperty("Accept", "application/json");
                http.setRequestProperty("Content-Type", "application/json;utf-8");

                prepareJsonEventString(String.valueOf(localDate), String.valueOf(localTime), eventName, UserId.instance.get());

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
        EventEditActivity.JsonPostTask task = new EventEditActivity.JsonPostTask();
        task.execute(urlForPosting);
    }
}