package cn.edu.nju.videoencode.service;

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

import cn.edu.nju.videoencode.config.MinioProp;
import cn.edu.nju.videoencode.util.FFmpegException;
import cn.edu.nju.videoencode.util.VideoTransformer;
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
        logger.info("create encode master thread for : "+fileInfo);
        return new Thread() {
            @Override
            public void run () {
                while(!fileQueue.isEmpty()) {
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
                isRunning = false;
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

                    Thread[] transformThreads = {
                        transformThread(fileName, contentType, originFile, VideoResolution.R_1080P),
                        transformThread(fileName, contentType, originFile, VideoResolution.R_720P),
                        transformThread(fileName, contentType, originFile, VideoResolution.R_360P)
                    };
                    for (int i=0;i<transformThreads.length;++i) {
                        transformThreads[i].start();
                    }
                    for (int i=0;i<transformThreads.length;++i) {
                        transformThreads[i].join();
                    }
                    logger.info(fileInfo + " finish");
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

    private Thread transformThread(String fileName, String contentType, File originFile, VideoResolution resolution) {
        return new Thread() {
            @Override
            public void run () {
                try {
                    transform(fileName, contentType, originFile, resolution);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private void transform(String fileName, String contentType, File originFile, VideoResolution resolution) throws IOException{
        File tempFile = null;
        InputStream inputStream = null;
        logger.info("transforming "+fileName+" into "+resolution.name());
        try {
            tempFile = File.createTempFile("tempFile", ".mp4");
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
