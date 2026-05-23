/**
 * @file KyyyUserWordFavorite
 * @project pipker-do
 * @module 考研英语 / 单词收藏
 * @description 映射用户收藏单词关系表，承载首页查词场景的收藏状态。
 * @logic 1. 绑定用户与单词；2. 通过唯一键约束防重；3. 记录收藏时间用于列表排序。
 * @dependencies Table: kyyy_user_word_favorite
 * @index_tags 考研英语, 单词收藏, 用户收藏, 实体
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
@TableName("kyyy_user_word_favorite")
public class KyyyUserWordFavorite implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long wordId;

    private LocalDateTime createdAt;
}
