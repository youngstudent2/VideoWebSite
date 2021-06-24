package cn.edu.nju;

import cn.edu.nju.videowebsite.util.VideoTransformer;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        VideoTransformer videoTransformer = new VideoTransformer();
        videoTransformer.transform("..\\ffmpeg\\bin\\ffmpeg.exe", originFile.getAbsolutePath(),
                            "..\\video_client\\720p\\video", "1280x720");
    }
}
