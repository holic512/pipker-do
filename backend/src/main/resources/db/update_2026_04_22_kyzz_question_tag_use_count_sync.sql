SET NAMES utf8mb4;

UPDATE kyzz_tag tag
LEFT JOIN (
    SELECT tag_id, COUNT(*) AS actual_use_count
    FROM kyzz_question_tag_rel
    GROUP BY tag_id
) rel ON rel.tag_id = tag.id
SET tag.use_count = COALESCE(rel.actual_use_count, 0)
WHERE tag.tag_type = 'question';
