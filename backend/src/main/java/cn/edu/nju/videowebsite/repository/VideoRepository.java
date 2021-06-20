package cn.edu.nju.videowebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import cn.edu.nju.videowebsite.model.VideoInfo;

@Repository
public interface VideoRepository extends JpaRepository<VideoInfo, Integer> {
    
}
