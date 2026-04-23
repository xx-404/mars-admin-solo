package com.mars.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 店铺外部接口配置属性
 * 用于读取 yaml 配置的外部接口域名和认证信息
 *
 * @author Mars
 * @date 2026-04-16
 */
@Data
@Component
@ConfigurationProperties(prefix = "shop.external")
public class ShopExternalProperties {

    /**
     * 拼多多平台配置（云兔）
     */
    private PinduoduoConfig pinduoduo = new PinduoduoConfig();

    /**
     * 抖音平台配置（好快）
     */
    private DouyinConfig douyin = new DouyinConfig();

    /**
     * 小红书平台配置
     */
    private XiaohongshuConfig xiaohongshu = new XiaohongshuConfig();

    /**
     * 拼多多平台配置（云兔中转）
     */
    @Data
    public static class PinduoduoConfig {
        /**
         * API 基础地址
         * 默认：http://ytbj.dongmingwangluo.com
         */
        private String baseUrl = "http://ytbj.dongmingwangluo.com";

        /**
         * 客户端ID
         */
        private String clientId;

        /**
         * 密钥
         */
        private String secret;

        /**
         * 订购地址
         * 默认：https://fuwu.pinduoduo.com/service-market/service-detail?detailId=50737
         */
        private String orderUrl = "https://fuwu.pinduoduo.com/service-market/service-detail?detailId=50737";
    }

    /**
     * 抖音平台配置（好快中转）
     */
    @Data
    public static class DouyinConfig {
        /**
         * API 基础地址
         * 默认：https://lhmgoods.dongmingwangluo.com
         */
        private String baseUrl = "https://lhmgoods.dongmingwangluo.com";

        /**
         * 应用Key
         */
        private String appKey;

        /**
         * 密钥
         */
        private String secret;

        /**
         * 订购地址
         * 默认：https://fuwu.jinritemai.com/authorize?service_id=32522
         */
        private String orderUrl = "https://fuwu.jinritemai.com/authorize?service_id=32522";
    }

    /**
     * 小红书平台配置
     */
    @Data
    public static class XiaohongshuConfig {
        /**
         * API 基础地址
         * 默认：http://xhsgoods.yanchongkeji.com
         */
        private String baseUrl = "http://xhsgoods.yanchongkeji.com";

        /**
         * 应用Key
         */
        private String appKey;

        /**
         * 密钥
         */
        private String secret;

        /**
         * 订购地址
         */
        private String orderUrl;
    }
}