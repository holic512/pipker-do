/**
 * @file KyyyReadingPageHelpers
 * @project pipker-do
 * @module 考研英语 / 阅读做题
 * @description 提供阅读页输入归一化与错误文案解析等轻量辅助函数。
 * @logic 1. 解析页面 query 中的正整数；2. 统一提取错误 message；3. 减少页面文件内的重复辅助代码。
 * @dependencies None
 * @index_tags 考研英语, 阅读页辅助, 错误信息, query解析
 * @author holic512
 */

export function toPositiveInt(value: unknown): number | null {
	const parsed = Number(value)
	return Number.isFinite(parsed) && parsed > 0 ? Math.floor(parsed) : null
}

export function resolveErrorMessage(error: unknown, fallback: string): string {
	if (error instanceof Error && error.message) {
		return error.message
	}
	return fallback
}
