package com.example.studyplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;

public class Player extends AppCompatActivity {
    private VideoView videoView;
    private ProgressDialog progressDialog;
    private String url;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initComponents();
        Intent intent=getIntent();
        url=intent.getStringExtra("url");
        playVideo();
    }
    private void initComponents()
    {
        videoView=findViewById(R.id.videoView);
    }
    private void playVideo()
    {
        try {
            progressDialog = ProgressDialog.show(Player.this, "",
                    "Buffering video...", false);
            progressDialog.setCancelable(true);
            getWindow().setFormat(PixelFormat.TRANSLUCENT);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialogInterface) {
                    finish();
                }
            });
            MediaController mediaController = new MediaController(Player.this);

            Uri video = Uri.parse(url);
            videoView.setMediaController(mediaController);
            videoView.setVideoURI(video);

            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                public void onPrepared(MediaPlayer mp) {
                    progressDialog.dismiss();
                    videoView.start();
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            Toast.makeText(Player.this,"Video Play Error"+e.getMessage(),Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        File fileToDelete=new File(url);
        if(fileToDelete.exists())
            fileToDelete.delete();
    }
}
