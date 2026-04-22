UPDATE kyzz_question_bank qb
LEFT JOIN admin_user au ON au.id = qb.created_by
SET qb.created_by = NULL
WHERE qb.created_by IS NOT NULL
  AND au.id IS NULL;

ALTER TABLE kyzz_question_bank
    DROP FOREIGN KEY fk_kyzz_question_bank_created_by;

ALTER TABLE kyzz_question_bank
    ADD CONSTRAINT fk_kyzz_question_bank_created_by
        FOREIGN KEY (created_by) REFERENCES admin_user (id);
