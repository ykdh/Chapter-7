package com.bytedance.videoplayer;

import android.content.Intent;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

import java.util.Formatter;

public class MainActivity extends AppCompatActivity {
    private Button buttonPlay;
    private Button buttonPause;
    private VideoView videoView;
    private SeekBar seekBar;
    private TextView textView;
    private int videoWidth;
    int videoHeight;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonPause = findViewById(R.id.buttonPause);
        buttonPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoView.pause();
            }
        });

        buttonPlay = findViewById(R.id.buttonPlay);
        buttonPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.postDelayed(runnable, 0);
                videoView.start();
                seekBar.setMax(videoView.getDuration());
            }
        });

        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView);

        videoView = findViewById(R.id.videoView);
        //videoView.setVideoPath(getVideoPath(R.raw.ynw1));
        Uri uri = getIntent().getData();
        videoView.setVideoURI(uri);

        //横竖屏转换
        Configuration mConfiguration = this.getResources().getConfiguration();
        int ori = mConfiguration.orientation;
        if (ori == mConfiguration.ORIENTATION_LANDSCAPE) {
            //横屏
            MediaController controller = new MediaController(MainActivity.this);
            videoView.setMediaController(controller);
            controller.setMediaPlayer(videoView);
            videoView.start();
        } else if (ori == mConfiguration.ORIENTATION_PORTRAIT) {
            //竖屏
        }

        //获取视频尺寸
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                        videoWidth = mp.getVideoWidth();
                        videoHeight = mp.getVideoHeight();
                        setDimension();
                    }
                });
            }
        });

        //滑动进度条
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int all = videoView.getDuration();
                int now = seekBar.getProgress();
                videoView.seekTo(now);
                String textAll = stringForTime(all);
                String textNow = stringForTime(now);
                textView.setText(textNow + " / " + textAll);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }

    private String getVideoPath(int resId) {
        return "android.resource://" + this.getPackageName() + "/" + resId;
    }

    //转换毫秒格式
    public static String stringForTime(int time) {
        int secAll = time / 1000;
        int sec = 0;
        if (secAll >= 60) {
            sec = secAll % 60;
        } else {
            sec = secAll;
        }
        int min = secAll / 60;
        return new Formatter().format("%02d:%02d", min, sec).toString();
    }

    //更新进度条
    private Handler handler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (videoView.isPlaying()) {
                int current = videoView.getCurrentPosition();
                int all = videoView.getDuration();
                seekBar.setProgress(current);
                textView.setText(stringForTime(videoView.getCurrentPosition()) + " / " + stringForTime(all));
            }
            handler.postDelayed(runnable, 1000);
        }
    };

    //视频尺寸缩放
    private void setDimension() {
        float videoProportion = (float) videoHeight / (float) videoWidth;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        float screenProportion = (float) screenHeight / (float) screenWidth;
        android.view.ViewGroup.LayoutParams lp = videoView.getLayoutParams();
        //以屏幕宽度为准
        if (videoProportion < screenProportion) {
            if (videoHeight > videoWidth) {
                screenWidth -= 160;
            }
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth * videoProportion);
        }
        //以屏幕高度为准
        else {
            screenHeight -= 205;
            lp.height = screenHeight;
            lp.width = (int) ((float) screenHeight / videoProportion);
        }
        videoView.setLayoutParams(lp);
    }
}
