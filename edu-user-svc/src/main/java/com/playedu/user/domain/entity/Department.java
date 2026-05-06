package com.playedu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("departments")
public class Department implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    @TableField("parent_id")
    private Integer parentId;

    @TableField("parent_chain")
    private String parentChain;

    private Integer sort;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
