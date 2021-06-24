package cn.edu.nju.videowebsite.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.minio.MinioClient;
import io.minio.errors.InvalidEndpointException;
import io.minio.errors.InvalidPortException;

@Configuration
@EnableConfigurationProperties(MinioProp.class)
public class MinioConfig {
    @Autowired
    private MinioProp minioProp;

    @Bean
    public MinioClient minioClient() throws InvalidPortException, InvalidEndpointException {
        MinioClient client = new MinioClient(minioProp.getEndPoint(),minioProp.getAccesskey(),minioProp.getSecretkey());
        try {
            boolean isExist = client.bucketExists("video");
            if (!isExist) {
                client.makeBucket("video");
            }
            isExist = client.bucketExists("1080p");
            if (!isExist) {
                client.makeBucket("1080p");
            }
            isExist = client.bucketExists("720p");
            if (!isExist) {
                client.makeBucket("720p");
            }
            isExist = client.bucketExists("360p");
            if (!isExist) {
                client.makeBucket("360p");
            }
        } catch (Exception e) {
        }
        return client;
    }
}
