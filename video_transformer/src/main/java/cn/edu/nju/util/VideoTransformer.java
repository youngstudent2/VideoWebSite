package cn.edu.nju.videowebsite.util;

import java.util.ArrayList;
import java.util.List;

public class VideoTransformer {
    
    public boolean transform(String ffmpegPath, String oldPath, String newPath, String resolution) throws FFmpegException {
        List<String> command = getFfmpegCommand(ffmpegPath, oldPath, newPath, resolution);
        if (command.size() > 0) {
            return process(command);
        }
        return false;
    }

    private boolean process(List<String> command) throws FFmpegException {
        try {
            if (null == command || command.size() == 0)
                return false;
            Process videoProcess = new ProcessBuilder(command).redirectErrorStream(true).start();
            videoProcess.getInputStream().close();
            int exitcode = videoProcess.waitFor();
            return exitcode != 1;
        } catch (Exception e) {
            throw new FFmpegException("file transfer failed", e);
        }
    }

    private List<String> getFfmpegCommand(String ffmpegPath, String oldfilepath, String outputPath, String resolution) throws FFmpegException {
        List<String> command = new ArrayList<String>();
        command.add(ffmpegPath); // 添加转换工具路径
        command.add("-i"); // 添加参数＂-i＂，该参数指定要转换的文件
        command.add(oldfilepath); // 添加要转换格式的视频文件的路径
        command.add("-qscale"); // 指定转换的质量
        command.add("4");

        command.add("-r"); // 设置帧速率
        command.add("24");
        command.add("-s"); // 设置分辨率
        command.add(resolution);
        command.add("-y"); // 添加参数＂-y＂，该参数指定将覆盖已存在的文件
        command.add(outputPath);
        return command;
    }
}
