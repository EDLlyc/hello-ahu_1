package com.ahu.helloahu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@MapperScan("com.ahu.helloahu") // 重点：让 MP 扫描到你刚才写的 Mapper
public class HelloAhuApplication {

    public static void main(String[] args) {
        SpringApplication.run(HelloAhuApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(AlumniMapper mapper) { // 1. 注入 Mapper
        return args -> {
            String myName = "大力夯";

            // 2. 使用 LambdaQueryWrapper 检查数据库里是否已有该姓名的校友
            Long count = mapper.selectCount(new LambdaQueryWrapper<Alumni>()
                    .eq(Alumni::getName, myName));

            if (count == 0) {
                // 3. 只有不存在时才创建并保存
                Alumni me = new Alumni();
                me.setName(myName);
                me.setMajor("计算机科学与技术");
                me.setEmail("DaLiHang@qq.com");

                mapper.insert(me); // MyBatis-Plus 的插入方法
                System.out.println(">>> 欢迎首位校友入驻：" + myName);
            } else {
                System.out.println(">>> 校友 " + myName + " 已经存在，无需重复初始化。");
            }
        };
    }
}