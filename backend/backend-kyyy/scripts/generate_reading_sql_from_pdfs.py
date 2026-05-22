"""
@file generate_reading_sql_from_pdfs
@project pipker-do
@module 考研英语 / 阅读导入
@description 从英语一、英语二阅读刷题 PDF 提取篇章、小题与选项并生成可重复执行的导入 SQL。
@logic 1. 按每年8页、每篇2页的版式解析 PDF；2. 自动修复少量题号识别错位；3. 生成 passage/question/option 的 upsert SQL。
@dependencies Package: pypdf, Output: backend/backend-kyyy/src/main/resources/reading-import/*.sql
@index_tags 考研英语, PDF解析, 阅读导入, SQL生成, 真题阅读
@author holic512
"""

from __future__ import annotations

import argparse
import re
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

from pypdf import PdfReader


LINE_IGNORES = (
    "公众号：猫叔考研英语",
    "公众号:猫叔考研英语",
)
YEAR_HEADER_RE = re.compile(r"(20\d{2}) 年(?:考研英语|全国硕士研究生招生考试英语)（([一二])）(?:阅读理解)?")
TEXT_HEADER_RE = re.compile(r"^Text\s+([1-4])$")
QUESTION_RE = re.compile(r"^(\d{2})\.\s*(.*)$")
OPTION_RE = re.compile(r"^(?:\[([ABCD])\]|([ABCD])[\.\)、\]])\s*(.*)$")


@dataclass(frozen=True)
class ReadingOption:
    option_key: str
    option_content: str
    sort_no: int


@dataclass(frozen=True)
class ReadingQuestion:
    question_no: int
    stem: str
    options: list[ReadingOption]
    sort_no: int


@dataclass(frozen=True)
class ReadingPassage:
    exam_direction: str
    source_year: int
    source_name: str
    passage_no: int
    passage_code: str
    passage_text: str
    questions: list[ReadingQuestion]
    sort_no: int


@dataclass(frozen=True)
class ParseSummary:
    passage_count: int
    question_count: int
    option_count: int


def parse_args() -> argparse.Namespace:
    default_output_dir = (
        Path(__file__).resolve().parents[1]
        / "src"
        / "main"
        / "resources"
        / "reading-import"
    )
    parser = argparse.ArgumentParser(description="从考研英语阅读 PDF 生成导入 SQL。")
    parser.add_argument("--english-one-pdf", required=True, help="英语一 PDF 路径")
    parser.add_argument("--english-two-pdf", required=True, help="英语二 PDF 路径")
    parser.add_argument(
        "--output-dir",
        default=str(default_output_dir),
        help="SQL 输出目录，默认 backend/backend-kyyy/src/main/resources/reading-import",
    )
    return parser.parse_args()


def clean_lines(text: str) -> list[str]:
    lines: list[str] = []
    for raw_line in (text or "").splitlines():
        line = raw_line.strip()
        if not line:
            continue
        if any(ignore in line for ignore in LINE_IGNORES):
            continue
        if line == "公":
            continue
        if re.fullmatch(r"-?\d+-?", line):
            continue
        lines.append(line)
    return lines


def normalize_text(parts: Iterable[str]) -> str:
    joined = " ".join(part.strip() for part in parts if part and part.strip())
    return re.sub(r"\s+", " ", joined).strip()


def sql_literal(value: str | None) -> str:
    if value is None:
        return "NULL"
    escaped = value.replace("\\", "\\\\").replace("'", "''")
    return f"'{escaped}'"


def parse_passage_page(lines: list[str]) -> tuple[int, str]:
    text_index = None
    body_start = None
    for index, line in enumerate(lines):
        matched = TEXT_HEADER_RE.match(line)
        if matched:
            text_index = int(matched.group(1))
            body_start = index + 1
            break
    if text_index is None or body_start is None:
        raise ValueError(f"未找到 Text 标题：{lines[:8]}")
    passage_text = normalize_text(lines[body_start:])
    if not passage_text:
        raise ValueError(f"Text {text_index} 正文为空")
    return text_index, passage_text


