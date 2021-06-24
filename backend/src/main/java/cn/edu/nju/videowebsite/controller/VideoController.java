package cn.edu.nju.videowebsite.controller;

import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.client.util.IOUtils;

import org.hibernate.annotations.common.util.impl.Log_.logger;
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
import cn.edu.nju.videowebsite.model.VideoInfo;
import cn.edu.nju.videowebsite.service.VideoService;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping(path="/api")
public class VideoController {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private VideoService videoService;

    @GetMapping("/videos")
    public Collection<VideoInfo> videoList() {
        return videoService.getAllVideos();
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

    @DeleteMapping("/video/{videoName}")
    @ResponseBody
    public Object removeVideo(@PathVariable("videoName") String videoName) {
        try {   
            videoService.deleteVideo(videoName);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        } 
    }

    @GetMapping(value="/video/{videoName}")
    @ResponseBody
    public void getVideo(@PathVariable("videoName") String videoName, @RequestParam(value = "resolution", required = false) String resolution, HttpServletResponse response) {
        try {
            if (resolution == null) resolution = "origin";
            InputStream stream = videoService.getVideoStream(videoName, resolution);
            response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
            String prefix = resolution + "_";
            if (prefix == "origin") prefix = "";
            response.setHeader("Content-Disposition", "attachment; filename=" + prefix + videoName.replace(" ", "_"));
            IOUtils.copy(stream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
}
