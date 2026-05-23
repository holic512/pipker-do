"""
@file generate_writing_sql_from_markdown
@project pipker-do
@module 考研英语 / 作文知识库
@description 解析英语考研作文 Markdown 目录并生成作文知识库初始化 SQL。
@logic 1. 读取英一英二年度 Markdown；2. 拆分小作文与大作文四段内容；3. 推断基础元数据并输出可重复执行的 UPSERT SQL。
@dependencies Path: 有关文档/英语考研作文, Table: kyyy_writing_essay
@index_tags 考研英语, 作文, SQL生成, 知识库, Markdown解析
@author holic512
"""

from __future__ import annotations

import re
from dataclasses import dataclass
from pathlib import Path


REPO_ROOT = Path(__file__).resolve().parents[3]
BACKEND_ROOT = Path(__file__).resolve().parents[2]
DOC_ROOT = REPO_ROOT / "有关文档" / "英语考研作文"
OUTPUT_SQL = BACKEND_ROOT / "backend-app" / "src" / "main" / "resources" / "db" / "migration" / "20260523_seed_kyyy_writing_essay.sql"

EXAM_DIRECTION_MAP = {
    "英一": "english_one",
    "英二": "english_two",
}

ESSAY_SECTION_MAP = {
    "小作文": "small",
    "大作文": "big",
}


@dataclass
class WritingEssayRecord:
    writing_code: str
    exam_direction: str
    source_year: int
    essay_section: str
    prompt_category: str
    source_title: str
    score_value: int
    word_limit_min: int | None
    word_limit_max: int | None
    prompt_content: str
    sample_content: str
    prompt_translation: str
    sample_translation: str
    knowledge_tags: str
    source_path: str
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
        ("### 题目", "prompt_content"),
        ("### 范文", "sample_content"),
        ("### 题目翻译", "prompt_translation"),
        ("### 范文翻译", "sample_translation"),
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


def detect_prompt_category(essay_section: str, prompt_content: str) -> str:
    lower = prompt_content.lower()
    if essay_section == "small":
        if "email" in lower:
            return "email"
        if "letter" in lower:
            return "letter"
        if "notice" in lower:
            return "notice"
        if "memo" in lower:
            return "memo"
        if "report" in lower:
            return "report"
        return "practical_writing"
    if "chart" in lower or "table" in lower or "graph" in lower or "bar chart" in lower:
        return "chart"
    if "drawing" in lower or "picture" in lower or "cartoon" in lower:
        return "picture"
    return "essay"


def detect_score_value(prompt_content: str, essay_section: str) -> int:
    match = re.search(r"\((\d+)\s*points?\)", prompt_content, flags=re.I)
    if match:
        return int(match.group(1))
    chinese_match = re.search(r"（(\d+)分）", prompt_content)
    if chinese_match:
        return int(chinese_match.group(1))
    return 10 if essay_section == "small" else 20


def detect_word_limits(prompt_content: str) -> tuple[int | None, int | None]:
    range_match = re.search(r"(\d+)\s*[-~]\s*(\d+)\s*words", prompt_content, flags=re.I)
    if range_match:
        return int(range_match.group(1)), int(range_match.group(2))
    about_match = re.search(r"about\s+(\d+)\s+words", prompt_content, flags=re.I)
    if about_match:
        value = int(about_match.group(1))
        return value, value
    answer_match = re.search(r"(\d+)\s*词", prompt_content)
    if answer_match:
        value = int(answer_match.group(1))
        return value, value
    return None, None


def build_tags(exam_label: str, essay_label: str, prompt_category: str) -> str:
    return ",".join([exam_label, essay_label, prompt_category, "历年真题", "范文"])


