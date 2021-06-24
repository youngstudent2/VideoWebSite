package cn.edu.nju.videoencode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class VideoEncodeApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideoEncodeApplication.class, args);
	}

}
