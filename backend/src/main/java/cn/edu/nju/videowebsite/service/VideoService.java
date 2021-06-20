package cn.edu.nju.videowebsite.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cn.edu.nju.videowebsite.config.MinioProp;
import cn.edu.nju.videowebsite.util.VideoTransformer;
import io.minio.MinioClient;

@Service
public class VideoService {
    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RabbitTemplate template;

    private static final String ENCODE_QUEUE_NAME = "encodeQueue";
    private volatile boolean isRunning = false;
    private java.util.Queue<String> fileQueue = new LinkedList<>();

    @Bean
	public Queue encodeQueue() {
		return new Queue(ENCODE_QUEUE_NAME, false);
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
        fileQueue.add(fileInfo);
        if (!isRunning) {
            isRunning = true;
            createEncodeMasterThread(fileInfo);
        }
    }

    private volatile int ThreadNum = 0;
    private static final int MAX_THREAD_NUM = 3;
    private Thread createEncodeMasterThread(String fileInfo) {
        return new Thread() {
            @Override
            public void run () {
                while(!fileQueue.isEmpty()) {
                    System.out.print("ThreadNum:   " + ThreadNum + "\n");
                    System.out.print("FizeSize:   " + fileQueue.size() + "\n");
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

                    VideoTransformer videoTransformer = new VideoTransformer();
                    File file_720p = File.createTempFile("file_720p", ".mp4");
                    File file_360p = File.createTempFile("file_360p", ".mp4");

                    videoTransformer.transform("..\\ffmpeg\\bin\\ffmpeg.exe", originFile.getAbsolutePath(),
                            file_720p.getAbsolutePath(), "1280x720");
                            videoTransformer.transform("..\\ffmpeg\\bin\\ffmpeg.exe", originFile.getAbsolutePath(),
                            file_360p.getAbsolutePath(), "600x360");

                    InputStream in_720p = new FileInputStream(file_720p);
                    InputStream in_360p = new FileInputStream(file_360p);
                    minioClient.putObject(MinioProp.BUCKET_720p, "720p_" + fileName, in_720p, null, null, null, contentType);
                    minioClient.putObject(MinioProp.BUCKET_360p, "360p_" + fileName, in_360p, null, null, null, contentType);

                    originFile.delete();
                    file_720p.delete();
                    file_360p.delete();
                    

                } catch (Exception e) {
                    e.printStackTrace();
                }
                ThreadNum--;
            }
        };
    }

}
