package cn.edu.nju.videoencode.controller;

import java.io.InputStream;
import java.util.Collection;
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

import cn.edu.nju.videoencode.config.MinioProp;
import cn.edu.nju.videoencode.service.VideoService;
import io.minio.MinioClient;
import io.minio.Result;
import io.minio.messages.Item;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping(path="/encode")
public class VideoController {

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private RabbitTemplate template;

    @Autowired
    private VideoService videoService;

    @GetMapping("/")
    public String welcome() {
        return "just for video encode";
    }
    
    
}
