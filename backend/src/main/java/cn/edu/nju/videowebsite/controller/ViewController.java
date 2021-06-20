package cn.edu.nju.videowebsite.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(path="/view")
public class ViewController {
    @GetMapping("/upload")
    public String uploadSite() {
        return "upload";
    }
}
