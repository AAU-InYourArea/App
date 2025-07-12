package aau.inyourarea.app;

import android.media.AudioFormat;

public class Constants {
    public static final String WEBSOCKET_URL = "ws://raw.webbiii.cc:48727";

    // Audio
    public static final int AUDIO_SAMPLE_RATE = 44100;
    public static final int AUDIO_CHANNEL_IN_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    public static final int AUDIO_CHANNEL_OUT_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    public static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
}
