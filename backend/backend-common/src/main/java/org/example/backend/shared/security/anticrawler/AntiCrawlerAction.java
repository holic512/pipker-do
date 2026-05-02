package org.example.backend.shared.security.anticrawler;

/**
 * AI 索引: 反扒处置动作。
 */
public enum AntiCrawlerAction {

    OBSERVE("observe"),
    COOLDOWN("cooldown"),
    BLOCKED("blocked");

    private final String value;

    AntiCrawlerAction(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
