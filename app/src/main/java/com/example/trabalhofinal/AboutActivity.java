package com.example.trabalhofinal;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void openAuthorRepo(View view) {
        openUrl("https://github.com/maiconm");
    }

    public void openGithubRepo(View view) {
        openUrl("https://github.com/maiconm/trabalho-final-android-utfpr");
    }

    public void openDogApi(View view) {
        openUrl("https://dog.ceo/dog-api/");
    }

    public void openInstagramLikeAnimationRepo(View view) {
        openUrl("https://github.com/stevdza-san/Instagram-Like-Animation-Android-Studio");
    }

    public void openUrl(String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }
}