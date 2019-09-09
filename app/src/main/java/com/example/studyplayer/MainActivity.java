package com.example.studyplayer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {
    ListView videos;
    ImageView placeholder;
    VideoLoader videoLoader;
    ProgressBar progressBar;
    LiveData<Boolean> videoObserver;
    ProgressDialog dialog;
    VideoAdaptor adaptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermissions();
        initComponents();
        setLoader();
        setObserver();
    }
    private void requestPermissions(){
        ActivityCompat.requestPermissions(MainActivity.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
    }
    @Override
    protected void onStart() {
        super.onStart();
        progressBar.setVisibility(View.VISIBLE);
        videoLoader.loadVideos();
    }

    private void initComponents() {
        videos = findViewById(R.id.videos);
        placeholder = findViewById(R.id.placeholder);
        videos.setEmptyView(placeholder);
        progressBar=findViewById(R.id.progressBar);
        dialog=new ProgressDialog(MainActivity.this);
        dialog.setMessage("Video is processing...");
        dialog.setCancelable(false);
    }
    private void setLoader() {
        videoLoader= ViewModelProviders.of(MainActivity.this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                return (T)new VideoLoader(MainActivity.this);
            }
        }).get(VideoLoader.class);
    }

    private void setObserver() {
        videoObserver=videoLoader.getVideoObserver();
        videoObserver.observe(MainActivity.this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.GONE);
                if(adaptor==null)
                    adaptor=new VideoAdaptor(MainActivity.this,videoLoader.getVideos(),dialog);
                if(aBoolean)
                {
                    if(videos.getAdapter()==null)
                        videos.setAdapter(adaptor);
                    else
                        adaptor.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
                break;
            case R.id.saved:
                startActivity(new Intent(MainActivity.this, Saved.class));
                break;
        }
        return true;
    }
}
