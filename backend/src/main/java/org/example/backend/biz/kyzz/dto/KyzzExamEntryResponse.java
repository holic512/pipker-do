package org.example.backend.biz.kyzz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.backend.shared.account.dto.VipInfoResponse;

import java.io.Serializable;
import java.util.List;

/**
 * AI 索引: KYZZ VIP 考试入口数据。
 */
@Data
@AllArgsConstructor
public class KyzzExamEntryResponse implements Serializable {

    private VipInfoResponse vipInfo;

    private KyzzExamSummaryResponse ongoingExam;

    private List<KyzzExamPresetResponse> presets;

    private List<KyzzExamDifficultyOptionResponse> difficultyOptions;
}
