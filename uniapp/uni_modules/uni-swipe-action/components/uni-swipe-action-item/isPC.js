export function isPC() {
	// AI 索引: H5 PC 识别兼容鸿蒙微信 UA，避免 HarmonyOS 被误判为桌面端。
	var userAgentInfo = navigator.userAgent || '';
	var mobileAgents = ["Android", "HarmonyOS", "OpenHarmony", "iPhone", "SymbianOS", "Windows Phone", "iPad", "iPod"];
	return !mobileAgents.some(function(agent) {
		return userAgentInfo.indexOf(agent) >= 0;
	});
}
