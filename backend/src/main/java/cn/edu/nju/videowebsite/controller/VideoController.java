package cn.edu.nju.videowebsite.controller;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.util.IOUtils;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import cn.edu.nju.videowebsite.config.MinioProp;
import cn.edu.nju.videowebsite.service.VideoService;
import io.minio.MinioClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping(path="/")
public class VideoController {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private VideoService videoService;

    @Autowired
    VideoController() {
        try {
            boolean isExist = minioClient.bucketExists("video");
            if (!isExist) {
                minioClient.makeBucket("video");
            }
            isExist = minioClient.bucketExists("720p");
            if (!isExist) {
                minioClient.makeBucket("720p");
            }
            isExist = minioClient.bucketExists("360p");
            if (!isExist) {
                minioClient.makeBucket("360p");
            }
        } catch (Exception e) {
        }
    }

    @PostMapping("/upload") 
    @ResponseBody
    public Object videoUpload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.getSize() == 0) {
            return ResponseEntity.badRequest().build();
        }
        String orgfileName = file.getOriginalFilename();
        if (videoService.upload(file))
            return ResponseEntity.ok().body(orgfileName);

        return ResponseEntity.badRequest().build();
    }

    

    @GetMapping(value="/{version}/{fileName}")
    @ResponseBody
    public void getVideo(@PathVariable("fileName") String fileName, @PathVariable("version") String version, HttpServletResponse response) {
        try {
            InputStream stream = minioClient.getObject(MinioProp.MINIO_BUCKET, fileName);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            response.setHeader("Content-Disposition", "attachment; filename=360p_"+fileName.replace(" ", "_"));
            IOUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
