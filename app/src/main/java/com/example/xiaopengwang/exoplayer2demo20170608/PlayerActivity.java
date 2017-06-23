package com.example.xiaopengwang.exoplayer2demo20170608;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.conviva.api.Client;
import com.conviva.api.ConvivaException;
import com.conviva.api.player.PlayerStateManager;
import com.example.xiaopengwang.exoplayer2demo20170608.helper.ConvivaSessionManager;
import com.example.xiaopengwang.exoplayer2demo20170608.helper.DemoPlayerAnalyticsInterface;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveVideoTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

public class PlayerActivity extends AppCompatActivity implements ExoPlayer.EventListener {
    private Context mActivity;
    private SimpleExoPlayerView simpleExoPlayerView;
    private SimpleExoPlayer player;
    private DemoPlayerAnalyticsInterface mPlayerInterface;
    private PlayerStateManager mStateManager;
    private Uri contentUri;
    private String mGatewayUrl;
    private boolean mIsBackPressed = false;
    private long mExoPlayerseek = -1;
    // Bitrate values used to simulate bitrate event
    private int[] bitrateKbps = {300, 600, 900, 1200};
    private int index = -1;
    MediaSource videoSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
        // first
        initConviva();
        createPlayer();
        readyPlay();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (player != null){
            player.seekTo(mExoPlayerseek);
        }
    }

    @Override
    protected void onStop() {
        if (!mIsBackPressed) {
            if (player != null){
                mExoPlayerseek = player.getCurrentPosition();
            }
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        //这里我们认为按了back键就是退出这个视频，销毁session
        if (mIsBackPressed) {
            //5:销毁session，表示对一段视频的检测结束
            releasePlayer();
            //6;释放mClient，退出app销毁即可
            ConvivaSessionManager.deinitClient();
        }
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mIsBackPressed = true;
         }
        return super.onKeyDown(keyCode, event);
    }

    private void releasePlayer() {
        if (player != null) {
            // Since mPlayer will be released, all related PlayerStateManager and analyticsInterface should be released too.
            releaseAnalytics();
        }
    }

    private void releaseAnalytics() {
        if (player != null) {
            player.release();
            if (mPlayerInterface != null) {
                mPlayerInterface.cleanup();
                mPlayerInterface = null;
            }
            ConvivaSessionManager.releasePlayerStateManager();
            ConvivaSessionManager.cleanupConvivaSession();
        }
    }

    private void initConviva() {
        // 1:初始化，app启动一次调用即可
        ConvivaSessionManager.initClient(this, mGatewayUrl);
        //2.创建session，在此之前实例化PlayerStateManager对象。创建session表示对一段视频的检测开始
        mStateManager = ConvivaSessionManager.getPlayerStateManager();
        ConvivaSessionManager.createConvivaSession(contentUri.toString());
    }

    private void readyPlay() {
         // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "ExoPlayerDemo"), bandwidthMeter);
        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
       // This is the MediaSource representing the media to be played.
        MediaSource videoSource = new ExtractorMediaSource(contentUri,
                dataSourceFactory, extractorsFactory, null, null);
       // Prepare the player with the source.
        player.prepare(videoSource);
//        player.setPlayWhenReady(true);
        if (player != null){
            player.addListener(this);
            // 3：player与session绑定，在mPlayer和mStateManager不为null的时候创建
            mPlayerInterface = new DemoPlayerAnalyticsInterface(mStateManager,player);
        }

    }

    private void createPlayer() {
        simpleExoPlayerView = (SimpleExoPlayerView) findViewById(R.id.simpleExoPlayerView);
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveVideoTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector =
                new DefaultTrackSelector(videoTrackSelectionFactory);
        LoadControl loadControl = new DefaultLoadControl();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mActivity,
                Util.getUserAgent(mActivity, "yourApplicationName"), bandwidthMeter);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        videoSource = new ExtractorMediaSource(contentUri,
                dataSourceFactory, extractorsFactory, null, null);
        player = ExoPlayerFactory.newSimpleInstance(mActivity, trackSelector, loadControl);
        if (simpleExoPlayerView != null){
            simpleExoPlayerView.requestFocus();
            simpleExoPlayerView.setPlayer(player);
            player.prepare(videoSource);
            player.setPlayWhenReady(true);
        }
    }

    private void init() {
        setContentView(R.layout.activity_player);
        mActivity = this;
        Intent intent = getIntent();
        contentUri = intent.getData();
        contentUri = Uri.parse("http://baobab.cdn.wandoujia.com/14463059939521445330477778425364388_x264.mp4");
        mGatewayUrl = intent.getStringExtra("gatewayUrl");
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        Log.e("activityInfo","onTimelineChanged:"+timeline+"  "+manifest);

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.e("activityInfo","onTracksChanged:"+trackGroups+"  "+trackSelections.toString());

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.e("activityInfo","onLoadingChanged:"+isLoading+"  ");


    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.e("activityInfo","onPlayerStateChanged:"+playWhenReady+"  "+playbackState);

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        Log.e("activityInfo","onPlayerError:"+error+"  ");

    }

    @Override
    public void onPositionDiscontinuity() {
        Log.e("activityInfo","onPositionDiscontinuity:");

    }
    // 4,click method,广告，自定义错误，自定义事件，比特率等
    public void simulateBitrate(View v) {
        if ((index < 0) || (index > bitrateKbps.length - 1)) {
            index = 0; // resetting the counter
        }
        try {
            Toast.makeText(this,"Update Bittare:"+bitrateKbps[index],Toast.LENGTH_LONG).show();
            ConvivaSessionManager.getPlayerStateManager().setBitrateKbps(bitrateKbps[index]);
        } catch (ConvivaException e) {
            e.printStackTrace();
        }
        index++;
    }

    public void simulateError(View v) {
        String ERROR_MSG = "Simulating Error event";
        try {
            Toast.makeText(this,"Report Error:"+ERROR_MSG,Toast.LENGTH_LONG).show();
            ConvivaSessionManager.getPlayerStateManager().sendError(ERROR_MSG, Client.ErrorSeverity.FATAL);
        } catch (ConvivaException e) {
            e.printStackTrace();
        }
    }

    public void podStart(View v) {
        Toast.makeText(this,"POD_START",Toast.LENGTH_LONG).show();
        ConvivaSessionManager.podEvent(ConvivaSessionManager.POD_EVENTS.POD_START);
        ConvivaSessionManager.adStart();
    }

    public void podEnd(View v) {
        Toast.makeText(this,"POD_END",Toast.LENGTH_LONG).show();
        ConvivaSessionManager.adEnd();
        ConvivaSessionManager.podEvent(ConvivaSessionManager.POD_EVENTS.POD_END);

    }
}
