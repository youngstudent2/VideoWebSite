package cn.edu.nju.videowebsite.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cn.edu.nju.videowebsite.config.MinioProp;
import cn.edu.nju.videowebsite.model.VideoInfo;
import cn.edu.nju.videowebsite.util.FFmpegException;
import cn.edu.nju.videowebsite.util.VideoTransformer;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.errors.InvalidArgumentException;
import io.minio.messages.Item;

@Service
public class VideoService {
    private final static Logger logger = LoggerFactory.getLogger(VideoService.class);

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private VideoTransformer videoTransformer;

    @Value("${ffmpeg.path}")
    private String ffmpegPath;

    private static final String ENCODE_QUEUE_NAME = "encodeQueue";
    private volatile boolean isRunning = false;
    private java.util.Queue<String> fileQueue = new LinkedList<>();

    @Bean
	public Queue encodeQueue() {
		return new Queue(ENCODE_QUEUE_NAME, false);
	}
    
    public Collection<VideoInfo> getAllVideos() {
        Collection<VideoInfo> videoInfos = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects("video");
            for (Result<Item> result : results) {
                Item item = result.get();
                videoInfos.add(new VideoInfo(item.lastModified(), item.objectSize(), item.objectName()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoInfos;
    }

    public void deleteVideo(String videoName) throws Exception {      
        minioClient.removeObject(MinioProp.MINIO_BUCKET, videoName);
        minioClient.removeObject(MinioProp.BUCKET_360p, "360p_" + videoName);
        minioClient.removeObject(MinioProp.BUCKET_720p, "720p_" + videoName);
        minioClient.removeObject(MinioProp.BUCKET_1080p, "1080p_" + videoName);
        
    }

    public InputStream getVideoStream(String videoName, String resolution) throws Exception {
        InputStream stream = null;
        if (resolution == "origin") {
            stream = minioClient.getObject(MinioProp.MINIO_BUCKET, videoName);
        }
        else if (resolution.equals("1080p")) {
            stream = minioClient.getObject(MinioProp.BUCKET_1080p, resolution + "_" + videoName);
        }
        else if (resolution.equals("720p")) {
            stream = minioClient.getObject(MinioProp.BUCKET_720p, resolution + "_" + videoName);
        }
        else if (resolution.equals("360p")) {
            stream = minioClient.getObject(MinioProp.BUCKET_360p, resolution + "_" + videoName);
        }
        else {
            throw new InvalidArgumentException("resolution's value is invalid");
        }
        return stream;
    }

    public boolean upload(MultipartFile file) {
        String orgfileName = file.getOriginalFilename();
        try {
            InputStream in = file.getInputStream();
            String contentType = file.getContentType();
            minioClient.putObject(MinioProp.MINIO_BUCKET, orgfileName, in, null, null, null, contentType);
            Map<String, Object> data = new HashMap<>();
            data.put("bucketName", MinioProp.MINIO_BUCKET);
            template.convertAndSend("encodeQueue", orgfileName+","+contentType);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @RabbitListener(queues = ENCODE_QUEUE_NAME)
    public void receiveMessage(String fileInfo) {
        logger.info("fileInfo: " + fileInfo);
        fileQueue.add(fileInfo);
        if (!isRunning) {
            isRunning = true;
            createEncodeMasterThread(fileInfo).start();
        }
    }

    private volatile int ThreadNum = 0;
    private static final int MAX_THREAD_NUM = 3;
    private Thread createEncodeMasterThread(String fileInfo) {
        logger.info("create encode master thread");
        return new Thread() {
            @Override
            public void run () {
                while(!fileQueue.isEmpty()) {
                    logger.info("ThreadNum:   " + ThreadNum);
                    logger.info("FizeSize:   " + fileQueue.size());
                    if (ThreadNum < MAX_THREAD_NUM) {
                        String fileInfo = fileQueue.remove();
                        System.out.print(fileInfo);
                        ThreadNum++;
                        createEncodeThread(fileInfo).start();
                    }
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                }
            }
        };
    }

    private Thread createEncodeThread(String fileInfo) {
        return new Thread() {
            @Override
            public void run () {
                String[] arr = fileInfo.split(",");
                String fileName = arr[0];
                String contentType = arr[1];
                try {
                    InputStream inputStream = minioClient.getObject(MinioProp.MINIO_BUCKET, fileName);

                    File originFile = File.createTempFile("originFile", ".mp4");
                    FileOutputStream fileOutputStream = new FileOutputStream(originFile);
                    int count = 0;
                    byte[] buffer = new byte[100];

                    while ((count = inputStream.read(buffer)) != -1) {
                        fileOutputStream.write(buffer, 0, count);
                    }

                    inputStream.close();
                    fileOutputStream.close();

                    transform(fileName, contentType, originFile, VideoResolution.R_1080P);
                    transform(fileName, contentType, originFile, VideoResolution.R_720P);
                    transform(fileName, contentType, originFile, VideoResolution.R_360P);
                    
                    originFile.delete();             

                } catch (Exception e) {
                    e.printStackTrace();
                }
                ThreadNum--;
            }
        };
    }

    private enum VideoResolution {
        R_1080P,
        R_720P,
        R_360P
    }

    private void transform(String fileName, String contentType, File originFile, VideoResolution resolution) throws IOException{
        File tempFile = null;
        InputStream inputStream = null;
        try {
            tempFile = File.createTempFile("tempFile" + resolution.name(), ".mp4");
            logger.info(tempFile.getName());
            switch(resolution) {
                case R_1080P:
                    videoTransformer.transform(ffmpegPath, originFile.getAbsolutePath(), tempFile.getAbsolutePath(), "1920x1080");
                    inputStream = new FileInputStream(tempFile);
                    minioClient.putObject(MinioProp.BUCKET_1080p, "1080p_" + fileName, inputStream, null, null, null, contentType);
                    break;
                case R_720P:
                    videoTransformer.transform(ffmpegPath, originFile.getAbsolutePath(), tempFile.getAbsolutePath(), "1280x720");
                    inputStream = new FileInputStream(tempFile);
                    minioClient.putObject(MinioProp.BUCKET_720p, "720p_" + fileName, inputStream, null, null, null, contentType);
                    break;
                case R_360P:
                    videoTransformer.transform(ffmpegPath, originFile.getAbsolutePath(), tempFile.getAbsolutePath(), "600x360");                
                    inputStream = new FileInputStream(tempFile);
                    minioClient.putObject(MinioProp.BUCKET_360p, "360p_" + fileName, inputStream, null, null, null, contentType);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if (tempFile != null) {
                tempFile.delete();
            }
        }
        
        
    }

}
