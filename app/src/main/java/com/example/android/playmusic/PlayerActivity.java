package com.example.android.playmusic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Random;

import static com.example.android.playmusic.MainActivity.musicFiles;
import static com.example.android.playmusic.MainActivity.repeatBoolean;
import static com.example.android.playmusic.MainActivity.shuffleBoolean;

public class PlayerActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener
{
    TextView songName,artistName,durationPlayed,durationTotal;
    ImageView nextBtn,prevBtn,backBtn,shuffleBtn,repeatBtn,playPauseBtn;
    ImageView coverArt;
    SeekBar seekbar;
    int position = -1;
    static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    static Uri uri;
    static MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Thread prevThread,nextThread,playThread;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        initViews();
        getIntentMethod();

        songName.setText(listSongs.get(position).getTitle());
        artistName.setText(listSongs.get(position).getArtist());



        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                if (mediaPlayer!=null && b)
                {
                    mediaPlayer.seekTo(i*1000);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mediaPlayer.setOnCompletionListener(this);

        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer!=null)
                {
                    int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                    seekbar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedTime(mCurrentPosition));

                }
                handler.postDelayed(this::run,1000);
            }
        });

        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (shuffleBoolean)
                {
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_24);
                }
                else
                {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_24_blue);
                }

            }
        });

        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                if (repeatBoolean)
                {
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_24_white);
                }
                else
                {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_24);
                }

            }
        });

    }

    protected void onResume() {
        super.onResume();
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
    }

    private void prevThreadBtn()
    {
        prevThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        prevBtnClicked();
                        songName.setText(listSongs.get(position).getTitle());
                        artistName.setText(listSongs.get(position).getArtist());
                    }
                });
            }
        };
        prevThread.start();
    }

    private void nextThreadBtn()
    {
        nextThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        nextBtnClicked();
                        songName.setText(listSongs.get(position).getTitle());
                        artistName.setText(listSongs.get(position).getArtist());
                    }
                });
            }
        };

        nextThread.start();
    }

    private void playThreadBtn()
    {
        playThread = new Thread()
        {
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        playPauseBtnClicked();

                    }
                });
            }
        };
        playThread.start();
    }

    private void playPauseBtnClicked()
    {
        if (mediaPlayer.isPlaying())
        {
            playPauseBtn.setImageResource(R.drawable.baseline_not_started_white_48);
            mediaPlayer.pause();
            seekbar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null)
                    {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this::run,1000);
                }
            });
        }

        else
        {
            playPauseBtn.setImageResource(R.drawable.baseline_pause_circle_filled_white_48);
            mediaPlayer.start();
            seekbar.setMax(mediaPlayer.getDuration()/1000);

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null)
                    {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this::run,1000);
                }
            });
        }
    }

    private void prevBtnClicked()
    {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean)
            {

                position = ((position-1) < 0 ? (listSongs.size() -1):(position-1));
            }

            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null)
                    {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this::run,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
        }

        else
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean)
            {

                position = ((position-1) < 0 ? (listSongs.size() -1):(position-1));
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null)
                    {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this::run,1000);
                }
            });
            mediaPlayer.setOnCompletionListener(this);
        }

    }

    private void nextBtnClicked()
    {
        if (mediaPlayer.isPlaying())
        {
            mediaPlayer.stop();
            mediaPlayer.release();

            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean)
            {
                position = (position+1)%listSongs.size();
            }
            position = ((position+1)%listSongs.size());
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null)
                    {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this::run,1000);
                }
            });

            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();

        }

        else
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            if (shuffleBoolean && !repeatBoolean)
            {
                position = getRandom(listSongs.size()-1);
            } else if (!shuffleBoolean && !repeatBoolean)
            {
                position = (position+1)%listSongs.size();
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getTitle());

            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer!=null)
                    {
                        int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000;
                        seekbar.setProgress(mCurrentPosition);

                    }
                    handler.postDelayed(this::run,1000);
                }
            });
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);

        }
    }

    private int getRandom(int i)
    {
        Random random = new Random();
        return random.nextInt(i+1);
    }

    private String formattedTime(int mCurrentPosition)
    {
        String totalOut = "";
        String totalNew = "";
        String sec = String.valueOf(mCurrentPosition%60);
        String min = String.valueOf(mCurrentPosition/60);
        totalOut = min + ":" + sec;
        totalNew = min + ":" + "0" + sec;

        if (sec.length()==1)
        {
            return totalNew;
        }
        else
        {
            return totalOut;
        }
    }

    private void getIntentMethod()
    {
        position = getIntent().getIntExtra("Position",-1);
        listSongs = musicFiles;
        System.out.println("THe uri is: "+uri);

        if (listSongs!=null)
        {
            System.out.println("THe uri is: "+uri);
            playPauseBtn.setImageResource(R.drawable.baseline_pause_circle_filled_white_48);
            uri = Uri.parse(listSongs.get(position).getPath());
            System.out.println(uri);
        }

        if(mediaPlayer!=null)
        {
            mediaPlayer.stop();
            mediaPlayer.release();
            System.out.println("THe uri is: "+uri);
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }
        else
        {
            System.out.println("THe uri is: "+uri);
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
        }

        seekbar.setMax(mediaPlayer.getDuration()/ 1000);
        metaData(uri);
    }

    private void initViews()
    {
        playPauseBtn = findViewById(R.id.playPauseBtn_np);
        songName = findViewById(R.id.songName_np);
        artistName  = findViewById(R.id.songArtist_np);
        durationPlayed = findViewById(R.id.durationStart_np);
        durationTotal = findViewById(R.id.duratioTotal_np);
        nextBtn = findViewById(R.id.nextSongBtn_np);
        prevBtn = findViewById(R.id.prevSongBtn_np);
        backBtn = findViewById(R.id.back_np);
        shuffleBtn = findViewById(R.id.shuffle_np);
        repeatBtn = findViewById(R.id.repeat_np);
        seekbar = findViewById(R.id.seekbar_np);
        coverArt = findViewById(R.id.music_img);
    }

    private void metaData(Uri uri)
    {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int duration_total = Integer.parseInt(listSongs.get(position).getDuration())/1000;
        durationTotal.setText(formattedTime(duration_total));
//        byte[] art = retriever.getEmbeddedPicture();
//        if (art!=null)
//        {
//            Glide.with(this).asBitmap().load(art).into(coverArt);
//        }
//        else
//        {
//            Glide.with(this).asBitmap().load(R.drawable.music_note).into(coverArt);
//
//        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer)
    {
        nextBtnClicked();
        if (mediaPlayer != null)
        {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),uri);
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.stop();
            mediaPlayer.release();
        }

    }

//    public void ImageAnination(Context context, ImageView imageView, Bitmap bitmap)
//    {
//        Animation animOut = AnimationUtils.loadAnimation(context,android.R.anim.fade_out);
//        Animation animIn = AnimationUtils.loadAnimation(context,android.R.anim.fade_in);
//
//        animOut.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation)
//            {
//                Glide.with(context).load(bitmap).into(imageView);
//                animIn.setAnimationListener(new Animation.AnimationListener() {
//                    @Override
//                    public void onAnimationStart(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animation animation) {
//
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animation animation) {
//
//                    }
//                });
//                imageView.startAnimation(animIn);
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//
//
//
//    }
}
