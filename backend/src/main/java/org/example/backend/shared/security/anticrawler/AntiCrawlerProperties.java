package org.example.backend.shared.security.anticrawler;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 索引: 反扒配置属性。
 */
@ConfigurationProperties(prefix = "anti-crawler")
public class AntiCrawlerProperties {

    private boolean enabled = true;
    private List<String> whitelistIps = new ArrayList<>();
    private Map<String, GroupRuleProperties> groups = new LinkedHashMap<>();
    private BehaviorRuleProperties behavior = new BehaviorRuleProperties();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getWhitelistIps() {
        return whitelistIps;
    }

    public void setWhitelistIps(List<String> whitelistIps) {
        this.whitelistIps = whitelistIps;
    }

    public Map<String, GroupRuleProperties> getGroups() {
        return groups;
    }

    public void setGroups(Map<String, GroupRuleProperties> groups) {
        this.groups = groups;
    }

    public BehaviorRuleProperties getBehavior() {
        return behavior;
    }

    public void setBehavior(BehaviorRuleProperties behavior) {
        this.behavior = behavior;
    }

    public GroupRuleProperties requireGroup(AntiCrawlerRouteGroup group) {
        GroupRuleProperties properties = groups.get(group.code());
        if (properties == null) {
            throw new IllegalStateException("Missing anti-crawler config for group: " + group.code());
        }
        return properties;
    }

    public static class GroupRuleProperties {

        private long oneSecondLimit;
        private long oneMinuteLimit;
        private long tenMinuteLimit;
        private long cooldownSeconds;
        private long blockSeconds;
        private long escalationWindowSeconds = 600;
        private long escalationThreshold = 3;

        public long getOneSecondLimit() {
            return oneSecondLimit;
        }

        public void setOneSecondLimit(long oneSecondLimit) {
            this.oneSecondLimit = oneSecondLimit;
        }

        public long getOneMinuteLimit() {
            return oneMinuteLimit;
        }

        public void setOneMinuteLimit(long oneMinuteLimit) {
            this.oneMinuteLimit = oneMinuteLimit;
        }

        public long getTenMinuteLimit() {
            return tenMinuteLimit;
        }

        public void setTenMinuteLimit(long tenMinuteLimit) {
            this.tenMinuteLimit = tenMinuteLimit;
        }

        public long getCooldownSeconds() {
            return cooldownSeconds;
        }

        public void setCooldownSeconds(long cooldownSeconds) {
            this.cooldownSeconds = cooldownSeconds;
        }

        public long getBlockSeconds() {
            return blockSeconds;
        }

        public void setBlockSeconds(long blockSeconds) {
            this.blockSeconds = blockSeconds;
        }

        public long getEscalationWindowSeconds() {
            return escalationWindowSeconds;
        }

        public void setEscalationWindowSeconds(long escalationWindowSeconds) {
            this.escalationWindowSeconds = escalationWindowSeconds;
        }

        public long getEscalationThreshold() {
            return escalationThreshold;
        }

        public void setEscalationThreshold(long escalationThreshold) {
            this.escalationThreshold = escalationThreshold;
        }
    }

    public static class BehaviorRuleProperties {

        private long scanQuestionWindowSeconds = 300;
        private long scanQuestionDistinctThreshold = 40;
        private long scanBankWindowSeconds = 600;
        private long scanBankDistinctThreshold = 8;
        private long robotWindowSeconds = 600;
        private long robotSubmitThreshold = 20;
        private long robotFastUsedSeconds = 2;
        private long robotFastRatioPercent = 80;
        private long multiIpWindowSeconds = 600;
        private long multiIpDistinctThreshold = 4;
        private long multiIpCooldownSeconds = 600;
        private long multiIpRepeatWindowSeconds = 86400;
        private long multiIpBlockSeconds = 1800;
        private int nightReadStartHour = 1;
        private int nightReadEndHour = 5;
        private double nightReadMultiplier = 1.5D;
        private double suspiciousThresholdFactor = 0.5D;

        public long getScanQuestionWindowSeconds() {
            return scanQuestionWindowSeconds;
        }

