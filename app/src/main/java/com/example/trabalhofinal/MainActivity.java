package com.example.trabalhofinal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.graphics.drawable.AnimatedStateListDrawableCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private static final String INPUT_ID = "_id";
    private static final String DB_NAME = "like_register";
    private static final String TABLE_NAME = "like_user";
    private static final String INPUT_URL = "url";
    private SQLiteDatabase database;
    private String currentUrl;
    private ImageView imageView, like;
    private AnimatedVectorDrawableCompat avd;
    private AnimatedVectorDrawable avd2;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
        database.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + INPUT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + INPUT_URL + " STRING );");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        progress = findViewById(R.id.progress);
        imageView = findViewById(R.id.image);
        like = findViewById(R.id.like);

        renderImage();
        determineIfItIsFirstTimeOpened();
    }

    private void determineIfItIsFirstTimeOpened() {
        Boolean isFirstRun = getSharedPreferences("PREFERENCES", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Se gostar da imagem, clique em cima ou pressione o botão abaixo para pular!")
                    .setTitle("Bem-vindo")
                    .setPositiveButton("OK", null);
            AlertDialog dialog = builder.create();
            dialog.show();
            getSharedPreferences("PREFERENCES", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_app, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.list:
                ArrayList<String> likesList = new ArrayList<String>();
                likesList = loadLikesListFromDatabase();
                Intent listIntent = new Intent(this, ListActivity.class);
                listIntent.putStringArrayListExtra("likesList", likesList);
                startActivity(listIntent);
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private ArrayList<String> loadLikesListFromDatabase() {
        Cursor cursor = database.query(TABLE_NAME, new String[]{INPUT_URL}, null, null, null, null, null);
        cursor.moveToFirst();
        ArrayList<String> likesList = new ArrayList<String>();

        while (cursor.moveToNext()) {
            likesList.add(cursor.getString(0));
        }

        return likesList;
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

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ContentValues values = new ContentValues();
                values.put(INPUT_URL, currentUrl);
                database.insert(TABLE_NAME, null, values);
                renderImage();
            }
        }, 900);

    }

    public void renderImage() {
        try {
            String jsonReturn = makeRequest();
            JSONObject jsonObject = new JSONObject(jsonReturn);
            String imageUrl = jsonObject.getString("message");
            currentUrl = imageUrl;
            new DownloadImageTask().execute(imageUrl);
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

    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            imageView.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            progress.setProgress(0);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progress.setProgress(values[0]);
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            Bitmap bitmap = null;
            InputStream inputStream = null;
            OutputStream outputStream = null;
            HttpURLConnection connection = null;
            InputStream fileInputStream = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                int fileSize = connection.getContentLength();

                inputStream = connection.getInputStream();
                String filePath = getFilesDir() + "/tmp.png";
                outputStream = new FileOutputStream(filePath);

                byte[] data = new byte[4096];
                long total = 0;
                int count;
                while ((count = inputStream.read(data)) != -1) {
                    total += count;
                    publishProgress((int) (total * 100 / fileSize));
                    outputStream.write(data, 0, count);
                }

                fileInputStream = new FileInputStream(filePath);
                bitmap = BitmapFactory.decodeStream(fileInputStream);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            progress.setVisibility(View.GONE);
            if (bitmap != null) {
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(bitmap);
            }
        }
    }
}