package org.example.backend.shared.project.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("app_user_project_preference")
public class AppUserProjectPreference implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String defaultProjectCode;

    private LocalDateTime lastVisitAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
