package com.example.pictureplayer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import cn.jzvd.JzvdStd;

public class MovieActivity extends AppCompatActivity {

    private JzvdStd jzvdStd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        jzvdStd = findViewById(R.id.jz_video);
        Bundle extras = getIntent().getExtras();
        jzvdStd.setUp(extras.getString("addr"), extras.getString("title"));
        Glide.with(this).load(extras.getString("cover")).into(jzvdStd.posterImageView);
    }

    @Override
    public void onBackPressed() {
        if (jzvdStd.backPress()) {
            return;
        }
        this.finish();
    }
    @Override
    protected void onPause() {
        super.onPause();
        jzvdStd.releaseAllVideos();
    }
}
