<template>
	<view class="profile-edit-page">
		<view class="profile-edit-page__inner">
			<view class="profile-edit-card">
				<view class="profile-edit-card__avatar-row">
					<view class="profile-edit-card__avatar-shell">
						<button
							v-if="supportsWechatProfile"
							class="profile-edit-card__avatar-button"
							open-type="chooseAvatar"
							@chooseavatar="handleWechatChooseAvatar"
						>
							<view class="profile-edit-card__avatar">
								<image
									v-if="form.avatarUrl"
									class="profile-edit-card__avatar-image"
									:src="form.avatarUrl"
									mode="aspectFill"
								/>
								<view v-else class="profile-edit-card__avatar-fallback">
									<uni-icons type="person-filled" size="34" color="#94a3b8" />
								</view>
							</view>
						</button>
						<view v-else class="profile-edit-card__avatar" @tap="chooseAvatar">
							<image
								v-if="form.avatarUrl"
								class="profile-edit-card__avatar-image"
								:src="form.avatarUrl"
								mode="aspectFill"
							/>
							<view v-else class="profile-edit-card__avatar-fallback">
								<uni-icons type="person-filled" size="34" color="#94a3b8" />
							</view>
						</view>
					</view>
					<view class="profile-edit-card__avatar-meta">
						<text class="profile-edit-card__avatar-title">头像</text>
						<text class="profile-edit-card__avatar-tip">{{ avatarTip }}</text>
					</view>
				</view>

				<view class="profile-edit-form">
					<view class="profile-edit-form__item">
						<text class="profile-edit-form__label">昵称</text>
						<input
							v-model="form.nickname"
							class="profile-edit-form__input"
							maxlength="20"
							:type="supportsWechatProfile ? 'nickname' : 'text'"
							placeholder="请输入昵称"
							placeholder-class="profile-edit-form__placeholder"
							@blur="handleNicknameBlur"
						/>
						<text v-if="supportsWechatProfile" class="profile-edit-form__tip">
							点击输入框可直接使用微信昵称
						</text>
					</view>

					<view class="profile-edit-form__item">
						<text class="profile-edit-form__label">性别</text>
						<picker
							class="profile-edit-form__picker"
							:value="genderIndex"
							:range="genderOptions"
							range-key="label"
							@change="handleGenderChange"
						>
							<view class="profile-edit-form__picker-value">
								<text>{{ genderOptions[genderIndex].label }}</text>
								<uni-icons type="right" size="14" color="#94a3b8" />
							</view>
						</picker>
					</view>

					<view class="profile-edit-form__item">
						<text class="profile-edit-form__label">简介</text>
						<textarea
							v-model="form.bio"
							class="profile-edit-form__textarea"
							maxlength="255"
							placeholder="一句话介绍自己"
							placeholder-class="profile-edit-form__placeholder"
						/>
					</view>
				</view>
			</view>

			<button class="profile-edit-page__submit" :disabled="submitting" @tap="submitProfile">
				{{ submitting ? '保存中...' : '保存资料' }}
			</button>
		</view>
	</view>
</template>

<script>
import { bootstrapAuth, getSessionSnapshot, setCurrentUser } from '@/store/session'
import { updateProfile, uploadAvatar } from '@/api/user'
import { resolveWechatAvatarPath, resolveWechatNickname, supportsWechatNativeProfile } from '@/utils/wechat-profile'

