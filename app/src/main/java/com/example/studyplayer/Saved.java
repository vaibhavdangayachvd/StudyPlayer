package com.example.studyplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.Adapter;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.android.volley.toolbox.StringRequest;

import java.util.ArrayList;

import javax.xml.parsers.SAXParser;

public class Saved extends AppCompatActivity {
    ListView list;
    ImageView placeholder;
    SavedVideoAdaptor adaptor;
    ArrayList<Video> savedList=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Saved Videos");
        setContentView(R.layout.activity_saved);
        initComponents();
        getDataFromDatabase();
        adaptor=new SavedVideoAdaptor(Saved.this, savedList);
        list.setAdapter(adaptor);
    }
    private void getDataFromDatabase()
    {
        SavedVideoDatabase database=new SavedVideoDatabase(Saved.this);
        Cursor cursor=database.getAllSavedVideos();
        if(cursor.getCount()==0)
            return;
        do{
            String id= String.valueOf(cursor.getInt(0));
            String title=cursor.getString(1);
            String description=cursor.getString(2);
            String filename=cursor.getString(3);
            String size=String.valueOf(cursor.getInt(4));
            String created=cursor.getString(5);

            savedList.add(new Video(id,title,description,filename,size,created));
        }
        while(cursor.moveToNext());
        database.close();
    }
    private void initComponents()
    {
        list=findViewById(R.id.savedList);
        placeholder=findViewById(R.id.placeholder);
        list.setEmptyView(placeholder);
    }
}
