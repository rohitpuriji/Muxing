package com.customer.ffmpegvideo1;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION = 1001;
    private Button btnRecord;
    private VideoView videoView;
    private String videoPath  = Environment.getExternalStorageDirectory() + File.separator + "video.mp4";
    private String accPath = Environment.getExternalStorageDirectory() + File.separator + "audio.mp3";
    private MediaRecorder mRecorder;
    MediaController mc ;
    private ProgressDialog progress;
    private String videoPath_muxer;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String TAG = "muxing";
        Log.e(TAG, "step 1 ");

        btnRecord = findViewById(R.id.btnRecord);
        videoView = findViewById(R.id.videoView);

        videoPath_muxer=Environment.getExternalStorageDirectory() +"/"+"output.mp4";

        mc = new MediaController(this);
        mc.setAnchorView(videoView);
        mc.setMediaPlayer(videoView);
        videoView.setMediaController(mc);


        progress = new ProgressDialog(this);
        progress.setMessage("Muxing in progress...");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setIndeterminate(true);

        getPermissionToRecordAudio();


        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!btnRecord.getText().toString().equalsIgnoreCase("play"))
                startPlayingVideo();
            }
        });


    }

    private void startPlayingVideo() {

        videoView.setVideoPath(videoPath);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer m) {
                try {
                    m.setVolume(0f, 0f);
                    m.setLooping(false);
                    Log.e("video time",videoView.getDuration()+"");
                    startRecording();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mRecorder.stop();
                mRecorder.release();
                mRecorder = null;
                Log.e("onComplete","done");
                Log.e("video size","size = "+mediaPlayer.getDuration());
                Log.e("audio size","size = "+getDuration(new File(accPath)));

                cloneMediaUsingMuxer();

            }
        });

        videoView.requestFocus();

    }

    private static String getDuration(File file) {
        MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
        mediaMetadataRetriever.setDataSource(file.getAbsolutePath());
        String durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        return formateMilliSeccond(Long.parseLong(durationStr));
    }

    public static String formateMilliSeccond(long milliseconds) {
        String finalTimerString = "";
        String secondsString;

        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);

        if (hours > 0) {
            finalTimerString = hours + ":";
        }

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }

        finalTimerString = finalTimerString + minutes + ":" + secondsString;

        return finalTimerString;
    }

    private void startRecording() {

        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setMaxDuration(videoView.getDuration());

        Log.e("filename",accPath);
        mRecorder.setOutputFile(accPath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            videoView.start();
            mRecorder.start();
            btnRecord.setVisibility(View.GONE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getPermissionToRecordAudio() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION);

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED){


                Toast.makeText(this, "Permission granted click on button to record audio.", Toast.LENGTH_SHORT).show();


            } else {
                Toast.makeText(this, "You must give permissions to use this app. App is exiting.", Toast.LENGTH_SHORT).show();
                finishAffinity();
            }
        }

    }

    private void cloneMediaUsingMuxer() {

        progress.show();

        String TAG = "muxing";

        try {

            String outputFile;

            try {

                File file = new File(videoPath_muxer);
                file.createNewFile();
                outputFile = file.getAbsolutePath();

                MediaExtractor videoExtractor = new MediaExtractor();
                videoExtractor.setDataSource(videoPath);

                MediaExtractor audioExtractor = new MediaExtractor();
                audioExtractor.setDataSource(accPath);

                Log.e(TAG, "Video Extractor Track Count " + videoExtractor.getTrackCount() );
                Log.e(TAG, "Audio Extractor Track Count " + audioExtractor.getTrackCount() );

                MediaMuxer muxer = new MediaMuxer(outputFile, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                videoExtractor.selectTrack(0);
                MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
                int videoTrack = muxer.addTrack(videoFormat);

                audioExtractor.selectTrack(0);
                MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
                int audioTrack = muxer.addTrack(audioFormat);

                Log.e(TAG, "Video Format " + videoFormat.toString() );
                Log.e(TAG, "Audio Format " + audioFormat.toString() );

                boolean sawEOS = false;
                int frameCount = 0;
                int offset = 100;
                int sampleSize = 256 * 1024;
                ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
                ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
                MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
                MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();


                videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

                muxer.start();

                while (!sawEOS)
                {
                    videoBufferInfo.offset = offset;
                    videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                    {
                        Log.e(TAG, "saw input EOS.");
                        sawEOS = true;
                        videoBufferInfo.size = 0;

                    }
                    else
                    {
                        videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                        videoBufferInfo.flags = videoExtractor.getSampleFlags();
                        muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                        videoExtractor.advance();


                        frameCount++;
                        Log.e(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                        Log.e(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

                    }
                }

                Toast.makeText(getApplicationContext() , "frame:" + frameCount , Toast.LENGTH_SHORT).show();



                boolean sawEOS2 = false;
                int frameCount2 =0;
                while (!sawEOS2)
                {
                    frameCount2++;

                    audioBufferInfo.offset = offset;
                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                    {
                        Log.e(TAG, "saw input EOS.");
                        sawEOS2 = true;
                        audioBufferInfo.size = 0;
                    }
                    else
                    {
                        audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                        audioBufferInfo.flags = audioExtractor.getSampleFlags();
                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                        audioExtractor.advance();


                        Log.e(TAG, "Frame (" + frameCount + ") Video PresentationTimeUs:" + videoBufferInfo.presentationTimeUs +" Flags:" + videoBufferInfo.flags +" Size(KB) " + videoBufferInfo.size / 1024);
                        Log.e(TAG, "Frame (" + frameCount + ") Audio PresentationTimeUs:" + audioBufferInfo.presentationTimeUs +" Flags:" + audioBufferInfo.flags +" Size(KB) " + audioBufferInfo.size / 1024);

                    }
                }

                muxer.stop();
                muxer.release();

                Log.e("video size","size = "+getDuration(new File(videoPath_muxer)));

                progress.dismiss();
                Toast.makeText(getApplicationContext(),"Clip editing done....",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(MainActivity.this,PlayVideo.class);
                startActivity(intent);


            } catch (IOException e) {
                progress.dismiss();
                Log.e(TAG, "Mixer Error 1 " + e.getMessage());
            } catch (Exception e) {
                progress.dismiss();
                Log.e(TAG, "Mixer Error 2 " + e.getMessage());
            }
        }catch (Exception e){
            progress.dismiss();
            e.printStackTrace();
        }
    }

}