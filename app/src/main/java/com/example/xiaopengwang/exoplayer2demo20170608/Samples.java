/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.xiaopengwang.exoplayer2demo20170608;


import java.util.Locale;

/**
 * Holds statically defined sample definitions.
 */
/* package */ class Samples {

    public static class Sample {

        public final String name;
        public final String contentId;
        public final String provider;
        public final String uri;

        public Sample(String name, String uri) {
            this(name, name.toLowerCase(Locale.US).replaceAll("\\s", ""), "", uri);
        }

        public Sample(String name, String contentId, String provider, String uri) {
            this.name = name;
            this.contentId = contentId;
            this.provider = provider;
            this.uri = uri;
        }

    }

    public static final Sample[] HLS = new Sample[]{
            new Sample("Akamai Http cherry blossom",
                    "http://ipad.akamai.com/Video_Content/npr/cherryblossoms_hdv_bug/all.m3u8" ),
            new Sample("BigBuckBunny",
                    "http://playertest.longtailvideo.com/adaptive/bunny/manifest.m3u8" ),
            new Sample("Small HLS Video",
                    "https://walterebert.com/playground/video/hls/sintel-trailer.m3u8" ),
    };

    public static final Sample[] HLS_LIVE = new Sample[]{
            new Sample("Vevo Live",
                    "http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8" ),
//                    "http://vevoplaylist-live.hls.adaptive.level3.net/vevo/ch1/appleman.m3u8" ),
    };

    public static final Sample[] MP4 = new Sample[]{
            new Sample("ToyStory MP4",
                    "http://www.html5videoplayer.net/videos/toystory.mp4" ),
            new Sample("Ocean Clip",
                    "http://video-js.zencoder.com/oceans-clip.mp4" ),
    };

    private Samples() {
    }

}
