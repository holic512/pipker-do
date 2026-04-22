-- AI 索引: KYZZ 题库题量历史回填。
UPDATE kyzz_question_bank qb
LEFT JOIN (
    SELECT question_bank_id, COUNT(*) AS actual_question_count
    FROM kyzz_question
    GROUP BY question_bank_id
) q ON q.question_bank_id = qb.id
SET qb.question_count = COALESCE(q.actual_question_count, 0);
