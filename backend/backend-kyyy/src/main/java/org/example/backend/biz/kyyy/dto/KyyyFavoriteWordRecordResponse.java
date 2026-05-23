/**
 * @file KyyyFavoriteWordRecordResponse
 * @project pipker-do
 * @module 考研英语 / 单词收藏
 * @description 承载单词收藏列表中的单条记录展示字段。
 * @logic 1. 暴露单词基础信息；2. 暴露收藏时间；3. 保持页面搜索与详情跳转所需字段完整。
 * @dependencies Entity: KyyyWord, Entity: KyyyUserWordFavorite
 * @index_tags 考研英语, 单词收藏, 收藏记录, 响应DTO
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KyyyFavoriteWordRecordResponse implements Serializable {

    private Long wordId;

    private String wordText;

    private String phoneticUs;

    private String phoneticUk;

    private String partOfSpeech;

    private String meaningCn;

    private LocalDateTime favoriteAt;
}
