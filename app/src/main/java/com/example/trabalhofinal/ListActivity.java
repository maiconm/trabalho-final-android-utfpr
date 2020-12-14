package com.example.trabalhofinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class ListActivity extends AppCompatActivity {
    private ListView listView;
    private static final String DB_NAME = "like_register";
    private static final String TABLE_NAME = "like_user";
    private static final String INPUT_URL = "url";
    private static final String INPUT_ID = "_id";
    private SQLiteDatabase database;
    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        database = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + INPUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + INPUT_URL + " STRING );");

        listView = findViewById(R.id.list);

        int[] elements = {android.R.id.text1, android.R.id.text2};

        Cursor cursor = database.query(TABLE_NAME, new String[]{INPUT_URL, INPUT_ID}, null, null, null, null, null);
        adapter = new SimpleCursorAdapter(this, android.R.layout.two_line_list_item, cursor, new String[]{INPUT_URL, INPUT_ID}, elements, 0);
        listView.setAdapter(adapter);
    }
}