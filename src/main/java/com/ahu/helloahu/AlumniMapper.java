package com.ahu.helloahu;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 告诉 Spring Boot 这是数据层的接口
public interface AlumniMapper extends BaseMapper<Alumni> {
    // 继承 BaseMapper 后，不需要写任何 SQL，你就拥有了增删改查的所有能力！
}