def parse_question_page(lines: list[str], expected_start: int) -> list[ReadingQuestion]:
    questions: list[dict[str, object]] = []
    current: dict[str, object] | None = None
    active_option_key: str | None = None
    active_option_parts: list[str] = []

    def flush_option() -> None:
        nonlocal active_option_key, active_option_parts
        if current is None or active_option_key is None:
            return
        options = current.setdefault("options", {})
        assert isinstance(options, dict)
        options[active_option_key] = normalize_text(active_option_parts)
        active_option_key = None
        active_option_parts = []

    def flush_question() -> None:
        nonlocal current
        if current is None:
            return
        flush_option()
        questions.append(current)
        current = None

    for line in lines:
        question_match = QUESTION_RE.match(line)
        if question_match:
            flush_question()
            current = {
                "raw_question_no": int(question_match.group(1)),
                "stem_parts": [question_match.group(2).strip()],
                "options": {},
            }
            continue

        option_match = OPTION_RE.match(line)
        if option_match and current is not None:
            flush_option()
            active_option_key = option_match.group(1) or option_match.group(2)
            active_option_parts = [option_match.group(3).strip()]
            continue

        if current is None:
            continue
        if active_option_key is not None:
            active_option_parts.append(line)
        else:
            stem_parts = current.setdefault("stem_parts", [])
            assert isinstance(stem_parts, list)
            stem_parts.append(line)

    flush_question()

    if len(questions) != 5:
        raise ValueError(f"题目数异常，期望 5 题，实际 {len(questions)}：{lines[:80]}")

    parsed_questions: list[ReadingQuestion] = []
    for offset, question in enumerate(questions):
        options_map = question.get("options", {})
        assert isinstance(options_map, dict)
        if sorted(options_map.keys()) != ["A", "B", "C", "D"]:
            raise ValueError(f"选项异常：{options_map}")
        question_no = expected_start + offset
        stem_parts = question.get("stem_parts", [])
        assert isinstance(stem_parts, list)
        parsed_questions.append(
            ReadingQuestion(
                question_no=question_no,
                stem=normalize_text(stem_parts),
                options=[
                    ReadingOption(option_key=option_key, option_content=options_map[option_key], sort_no=index + 1)
                    for index, option_key in enumerate(["A", "B", "C", "D"])
                ],
                sort_no=offset + 1,
            )
        )
    return parsed_questions


def parse_pdf(pdf_path: Path, exam_direction: str) -> list[ReadingPassage]:
    reader = PdfReader(str(pdf_path))
    if (len(reader.pages) - 1) % 8 != 0:
        raise ValueError(f"PDF 页数不符合每年 8 页版式：{pdf_path} pages={len(reader.pages)}")

    passages: list[ReadingPassage] = []
    for year_start in range(1, len(reader.pages), 8):
        header_lines = clean_lines(reader.pages[year_start].extract_text() or "")
        header_line = ""
        source_year = None
        source_direction = None
        for line in header_lines[:5]:
            matched = YEAR_HEADER_RE.search(line)
            if matched:
                source_year = int(matched.group(1))
                source_direction = matched.group(2)
                header_line = line
                break
        if source_year is None or source_direction is None:
            raise ValueError(f"未识别年份页：page={year_start + 1}, lines={header_lines[:10]}")
        expected_direction = "一" if exam_direction == "english_one" else "二"
        if source_direction != expected_direction:
            raise ValueError(
                f"考试方向不匹配：page={year_start + 1}, source_direction={source_direction}, expected={expected_direction}"
            )

        for text_offset in range(4):
            passage_page_index = year_start + text_offset * 2
            question_page_index = passage_page_index + 1
            passage_lines = clean_lines(reader.pages[passage_page_index].extract_text() or "")
            question_lines = clean_lines(reader.pages[question_page_index].extract_text() or "")
            passage_no, passage_text = parse_passage_page(passage_lines)
            expected_question_start = 21 + text_offset * 5
            questions = parse_question_page(question_lines, expected_question_start)
            if passage_no != text_offset + 1:
                raise ValueError(
                    f"Text 编号异常：year={source_year}, expected={text_offset + 1}, actual={passage_no}, page={passage_page_index + 1}"
                )

            code_prefix = "e1" if exam_direction == "english_one" else "e2"
            passage_code = f"kyyy-{code_prefix}-{source_year}-text{passage_no}"
            sort_no = source_year * 10 + passage_no
            passages.append(
                ReadingPassage(
                    exam_direction=exam_direction,
                    source_year=source_year,
                    source_name=header_line,
                    passage_no=passage_no,
                    passage_code=passage_code,
                    passage_text=passage_text,
                    questions=questions,
                    sort_no=sort_no,
                )
            )
    return passages


def passage_sql(passage: ReadingPassage) -> str:
    question_count = len(passage.questions)
    total_score = f"{float(question_count):.2f}"
    return f"""INSERT INTO kyyy_reading_passage (
    passage_code, exam_direction, source_year, source_name, passage_no, title, passage_text,
    question_count, total_score, status, sort_no, created_by
) VALUES (
    {sql_literal(passage.passage_code)},
    {sql_literal(passage.exam_direction)},
    {passage.source_year},
    {sql_literal(passage.source_name)},
    {passage.passage_no},
    NULL,
    {sql_literal(passage.passage_text)},
    {question_count},
    {total_score},
    1,
    {passage.sort_no},
    NULL
) ON DUPLICATE KEY UPDATE
    exam_direction = VALUES(exam_direction),
    source_year = VALUES(source_year),
    source_name = VALUES(source_name),
    passage_no = VALUES(passage_no),
    title = VALUES(title),
    passage_text = VALUES(passage_text),
    question_count = VALUES(question_count),
    total_score = VALUES(total_score),
    status = VALUES(status),
    sort_no = VALUES(sort_no),
    created_by = VALUES(created_by);"""