def parse_markdown_file(path: Path, exam_label: str) -> list[WritingEssayRecord]:
    source_year = int(path.stem)
    content = path.read_text(encoding="utf-8")
    records: list[WritingEssayRecord] = []
    section_pattern = re.compile(r"^##\s+(小作文|大作文)\s*$", flags=re.M)
    matches = list(section_pattern.finditer(content))
    if len(matches) != 2:
        raise ValueError(f"unexpected essay section count in {path}")
    for section_index, match in enumerate(matches):
        essay_label = match.group(1)
        start = match.end()
        end = matches[section_index + 1].start() if section_index + 1 < len(matches) else len(content)
        section_content = content[start:end].strip()
        extracted = extract_sections(section_content)
        essay_section = ESSAY_SECTION_MAP[essay_label]
        prompt_category = detect_prompt_category(essay_section, extracted["prompt_content"])
        word_limit_min, word_limit_max = detect_word_limits(extracted["prompt_content"])
        source_title = f"{source_year}年{exam_label}{essay_label}"
        records.append(
            WritingEssayRecord(
                writing_code=f"kyyy_{EXAM_DIRECTION_MAP[exam_label]}_{source_year}_{essay_section}",
                exam_direction=EXAM_DIRECTION_MAP[exam_label],
                source_year=source_year,
                essay_section=essay_section,
                prompt_category=prompt_category,
                source_title=source_title,
                score_value=detect_score_value(extracted["prompt_content"], essay_section),
                word_limit_min=word_limit_min,
                word_limit_max=word_limit_max,
                prompt_content=extracted["prompt_content"],
                sample_content=extracted["sample_content"],
                prompt_translation=extracted["prompt_translation"],
                sample_translation=extracted["sample_translation"],
                knowledge_tags=build_tags(exam_label, essay_label, prompt_category),
                source_path=str(path.relative_to(REPO_ROOT)).replace("\\", "/"),
                sort_no=(source_year * 10) + (1 if essay_section == "small" else 2),
            )
        )
    return records


def load_records() -> list[WritingEssayRecord]:
    records: list[WritingEssayRecord] = []
    for exam_label in ["英一", "英二"]:
        exam_dir = DOC_ROOT / exam_label
        for path in sorted(exam_dir.glob("*.md")):
            records.extend(parse_markdown_file(path, exam_label))
    return records


def render_sql(records: list[WritingEssayRecord]) -> str:
    header = """-- @file kyyy_writing_essay_seed
-- @project pipker-do
-- @module 考研英语 / 作文知识库
-- @description 将英语考研作文 Markdown 内容转换为作文知识库初始化数据。
-- @logic 1. 预置英一英二 2010-2025 年大小作文；2. 保留题目、范文及中英翻译；3. 使用 writing_code 做幂等更新。
-- @dependencies Table: kyyy_writing_essay, Script: backend/backend-kyyy/scripts/generate_writing_sql_from_markdown.py
-- @index_tags 考研英语, 作文, 初始化数据, SQL, 知识库
-- @author holic512
SET NAMES utf8mb4;

"""
    statements = [header]
    columns = [
        "writing_code",
        "exam_direction",
        "source_year",
        "essay_section",
        "prompt_category",
        "source_title",
        "score_value",
        "word_limit_min",
        "word_limit_max",
        "prompt_content",
        "sample_content",
        "prompt_translation",
        "sample_translation",
        "knowledge_tags",
        "source_path",
        "status",
        "sort_no",
    ]
    for record in records:
        values = [
            record.writing_code,
            record.exam_direction,
            record.source_year,
            record.essay_section,
            record.prompt_category,
            record.source_title,
            record.score_value,
            record.word_limit_min,
            record.word_limit_max,
            record.prompt_content,
            record.sample_content,
            record.prompt_translation,
            record.sample_translation,
            record.knowledge_tags,
            record.source_path,
            1,
            record.sort_no,
        ]
        rendered_values = ",\n    ".join(sql_quote(value) for value in values)
        statements.append(
            "INSERT INTO kyyy_writing_essay (\n    "
            + ",\n    ".join(columns)
            + "\n) VALUES (\n    "
            + rendered_values
            + "\n)\nON DUPLICATE KEY UPDATE\n"
            + "    prompt_category = VALUES(prompt_category),\n"
            + "    source_title = VALUES(source_title),\n"
            + "    score_value = VALUES(score_value),\n"
            + "    word_limit_min = VALUES(word_limit_min),\n"
            + "    word_limit_max = VALUES(word_limit_max),\n"
            + "    prompt_content = VALUES(prompt_content),\n"
            + "    sample_content = VALUES(sample_content),\n"
            + "    prompt_translation = VALUES(prompt_translation),\n"
            + "    sample_translation = VALUES(sample_translation),\n"
            + "    knowledge_tags = VALUES(knowledge_tags),\n"
            + "    source_path = VALUES(source_path),\n"
            + "    status = VALUES(status),\n"
            + "    sort_no = VALUES(sort_no),\n"
            + "    updated_at = CURRENT_TIMESTAMP;\n"
        )
    return "\n".join(statements)


def main() -> None:
    records = load_records()
    OUTPUT_SQL.write_text(render_sql(records), encoding="utf-8")
    print(f"generated {len(records)} records -> {OUTPUT_SQL.relative_to(REPO_ROOT)}")


if __name__ == "__main__":
    main()
