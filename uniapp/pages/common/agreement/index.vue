<template>
	<!-- AI 索引: 小程序用户协议阅读页，由启动弹窗中的协议链接进入。 -->
	<view class="agreement-page theme-page">
		<scroll-view scroll-y class="agreement-page__scroll">
			<view class="agreement-page__hero">
				<text class="agreement-page__eyebrow">USER AGREEMENT</text>
				<text class="agreement-page__title">用户协议</text>
				<text class="agreement-page__summary">
					欢迎使用 pipker-do（考研政治学习小程序）。请在使用本应用前仔细阅读并充分理解本协议。
				</text>
				<view class="agreement-page__meta">
					<text>版本：2026-04-25</text>
					<text>生效日期：2026-04-25</text>
				</view>
			</view>

			<view class="agreement-page__card">
				<view v-for="section in sections" :key="section.title" class="agreement-page__section">
					<text class="agreement-page__section-title">{{ section.title }}</text>
					<text v-for="item in section.items" :key="item" class="agreement-page__paragraph">{{ item }}</text>
				</view>
			</view>
		</scroll-view>

	</view>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
interface AgreementSection {
	title: string
	items: string[]
}

const AGREEMENT_SECTIONS: AgreementSection[] = [
	{
		title: '一、协议范围与接受方式',
		items: [
			'本协议是你与本应用运营方之间就注册、登录、访问和使用 pipker-do（考研政治学习小程序）相关服务所订立的协议。',
			'你通过微信授权登录、访问页面、使用题库、练习、考试、收藏、评论、会员兑换等功能，或点击同意本协议，即视为你已阅读、理解并同意本协议全部内容。',
			'如你不同意本协议任一条款，应立即停止访问或使用本应用。'
		]
	},
	{
		title: '二、账号登录与用户资料',
		items: [
			'本应用主要通过微信小程序登录能力识别用户身份。你应确保授权、填写或更新的昵称、头像、简介等资料真实、合法、适当，不得冒用他人身份或侵犯他人权益。',
			'你应妥善保管微信账号及设备安全。通过你的账号进行的操作通常视为你本人行为；如发现账号异常，应及时通过小程序内客服或反馈入口联系我们。',
			'本应用有权基于账号安全、合规或服务维护需要，对异常账号采取提示、限制使用、暂停或终止服务等措施。'
		]
	},
	{
		title: '三、学习内容与服务规则',
		items: [
			'本应用提供考研政治相关题库、练习、考试、错题、收藏、排行榜等学习辅助能力，相关内容仅供学习参考，不构成考试结果、录取结果或其他确定性承诺。',
			'你在使用评论、反馈等互动功能时，应遵守法律法规、公序良俗和平台规则，不得发布违法违规、侵权、广告营销、恶意刷量、干扰服务秩序或与学习场景无关的内容。',
			'为保障服务质量，本应用可根据运营安排对题库、题目、解析、会员权益、页面功能和服务入口进行新增、调整、下线或维护。'
		]
	},
	{
		title: '四、VIP、兑换码与权益说明',
		items: [
			'本应用可能提供 VIP、兑换码或其他权益服务。具体权益内容、有效期、使用范围、开通或兑换规则，以页面展示、后台配置或实际活动说明为准。',
			'兑换码仅限合法取得并在有效状态下使用，不得通过盗取、破解、倒卖、批量注册、自动化脚本等不正当方式获取或使用。',
			'因用户输入错误、账号异常、违反规则或第三方平台原因导致权益无法正常使用的，本应用将在合理范围内协助处理，但不承诺超出法律规定或页面说明的补偿。'
		]
	},
	{
		title: '五、用户行为规范',
		items: [
			'你不得利用本应用从事危害国家安全、扰乱社会秩序、侵犯他人合法权益、传播违法违规信息、破坏网络安全或违反微信平台规则的行为。',
			'你不得对本应用进行反向工程、恶意抓取、批量请求、绕过风控、攻击服务、干扰正常运营，或以任何方式未经授权获取、复制、传播题库和数据。',
			'如你违反本协议或相关规则，本应用有权删除违规内容、限制功能、暂停或终止账号服务，并依法保留追究责任的权利。'
		]
	},
	{
		title: '六、内容与知识产权',
		items: [
			'本应用中的页面设计、程序代码、题库数据、解析、图文、标识、交互体验及相关资料，除依法归属于第三方或用户自行发布内容外，相关权益由本应用运营方或合法权利人享有。',
			'未经授权，你不得以复制、改编、传播、出售、抓取、镜像、训练模型、建立题库或其他商业化方式使用本应用内容。',
			'你在本应用中提交的反馈、评论等内容，应保证你拥有合法权利；你同意本应用可在提供、优化和展示服务所需范围内使用相关内容。'
		]
	},
	{
		title: '七、个人信息保护',
		items: [
			'本应用会在提供登录、学习记录、会员权益、资料展示、风险控制、客服反馈等服务所必需的范围内处理你的个人信息。',
			'本应用将尽合理努力保护你的个人信息安全，并按照法律法规、微信平台规则及后续发布的隐私政策处理相关信息。',
			'如你对个人信息处理有疑问，或希望行使查询、更正、删除等权利，可通过小程序内客服或意见反馈入口联系我们。'
		]
	},
	{
		title: '八、服务变更、中止与免责',
		items: [
			'本应用会尽力保障服务连续和数据准确，但受网络环境、设备兼容、第三方服务、系统维护、不可抗力等因素影响，服务可能出现延迟、中断、错误或数据展示异常。',
			'对于因用户自身原因、第三方平台原因、不可抗力或非本应用故意/重大过失导致的损失，本应用在法律允许范围内不承担超出法定范围的责任。',
			'本应用可基于业务发展、合规要求或安全原因调整、中止或终止部分服务，并尽可能以合理方式提示用户。'
		]
	},
	{
		title: '九、未成年人使用提示',
		items: [
			'若你属于未成年人，应在监护人同意和指导下阅读、理解并使用本应用服务。',
			'监护人应关注未成年人使用网络学习产品的行为，引导其合理安排学习时间并保护个人信息安全。'
		]
	},
	{
		title: '十、协议更新、法律适用与联系方式',
		items: [
			'本应用可根据法律法规变化、产品功能调整或运营需要更新本协议。协议更新后，可能通过页面提示、首次进入弹窗或其他合理方式通知你。',
			'如你在协议更新后继续使用本应用，视为你同意更新后的协议；如你不同意，应停止使用相关服务。',
			'本协议的订立、履行、解释及争议解决适用中华人民共和国法律。你可通过小程序内“联系客服”或“意见反馈”入口与本应用运营方联系。'
		]
	}
]

