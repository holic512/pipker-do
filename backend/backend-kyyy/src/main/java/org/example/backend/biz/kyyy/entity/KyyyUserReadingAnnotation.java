/**
 * @file KyyyUserReadingAnnotation
 * @project pipker-do
 * @module 考研英语 / 阅读标注
 * @description 映射用户对阅读正文和题干的区间标注记录。
 * @logic 1. 绑定用户、篇章与题目；2. 保存选区偏移、选中文本与备注；3. 通过 sourceTextHash 识别题面变更。
 * @dependencies Table: kyyy_user_reading_annotation
 * @index_tags 考研英语, 阅读标注, 高亮备注, 区间注解
 * @author holic512
 */
package org.example.backend.biz.kyyy.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("kyyy_user_reading_annotation")
public class KyyyUserReadingAnnotation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long passageId;

    private Long questionId;

    private String contentType;

    private Integer startOffset;

    private Integer endOffset;

    private String selectedTextSnapshot;

    private String noteContent;

    private String sourceTextHash;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
