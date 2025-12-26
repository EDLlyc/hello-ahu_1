package com.ahu.helloahu;

import jakarta.persistence.*;

@Entity // 告诉 Spring Boot 这是一个数据库实体类
@Table(name = "alumni") // 指定数据库表名
public class Alumni {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增
    private Long id;

    private String name;    // 姓名
    private String major;   // 专业
    private String email;   // 邮箱
    private String phone;
    private String avatar; // 存放图片的文件名

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // 标准 Getter 和 Setter（手动保留，不依赖 Lombok 插件）
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}