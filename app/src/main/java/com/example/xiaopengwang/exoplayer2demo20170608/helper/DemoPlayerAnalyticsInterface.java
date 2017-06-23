package com.example.xiaopengwang.exoplayer2demo20170608.helper;

import android.media.MediaPlayer;
import android.util.Log;

import com.conviva.api.Client;
import com.conviva.api.ContentMetadata;
import com.conviva.api.ConvivaException;
import com.conviva.api.SystemSettings;
import com.conviva.api.player.IClientMeasureInterface;
import com.conviva.api.player.IPlayerInterface;
import com.conviva.api.player.PlayerStateManager;
import com.conviva.api.player.PlayerStateManager.PlayerState;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerLibraryInfo;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

import java.lang.reflect.Field;


public class DemoPlayerAnalyticsInterface implements IClientMeasureInterface, ExoPlayer.EventListener,IPlayerInterface {
    private PlayerStateManager mStateManager = null;
    private SimpleExoPlayer mPlayer = null;
    private boolean isContentSet = false;
    private SimpleExoPlayer.EventListener eventListener = null;
    private SimpleExoPlayer.VideoListener videoListener = null;
    // FATAL Errors
    public static final String TYPE_SOURCE = "Error Occurred Loading Data From MediaSource";
    public static final String TYPE_RENDERER = "Error Occurred In a Renderer";
    public static final String TYPE_RUNTIME = "error was an unexpected RuntimeException";
    public static final String UNKONW_EXPECTION = "Unkoown Exception";
    public boolean _inListener = false; // True if executing listener (to prevent infinite recursion)

