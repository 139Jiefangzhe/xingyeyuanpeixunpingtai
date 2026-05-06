package com.playedu.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName("users")
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String email;

    private String name;

    private Integer avatar;

    @JsonIgnore
    private String password;

    @JsonIgnore
    private String salt;

    @JsonIgnore
    @TableField("id_card")
    private String idCard;

    private Integer credit1;

    @TableField("create_ip")
    private String createIp;

    @TableField("create_city")
    private String createCity;

    @TableField("is_active")
    private Integer isActive;

    @TableField("is_lock")
    private Integer isLock;

    @TableField("is_verify")
    private Integer isVerify;

    @TableField("verify_at")
    private Date verifyAt;

    @TableField("is_set_password")
    private Integer isSetPassword;

    @TableField("login_at")
    private Date loginAt;

    @TableField("created_at")
    private Date createdAt;

    @TableField("updated_at")
    private Date updatedAt;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
