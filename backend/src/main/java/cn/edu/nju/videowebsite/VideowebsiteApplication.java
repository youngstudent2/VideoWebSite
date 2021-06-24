package cn.edu.nju.videowebsite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class VideowebsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(VideowebsiteApplication.class, args);
	}

}
