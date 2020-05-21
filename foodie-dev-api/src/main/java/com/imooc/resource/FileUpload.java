package com.imooc.resource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

// 添加 Component 才能被扫描
@Component
// 与 properties 文件进行关联
// 添加前缀
@ConfigurationProperties(prefix = "file")
// 定义开发用的配置和生产环境用的配置
@PropertySource("classpath:file-upload-dev.properties")
public class FileUpload {

    private String imageUserFaceLocation;
    private String imageServerUrl;

    public String getImageUserFaceLocation() {
        return imageUserFaceLocation;
    }

    public void setImageUserFaceLocation(String imageUserFaceLocation) {
        this.imageUserFaceLocation = imageUserFaceLocation;
    }

    public String getImageServerUrl() {
        return imageServerUrl;
    }

    public void setImageServerUrl(String imageServerUrl) {
        this.imageServerUrl = imageServerUrl;
    }
}