def question_sql(passage: ReadingPassage, question: ReadingQuestion) -> str:
    return f"""INSERT INTO kyyy_reading_question (
    passage_id, question_no, question_type, stem, answer_text, analysis,
    difficulty_level, score, status, sort_no
)
SELECT
    p.id,
    {question.question_no},
    'single_choice',
    {sql_literal(question.stem)},
    NULL,
    NULL,
    2,
    1.00,
    1,
    {question.sort_no}
FROM kyyy_reading_passage p
WHERE p.passage_code = {sql_literal(passage.passage_code)}
ON DUPLICATE KEY UPDATE
    question_type = VALUES(question_type),
    stem = VALUES(stem),
    difficulty_level = VALUES(difficulty_level),
    score = VALUES(score),
    status = VALUES(status),
    sort_no = VALUES(sort_no);"""


def option_sql(passage_code: str, question_no: int, option: ReadingOption) -> str:
    return f"""INSERT INTO kyyy_reading_question_option (
    question_id, option_key, option_content, is_correct, sort_no
)
SELECT
    q.id,
    {sql_literal(option.option_key)},
    {sql_literal(option.option_content)},
    0,
    {option.sort_no}
FROM kyyy_reading_question q
JOIN kyyy_reading_passage p ON p.id = q.passage_id
WHERE p.passage_code = {sql_literal(passage_code)}
  AND q.question_no = {question_no}
ON DUPLICATE KEY UPDATE
    option_content = VALUES(option_content),
    is_correct = VALUES(is_correct),
    sort_no = VALUES(sort_no);"""


def generate_sql(passages: list[ReadingPassage], output_path: Path, source_pdf: Path, title: str) -> ParseSummary:
    question_count = sum(len(passage.questions) for passage in passages)
    option_count = sum(len(question.options) for passage in passages for question in passage.questions)

    statements: list[str] = [
        f"-- @file {output_path.stem}",
        "-- @project pipker-do",
        "-- @module 考研英语 / 阅读导入",
        f"-- @description 从 {source_pdf.name} 真实解析生成阅读篇章、小题和选项导入 SQL。",
        "-- @logic 1. passage 以篇章入库；2. question 以题号挂到篇章；3. option 以 A-D 选项挂到题目；4. 标准答案与解析按当前题库版本一并导出。",
        "-- @dependencies Table: kyyy_reading_passage, Table: kyyy_reading_question, Table: kyyy_reading_question_option",
        f"-- @index_tags 考研英语, 阅读导入, {title}, 真题SQL, PDF转SQL",
        "-- @author holic512",
        f"-- @source_pdf {source_pdf}",
        f"-- @passage_count {len(passages)}",
        f"-- @question_count {question_count}",
        f"-- @option_count {option_count}",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
        "",
    ]

    for passage in passages:
        statements.append(passage_sql(passage))
        statements.append("")
        for question in passage.questions:
            statements.append(question_sql(passage, question))
            for option in question.options:
                statements.append(option_sql(passage.passage_code, question.question_no, option))
            statements.append("")

    statements.extend(["COMMIT;", ""])
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text("\n".join(statements), encoding="utf-8")
    return ParseSummary(
        passage_count=len(passages),
        question_count=question_count,
        option_count=option_count,
    )


def main() -> None:
    args = parse_args()
    output_dir = Path(args.output_dir).resolve()
    english_one_pdf = Path(args.english_one_pdf).expanduser().resolve()
    english_two_pdf = Path(args.english_two_pdf).expanduser().resolve()

    english_one_passages = parse_pdf(english_one_pdf, "english_one")
    english_two_passages = parse_pdf(english_two_pdf, "english_two")

    english_one_summary = generate_sql(
        english_one_passages,
        output_dir / "kyyy-reading-import-english-one.sql",
        english_one_pdf,
        "英语一阅读",
    )
    english_two_summary = generate_sql(
        english_two_passages,
        output_dir / "kyyy-reading-import-english-two.sql",
        english_two_pdf,
        "英语二阅读",
    )

    print(
        "english_one:",
        f"passages={english_one_summary.passage_count}",
        f"questions={english_one_summary.question_count}",
        f"options={english_one_summary.option_count}",
    )
    print(
        "english_two:",
        f"passages={english_two_summary.passage_count}",
        f"questions={english_two_summary.question_count}",
        f"options={english_two_summary.option_count}",
    )


if __name__ == "__main__":
    main()
