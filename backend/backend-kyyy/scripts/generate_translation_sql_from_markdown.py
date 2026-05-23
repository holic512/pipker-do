"""
@file generate_translation_sql_from_markdown
@project pipker-do
@module 考研英语 / 翻译知识库
@description 解析英语考研翻译 Markdown 目录并生成翻译知识库初始化 SQL。
@logic 1. 读取英一英二 2010-2025 年翻译 Markdown；2. 拆分题干、答案、来源与分段；3. 输出主表与分段表的幂等 UPSERT SQL。
@dependencies Path: 有关文档/英语考研翻译, Table: kyyy_translation_passage, Table: kyyy_translation_segment
@index_tags 考研英语, 翻译, SQL生成, 知识库, Markdown解析
@author holic512
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[3]
BACKEND_ROOT = Path(__file__).resolve().parents[2]
DOC_ROOT = REPO_ROOT / "有关文档" / "英语考研翻译"
OUTPUT_SQL = BACKEND_ROOT / "backend-app" / "src" / "main" / "resources" / "db" / "migration" / "20260523_seed_kyyy_translation_passage.sql"

EXAM_DIRECTION_MAP = {
    "英一": "english_one",
    "英二": "english_two",
}


@dataclass
class TranslationPassageRecord:
    translation_code: str
    exam_direction: str
    source_year: int
    translation_mode: str
    source_title: str
    score_value: int
    segment_count: int
    prompt_instruction: str
    prompt_content: str
    prompt_translation: str
    reference_translation: str
    reference_note: str
    knowledge_tags: str
    source_path: str
    source_prompt_ref: str | None
    source_answer_ref: str | None
    sort_no: int


@dataclass
class TranslationSegmentRecord:
    translation_code: str
    segment_code: str
    segment_no: int
    source_text: str
    translated_text: str | None
    sort_no: int


def normalize_text(value: str) -> str:
    lines = [line.rstrip() for line in value.strip().splitlines()]
    return "\n".join(lines).strip()


def sql_quote(value: str | int | None) -> str:
    if value is None:
        return "NULL"
    if isinstance(value, int):
        return str(value)
    escaped = value.replace("\\", "\\\\").replace("'", "''")
    return f"'{escaped}'"


def extract_sections(content: str) -> dict[str, str]:
    markers = [
        ("### 题目", "prompt_block"),
        ("### 范文", "reference_translation"),
        ("### 题目翻译", "prompt_translation"),
        ("### 范文翻译", "reference_note"),
        ("## 来源", "source_block"),
    ]
    values: dict[str, str] = {}
    for index, (marker, key) in enumerate(markers):
        start = content.find(marker)
        if start < 0:
            raise ValueError(f"missing marker: {marker}")
        start += len(marker)
        end = len(content)
        if index + 1 < len(markers):
            next_marker = markers[index + 1][0]
            next_pos = content.find(next_marker, start)
            if next_pos >= 0:
                end = next_pos
        values[key] = normalize_text(content[start:end])
    return values


def split_instruction_and_content(prompt_block: str) -> tuple[str, str]:
    lines = [line.strip() for line in prompt_block.splitlines() if line.strip()]
    if not lines:
        return "", ""
    content_start = 1
    for index, line in enumerate(lines):
        if re.search(r"(points?\)|（\d+分）|ANSWER SHEET|答题纸上|ANSWERSHEET)", line, flags=re.I):
            content_start = min(index + 1, len(lines))
            break
        if "英译汉" in line and index == 0:
            content_start = 1
    instruction = "\n".join(lines[:content_start]).strip()
    content = "\n".join(lines[content_start:]).strip()
    return instruction, content


def parse_source_refs(source_block: str) -> tuple[str | None, str | None]:
    prompt_ref = None
    answer_ref = None
    for line in source_block.splitlines():
        line = line.strip()
        if line.startswith("- 真题来源："):
            prompt_ref = line.removeprefix("- 真题来源：").strip()
        elif line.startswith("- 译文来源："):
            answer_ref = line.removeprefix("- 译文来源：").strip()
    return prompt_ref, answer_ref


def detect_score_value(prompt_instruction: str, exam_direction: str) -> int:
    score_match = re.search(r"\((\d+)\s*points?\)", prompt_instruction, flags=re.I)
    if score_match:
        return int(score_match.group(1))
    chinese_match = re.search(r"（(\d+)分）", prompt_instruction)
    if chinese_match:
        return int(chinese_match.group(1))
    return 10 if exam_direction == "english_one" else 15


def detect_translation_mode(exam_label: str) -> str:
    return "segmented" if exam_label == "英一" else "passage"


def build_tags(exam_label: str, translation_mode: str) -> str:
    mode_label = "划线句翻译" if translation_mode == "segmented" else "全文翻译"
    return ",".join([exam_label, "翻译", mode_label, "历年真题", "知识库"])


SEGMENT_MARK_RE = re.compile(r"[（(]?([4][6-9]|50)[)）\.．]\s*")


def sanitize_segment_source(value: str) -> str:
    lines: list[str] = []
    for raw in value.splitlines():
        line = raw.strip()
        if not line:
            continue
        if line.startswith("【"):
            break
        if re.match(r"^(?:[4][6-9]|50)[\.．]", line):
            break
        lines.append(line)
    return normalize_text("\n".join(lines))


def sanitize_segment_answer(value: str) -> str:
    cleaned = value.replace("【题干】", "").replace("【答案】", "").strip()
    return normalize_text(cleaned)


def extract_marked_segments(text: str, answer_mode: bool) -> dict[int, str]:
    matches = list(SEGMENT_MARK_RE.finditer(text))
    result: dict[int, str] = {}
    for index, matched in enumerate(matches):
        segment_no = int(matched.group(1))
        if segment_no in result:
            continue
        start = matched.end()
        end = matches[index + 1].start() if index + 1 < len(matches) else len(text)
        block = text[start:end]
        result[segment_no] = sanitize_segment_answer(block) if answer_mode else sanitize_segment_source(block)
    return result


def parse_segmented_segments(prompt_content: str, reference_translation: str, translation_code: str) -> list[TranslationSegmentRecord]:
    source_map = extract_marked_segments(prompt_content, answer_mode=False)
    answer_map = extract_marked_segments(reference_translation, answer_mode=True)
    records: list[TranslationSegmentRecord] = []
    for sort_no, segment_no in enumerate(range(46, 51), start=1):
        source_text = source_map.get(segment_no)
        if not source_text:
            continue
        records.append(
            TranslationSegmentRecord(
                translation_code=translation_code,
                segment_code=f"{translation_code}_{segment_no}",
                segment_no=segment_no,
                source_text=source_text,
                translated_text=answer_map.get(segment_no),
                sort_no=sort_no,
            )
        )
    return records


def parse_passage_segments(prompt_content: str, reference_translation: str, translation_code: str) -> list[TranslationSegmentRecord]:
    return [
        TranslationSegmentRecord(
            translation_code=translation_code,
            segment_code=f"{translation_code}_46",
            segment_no=46,
            source_text=normalize_text(prompt_content),
            translated_text=normalize_text(reference_translation),
            sort_no=1,
        )
    ]


def parse_markdown_file(path: Path, exam_label: str) -> tuple[TranslationPassageRecord, list[TranslationSegmentRecord]]:
    source_year = int(path.stem)
    content = path.read_text(encoding="utf-8")
    extracted = extract_sections(content)
    prompt_instruction, prompt_content = split_instruction_and_content(extracted["prompt_block"])
    translation_mode = detect_translation_mode(exam_label)
    translation_code = f"kyyy_{EXAM_DIRECTION_MAP[exam_label]}_{source_year}_translation"
    source_prompt_ref, source_answer_ref = parse_source_refs(extracted["source_block"])
    segment_records = (
        parse_segmented_segments(prompt_content, extracted["reference_translation"], translation_code)
        if translation_mode == "segmented"
        else parse_passage_segments(prompt_content, extracted["reference_translation"], translation_code)
    )
    normalized_prompt_content = prompt_content
    normalized_reference_translation = extracted["reference_translation"]
    if translation_mode == "segmented" and segment_records:
        normalized_prompt_content = "\n".join(
            f"({segment.segment_no}) {segment.source_text}" for segment in segment_records
        )
        if all(segment.translated_text for segment in segment_records):
            normalized_reference_translation = "\n\n".join(
                f"（{segment.segment_no}）{segment.translated_text}" for segment in segment_records
            )
    passage_record = TranslationPassageRecord(
        translation_code=translation_code,
        exam_direction=EXAM_DIRECTION_MAP[exam_label],
        source_year=source_year,
        translation_mode=translation_mode,
        source_title=f"{source_year}年{exam_label}翻译",
        score_value=detect_score_value(prompt_instruction, EXAM_DIRECTION_MAP[exam_label]),
        segment_count=len(segment_records),
        prompt_instruction=prompt_instruction,
        prompt_content=normalized_prompt_content,
        prompt_translation=extracted["prompt_translation"],
        reference_translation=normalized_reference_translation,
        reference_note=extracted["reference_note"],
        knowledge_tags=build_tags(exam_label, translation_mode),
        source_path=str(path.relative_to(REPO_ROOT)).replace("\\", "/"),
        source_prompt_ref=source_prompt_ref,
        source_answer_ref=source_answer_ref,
        sort_no=source_year * 10,
    )
    return passage_record, segment_records


def load_records() -> tuple[list[TranslationPassageRecord], list[TranslationSegmentRecord]]:
    passages: list[TranslationPassageRecord] = []
    segments: list[TranslationSegmentRecord] = []
    for exam_label in ["英一", "英二"]:
        exam_dir = DOC_ROOT / exam_label
        for path in sorted(exam_dir.glob("*.md")):
            passage, segment_list = parse_markdown_file(path, exam_label)
            passages.append(passage)
            segments.extend(segment_list)
    return passages, segments


def render_sql(passages: list[TranslationPassageRecord], segments: list[TranslationSegmentRecord]) -> str:
    header = """-- @file kyyy_translation_passage_seed
