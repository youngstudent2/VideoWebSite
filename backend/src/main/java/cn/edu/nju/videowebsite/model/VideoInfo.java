package cn.edu.nju.videowebsite.model;

import java.io.Serializable;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoInfo implements Serializable {
    private Date lastModified;
    private Long size;
    private String name;
}
