package org.example.backend.biz.kyzz.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 考研政治分类实体。
 */
@Data
@TableName("kyzz_category")
public class KyzzCategory implements Serializable {

    /** 分类主键 ID。 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类编码。 */
    private String categoryCode;

    /** 分类名称。 */
    private String categoryName;

    /** 分类层级。 */
    private Integer categoryLevel;

    /** 排序号。 */
    private Integer sortNo;

    /** 是否启用。 */
    private Integer isEnabled;

    /** 创建时间。 */
    private LocalDateTime createdAt;

    /** 更新时间。 */
    private LocalDateTime updatedAt;
}
