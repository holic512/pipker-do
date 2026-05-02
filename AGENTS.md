# Codex Project Rules

<!-- ai-index: root rules entry -->

本文件只作为项目规则入口，保持精简。

## 1. 读取顺序

1. 先读 [rules/README.md](rules/README.md)。
2. 始终补读 [rules/common.md](rules/common.md)。
3. 再按当前任务范围，只打开必要规则，不一次性加载全部规则。

## 2. 任务选读

- 后端任务：读 [rules/backend.md](rules/backend.md)；涉及具体业务再补读 `rules/projects/*.md`。
- 小程序任务：读 [rules/uniapp.md](rules/uniapp.md) 和 [rules/frontend-style.md](rules/frontend-style.md)；涉及具体业务再补读 `rules/projects/*.md`。
- 后台任务：读 [rules/admin-web.md](rules/admin-web.md) 和 [rules/frontend-style.md](rules/frontend-style.md)；涉及具体业务再补读 `rules/projects/*.md`。
- 考研政治：读 [rules/projects/kyzz.md](rules/projects/kyzz.md)。
- 考研英语：读 [rules/projects/kyyy.md](rules/projects/kyyy.md)。
- 考研数学：读 [rules/projects/kysx.md](rules/projects/kysx.md)。

## 3. 入口约束

- 规则扩展统一放在 `rules/` 目录，不再持续膨胀根 `AGENTS.md`。
- 若新增业务项目，优先新增 `rules/projects/<project>.md`，再按需补充端侧规则引用。
