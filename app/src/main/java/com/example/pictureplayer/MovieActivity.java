package com.example.pictureplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.danikula.videocache.file.FileNameGenerator;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.cache.CacheFactory;
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager;
import com.shuyu.gsyvideoplayer.player.PlayerFactory;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.exo2.ExoPlayerCacheManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class MovieActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer> {

    StandardGSYVideoPlayer detailPlayer;

    private static Map<String, String> urlTitleCache = new HashMap<>();

    {
        PlayerFactory.setPlayManager(IjkPlayerManager.class);
        CacheFactory.setCacheManager(ProxyCacheManager.class);
        ProxyCacheManager.setFileNameGenerator(new FileNameGenerator() {
            @Override
            public String generate(String url) {
                return urlTitleCache.get(url);
            }
        });
        GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
        GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", -1));
        list.add(new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1));
        GSYVideoManager.instance().setOptionModelList(list);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.fromParts("package", this.getPackageName(), null));
                startActivity(intent);
            }
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 0);
        }

        setContentView(R.layout.activity_movie);

        Bundle extras = getIntent().getExtras();
        urlTitleCache.putIfAbsent(extras.getString("addr"), extras.getString("title"));

        detailPlayer = (StandardGSYVideoPlayer) findViewById(R.id.detail_player);
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);
        initVideoBuilderMode();
    }

    @Override
    public StandardGSYVideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        Bundle extras = getIntent().getExtras();
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", extras.getString("cookie"));

        return new GSYVideoOptionBuilder()
                .setUrl(extras.getString("addr"))
                .setCacheWithPlay(true)
                .setVideoTitle(extras.getString("title"))
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setAutoFullWithSize(true)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setCacheWithPlay(true)
                .setCachePath(new File(Environment.getExternalStorageDirectory(), "PicturePlayer"))
                .setMapHeadData(headers)
                .setSeekRatio(1);
    }

    @Override
    public void clickForFullScreen() {

    }

    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }

    private static String removeInvalidChars(String fileName) {
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "");
    }
}
