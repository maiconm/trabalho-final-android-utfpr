package com.example.trabalhofinal;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String DB_NAME = "like_register";
    private static final String TABLE_NAME = "like_user";
    private static final String INPUT_URL = "url";
    private SQLiteDatabase database;
    private String currentUrl;
    private ImageView imageView, like;
    AnimatedVectorDrawableCompat avd;
    AnimatedVectorDrawable avd2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + INPUT_URL + " STRING PRIMARY KEY);");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        imageView = findViewById(R.id.image);
        like = findViewById(R.id.like);

        renderImage();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    public void skip(View view) {
        renderImage();
    }

    public void like(View view) {
        final Drawable drawable = like.getDrawable();
        like.setAlpha(.70f);

        if (drawable instanceof AnimatedStateListDrawableCompat) {
            avd = (AnimatedVectorDrawableCompat) drawable;
            avd.start();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (drawable instanceof AnimatedVectorDrawable) {
                avd2 = (AnimatedVectorDrawable) drawable;
                avd2.start();
            }
        }
        ContentValues values = new ContentValues();
        values.put(INPUT_URL, currentUrl);
        database.insert(TABLE_NAME, null, values);
        renderImage();
    }

    public void renderImage() {
        try {
            String jsonReturn = makeRequest();

            JSONObject jsonObject = new JSONObject(jsonReturn);

            String imageUrl = jsonObject.getString("message");
            currentUrl = imageUrl;
            URL url = new URL(imageUrl);
            Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            imageView.setImageBitmap(bmp);

        } catch (final Exception ex) {
            ex.printStackTrace();
            Log.e("Error Image", "renderImage()");
        }
    }

    private String makeRequest() throws IOException {
        HttpURLConnection connection = null;
        String jsonReturn = null;
        try {
            URL url = new URL("https://dog.ceo/api/breeds/image/random");
            connection = (HttpURLConnection) url.openConnection();
            InputStream is = new BufferedInputStream(connection.getInputStream());
            jsonReturn = readStream(is);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return jsonReturn;
    }

    private String readStream(InputStream is) throws IOException {
        String jsonReturn = null;
        BufferedReader reader = null;
        StringBuilder builder = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = reader.readLine()) != null) {
                builder.append(line + "\n");
            }
            jsonReturn = builder.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return jsonReturn;
    }
}