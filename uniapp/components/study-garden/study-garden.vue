<template>
  <view class="cute-garden">
    <!-- 天空与光晕合并精简 -->
    <view class="cute-garden__sky"></view>
    <view class="cute-garden__glow"></view>

    <!-- 靠后的小草层 -->
    <view class="cute-garden__grass-layer">
      <view
          v-for="grass in grasses"
          :key="grass.key"
          class="grass-blade"
          :class="grass.type"
          :style="{ left: grass.left, height: grass.height, animationDelay: grass.delay }"
      ></view>
    </view>

    <!-- Q版胖胖花朵 -->
    <view
        v-for="flower in flowers"
        :key="flower.key"
        class="cute-flower"
        :style="{ left: flower.left, bottom: flower.bottom, animationDelay: flower.delay, '--f-color': flower.color }"
    >
      <view class="cute-flower__stem">
        <!-- 可爱的小胖叶子 -->
        <view class="cute-flower__leaf"></view>
      </view>
      <view class="cute-flower__head">
        <!-- 利用 CSS 变量和伪元素生成花瓣，花心自带高光 -->
        <view class="cute-flower__core"></view>
      </view>
    </view>

    <!-- 软萌圆润的地面 -->
    <view class="cute-garden__ground"></view>
  </view>
</template>

<script>
export default {
  name: 'StudyGardenCute',
  data() {
    return {
      // 直接在数组配置颜色，去掉冗余的CSS class，方便拓展
      flowers: [
        { key: 'f1', left: '8%', bottom: '28rpx', delay: '0s', color: '#ff8eb4' },   // 粉
        { key: 'f2', left: '26%', bottom: '18rpx', delay: '0.8s', color: '#ffd55e' }, // 黄
        { key: 'f3', left: '48%', bottom: '32rpx', delay: '0.4s', color: '#b690ff' }, // 紫
        { key: 'f4', left: '70%', bottom: '20rpx', delay: '1.1s', color: '#ffa485' }, // 珊瑚
        { key: 'f5', left: '88%', bottom: '30rpx', delay: '0.2s', color: '#6fd2ff' }  // 蓝
      ],
      // 混合前后草丛，利用伪元素裂变，节点减半
      grasses: [
        { key: 'g1', left: '5%', height: '32rpx', delay: '0.1s', type: 'back' },
        { key: 'g2', left: '18%', height: '42rpx', delay: '0.7s', type: 'front' },
        { key: 'g3', left: '35%', height: '36rpx', delay: '1.2s', type: 'back' },
        { key: 'g4', left: '42%', height: '48rpx', delay: '0.4s', type: 'front' },
        { key: 'g5', left: '60%', height: '30rpx', delay: '0.9s', type: 'back' },
        { key: 'g6', left: '78%', height: '44rpx', delay: '0.3s', type: 'front' },
        { key: 'g7', left: '85%', height: '34rpx', delay: '1.4s', type: 'back' },
        { key: 'g8', left: '94%', height: '40rpx', delay: '0.6s', type: 'front' }
      ]
    }
  }
}
</script>

<style lang="scss" scoped>
.cute-garden {
  position: relative;
  z-index: 2;
  height: 130rpx; /* 高度减半 */
  margin-top: 30rpx;
  overflow: hidden;
}

/* 缩小且更柔和的天空和光晕 */
.cute-garden__sky {
  position: absolute;
  left: 50%;
  top: 10rpx;
  width: 400rpx;
  height: 60rpx;
  border-radius: 999rpx;
  background: radial-gradient(circle, rgba(255, 240, 176, 0.4) 0%, transparent 70%);
  transform: translateX(-50%);
}

.cute-garden__glow {
  position: absolute;
  bottom: 20rpx;
  left: 10%;
  right: 10%;
  height: 40rpx;
  border-radius: 50%;
  filter: blur(12rpx);
  background: linear-gradient(90deg, rgba(255, 166, 197, 0.2), rgba(135, 218, 255, 0.2));
}

