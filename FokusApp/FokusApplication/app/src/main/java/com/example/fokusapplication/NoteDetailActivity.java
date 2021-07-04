 package com.example.fokusapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class NoteDetailActivity extends AppCompatActivity {
    EditText subject, title, note;
    ImageButton submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        subject = findViewById(R.id.edit_text_add_subject);
        title = findViewById(R.id.edit_text_add_title);
        note = findViewById(R.id.edit_text_add_note_taken);
        submit = findViewById(R.id.note_add_button);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String subjectString = subject.getText().toString();
                String titleString = title.getText().toString();
                String noteString = note.getText().toString();

                Intent intent = new Intent(NoteDetailActivity.this, NotesActivity.class);
                intent.putExtra("keySubject", subjectString);
                intent.putExtra("keyTitle", titleString);
                intent.putExtra("keyNote", noteString);

                startActivity(intent);
            }
        });
    }
}