        public void setScanQuestionWindowSeconds(long scanQuestionWindowSeconds) {
            this.scanQuestionWindowSeconds = scanQuestionWindowSeconds;
        }

        public long getScanQuestionDistinctThreshold() {
            return scanQuestionDistinctThreshold;
        }

        public void setScanQuestionDistinctThreshold(long scanQuestionDistinctThreshold) {
            this.scanQuestionDistinctThreshold = scanQuestionDistinctThreshold;
        }

        public long getScanBankWindowSeconds() {
            return scanBankWindowSeconds;
        }

        public void setScanBankWindowSeconds(long scanBankWindowSeconds) {
            this.scanBankWindowSeconds = scanBankWindowSeconds;
        }

        public long getScanBankDistinctThreshold() {
            return scanBankDistinctThreshold;
        }

        public void setScanBankDistinctThreshold(long scanBankDistinctThreshold) {
            this.scanBankDistinctThreshold = scanBankDistinctThreshold;
        }

        public long getRobotWindowSeconds() {
            return robotWindowSeconds;
        }

        public void setRobotWindowSeconds(long robotWindowSeconds) {
            this.robotWindowSeconds = robotWindowSeconds;
        }

        public long getRobotSubmitThreshold() {
            return robotSubmitThreshold;
        }

        public void setRobotSubmitThreshold(long robotSubmitThreshold) {
            this.robotSubmitThreshold = robotSubmitThreshold;
        }

        public long getRobotFastUsedSeconds() {
            return robotFastUsedSeconds;
        }

        public void setRobotFastUsedSeconds(long robotFastUsedSeconds) {
            this.robotFastUsedSeconds = robotFastUsedSeconds;
        }

        public long getRobotFastRatioPercent() {
            return robotFastRatioPercent;
        }

        public void setRobotFastRatioPercent(long robotFastRatioPercent) {
            this.robotFastRatioPercent = robotFastRatioPercent;
        }

        public long getMultiIpWindowSeconds() {
            return multiIpWindowSeconds;
        }

        public void setMultiIpWindowSeconds(long multiIpWindowSeconds) {
            this.multiIpWindowSeconds = multiIpWindowSeconds;
        }

        public long getMultiIpDistinctThreshold() {
            return multiIpDistinctThreshold;
        }

        public void setMultiIpDistinctThreshold(long multiIpDistinctThreshold) {
            this.multiIpDistinctThreshold = multiIpDistinctThreshold;
        }

        public long getMultiIpCooldownSeconds() {
            return multiIpCooldownSeconds;
        }

        public void setMultiIpCooldownSeconds(long multiIpCooldownSeconds) {
            this.multiIpCooldownSeconds = multiIpCooldownSeconds;
        }

        public long getMultiIpRepeatWindowSeconds() {
            return multiIpRepeatWindowSeconds;
        }

        public void setMultiIpRepeatWindowSeconds(long multiIpRepeatWindowSeconds) {
            this.multiIpRepeatWindowSeconds = multiIpRepeatWindowSeconds;
        }

        public long getMultiIpBlockSeconds() {
            return multiIpBlockSeconds;
        }

        public void setMultiIpBlockSeconds(long multiIpBlockSeconds) {
            this.multiIpBlockSeconds = multiIpBlockSeconds;
        }

        public int getNightReadStartHour() {
            return nightReadStartHour;
        }

        public void setNightReadStartHour(int nightReadStartHour) {
            this.nightReadStartHour = nightReadStartHour;
        }

        public int getNightReadEndHour() {
            return nightReadEndHour;
        }

        public void setNightReadEndHour(int nightReadEndHour) {
            this.nightReadEndHour = nightReadEndHour;
        }

        public double getNightReadMultiplier() {
            return nightReadMultiplier;
        }

        public void setNightReadMultiplier(double nightReadMultiplier) {
            this.nightReadMultiplier = nightReadMultiplier;
        }

        public double getSuspiciousThresholdFactor() {
            return suspiciousThresholdFactor;
        }

        public void setSuspiciousThresholdFactor(double suspiciousThresholdFactor) {
            this.suspiciousThresholdFactor = suspiciousThresholdFactor;
        }
    }
}
