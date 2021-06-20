package cn.edu.nju.videowebsite.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class VideoInfo {
    @Id
    private String name;

    private String dpi;

    private String duration;

    private String bitrate;

    private String charset;

    private String RM;
    
}
