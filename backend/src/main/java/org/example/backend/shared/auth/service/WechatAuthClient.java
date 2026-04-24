package org.example.backend.shared.auth.service;

import org.example.backend.common.api.ApiResponseCode;
import org.example.backend.common.exception.BusinessException;
import org.example.backend.shared.auth.config.WechatMiniappProperties;
import org.example.backend.shared.auth.dto.WechatCode2SessionResponse;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.util.Map;

/**
 * AI 索引: 微信登录客户端，生产环境使用可配置连接超时和读取超时。
 */
@Component
public class WechatAuthClient {

    private final WechatMiniappProperties properties;
    private final RestClient restClient;

    public WechatAuthClient(WechatMiniappProperties properties) {
        this.properties = properties;
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getReadTimeout());
        requestFactory.enableCompression(true);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.weixin.qq.com")
                .requestFactory(requestFactory)
                .build();
    }

    public WechatCode2SessionResponse code2Session(String code) {
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getAppSecret())) {
            throw new BusinessException(ApiResponseCode.WECHAT_LOGIN_FAILED, "微信小程序登录配置缺失");
        }
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(ApiResponseCode.BAD_REQUEST, "登录 code 不能为空");
        }

        String responseBody = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/sns/jscode2session")
                        .queryParam("appid", properties.getAppId())
                        .queryParam("secret", properties.getAppSecret())
                        .queryParam("js_code", code)
                        .queryParam("grant_type", "authorization_code")
                        .build())
                .accept(MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN)
                .retrieve()
                .body(String.class);

        WechatCode2SessionResponse response = parseCode2SessionResponse(responseBody);

        if (response == null || !response.success() || !StringUtils.hasText(response.getOpenid())) {
            String message = response == null ? "微信登录失败" : response.getErrMsg();
            throw new BusinessException(ApiResponseCode.WECHAT_LOGIN_FAILED, "微信登录失败: " + message);
        }
        return response;
    }

    private WechatCode2SessionResponse parseCode2SessionResponse(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            throw new BusinessException(ApiResponseCode.WECHAT_LOGIN_FAILED, "微信登录失败: 空响应");
        }
        try {
            Map<String, Object> payload = JsonParserFactory.getJsonParser().parseMap(responseBody);
            WechatCode2SessionResponse response = new WechatCode2SessionResponse();
            response.setOpenid(asString(payload.get("openid")));
            response.setSessionKey(asString(payload.get("session_key")));
            response.setUnionid(asString(payload.get("unionid")));
            response.setErrCode(asInteger(payload.get("errcode")));
            response.setErrMsg(asString(payload.get("errmsg")));
            return response;
        } catch (Exception ex) {
            throw new BusinessException(ApiResponseCode.WECHAT_LOGIN_FAILED, "微信登录响应解析失败");
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
