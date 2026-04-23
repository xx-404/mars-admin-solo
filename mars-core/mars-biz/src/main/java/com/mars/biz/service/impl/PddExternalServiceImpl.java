package com.mars.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.mars.biz.config.ShopExternalProperties;
import com.mars.biz.dto.req.ProductStatusReq;
import com.mars.biz.dto.rsp.*;
import com.mars.biz.service.PddExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 拼多多外部接口服务实现
 * 使用 hutool 工具包调用拼多多电商平台外部接口
 *
 * @author Mars
 * @date 2026-04-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PddExternalServiceImpl implements PddExternalService {

    private final ShopExternalProperties properties;

    // ==================== 授权链接 ====================

    @Override
    public ExternalRsp<AuthUrlRsp> getAuthUrl(String shopId) {
        String randomToken = generateRandomToken();
        ShopExternalProperties.PinduoduoConfig pddConfig = properties.getPinduoduo();

        // state 格式：clientId_secret！token（注意中文感叹号）
        String state = pddConfig.getClientId() + "_" + pddConfig.getSecret() + "!" + randomToken;
        String initAuthUrl = pddConfig.getBaseUrl() + "/api/open/go-auth?type=JSON&state=" + encodeUrl(state);

        // http 调用 initAuthUrl 接口获取实际授权 URL
        HttpResponse authResponse = HttpRequest.get(initAuthUrl).timeout(30000).execute();
        JSONObject authResult = JSONUtil.parseObj(authResponse.body());
        String authUrl;
        if (authResult.getInt("code") == 0 && authResult.getJSONObject("data") != null) {
            authUrl = authResult.getJSONObject("data").getStr("url");
        } else {
            return ExternalRsp.fail("获取授权 URL 失败：" + authResult.getStr("msg"));
        }

        String orderUrl = pddConfig.getOrderUrl();
        String orderName = "云兔服务";

        AuthUrlRsp response = AuthUrlRsp.builder()
                .authUrl(authUrl)
                .state(state)
                .platform("PINDUODUO")
                .platformName("拼多多")
                .orderUrl(orderUrl)
                .orderName(orderName)
                .expiresIn(3600L)
                .build();

        return ExternalRsp.ok(response);
    }

    // ==================== 授权回调 ====================

    @Override
    public ExternalRsp<ShopAuthInfoRsp> handleAuthCallback(String state) {
        try {
            return handlePinduoduoCallback(state);
        } catch (Exception e) {
            log.error("[拼多多] 授权回调处理失败：{}", e.getMessage(), e);
            return ExternalRsp.fail("授权处理失败：" + e.getMessage());
        }
    }

    /**
     * 拼多多授权回调
     */
    private ExternalRsp<ShopAuthInfoRsp> handlePinduoduoCallback(String state) {
        ShopExternalProperties.PinduoduoConfig config = properties.getPinduoduo();

        // 从 state 中提取 token（state 格式：clientId_secret！token）
        String token = extractTokenFromState(state, "!");
        if (token == null) {
            return ExternalRsp.fail("无效的授权状态");
        }

        String sysTime = String.valueOf(System.currentTimeMillis());
        String sign = generatePddSign(config.getClientId(), config.getSecret(), sysTime);

        Map<String, Object> params = new HashMap<>();
        params.put("clientId", config.getClientId());
        params.put("sysTime", sysTime);
        params.put("sign", sign);
        params.put("Token", token);

        String url = config.getBaseUrl() + "/api/open/info";
        log.info("[拼多多] 调用查询店铺信息接口：{}", url);

        HttpResponse response = HttpRequest.post(url)
                .body(JSONUtil.toJsonStr(params))
                .contentType("application/json")
                .timeout(30000)
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        log.info("[拼多多] 返回结果：{}", result);

        if (result.getInt("code") != 0) {
            return ExternalRsp.fail(result.getInt("code"), result.getStr("msg"));
        }

        JSONObject data = result.getJSONObject("data");
        ShopAuthInfoRsp authInfo = ShopAuthInfoRsp.builder()
                .shopId(data.getStr("shopId"))
                .shopName(data.getStr("shopName"))
                .ownerId(data.getStr("ownerId"))
                .authToken(token)
                .expireTime(data.getStr("expireTime"))
                .tokenExpire(data.getStr("tokenExpire"))
                .build();

        return ExternalRsp.ok(authInfo);
    }

    // ==================== 商品同步 ====================

    @Override
    public ExternalRsp<ProductListRsp> syncProducts(String ownerId, Integer page, Integer pageSize) {
        try {
            return syncPinduoduoProducts(ownerId, page, pageSize);
        } catch (Exception e) {
            log.error("[拼多多] 商品同步失败：{}", e.getMessage(), e);
            return ExternalRsp.fail("商品同步失败：" + e.getMessage());
        }
    }

    /**
     * 拼多多商品同步
     */
    private ExternalRsp<ProductListRsp> syncPinduoduoProducts(String ownerId, Integer page, Integer pageSize) {
        ShopExternalProperties.PinduoduoConfig config = properties.getPinduoduo();

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("page_size", pageSize);

        ExternalRsp<JSONObject> response = callPddRouter(config, ownerId, "pdd.goods.list.get", JSONUtil.toJsonStr(params));
        if (!response.isSuccess()) {
            return ExternalRsp.fail(response.getCode(), response.getError());
        }

        JSONObject data = response.getData();
        JSONArray goodsList = data.getJSONArray("goods_list");
        int total = data.getInt("total", goodsList.size());

        List<ProductInfoRsp> products = new ArrayList<>();
        for (int i = 0; i < goodsList.size(); i++) {
            JSONObject goods = goodsList.getJSONObject(i);
            ProductInfoRsp product = ProductInfoRsp.builder()
                    .productId(String.valueOf(goods.getLong("goods_id")))
                    .name(goods.getStr("goods_name"))
                    .imageUrl(goods.getStr("thumb_url"))
                    .price(parsePddPrice(goods.get("min_group_price")))
                    .stock(goods.getInt("quantity", 0))
                    .status(mapPddStatus(goods.getInt("is_onsale"), goods.getInt("goods_status")))
                    .category(goods.getStr("cat_id"))
                    .build();
            products.add(product);
        }

        ProductListRsp result = ProductListRsp.builder()
                .products(products)
                .total(total)
                .page(page)
                .pageSize(pageSize)
                .synced(products.size())
                .build();

        return ExternalRsp.ok(result);
    }

    // ==================== 商品上下架 ====================

    @Override
    public ExternalRsp<ProductStatusRsp> listProducts(String ownerId, List<String> productIds) {
        return updateProductStatus(ProductStatusReq.builder()
                .ownerId(ownerId)
                .productIds(productIds)
                .action("list")
                .build());
    }

    @Override
    public ExternalRsp<ProductStatusRsp> unlistProducts(String ownerId, List<String> productIds) {
        return updateProductStatus(ProductStatusReq.builder()
                .ownerId(ownerId)
                .productIds(productIds)
                .action("unlist")
                .build());
    }

    @Override
    public ExternalRsp<ProductStatusRsp> updateProductStatus(ProductStatusReq req) {
        String ownerId = req.getOwnerId();
        List<String> productIds = req.getProductIds();
        String action = req.getAction();

        try {
            int failedCount = 0;
            int successCount = 0;

            ExternalRsp<JSONObject> pddResult = action.equals("list")
                    ? callPddProductList(ownerId, productIds)
                    : callPddProductUnlist(ownerId, productIds);
            if (pddResult.isSuccess()) {
                successCount = productIds.size();
            } else {
                failedCount = productIds.size();
            }

            ProductStatusRsp response = ProductStatusRsp.builder()
                    .action(action)
                    .affected(successCount)
                    .failedCount(failedCount)
                    .totalCount(productIds.size())
                    .message(action.equals("list") ? "上架成功" : "下架成功")
                    .build();

            return ExternalRsp.ok(response);
        } catch (Exception e) {
            log.error("商品{}失败：{}", action.equals("list") ? "上架" : "下架", e.getMessage(), e);
            return ExternalRsp.fail("操作失败：" + e.getMessage());
        }
    }

    // ==================== 商品详情 ====================

    @Override
    public ExternalRsp<ProductInfoRsp> getProductDetail(String ownerId, String productId) {
        try {
            return getPinduoduoProductDetail(ownerId, productId);
        } catch (Exception e) {
            log.error("[拼多多] 获取商品详情失败：{}", e.getMessage(), e);
            return ExternalRsp.fail("获取商品详情失败：" + e.getMessage());
        }
    }

    private ExternalRsp<ProductInfoRsp> getPinduoduoProductDetail(String ownerId, String productId) {
        ShopExternalProperties.PinduoduoConfig config = properties.getPinduoduo();

        Map<String, Object> params = new HashMap<>();
        params.put("goods_id_list", List.of(Long.parseLong(productId)));

        ExternalRsp<JSONObject> response = callPddRouter(config, ownerId, "pdd.goods.information.get", JSONUtil.toJsonStr(params));
        if (!response.isSuccess()) {
            return ExternalRsp.fail(response.getCode(), response.getError());
        }

        JSONArray goodsList = response.getData().getJSONArray("goods_info_list");
        if (goodsList.isEmpty()) {
            return ExternalRsp.fail("商品不存在");
        }

        JSONObject goods = goodsList.getJSONObject(0);
        ProductInfoRsp product = ProductInfoRsp.builder()
                .productId(productId)
                .name(goods.getStr("goods_name"))
                .imageUrl(goods.getStr("thumb_url"))
                .price(parsePddPrice(goods.get("min_group_price")))
                .stock(goods.getInt("quantity", 0))
                .status(mapPddStatus(goods.getInt("is_onsale"), goods.getInt("goods_status")))
                .build();

        return ExternalRsp.ok(product);
    }

    // ==================== 辅助方法 ====================

    @Override
    public String getOrderUrl() {
        return properties.getPinduoduo().getOrderUrl();
    }

    @Override
    public String getPlatformName() {
        return "拼多多";
    }

    /**
     * 生成随机 Token
     */
    private String generateRandomToken() {
        return DigestUtil.md5Hex(IdUtil.fastSimpleUUID()).substring(0, 32);
    }

    /**
     * URL 编码
     */
    private String encodeUrl(String str) {
        return cn.hutool.core.util.URLUtil.encodeAll(str);
    }

    /**
     * 从 state 中提取 token
     */
    private String extractTokenFromState(String state, String delimiter) {
        if (state == null || !state.contains(delimiter)) {
            return null;
        }
        String[] parts = state.split(delimiter);
        return parts.length > 1 ? parts[parts.length - 1] : null;
    }

    /**
     * 拼多多签名：MD5(clientId + secret + sysTime + clientId).toUpperCase()
     */
    private String generatePddSign(String clientId, String secret, String sysTime) {
        String str = clientId + secret + sysTime + clientId;
        return DigestUtil.md5Hex(str).toUpperCase();
    }

    /**
     * 解析拼多多价格（单位是毫）
     */
    private BigDecimal parsePddPrice(Object priceObj) {
        if (priceObj == null) {
            return BigDecimal.ZERO;
        }
        long price = Long.parseLong(String.valueOf(priceObj));
        return BigDecimal.valueOf(price).divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP);
    }

    /**
     * 映射拼多多商品状态
     */
    private String mapPddStatus(Integer isOnsale, Integer goodsStatus) {
        if (isOnsale != null && isOnsale == 1) {
            return "on_sale";
        }
        if (goodsStatus == null) {
            return "unknown";
        }
        switch (goodsStatus) {
            case 1: return "off_sale";
            case 2: return "rejected";
            case 3: return "auditing";
            default: return "unknown";
        }
    }

    // ==================== 平台接口调用 ====================

    /**
     * 调用拼多多中转接口
     */
    private ExternalRsp<JSONObject> callPddRouter(ShopExternalProperties.PinduoduoConfig config, String ownerId, String apiName, String paramsJson) {
        String sysTime = String.valueOf(System.currentTimeMillis());
        String sign = generatePddSign(config.getClientId(), config.getSecret(), sysTime);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("clientId", config.getClientId());
        requestBody.put("sysTime", sysTime);
        requestBody.put("sign", sign);
        requestBody.put("ownerId", ownerId);
        requestBody.put("params", paramsJson);
        requestBody.put("apiName", apiName);

        String url = config.getBaseUrl() + "/api/open/router";
        log.info("[拼多多] 调用中转接口：{} - {}", apiName, url);

        HttpResponse response = HttpRequest.post(url)
                .body(JSONUtil.toJsonStr(requestBody))
                .contentType("application/json")
                .timeout(30000)
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        log.info("[拼多多] 中转接口返回：{}", result);

        int code = result.getInt("code", -1);
        if (code != 0) {
            return ExternalRsp.fail(code, result.getStr("msg", "接口调用失败"));
        }

        return ExternalRsp.ok(result.getJSONObject("data"));
    }

    /**
     * 拼多多商品上架
     */
    private ExternalRsp<JSONObject> callPddProductList(String ownerId, List<String> productIds) {
        ShopExternalProperties.PinduoduoConfig config = properties.getPinduoduo();
        List<Long> ids = productIds.stream().map(Long::parseLong).toList();
        Map<String, Object> params = new HashMap<>();
        params.put("goods_id_list", ids);
        return callPddRouter(config, ownerId, "pdd.goods.sale.set", JSONUtil.toJsonStr(params));
    }

    /**
     * 拼多多商品下架
     */
    private ExternalRsp<JSONObject> callPddProductUnlist(String ownerId, List<String> productIds) {
        ShopExternalProperties.PinduoduoConfig config = properties.getPinduoduo();
        List<Long> ids = productIds.stream().map(Long::parseLong).toList();
        Map<String, Object> params = new HashMap<>();
        params.put("goods_id_list", ids);
        params.put("is_onsale", 0);
        return callPddRouter(config, ownerId, "pdd.goods.update", JSONUtil.toJsonStr(params));
    }
}
