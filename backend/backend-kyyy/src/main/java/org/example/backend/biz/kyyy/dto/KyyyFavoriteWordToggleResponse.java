/**
 * @file KyyyFavoriteWordToggleResponse
 * @project pipker-do
 * @module 考研英语 / 单词收藏
 * @description 返回单词收藏开关操作结果，供查词页即时更新星标状态。
 * @logic 1. 标识目标单词；2. 返回最新收藏状态；3. 保持 PUT/DELETE 响应结构统一。
 * @dependencies API: /api/kyyy/favorite-words
 * @index_tags 考研英语, 单词收藏, 收藏开关, 响应DTO
 * @author holic512
 */
package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KyyyFavoriteWordToggleResponse implements Serializable {

    private Long wordId;

    private Boolean isFavorite;
}