/* ================= 🌸 Q版胖胖花朵 ================= */
.cute-flower {
  position: absolute;
  width: 44rpx;
  height: 70rpx; /* 变矮变胖 */
  transform-origin: center bottom;
  animation: jelly-bounce 3s ease-in-out infinite;
  will-change: transform;
}

.cute-flower__stem {
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 8rpx;
  height: 50rpx;
  border-radius: 8rpx;
  background: linear-gradient(180deg, #81df63 0%, #3ebd63 100%);
  transform: translateX(-50%);
}

.cute-flower__leaf {
  position: absolute;
  bottom: 12rpx;
  left: 6rpx;
  width: 16rpx;
  height: 10rpx;
  border-radius: 16rpx 16rpx 0 16rpx;
  background: #60ce5d;
  transform: rotate(-20deg);
}

.cute-flower__head {
  position: absolute;
  left: 50%;
  top: -6rpx;
  width: 42rpx;
  height: 42rpx;
  transform: translateX(-50%);
}

/* 用 ::before 和 ::after 画出四叶草型花瓣，省去大量节点 */
.cute-flower__head::before,
.cute-flower__head::after {
  content: '';
  position: absolute;
  background: var(--f-color);
  border-radius: 14rpx;
  box-shadow: 0 4rpx 8rpx rgba(0, 0, 0, 0.08);
}

/* 横向花瓣 */
.cute-flower__head::before {
  top: 15%; bottom: 15%; left: 0; right: 0;
}
/* 纵向花瓣 */
.cute-flower__head::after {
  left: 15%; right: 15%; top: 0; bottom: 0;
}

.cute-flower__core {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
  background: #ffe066;
  transform: translate(-50%, -50%);
  z-index: 2;
  box-shadow: inset -2rpx -2rpx 4rpx rgba(230, 190, 40, 0.6); /* 增加立体感 */
  animation: core-breathe 2s ease-in-out infinite;
}

/* 花心可爱的高光点 (纯粹增加可爱度) */
.cute-flower__core::after {
  content: '';
  position: absolute;
  top: 2rpx;
  left: 2rpx;
  width: 4rpx;
  height: 4rpx;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
}

/* ================= 🌿 软萌胶囊草丛 ================= */
.cute-garden__grass-layer {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 16rpx;
  height: 60rpx;
}

.grass-blade {
  position: absolute;
  bottom: 0;
  border-radius: 20rpx 20rpx 0 0; /* 圆润顶部 */
  transform-origin: center bottom;
  animation: jelly-bounce 2.6s ease-in-out infinite;
  will-change: transform;
}

/* 后景草 */
.grass-blade.back {
  width: 12rpx;
  background: #84de63;
  z-index: 1;
}

/* 前景胖草 */
.grass-blade.front {
  width: 16rpx;
  background: #64cb54;
  z-index: 3;
}

/* ✨ 神奇优化：用伪元素在旁边生成一根矮草，无需新增节点就能让草丛更茂密错落 */
.grass-blade::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 110%;
  width: 70%;
  height: 60%;
  border-radius: 20rpx 20rpx 0 0;
  background: inherit;
  opacity: 0.8;
}

/* ================= 🌍 丘陵地面 ================= */
.cute-garden__ground {
  position: absolute;
  left: -5%;
  right: -5%;
  bottom: 0;
  height: 32rpx;
  border-radius: 50% 50% 0 0;
  background: linear-gradient(180deg, #6fd35a 0%, #46c152 100%);
  border-top: 4rpx solid #94ec7c; /* 增加一道高光边，让地面像卡通山丘 */
}

/* ================= 🎬 动画：果冻弹跳微动效 ================= */
@keyframes jelly-bounce {
  0%, 100% {
    transform: rotate(-3deg) scaleY(0.96);
  }
  50% {
    transform: rotate(4deg) scaleY(1.04); /* 伸缩感，显胖显可爱 */
  }
}

@keyframes core-breathe {
  0%, 100% { transform: translate(-50%, -50%) scale(1); }
  50% { transform: translate(-50%, -50%) scale(1.1); }
}
</style>