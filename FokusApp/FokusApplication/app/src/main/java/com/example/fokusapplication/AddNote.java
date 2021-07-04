package com.example.fokusapplication;

public class AddNote {
    String subject;
    String title;
    String noteTaken;



    public AddNote(String subject, String title, String noteTaken) {
        this.subject = subject;
        this.title = title;
        this.noteTaken = noteTaken;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNoteTaken() {
        return noteTaken;
    }

    public void setNoteTaken(String noteTaken) {
        this.noteTaken = noteTaken;
    }

//    @Override
//    public String toString() {
//        return subject ;
//    }
}


