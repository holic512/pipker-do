package org.example.backend.shared.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("admin_role")
public class AdminRole implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roleCode;

    private String roleName;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
