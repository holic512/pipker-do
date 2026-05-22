/**
 * @file KyyyReadingAnnotationUtil
 * @project pipker-do
 * @module 考研英语 / 小程序阅读标注
 * @description 提供阅读正文与题干的词级切片、区间计算和标注命中判断能力。
 * @logic 1. 将原始文本切成可交互 token；2. 根据起止 token 生成选区区间；3. 判断 token 与标注或预选区的命中关系。
 * @dependencies Types: @/pages/kyyy/reading/types
 * @index_tags 考研英语, 阅读标注, token切片, 区间计算
 * @author holic512
 */

import type {
	KyyyReadingAnnotationState,
	KyyyReadingTextToken,
	KyyyReadingTextTokenKind
} from '@/pages/kyyy/reading/types'

export interface KyyyReadingRangeState {
	startOffset: number
	endOffset: number
	selectedText: string
}

const TOKEN_REGEX = /[A-Za-z0-9]+(?:['’-][A-Za-z0-9]+)*|\r\n|\n|\s+|[^\sA-Za-z0-9]/g

function normalizeTokenKind(tokenText: string): KyyyReadingTextTokenKind {
	if (tokenText === '\n' || tokenText === '\r\n') {
		return 'newline'
	}
	if (/^\s+$/.test(tokenText)) {
		return 'space'
	}
	if (/^[A-Za-z0-9]+(?:['’-][A-Za-z0-9]+)*$/.test(tokenText)) {
		return 'word'
	}
	return 'punct'
}

export function tokenizeReadingText(sourceText: string): KyyyReadingTextToken[] {
	const text = typeof sourceText === 'string' ? sourceText : ''
	if (!text) {
		return []
	}
	const tokens: KyyyReadingTextToken[] = []
	const matcher = new RegExp(TOKEN_REGEX)
	let matched: RegExpExecArray | null
	while ((matched = matcher.exec(text)) !== null) {
		const tokenText = matched[0]
		const startOffset = matched.index
		const endOffset = startOffset + tokenText.length
		tokens.push({
			key: `${startOffset}-${endOffset}`,
			text: tokenText,
			startOffset,
			endOffset,
			kind: normalizeTokenKind(tokenText)
		})
	}
	return tokens
}

export function isSelectableToken(token: KyyyReadingTextToken | null | undefined): boolean {
	if (!token) {
		return false
	}
	return token.kind !== 'space' && token.kind !== 'newline'
}

export function buildRangeFromTokens(
	sourceText: string,
	anchorToken: KyyyReadingTextToken,
	focusToken: KyyyReadingTextToken
): KyyyReadingRangeState {
	const startOffset = Math.min(anchorToken.startOffset, focusToken.startOffset)
	const endOffset = Math.max(anchorToken.endOffset, focusToken.endOffset)
	return {
		startOffset,
		endOffset,
		selectedText: sliceSourceText(sourceText, startOffset, endOffset)
	}
}

export function sliceSourceText(sourceText: string, startOffset: number, endOffset: number): string {
	const text = typeof sourceText === 'string' ? sourceText : ''
	if (startOffset < 0 || endOffset <= startOffset || endOffset > text.length) {
		return ''
	}
	return text.substring(startOffset, endOffset)
}

export function annotationHitsToken(annotation: KyyyReadingAnnotationState,
                                    token: KyyyReadingTextToken): boolean {
	return annotation.startOffset < token.endOffset && annotation.endOffset > token.startOffset
}

export function rangeHitsToken(range: KyyyReadingRangeState | null | undefined,
                               token: KyyyReadingTextToken): boolean {
	if (!range) {
		return false
	}
	return range.startOffset < token.endOffset && range.endOffset > token.startOffset
}

export function findAnnotationByToken(annotations: KyyyReadingAnnotationState[],
                                      token: KyyyReadingTextToken): KyyyReadingAnnotationState | null {
	return annotations.find((item) => annotationHitsToken(item, token)) || null
}

export function hasAnnotationOverlap(annotations: KyyyReadingAnnotationState[],
                                     range: KyyyReadingRangeState,
                                     excludeAnnotationId: number | null = null): boolean {
	return annotations.some((item) => {
		if (excludeAnnotationId && item.id === excludeAnnotationId) {
			return false
		}
		return item.startOffset < range.endOffset && item.endOffset > range.startOffset
	})
}