-- @project pipker-do
-- @module 考研英语 / 翻译知识库
-- @description 将英语考研翻译 Markdown 内容转换为翻译知识库初始化数据。
-- @logic 1. 预置英一英二 2010-2025 年翻译主表；2. 为英一按 46-50 划线句拆段，为英二保留全文段；3. 使用 translation_code 与 segment_code 做幂等更新。
-- @dependencies Table: kyyy_translation_passage, Table: kyyy_translation_segment, Script: backend/backend-kyyy/scripts/generate_translation_sql_from_markdown.py
-- @index_tags 考研英语, 翻译, 初始化数据, SQL, 知识库
-- @author holic512
SET NAMES utf8mb4;

"""
    statements = [header]
    passage_columns = [
        "translation_code",
        "exam_direction",
        "source_year",
        "translation_mode",
        "source_title",
        "score_value",
        "segment_count",
        "prompt_instruction",
        "prompt_content",
        "prompt_translation",
        "reference_translation",
        "reference_note",
        "knowledge_tags",
        "source_path",
        "source_prompt_ref",
        "source_answer_ref",
        "status",
        "sort_no",
    ]
    for record in passages:
        values = [
            record.translation_code,
            record.exam_direction,
            record.source_year,
            record.translation_mode,
            record.source_title,
            record.score_value,
            record.segment_count,
            record.prompt_instruction,
            record.prompt_content,
            record.prompt_translation,
            record.reference_translation,
            record.reference_note,
            record.knowledge_tags,
            record.source_path,
            record.source_prompt_ref,
            record.source_answer_ref,
            1,
            record.sort_no,
        ]
        rendered_values = ",\n    ".join(sql_quote(value) for value in values)
        statements.append(
            "INSERT INTO kyyy_translation_passage (\n    "
            + ",\n    ".join(passage_columns)
            + "\n) VALUES (\n    "
            + rendered_values
            + "\n)\nON DUPLICATE KEY UPDATE\n"
            + "    translation_mode = VALUES(translation_mode),\n"
            + "    source_title = VALUES(source_title),\n"
            + "    score_value = VALUES(score_value),\n"
            + "    segment_count = VALUES(segment_count),\n"
            + "    prompt_instruction = VALUES(prompt_instruction),\n"
            + "    prompt_content = VALUES(prompt_content),\n"
            + "    prompt_translation = VALUES(prompt_translation),\n"
            + "    reference_translation = VALUES(reference_translation),\n"
            + "    reference_note = VALUES(reference_note),\n"
            + "    knowledge_tags = VALUES(knowledge_tags),\n"
            + "    source_path = VALUES(source_path),\n"
            + "    source_prompt_ref = VALUES(source_prompt_ref),\n"
            + "    source_answer_ref = VALUES(source_answer_ref),\n"
            + "    status = VALUES(status),\n"
            + "    sort_no = VALUES(sort_no),\n"
            + "    updated_at = CURRENT_TIMESTAMP;\n"
        )

    segment_columns = [
        "translation_id",
        "segment_code",
        "segment_no",
        "source_text",
        "translated_text",
        "sort_no",
    ]
    for record in segments:
        values = [
            f"(SELECT id FROM kyyy_translation_passage WHERE translation_code = {sql_quote(record.translation_code)} LIMIT 1)",
            record.segment_code,
            record.segment_no,
            record.source_text,
            record.translated_text,
            record.sort_no,
        ]
        rendered_values = ",\n    ".join(
            value if isinstance(value, str) and value.startswith("(SELECT ") else sql_quote(value)
            for value in values
        )
        statements.append(
            "INSERT INTO kyyy_translation_segment (\n    "
            + ",\n    ".join(segment_columns)
            + "\n) VALUES (\n    "
            + rendered_values
            + "\n)\nON DUPLICATE KEY UPDATE\n"
            + "    source_text = VALUES(source_text),\n"
            + "    translated_text = VALUES(translated_text),\n"
            + "    sort_no = VALUES(sort_no),\n"
            + "    updated_at = CURRENT_TIMESTAMP;\n"
        )
    return "\n".join(statements)


def main() -> None:
    passages, segments = load_records()
    OUTPUT_SQL.parent.mkdir(parents=True, exist_ok=True)
    OUTPUT_SQL.write_text(render_sql(passages, segments), encoding="utf-8")
    print(f"generated {len(passages)} passages and {len(segments)} segments -> {OUTPUT_SQL}")


if __name__ == "__main__":
    main()
