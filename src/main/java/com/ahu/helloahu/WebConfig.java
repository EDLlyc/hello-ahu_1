package com.ahu.helloahu;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 让浏览器可以通过 http://localhost:8080/pics/文件名 看到照片
        registry.addResourceHandler("/pics/**")
                .addResourceLocations("file:D:/uploads/");
    }
}