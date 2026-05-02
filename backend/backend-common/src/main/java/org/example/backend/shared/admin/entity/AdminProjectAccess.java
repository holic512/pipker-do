package org.example.backend.shared.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("admin_project_access")
public class AdminProjectAccess implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String projectCode;

    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
