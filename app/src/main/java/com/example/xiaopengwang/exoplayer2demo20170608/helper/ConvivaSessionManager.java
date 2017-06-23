/*! (C) 2015 Conviva, Inc. All rights reserved. Confidential and
proprietary. */


package com.example.xiaopengwang.exoplayer2demo20170608.helper;

import android.content.Context;
import android.util.Log;

import com.conviva.api.AndroidSystemInterfaceFactory;
import com.conviva.api.Client;
import com.conviva.api.ClientSettings;
import com.conviva.api.ContentMetadata;
import com.conviva.api.ConvivaException;
import com.conviva.api.SystemFactory;
import com.conviva.api.SystemSettings;
import com.conviva.api.player.PlayerStateManager;
import com.conviva.api.system.SystemInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that manages Conviva Client, Session, PlayerStateManager and integration API calls.
 */
public class ConvivaSessionManager {
    private static boolean initialized = false;
    private static PlayerStateManager mPlayerStateManager = null;
    private static SystemInterface mAndroidSystemInterface;
    private static SystemFactory mAndroidSystemFactory;
    private static SystemSettings mSystemSettings;
    private static ClientSettings mClientSettings;
    private static Client mClient = null;

    public static int mSessionKey = -1;
    private static final String PLAYER = "ConvivaSessionManager";
    //替换您的Key值
    private static final String customerKey = "1a6d7f0de15335c201e8e9aacbc7a0952f5191d7";
    // The customerKey on Production for customer "c3.DryRun".


    /**
     * Should be called first
     */
    public enum POD_EVENTS {
        POD_START,
        POD_END
    }
    public static Client initClient(Context context, String gatewayUrl) {
        try {
            if (!initialized) {
                mAndroidSystemInterface = AndroidSystemInterfaceFactory.build(context);

                mSystemSettings = new SystemSettings();
                // Do not use DEBUG for production app
                mSystemSettings.logLevel = SystemSettings.LogLevel.DEBUG;
                mSystemSettings.allowUncaughtExceptions = false;

                mAndroidSystemFactory = new SystemFactory(mAndroidSystemInterface, mSystemSettings);

                mClientSettings = new ClientSettings(customerKey);
                if(gatewayUrl != null && !gatewayUrl.isEmpty()) {
                    mClientSettings.gatewayUrl = gatewayUrl;            // client should provide a proper gateway url
                }

                mClient = new Client(mClientSettings, mAndroidSystemFactory);
                initialized = true;
            }

        } catch (Exception ex) {
            Log.e(PLAYER, "Failed to initialize LivePass");
            ex.printStackTrace();
        }
        return mClient;
    }

    // return new playerStateManager
    public static PlayerStateManager getPlayerStateManager() {
        if (mPlayerStateManager == null) {
            mPlayerStateManager = new PlayerStateManager(mAndroidSystemFactory);
        }
        return mPlayerStateManager;
    }

    public static void releasePlayerStateManager() {
        try {
            if (mPlayerStateManager != null) {
                mPlayerStateManager.release();
                mPlayerStateManager = null;
            }
        } catch (Exception e) {
            Log.e(PLAYER, "Failed to release mPlayerStateManager");
        }
    }

    public static void deinitClient() {
        if (!initialized)
            return;

        if (mClient == null) {
            Log.w(PLAYER, "Unable to deinit since client has not been initialized");
            return;
        }

        if (mAndroidSystemFactory != null)
            mAndroidSystemFactory.release();
        try {
            releasePlayerStateManager();
            mClient.release();
        } catch (Exception e) {
            Log.e(PLAYER, "Failed to release client");
        }

        mAndroidSystemFactory = null;
        mClient = null;
        initialized = false;
    }

