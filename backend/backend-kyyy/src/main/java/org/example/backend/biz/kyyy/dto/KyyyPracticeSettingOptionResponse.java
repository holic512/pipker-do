package org.example.backend.biz.kyyy.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * AI 索引: KYYY 用户侧考试方向选项。
 */
@Data
@AllArgsConstructor
public class KyyyPracticeSettingOptionResponse implements Serializable {

    private String value;

    private String label;
}
