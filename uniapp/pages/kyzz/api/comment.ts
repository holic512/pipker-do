import request from '@/shared/network/request'
import type {
	KyzzPracticeCommentCreateRequest,
	KyzzPracticeCommentItem,
	KyzzPracticeCommentLikeToggleResponse,
	KyzzPracticeCommentPageResponse,
	KyzzPracticeCommentQuery
} from '@/pages/kyzz/practice/types'

// AI 索引: KYZZ 小程序题目评论 API。

export function getPracticeQuestionComments(
	questionId: number,
	params: KyzzPracticeCommentQuery
): Promise<KyzzPracticeCommentPageResponse> {
	return request({
		url: `/api/kyzz/comments/questions/${questionId}`,
		method: 'GET',
		data: params
	})
}

export function createPracticeQuestionComment(
	questionId: number,
	data: KyzzPracticeCommentCreateRequest
): Promise<KyzzPracticeCommentItem> {
	return request({
		url: `/api/kyzz/comments/questions/${questionId}`,
		method: 'POST',
		header: {
			'Content-Type': 'application/json'
		},
		data
	})
}

export function likePracticeQuestionComment(commentId: number): Promise<KyzzPracticeCommentLikeToggleResponse> {
	return request({
		url: `/api/kyzz/comments/${commentId}/like`,
		method: 'PUT'
	})
}

export function unlikePracticeQuestionComment(commentId: number): Promise<KyzzPracticeCommentLikeToggleResponse> {
	return request({
		url: `/api/kyzz/comments/${commentId}/like`,
		method: 'DELETE'
	})
}