    /**
     * Called when player has been created and the media url is known.
     * Note that:
     * This function may be called multiple times by the same player and
     * for different sessions,
     */
    public static void createConvivaSession(String mediaUrl) {
        if (!initialized || mClient == null) {
            Log.e(PLAYER, "Unable to create session since client not initialized");
            return;
        }

        try {
            if (mSessionKey != -1) {
                cleanupConvivaSession();
            }
        } catch (Exception e) {
            Log.e(PLAYER, "Unable to cleanup session: " + e.toString());
        }

        try {

            ContentMetadata convivaMetaData = new ContentMetadata();
            //视频别名
            convivaMetaData.assetName = "Exoplsyer Test Video";
            //自定义tag
            Map<String, String> tags = new HashMap<String, String>();
            tags.put("key", "value");
            convivaMetaData.custom = tags;
            //比特率
//            convivaMetaData.defaultBitrateKbps = -1;（API废弃）
            mPlayerStateManager.setBitrateKbps(-1);
            //视频的来源
            convivaMetaData.defaultResource = "AKAMAI";
            //设备唯一标识符
            convivaMetaData.viewerId = "Exoplsyer Test Video";
            //app的name
            convivaMetaData.applicationName = "ConvivarDemoPlayer";
            //加载视频的地址
            convivaMetaData.streamUrl = mediaUrl;
            //直播或点播类型
            convivaMetaData.streamType = ContentMetadata.StreamType.VOD;
            //视频持续时间
            convivaMetaData.duration = 0;
            //以帧/秒为单位的视频编码帧速率
            convivaMetaData.encodedFrameRate = -1;

            mSessionKey = mClient.createSession(convivaMetaData);
            Log.e("key",mSessionKey+"");
            mClient.attachPlayer(mSessionKey, mPlayerStateManager);
        } catch (Exception ex) {
            Log.e(PLAYER, "Failed to create session");
            ex.printStackTrace();
        }
    }

    /**
     * Called after video session has completed
     */
    public static void cleanupConvivaSession() {
        if (!initialized || mClient == null) {
            Log.w(PLAYER, "Unable to clean session since client not initialized");
            return;
        }

        if (mSessionKey != -1) {
            Log.d(PLAYER, "cleanup session: " + mSessionKey);
            try {
                mClient.cleanupSession(mSessionKey);

            } catch (Exception ex) {
                Log.e(PLAYER, "Failed to cleanup");
                ex.printStackTrace();
            }
            mSessionKey = -1;
        }
    }

    public static void reportError(String err, boolean fatal) {
        if (!initialized || mClient == null) {
            Log.e(PLAYER, "Unable to report error since client not initialized");
            return;
        }

        Client.ErrorSeverity severity = fatal ? Client.ErrorSeverity.FATAL : Client.ErrorSeverity.WARNING;
        try {
            mClient.reportError(mSessionKey, err, severity);
        } catch (Exception ex) {
            Log.e(PLAYER, "Failed to report error");
            ex.printStackTrace();
        }
    }

    public static void adStart() {
        if (!initialized || mClient == null) {
            Log.e(PLAYER, "Unable to start Ad since client not initialized");
            return;
        }

        if (mSessionKey == -1) {
            Log.e(PLAYER, "adStart() requires a session");
            return;
        }
        try {
            mClient.adStart(mSessionKey, Client.AdStream.SEPARATE,
                    Client.AdPlayer.SEPARATE,
                    Client.AdPosition.PREROLL);
        } catch (Exception ex) {
            Log.e(PLAYER, "Failed to start Ad");
            ex.printStackTrace();
        }
    }

    public static void adEnd() {
        if (!initialized || mClient == null) {
            Log.e(PLAYER, "Unable to stop Ad since client not initialized");
            return;
        }

        if (mSessionKey == -1) {
            Log.e(PLAYER, "adEnd() requires a session");
            return;
        }
        try {
            mClient.adEnd(mSessionKey);
        } catch (Exception ex) {
            Log.e(PLAYER, "Failed to end Ad");
            ex.printStackTrace();
        }
    }
    public static void seek(int newpos){
        try{
            PlayerStateManager mCurrPlayerStateManager = getPlayerStateManager();
            if(mCurrPlayerStateManager!= null) {
                getPlayerStateManager().setPlayerSeekStart(newpos);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    //Conviva发送一个自定义事件。
    public static void podEvent(POD_EVENTS event) {

        Map<String, Object> attributes = new HashMap();
        attributes.put("podDuration", "60");
        attributes.put("podPosition", "Pre-roll");
        attributes.put("podIndex", "1");

        try {
            switch (event) {
                case POD_START:
                    mClient.sendCustomEvent(mSessionKey, "Conviva.PodStart", attributes);
                    break;

                case POD_END:
                    mClient.sendCustomEvent(mSessionKey, "Conviva.PodEnd", attributes);
                    break;

                default:
                    break;
            }
        } catch (ConvivaException e) {
            e.printStackTrace();
        }

    }
}
