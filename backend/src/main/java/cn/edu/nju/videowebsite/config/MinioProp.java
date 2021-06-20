package cn.edu.nju.videowebsite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "minio")
public class MinioProp {
    public static final String MINIO_BUCKET = "video";
    public static final String BUCKET_360p = "360p";
    public static final String BUCKET_720p = "720p";
    @Getter
    @Setter
    private String endPoint = "http://host.docker.internal:9293";
    
    @Getter
    @Setter
    private String accesskey = "root"; // 用户名

    @Getter
    @Setter
    private String secretkey = "root"; // 密码
}
