package org.example.backend.shared.account.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("app_user")
public class AppUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String wechatOpenid;

    private String wechatUnionid;

    private String username;

    private String nickname;

    private String passwordHash;

    private String phone;

    private String email;

    private String avatarUrl;

    private Integer gender;

    private String bio;

    private Integer status;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