export default {
	name: 'ProfileEditPage',
	data() {
		return {
			submitting: false,
			uploadingAvatar: false,
			supportsWechatProfile: supportsWechatNativeProfile(),
			form: {
				nickname: '',
				avatarUrl: '',
				avatarStorageKey: '',
				gender: 0,
				bio: ''
			},
			genderOptions: [
				{ label: '保密', value: 0 },
				{ label: '男', value: 1 },
				{ label: '女', value: 2 }
			]
		}
	},
	computed: {
		genderIndex() {
			const index = this.genderOptions.findIndex((item) => item.value === this.form.gender)
			return index === -1 ? 0 : index
		},
		avatarTip() {
			return this.supportsWechatProfile
				? '点击头像可直接调用微信头像选择器'
				: '点击更换头像，支持 jpg/png，最大 2MB'
		}
	},
	onLoad() {
		this.initForm()
	},
	onShow() {
		bootstrapAuth({ silent: true }).then(() => {
			this.initForm()
		}).catch((error) => {
			console.warn('[profile] bootstrap failed', error)
		})
	},
	methods: {
		initForm() {
			const currentUser = getSessionSnapshot().currentUser
			this.form = {
				nickname: currentUser && currentUser.nickname ? currentUser.nickname : '',
				avatarUrl: currentUser && currentUser.avatarUrl ? currentUser.avatarUrl : '',
				avatarStorageKey: '',
				gender: currentUser && typeof currentUser.gender === 'number' ? currentUser.gender : 0,
				bio: currentUser && currentUser.bio ? currentUser.bio : ''
			}
		},
		handleGenderChange(event) {
			const index = Number(event.detail.value || 0)
			this.form.gender = this.genderOptions[index].value
		},
		handleNicknameBlur(event) {
			const nickname = resolveWechatNickname(event)
			if (nickname) {
				this.form.nickname = nickname
			}
		},
		async handleWechatChooseAvatar(event) {
			const filePath = resolveWechatAvatarPath(event)
			if (!filePath) {
				uni.showToast({
					title: '未获取到微信头像',
					icon: 'none'
				})
				return
			}
			await this.uploadAvatarFile(filePath)
		},
		chooseAvatar() {
			if (this.uploadingAvatar) return

			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: async (result) => {
					const filePath = result.tempFilePaths && result.tempFilePaths[0]
					if (!filePath) return
					await this.uploadAvatarFile(filePath)
				}
			})
		},
		async uploadAvatarFile(filePath) {
			if (this.uploadingAvatar) return

			this.uploadingAvatar = true
			uni.showLoading({ title: '上传中...' })
			try {
				const uploadResult = await uploadAvatar(filePath)
				this.form.avatarUrl = uploadResult.url
				this.form.avatarStorageKey = uploadResult.storageKey
				uni.showToast({
					title: '头像上传成功',
					icon: 'none'
				})
			} catch (error) {
				uni.showToast({
					title: error.message || '头像上传失败',
					icon: 'none'
				})
			} finally {
				this.uploadingAvatar = false
				uni.hideLoading()
			}
		},
		async submitProfile() {
			if (this.submitting) return
			if (!this.form.nickname.trim()) {
				uni.showToast({
					title: '请输入昵称',
					icon: 'none'
				})
				return
			}

			this.submitting = true
			try {
				const user = await updateProfile({
					nickname: this.form.nickname,
					avatarUrl: this.form.avatarStorageKey || this.form.avatarUrl,
					gender: this.form.gender,
					bio: this.form.bio
				})
				setCurrentUser(user)
				uni.showToast({
					title: '资料已更新',
					icon: 'none'
				})
				setTimeout(() => {
					uni.navigateBack()
				}, 300)
			} catch (error) {
				uni.showToast({
					title: error.message || '保存失败',
					icon: 'none'
				})
			} finally {
				this.submitting = false
			}
		}
	}
}
</script>

<style lang="scss">
.profile-edit-page {
	min-height: 100vh;
	padding: 32rpx;
	background: linear-gradient(180deg, #fafbfc 0%, #f3f5f7 100%);
}

.profile-edit-page__inner {
	padding-top: 24rpx;
}

.profile-edit-card {
	padding: 32rpx;
	border-radius: 24rpx;
	background: #ffffff;
	box-shadow: 0 18rpx 42rpx rgba(176, 185, 198, 0.16);
}

.profile-edit-card__avatar-row {
	display: flex;
	align-items: center;
}

.profile-edit-card__avatar-shell {
	flex-shrink: 0;
}

.profile-edit-card__avatar-button {
	padding: 0;
	background: transparent;
	line-height: 1;
	border-radius: 0;
}

.profile-edit-card__avatar-button::after {
	border: 0;
}

.profile-edit-card__avatar {
	width: 120rpx;
	height: 120rpx;
	border-radius: 24rpx;
	overflow: hidden;
	background: #edf2f7;
	flex-shrink: 0;
}

.profile-edit-card__avatar-image,
.profile-edit-card__avatar-fallback {
	width: 100%;
	height: 100%;
}

.profile-edit-card__avatar-fallback {
	display: flex;
	align-items: center;
	justify-content: center;
}

.profile-edit-card__avatar-meta {
	margin-left: 24rpx;
}

.profile-edit-card__avatar-title {
	display: block;
	font-size: 30rpx;
	font-weight: 700;
	color: #1f2937;
}

.profile-edit-card__avatar-tip {
	display: block;
	margin-top: 10rpx;
	font-size: 24rpx;
	line-height: 1.6;
	color: #7b8494;
}

.profile-edit-form {
	margin-top: 40rpx;
}

.profile-edit-form__item + .profile-edit-form__item {
	margin-top: 28rpx;
}

.profile-edit-form__label {
	display: block;
	margin-bottom: 14rpx;
	font-size: 25rpx;
	font-weight: 600;
	color: #334155;
}

.profile-edit-form__input,
.profile-edit-form__picker-value,
.profile-edit-form__textarea {
	width: 100%;
	padding: 24rpx 26rpx;
	border-radius: 18rpx;
	background: #f6f8fb;
	font-size: 26rpx;
	color: #1f2937;
}

.profile-edit-form__input {
	height: 88rpx;
	line-height: 40rpx;
	padding-top: 0;
	padding-bottom: 0;
}

.profile-edit-form__picker-value {
	min-height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.profile-edit-form__textarea {
	min-height: 180rpx;
}

.profile-edit-form__placeholder {
	color: #9ca3af;
}

.profile-edit-form__tip {
	display: block;
	margin-top: 10rpx;
	font-size: 22rpx;
	line-height: 1.5;
	color: #7b8494;
}

.profile-edit-page__submit {
	margin-top: 32rpx;
	border-radius: 999rpx;
	background: #20272e;
	color: #ffffff;
	font-size: 28rpx;
	font-weight: 600;
}

.profile-edit-page__submit[disabled] {
	opacity: 0.72;
}
</style>
