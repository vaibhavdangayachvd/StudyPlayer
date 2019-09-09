package com.example.studyplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public final class SavedVideoDatabase extends SQLiteOpenHelper {

    public SavedVideoDatabase(Context context) {
        super(context, "StudyPlayer", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String table="create table saved(id integer PRIMARY KEY,title varchar(50),description varchar(200),filename varchar(30),size integer,created varchar(30))";
        sqLiteDatabase.execSQL(table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public void add_record(Video video)
    {
        SQLiteDatabase call=this.getWritableDatabase();
        ContentValues coll=new ContentValues();
        coll.put("id",video.getId());
        coll.put("title",video.getTitle());
        coll.put("description",video.getDescription());
        coll.put("filename",video.getFilename());
        coll.put("size",video.getSize());
        coll.put("created",video.getCreated());
        call.insert("saved",null,coll);
        call.close();
    }
    public Cursor getAllSavedVideos()
    {
        String query="select * from saved";
        SQLiteDatabase call=this.getReadableDatabase();
        Cursor cursor = call.rawQuery(query,null);
        cursor.moveToFirst();
        return cursor;
    }
    public void delete(String id)
    {
        String query="delete from saved where id="+id;
        SQLiteDatabase call=this.getWritableDatabase();
        call.execSQL(query);
        call.close();
    }
}
