package com.offcn.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.utils.FastDFSClient;
import org.csource.fastdfs.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;

@RestController
public class UploadController {

    @Value("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;

    @PostMapping("/upload")
    public Result upload(@RequestParam(name = "file") MultipartFile file) {
        //拿到文件拓展名称
        String originalFilename = file.getOriginalFilename();
        //取文件后缀名称
        String extName = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        try {
            //创建fastdfs客户端
            FastDFSClient fastDFSClient = new FastDFSClient("classpath:fdfs_client.conf");
            //执行文件上传
            String uploadFile = fastDFSClient.uploadFile(file.getBytes(), extName);
            System.out.println(Arrays.toString(file.getBytes()));
            String url = FILE_SERVER_URL + uploadFile;
            return new Result(true, StatusCode.OK, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, StatusCode.ERROR, "上传失败");
        }
    }


}
