package com.mmlab.n1.save_data;

import com.mmlab.n1.helper.ExternalStorage;

import java.nio.charset.Charset;

/**
 * Created by mmlab on 2015/9/23.
 */
public class Utils {

    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
    private static final String IMAGE_JPG = "jpg";
    private static final String VIDEO_MP4 = "mp4";
    private static final String AUDIO_MP3 = "mp3";

    public static String byteArrayToString(byte[] bytes) {
        return new String(bytes, UTF8_CHARSET);
    }

    public static byte[] stringToByteArray(String string) {
        return string.getBytes(UTF8_CHARSET);
    }

    public static boolean isVideo(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        return extension.equals(VIDEO_MP4) || extension.toLowerCase().equals(VIDEO_MP4);
    }

    public static boolean isImage(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        return extension.equals(IMAGE_JPG) || extension.toLowerCase().equals(IMAGE_JPG);
    }

    public static boolean isAudio(String filename) {
        String extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        return extension.equals(AUDIO_MP3) || extension.toLowerCase().equals(AUDIO_MP3);
    }

    public static int fileType(String filename) {
        if (isImage(filename)) return ExternalStorage.TYPE_IMAGE;
        if (isVideo(filename)) return ExternalStorage.TYPE_VIDEO;
        if (isAudio(filename)) return ExternalStorage.TYPE_AUDIO;
        return ExternalStorage.TYPE_NONE;
    }

    /**
     * 處理單個Poi的資訊
     *
     * @param result
     * @return
     */


    /**
     * 將url轉換成檔案名稱
     *
     * @param url
     * @return
     */
    public static String urlToFilename(String url) {
        return url.substring(url.lastIndexOf('/') + 1);
    }
}
