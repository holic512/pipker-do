package org.example.backend.kyzz.entiy;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyzz_tag")
public class KyzzTag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String tagName;

    private String tagType;

    private String color;

    private Integer useCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
