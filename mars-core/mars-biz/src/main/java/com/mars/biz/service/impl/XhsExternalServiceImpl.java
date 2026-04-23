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
import com.mars.biz.service.XhsExternalService;
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
 * 小红书外部接口服务实现
 * 使用 hutool 工具包调用小红书电商平台外部接口
 *
 * @author Mars
 * @date 2026-04-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XhsExternalServiceImpl implements XhsExternalService {

    private final ShopExternalProperties properties;

    // ==================== 授权链接 ====================

    @Override
    public ExternalRsp<AuthUrlRsp> getAuthUrl(String shopId) {
        String randomToken = generateRandomToken();
        ShopExternalProperties.XiaohongshuConfig xhsConfig = properties.getXiaohongshu();

        // state 格式：appKey_randomToken
        String state = xhsConfig.getAppKey() + "_" + randomToken;
        String authUrl = xhsConfig.getBaseUrl() + "/api/auth/link?state=" + encodeUrl(state);
        String orderUrl = xhsConfig.getOrderUrl();
        String orderName = "云兔服务";

        AuthUrlRsp response = AuthUrlRsp.builder()
                .authUrl(authUrl)
                .state(state)
                .platform("XIAOHONGSHU")
                .platformName("小红书")
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
            return handleXiaohongshuCallback(state);
        } catch (Exception e) {
            log.error("[小红书] 授权回调处理失败：{}", e.getMessage(), e);
            return ExternalRsp.fail("授权处理失败：" + e.getMessage());
        }
    }

    /**
     * 小红书授权回调
     */
    private ExternalRsp<ShopAuthInfoRsp> handleXiaohongshuCallback(String state) {
        ShopExternalProperties.XiaohongshuConfig config = properties.getXiaohongshu();

        String sendTime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = generateDouyinSign(sendTime, config.getSecret());

        Map<String, Object> params = new HashMap<>();
        params.put("appKey", config.getAppKey());
        params.put("state", state);
        params.put("sendTime", sendTime);
        params.put("sign", sign);

        String url = config.getBaseUrl() + "/api/v1/shop/getShopInfo";
        log.info("[小红书] 调用获取店铺信息接口：{}", url);

        HttpResponse response = HttpRequest.post(url)
                .body(JSONUtil.toJsonStr(params))
                .contentType("application/json")
                .timeout(30000)
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        log.info("[小红书] 返回结果：{}", result);

        int code = parseCode(result.get("code"));
        if (code != 0 && code != 200) {
            return ExternalRsp.fail(code, result.getStr("msg"));
        }

        JSONObject data = result.getJSONObject("data");
        ShopAuthInfoRsp authInfo = ShopAuthInfoRsp.builder()
                .shopId(data.getStr("shopId"))
                .shopName(data.getStr("shopName"))
                .ownerId(data.getStr("ownerId", data.getStr("shopId")))
                .authToken(state)
                .expireTime(data.getStr("expireTime"))
                .build();

        return ExternalRsp.ok(authInfo);
    }

    // ==================== 商品同步 ====================

    @Override
    public ExternalRsp<ProductListRsp> syncProducts(String ownerId, Integer page, Integer pageSize) {
        try {
            return syncXiaohongshuProducts(ownerId, page, pageSize);
        } catch (Exception e) {
            log.error("[小红书] 商品同步失败：{}", e.getMessage(), e);
            return ExternalRsp.fail("商品同步失败：" + e.getMessage());
        }
    }

    /**
     * 小红书商品同步
     */
    private ExternalRsp<ProductListRsp> syncXiaohongshuProducts(String ownerId, Integer page, Integer pageSize) {
        ShopExternalProperties.XiaohongshuConfig config = properties.getXiaohongshu();

        Map<String, Object> params = new HashMap<>();
        params.put("page", page);
        params.put("page_size", pageSize);

        ExternalRsp<JSONObject> response = callXhsRouter(config, ownerId, "/product/getList", params);
        if (!response.isSuccess()) {
            return ExternalRsp.fail(response.getCode(), response.getError());
        }

        JSONObject data = response.getData();
        JSONArray productList = data.getJSONArray("products");
        int total = data.getInt("total", productList.size());

        List<ProductInfoRsp> products = new ArrayList<>();
        for (int i = 0; i < productList.size(); i++) {
            JSONObject item = productList.getJSONObject(i);
            ProductInfoRsp product = ProductInfoRsp.builder()
                    .productId(item.getStr("productId"))
                    .name(item.getStr("name"))
                    .imageUrl(item.getStr("mainImage"))
                    .price(new BigDecimal(item.getStr("price", "0")))
                    .stock(item.getInt("stock", 0))
                    .status(item.getStr("status"))
                    .category(item.getStr("category"))
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

            ExternalRsp<JSONObject> xhsResult = action.equals("list")
                    ? callXhsProductList(ownerId, productIds)
                    : callXhsProductUnlist(ownerId, productIds);
            if (xhsResult.isSuccess()) {
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
            return getXiaohongshuProductDetail(ownerId, productId);
        } catch (Exception e) {
            log.error("[小红书] 获取商品详情失败：{}", e.getMessage(), e);
            return ExternalRsp.fail("获取商品详情失败：" + e.getMessage());
        }
    }

    private ExternalRsp<ProductInfoRsp> getXiaohongshuProductDetail(String ownerId, String productId) {
        ShopExternalProperties.XiaohongshuConfig config = properties.getXiaohongshu();

        Map<String, Object> params = new HashMap<>();
        params.put("product_id", productId);

        ExternalRsp<JSONObject> response = callXhsRouter(config, ownerId, "/product/detail", params);
        if (!response.isSuccess()) {
            return ExternalRsp.fail(response.getCode(), response.getError());
        }

        JSONObject data = response.getData();
        ProductInfoRsp product = ProductInfoRsp.builder()
                .productId(productId)
                .name(data.getStr("name"))
                .imageUrl(data.getStr("mainImage"))
                .price(new BigDecimal(data.getStr("price", "0")))
                .stock(data.getInt("stock", 0))
                .status(data.getStr("status"))
                .build();

        return ExternalRsp.ok(product);
    }

    // ==================== 辅助方法 ====================

    @Override
    public String getOrderUrl() {
        return properties.getXiaohongshu().getOrderUrl();
    }

    @Override
    public String getPlatformName() {
        return "小红书";
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
     * 抖音/小红书签名：MD5(sendTime + secret).toUpperCase()
     */
    private String generateDouyinSign(String sendTime, String secret) {
        String str = sendTime + secret;
        return DigestUtil.md5Hex(str).toUpperCase();
    }

    /**
     * 解析响应码（兼容字符串类型）
     */
    private int parseCode(Object codeObj) {
        if (codeObj instanceof Integer) {
            return (Integer) codeObj;
        }
        if (codeObj instanceof String) {
            return Integer.parseInt((String) codeObj);
        }
        return -1;
    }

    // ==================== 平台接口调用 ====================

    /**
     * 调用小红书中转接口
     */
    private ExternalRsp<JSONObject> callXhsRouter(ShopExternalProperties.XiaohongshuConfig config, String ownerId, String method, Map<String, Object> params) {
        String sendTime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = generateDouyinSign(sendTime, config.getSecret());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("method", method);
        requestBody.put("appKey", config.getAppKey());
        requestBody.put("ownerId", ownerId);
        requestBody.put("params", JSONUtil.toJsonStr(params));
        requestBody.put("sendTime", sendTime);
        requestBody.put("sign", sign);

        String url = config.getBaseUrl() + "/api/v1/router";
        log.info("[小红书] 调用中转接口：{} - {}", method, url);

        HttpResponse response = HttpRequest.post(url)
                .body(JSONUtil.toJsonStr(requestBody))
                .contentType("application/json")
                .timeout(30000)
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        log.info("[小红书] 中转接口返回：{}", result);

        int code = parseCode(result.get("code"));
        if (code != 0 && code != 200) {
            return ExternalRsp.fail(code, result.getStr("msg", "接口调用失败"));
        }

        return ExternalRsp.ok(result.getJSONObject("data"));
    }

    /**
     * 小红书商品上架
     */
    private ExternalRsp<JSONObject> callXhsProductList(String ownerId, List<String> productIds) {
        ShopExternalProperties.XiaohongshuConfig config = properties.getXiaohongshu();
        Map<String, Object> params = new HashMap<>();
        params.put("product_ids", productIds);
        return callXhsRouter(config, ownerId, "/product/list", params);
    }

    /**
     * 小红书商品下架
     */
    private ExternalRsp<JSONObject> callXhsProductUnlist(String ownerId, List<String> productIds) {
        ShopExternalProperties.XiaohongshuConfig config = properties.getXiaohongshu();
        Map<String, Object> params = new HashMap<>();
        params.put("product_ids", productIds);
        return callXhsRouter(config, ownerId, "/product/unlist", params);
    }
}
