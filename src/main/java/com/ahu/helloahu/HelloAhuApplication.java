package com.ahu.helloahu;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class HelloAhuApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloAhuApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(AlumniRepository repository) {
        return args -> {
            // 1. 创建一个校友对象
            Alumni me = new Alumni();
            me.setName("大力夯"); // 你的名字
            me.setMajor("计算机科学与技术");
            me.setEmail("DaLiHang@qq.com");

            // 2. 保存到数据库
            repository.save(me);
            System.out.println(">>> Day 3 任务成功：校友数据已存入数据库！");
        };
    }
}