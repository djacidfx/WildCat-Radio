package com.app.mywild.activities;


import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.app.mywild.R;
import com.app.mywild.models.SocialItem;
import com.app.mywild.services.RadioPlayerService;
import com.app.mywild.utils.Constant;
import com.app.mywild.utils.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;
import com.makeramen.roundedimageview.RoundedImageView;
import com.next.androidintentlibrary.BrowserIntents;

import java.util.ArrayList;
import java.util.List;

import eu.gsottbauer.equalizerview.EqualizerView;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class MainActivity extends AppCompatActivity {

    RoundedImageView imgRadio;
    ImageView imgMusicBackground;

    private static final String TAG = "MainActivity";
    ProgressBar progressBar;
    MaterialButton fabPlayExpand;
    TextView txtRadioExpand, txtSongExpand;
    EqualizerView equalizerView;
    Utils utils;
    LinearLayout lytExit;
    View lytDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils(this);

        initComponent();
        initExitDialog();
        displayData();

        final AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert am != null;

        SeekBar volumeSeekBar = findViewById(R.id.volumeSeekBar);
        volumeSeekBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekBar.setProgress(am.getStreamVolume(AudioManager.STREAM_MUSIC));
        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                try {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                } catch (Exception e) {
                    e.printStackTrace();
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, i, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    private void displayData() {
        fabPlayExpand.setOnClickListener(view -> {
            if (!utils.isNetworkAvailable()) {
                Toast.makeText(MainActivity.this, getResources().getString(R.string.internet_not_connected), Toast.LENGTH_SHORT).show();
            } else {
                final Intent intent = new Intent(MainActivity.this, RadioPlayerService.class);

                if (RadioPlayerService.getInstance() != null) {
                    intent.setAction(RadioPlayerService.ACTION_TOGGLE);
                } else {
                    RadioPlayerService.createInstance().initializeRadio(this);
                    intent.setAction(RadioPlayerService.ACTION_PLAY);
                }
                startService(intent);
            }
        });


        if (Boolean.parseBoolean(getString(R.string.autoplay_enabled))) {
            if (utils.isNetworkAvailable()) {
                new Handler(Looper.getMainLooper()).postDelayed(() -> fabPlayExpand.performClick(), Constant.DELAY_PERFORM_CLICK);
            }
        }

        initSocialItems();

    }

    public void initSocialItems() {
        LinearLayout socialLyt = findViewById(R.id.social_items_layout);

        if(Boolean.parseBoolean(getString(R.string.enable_social_items))){
            socialLyt.setVisibility(View.VISIBLE);
        }
        else {
            socialLyt.setVisibility(View.GONE);
        }

        TypedArray typedArray = getResources().obtainTypedArray(R.array.social_items);

        List<SocialItem> socialItems = new ArrayList<>();
        int length = typedArray.length();

        for (int i = 0; i < length; i += 2) {
            int drawableResId = typedArray.getResourceId(i, 0);
            String url = typedArray.getString(i + 1);

            socialItems.add(new SocialItem(drawableResId, url));
        }

        typedArray.recycle();

        for (SocialItem item : socialItems) {
            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(120, 120));
            imageView.setPadding(10, 5, 10, 5);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setImageResource(item.getDrawableResId());

            imageView.setOnClickListener(v -> {
                loadWebsite(item.getUrl());
            });

            socialLyt.addView(imageView);
        }
    }



    public void initComponent() {

        imgRadio = findViewById(R.id.img_radio_large);
        imgRadio.setOval(Boolean.parseBoolean(getString(R.string.circular_album_art)));
        imgMusicBackground = findViewById(R.id.img_music_background);

        equalizerView = findViewById(R.id.equalizer);
        progressBar = findViewById(R.id.progress_bar);

        fabPlayExpand = findViewById(R.id.fab_play);
        txtRadioExpand = findViewById(R.id.txt_radio_name_expand);
        txtSongExpand = findViewById(R.id.txt_metadata_expand);

        if (!utils.isNetworkAvailable()) {
            txtRadioExpand.setText(getResources().getString(R.string.app_name));
            txtSongExpand.setText(getResources().getString(R.string.internet_not_connected));
        }

        setIfPlaying();

    }

    private void loadWebsite(String link) {
        BrowserIntents.from(MainActivity.this).openLink(Uri.parse(link)).show();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    public void changePlayPause(Boolean flag) {
        if (flag) {
            fabPlayExpand.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_button_pause));
            equalizerView.animateBars();
        } else {
            fabPlayExpand.setIcon(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_button_play));
            equalizerView.stopBars();
        }
    }

    public void changeSongName(String songName) {
        Constant.metadata = songName;
        txtSongExpand.setText(songName);
    }

    public void changeAlbumArt(String artworkUrl) {

        Constant.albumArt = artworkUrl;

        Glide.with(getApplicationContext())
                .load(artworkUrl.replace(" ", "%20"))
                .placeholder(R.drawable.radio_image)
                .thumbnail(0.3f)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgRadio);

        Glide.with(getApplicationContext())
                .load(artworkUrl.replace(" ", "%20"))
                .placeholder(R.drawable.radio_image)
                .transform(new BlurTransformation(Integer.parseInt(getString(R.string.background_blur_amount))))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgMusicBackground);
    }


    public void setIfPlaying() {
        if (RadioPlayerService.getInstance() != null) {
            RadioPlayerService.initialize(MainActivity.this);
            changePlayPause(RadioPlayerService.getInstance().isPlaying());
        } else {
            changePlayPause(false);
        }
    }


    public void setBuffer(Boolean flag) {
        if (flag) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        showExitDialog(true);
    }

    public void showExitDialog(boolean exit) {
        if (exit) {
            if (lytExit.getVisibility() != View.VISIBLE) {
                lytExit.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up));
            }
            lytExit.setVisibility(View.VISIBLE);
        } else {
            lytExit.clearAnimation();
            lytExit.setVisibility(View.GONE);
        }
    }

    public void initExitDialog() {

        lytExit = findViewById(R.id.lyt_exit);
        lytDialog = findViewById(R.id.lyt_dialog);

        lytExit.setOnClickListener(v -> {
        });
        lytDialog.setOnClickListener(v -> {
        });

        findViewById(R.id.txt_cancel).setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> showExitDialog(false), 200));
        findViewById(R.id.txt_minimize).setOnClickListener(v -> {
                    showExitDialog(false);
                    new Handler(Looper.getMainLooper()).postDelayed(this::minimizeApp, 200);
                }
        );
        findViewById(R.id.txt_exit).setOnClickListener(v -> new Handler(Looper.getMainLooper()).postDelayed(() -> {
            finish();

            if (isServiceRunning()) {
                Intent stop = new Intent(MainActivity.this, RadioPlayerService.class);
                stop.setAction(RadioPlayerService.ACTION_STOP);
                startService(stop);
                Log.d(TAG, "Radio service is running");
            } else {
                Log.d(TAG, "Radio service is not running");
            }
        }, 200));

    }

    public void minimizeApp() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RadioPlayerService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        initComponent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
