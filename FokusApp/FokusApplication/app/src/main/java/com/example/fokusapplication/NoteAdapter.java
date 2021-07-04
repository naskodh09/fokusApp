package com.example.fokusapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class NoteAdapter extends ArrayAdapter<AddNote> {

    public NoteAdapter(Context context , int resource, List<AddNote> noteList){

        super(context, resource, noteList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        AddNote addNote = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.note_layout, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.text_view_title);

        title.setText(addNote.getTitle());

        return convertView;
    }
}
