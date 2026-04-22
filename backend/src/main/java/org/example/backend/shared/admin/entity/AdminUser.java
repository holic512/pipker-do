package org.example.backend.shared.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("admin_user")
public class AdminUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String displayName;

    private String passwordHash;

    private Integer status;

    private String defaultProjectCode;

    private LocalDateTime lastLoginAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
