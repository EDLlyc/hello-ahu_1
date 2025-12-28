package com.ahu.helloahu; // 1. 修改为匹配你目前的文件夹路径

import com.baomidou.mybatisplus.annotation.IdType; // 2. 注意这里没有横杠！
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data // 自动生成 Getter/Setter
@TableName("alumni") // 明确对应数据库里的表名
public class Alumni {

    @TableId(type = IdType.AUTO) // 指定主键自增策略
    private Long id;

    private String name;
    private String major;
    private String email;
    private String phone;
    private String avatar;
}