    public DemoPlayerAnalyticsInterface(PlayerStateManager playerStateManager, Object player)  {
        super();
        if (playerStateManager == null) {
            Log("DemoPlayerAnalyticsInterface(): Null playerStateManager argument", SystemSettings.LogLevel.ERROR);
            return;
        }

        if (player == null) {
            Log("DemoPlayerAnalyticsInterface(): Null Player argument", SystemSettings.LogLevel.ERROR);
            return;
        }
        mPlayer = (SimpleExoPlayer) player;
        mStateManager = playerStateManager;
        mStateManager.setPlayerVersion(ExoPlayerLibraryInfo.VERSION);
        mStateManager.setPlayerType("ExoPlayer2");
        mStateManager.setClientMeasureInterface(this);
        for (Field f : SimpleExoPlayer.class.getDeclaredFields()) {
            Class<?> t = f.getType();
            String n = f.getName();
//            Log.e("tag",n);
            if (SimpleExoPlayer.EventListener.class.equals(t)){
                f.setAccessible(true);
                try {
                    eventListener = (SimpleExoPlayer.EventListener)f.get(mPlayer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if (SimpleExoPlayer.VideoListener.class.equals(t)){
                f.setAccessible(true);
                try {
                    videoListener = (SimpleExoPlayer.VideoListener)f.get(mPlayer);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if (mPlayer != null){
            mPlayer.addListener(this);
//            mPlayer.setVideoListener(this);
        }
    }

    public void Log(String message, SystemSettings.LogLevel logLevel) {
        String TAG = "DemoPlayerAnalyticsInterface";
        switch (logLevel) {
            case DEBUG:
                Log.d(TAG, message);
                break;
            case INFO:
                Log.i(TAG, message);
                break;
            case WARNING:
                Log.w(TAG, message);
                break;
            case ERROR:
                Log.e(TAG, message);
                break;
            case NONE:
                break;
            default:
                break;
        }
    }


    public void updateError(ExoPlaybackException errorMsg) throws ConvivaException {
        String errorCode = null;
        if (errorMsg.type == ExoPlaybackException.TYPE_SOURCE){
            errorCode = TYPE_SOURCE+":"+errorMsg.getSourceException();
        }else if (errorMsg.type == ExoPlaybackException.TYPE_RENDERER){
            errorCode = TYPE_RENDERER+":"+errorMsg.getRendererException();
        }else if (errorMsg.type  == ExoPlaybackException.TYPE_UNEXPECTED){
            errorCode = TYPE_RUNTIME+":"+errorMsg.getUnexpectedException();
        }else {
            errorCode = UNKONW_EXPECTION+":"+errorMsg.getMessage();
        }
        if (mStateManager!= null){
            mStateManager.setPlayerState(PlayerState.STOPPED);
            mStateManager.sendError(errorCode, Client.ErrorSeverity.FATAL);
        }

    }

	
	@Override
    public long getPHT() {
        if (mPlayer != null) {
            Log("Current position " + mPlayer.getCurrentPosition(), SystemSettings.LogLevel.ERROR);
            return mPlayer.getCurrentPosition();
        } else {
            return -1;
        }
    }

    @Override
    public int getBufferLength() {
        Log.e("tag","BufferLength");
        return -1;
    }

    @Override
    public double getSignalStrength() {
        Log.e("tag","SignalStrength");

        return -1;
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        if (_inListener){
            return;
        }
        if (eventListener != null){
            _inListener = true;
            eventListener.onTimelineChanged(timeline,manifest);
            _inListener = false;
        }


    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        if (_inListener){
            return;
        }
        Log.e("onTracksChanged", trackGroups+"---"+trackSelections);
        if (eventListener != null){
            _inListener = true;
            eventListener.onTracksChanged(trackGroups,trackSelections);
            _inListener = false;
        }

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        if (_inListener){
            return;
        }
        if (eventListener != null){
            _inListener = true;
            eventListener.onLoadingChanged(isLoading);
            _inListener = false;
        }
    }


    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (_inListener){
            return;
        }
        stateChanged(playWhenReady,playbackState);
        if (eventListener != null){
            _inListener = true;
            eventListener.onPlayerStateChanged(playWhenReady, playbackState);
            _inListener = false;
        }

    }

    public void stateChanged(boolean playWhenReady, int playbackState) {
        try {
            switch (playbackState) {
                case ExoPlayer.STATE_BUFFERING:
                    mStateManager.setPlayerState(PlayerState.BUFFERING);
                    break;
                case ExoPlayer.STATE_ENDED:
                    mStateManager.setPlayerState(PlayerState.STOPPED);
                    break;
                case ExoPlayer.STATE_IDLE:
                    mStateManager.setPlayerState(PlayerState.STOPPED);
                    break;
                case ExoPlayer.STATE_READY:
                    if (playWhenReady) {
                        mStateManager.setPlayerState(PlayerState.PLAYING);
                        if (!isContentSet) { //content length is available only after preparing state
//                            mStateManager.setDuration(((int) mPlayer.getDuration() / 1000));
                            ContentMetadata metadata = new ContentMetadata();
                            metadata.duration =(int) mPlayer.getDuration() / 1000;
                            mStateManager.updateContentMetadata(metadata);
                            isContentSet = true;
                        }
                    } else {
                        mStateManager.setPlayerState(PlayerState.PAUSED);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log("Player state exception", SystemSettings.LogLevel.DEBUG);
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        if (_inListener)
            return;
        try {
            updateError(error);
            mPlayer.removeListener(this);
            if (eventListener != null){
                _inListener = true;
                 eventListener.onPlayerError(error);
                _inListener = false;
            }
        } catch (ConvivaException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPositionDiscontinuity() {
        try {
            mStateManager.setPlayerSeekEnd();
        } catch (ConvivaException e) {
            e.printStackTrace();
        }
        Log.e("msg",mPlayer.getCurrentPosition()+"");

    }

//    public void UpdateVideoSize(int width, int height, int unappliedRotationDegrees,
//                                   float pixelWidthHeightRatio) {
//        Log("video size change. width:" + width + " height:" + height + " unappliedRotationDegrees:" + unappliedRotationDegrees
//                + " pixelWidthHeightRatio:" + pixelWidthHeightRatio , SystemSettings.LogLevel.DEBUG);
//        if (mStateManager != null) {
//
//            try {
//                mStateManager.setVideoWidth(width);
//                mStateManager.setVideoHeight(height);
//            } catch (Exception e) {
//                Log(e.toString(), SystemSettings.LogLevel.DEBUG);
//            }
//        }
//    }

    @Override
    public void cleanup() {
        Log("DemoPlayerAnalyticsInterface.Cleanup()", SystemSettings.LogLevel.DEBUG);
        mStateManager = null;
        isContentSet = false;
        mStateManager = null;
        if (mPlayer != null) {
            mPlayer.addListener(eventListener);
            mPlayer.setVideoListener(videoListener);
            mPlayer = null;
        }
        eventListener = null;
        videoListener = null;
    }

//    @Override
//    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
//        if (_inListener)
//            return;
//        UpdateVideoSize(width,height,unappliedRotationDegrees,pixelWidthHeightRatio);
//        if (videoListener != null){
//            _inListener = true;
//            videoListener.onVideoSizeChanged(width, height, unappliedRotationDegrees,pixelWidthHeightRatio);
//            _inListener = false;
//        }
//    }
//
//    @Override
//    public void onRenderedFirstFrame() {
//        if (_inListener)
//            return;
//        if (videoListener != null){
//            _inListener = true;
//            videoListener.onRenderedFirstFrame();
//            _inListener = false;
//        }
//    }
}