export default defineComponent({
	name: 'AgreementPage',
	data() {
		return {
			sections: AGREEMENT_SECTIONS
		}
	}
})
</script>

<style lang="scss" scoped>
@import '@/uni.scss';

.agreement-page {
	position: relative;
	min-height: 100vh;
	background: linear-gradient(180deg, #fafbfc 0%, #f3f5f7 100%);
}

.agreement-page__scroll {
	height: 100vh;
}

.agreement-page__hero {
	padding: 38rpx 32rpx 26rpx;
}

.agreement-page__eyebrow {
	display: block;
	font-size: 22rpx;
	line-height: 1.4;
	font-weight: 700;
	letter-spacing: 0.12em;
	color: $uni-secondary-color;
}

.agreement-page__title {
	display: block;
	margin-top: 12rpx;
	font-family: $heading-font-family;
	font-size: 48rpx;
	line-height: 1.18;
	font-weight: 700;
	color: $uni-main-color;
}

.agreement-page__summary {
	display: block;
	margin-top: 18rpx;
	font-size: 27rpx;
	line-height: 1.72;
	color: $uni-base-color;
}

.agreement-page__meta {
	display: flex;
	flex-direction: column;
	gap: 8rpx;
	margin-top: 22rpx;
	font-size: 22rpx;
	line-height: 1.4;
	color: $uni-secondary-color;
}

.agreement-page__card {
	margin: 0 24rpx 36rpx;
	padding: 30rpx 28rpx;
	border-radius: 24rpx;
	background: #ffffff;
	box-shadow: 0 16rpx 38rpx rgba(176, 185, 198, 0.12);
}

.agreement-page__section + .agreement-page__section {
	margin-top: 34rpx;
	padding-top: 30rpx;
	border-top: 1rpx solid rgba(226, 232, 240, 0.86);
}

.agreement-page__section-title {
	display: block;
	font-size: 30rpx;
	line-height: 1.35;
	font-weight: 700;
	color: $uni-main-color;
}

.agreement-page__paragraph {
	display: block;
	margin-top: 18rpx;
	font-size: 26rpx;
	line-height: 1.82;
	color: $uni-base-color;
}

</style>
