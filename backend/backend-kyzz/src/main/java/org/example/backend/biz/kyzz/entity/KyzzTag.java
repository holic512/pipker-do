package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考研政治标签实体。
 */
@Data
@TableName("kyzz_tag")
public class KyzzTag implements Serializable {

    /** 标签主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称。 */
    private String tagName;

    /** 标签类型。 */
    private String tagType;

    /** 标签颜色。 */
    private String color;

    /** 使用次数。 */
    private Integer useCount;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
