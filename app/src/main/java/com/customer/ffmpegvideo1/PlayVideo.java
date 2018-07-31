package com.customer.ffmpegvideo1;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;

public class PlayVideo extends AppCompatActivity {

    private MediaController mc ;
    private String videoPath_muxer;
    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_video);

        videoView = findViewById(R.id.videoViewPlay);

        videoPath_muxer=Environment.getExternalStorageDirectory() +"/"+"output.mp4";
        mc = new MediaController(this);
        mc.setAnchorView(videoView);
        mc.setMediaPlayer(videoView);
        videoView.setMediaController(mc);
        videoView.setVideoPath(videoPath_muxer);
        videoView.requestFocus();

    }

}
