package com.example.fokusapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import static com.example.fokusapplication.NotesActivity.listView;

public class GetDetails extends AppCompatActivity {
   TextView subject, title, note;
    //AddNote noteSelected;
    //int indexInt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_details);

        subject = findViewById(R.id.text_view_get_details_subject);
        title = findViewById(R.id.text_view_get_details_title);
        note = findViewById(R.id.text_view_get_details_note);

        String subjectString = getIntent().getStringExtra("theSubject");
        String titleString = getIntent().getStringExtra("theTitle");
        String noteString = getIntent().getStringExtra("theNote");
       // String indexString = getIntent().getStringExtra("noteIndex");
        //indexInt = Integer.parseInt(indexString);

        subject.setText(subjectString);
        title.setText(titleString);
        note.setText(noteString);


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
                        startActivity(new Intent(getApplication(), StatisticsActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.schedule:
                        startActivity(new Intent(getApplication(), ScheduleActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.timer:
                        startActivity(new Intent(getApplication(), TimerActivity.class));
                        overridePendingTransition(0,0);
                        return true;

                    case R.id.notes:

                        return true;

                    case R.id.settings:
                        startActivity(new Intent(getApplication(), SettingsActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });

    }

    public void updateNote(View view)
    {
//        AddNote noteSelected = (AddNote) (listView.getItemAtPosition(indexInt));
//        Intent goToDetail = new Intent(GetDetails.this, NoteDetailActivity.class);
//        goToDetail.putExtra("theTitle", noteSelected.getTitle());
//        goToDetail.putExtra("theNote", noteSelected.getNoteTaken());
//        goToDetail.putExtra("theSubject", noteSelected.getSubject());
//        startActivity(goToDetail);
    }
}