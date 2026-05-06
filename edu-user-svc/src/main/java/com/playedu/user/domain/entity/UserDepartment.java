package com.playedu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("user_department")
public class UserDepartment implements Serializable {
    @TableId(value = "user_id", type = IdType.NONE)
    private Integer userId;

    @TableField("dep_id")
    private Integer depId;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
