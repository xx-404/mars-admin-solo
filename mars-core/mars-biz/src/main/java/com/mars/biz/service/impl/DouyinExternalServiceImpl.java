package com.mars.biz.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.doudian.open.api.brand_list.data.BrandListData;
import com.doudian.open.api.brand_list.param.BrandListParam;
import com.doudian.open.api.product_GetRecommendCategory.data.ProductGetRecommendCategoryData;
import com.doudian.open.api.product_GetRecommendCategory.param.ProductGetRecommendCategoryParam;
import com.doudian.open.api.product_addV2.data.ProductAddV2Data;
import com.doudian.open.api.product_addV2.param.ProductAddV2Param;
import com.doudian.open.api.product_detail.data.ProductDetailData;
import com.doudian.open.api.product_detail.param.ProductDetailParam;
import com.doudian.open.api.product_listV2.data.ProductListV2Data;
import com.doudian.open.api.product_listV2.param.ProductListV2Param;
import com.doudian.open.api.product_setOffline.data.ProductSetOfflineData;
import com.doudian.open.api.product_setOffline.param.ProductSetOfflineParam;
import com.doudian.open.api.product_setOnline.data.ProductSetOnlineData;
import com.doudian.open.api.product_setOnline.param.ProductSetOnlineParam;
import com.doudian.open.api.freightTemplate_create.data.FreightTemplateCreateData;
import com.doudian.open.api.freightTemplate_create.param.FreightTemplateCreateParam;
import com.doudian.open.api.freightTemplate_detail.data.FreightTemplateDetailData;
import com.doudian.open.api.freightTemplate_detail.param.FreightTemplateDetailParam;
import com.doudian.open.api.freightTemplate_list.data.FreightTemplateListData;
import com.doudian.open.api.freightTemplate_list.param.FreightTemplateListParam;
import com.doudian.open.api.freightTemplate_update.data.FreightTemplateUpdateData;
import com.doudian.open.api.freightTemplate_update.param.FreightTemplateUpdateParam;
import com.doudian.open.api.material_batchUploadImageSync.data.MaterialBatchUploadImageSyncData;
import com.doudian.open.api.material_batchUploadImageSync.param.MaterialBatchUploadImageSyncParam;
import com.doudian.open.api.material_searchFolder.data.MaterialSearchFolderData;
import com.doudian.open.api.material_searchFolder.param.MaterialSearchFolderParam;
import com.doudian.open.api.material_searchMaterial.data.MaterialSearchMaterialData;
import com.doudian.open.api.material_searchMaterial.param.MaterialSearchMaterialParam;
import com.doudian.open.api.shop_getShopCategory.data.ShopGetShopCategoryData;
import com.doudian.open.api.shop_getShopCategory.param.ShopGetShopCategoryParam;
import com.doudian.open.api.product_getCatePropertyV2.data.ProductGetCatePropertyV2Data;
import com.doudian.open.api.product_getCatePropertyV2.param.ProductGetCatePropertyV2Param;
import com.doudian.open.utils.JsonUtil;
import com.mars.biz.config.ShopExternalProperties;
import com.mars.biz.dto.rsp.AuthUrlRsp;
import com.mars.biz.dto.rsp.ExternalRsp;
import com.mars.biz.dto.rsp.ShopAuthInfoRsp;
import com.mars.biz.service.DouyinExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 抖音外部接口服务实现
 * 使用 hutool 工具包调用抖音电商平台外部接口
 *
 * @author Mars
 * @date 2026-04-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DouyinExternalServiceImpl implements DouyinExternalService {

    private final ShopExternalProperties properties;

    // ==================== 授权链接 ====================

    @Override
    public ExternalRsp<AuthUrlRsp> getAuthUrl(String shopId) {
        String randomToken = generateRandomToken();
        ShopExternalProperties.DouyinConfig dyConfig = properties.getDouyin();

        // state 格式：appKey_randomToken
        String state = dyConfig.getAppKey() + "_" + randomToken;
        String authUrl = dyConfig.getBaseUrl() + "/api/auth?state=" + encodeUrl(state);
        String orderUrl = dyConfig.getOrderUrl();
        String orderName = "好快服务";

        AuthUrlRsp response = AuthUrlRsp.builder()
                .authUrl(authUrl)
                .state(state)
                .platform("DOUYIN")
                .platformName("抖音")
                .orderUrl(orderUrl)
                .orderName(orderName)
                .expiresIn(3600L)
                .build();

        return ExternalRsp.ok(response);
    }

    @Override
    public ExternalRsp<ShopAuthInfoRsp> handleAuthCallback(String state) {
        ShopExternalProperties.DouyinConfig config = properties.getDouyin();

        // 从 state 中提取 token（state 格式：appKey_token）
        String token = extractTokenFromState(state, "_");
        if (token == null) {
            return ExternalRsp.fail("无效的授权状态");
        }

        String sendTime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = generateDouyinSign(sendTime, config.getSecret());

        Map<String, Object> params = new HashMap<>();
        params.put("token", token);
        params.put("appKey", config.getAppKey());
        params.put("sendTime", sendTime);
        params.put("sign", sign);

        String url = config.getBaseUrl() + "/api/v1/shop/getShopInfo";
        log.info("[抖音] 调用获取店铺信息接口：{}", url);

        HttpResponse response = HttpRequest.post(url)
                .body(JSONUtil.toJsonStr(params))
                .contentType("application/json")
                .timeout(30000)
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        log.info("[抖音] 返回结果：{}", result);

        int code = result.getInt("code");
        if (code != 10000 && code != 0) {
            return ExternalRsp.fail(code, result.getStr("message"));
        }

        JSONObject data = result.getJSONObject("result");
        ShopAuthInfoRsp authInfo = ShopAuthInfoRsp.builder()
                .shopId(data.getStr("shopId"))
                .shopName(data.getStr("shopName"))
                .ownerId(data.getStr("ownerId"))
                .authToken(state)
                .build();

        return ExternalRsp.ok(authInfo);
    }


    // ==================== 抖音关联码绑定 ====================

    @Override
    public ExternalRsp<ShopAuthInfoRsp> bindDouyinShop(String bindCode, String shopName) {
        ShopExternalProperties.DouyinConfig config = properties.getDouyin();

        String sendTime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = generateDouyinSign(sendTime, config.getSecret());

        Map<String, Object> params = new HashMap<>();
        params.put("appKey", config.getAppKey());
        params.put("code", bindCode);
        params.put("mall_name", shopName);
        params.put("sendTime", sendTime);
        params.put("sign", sign);

        String url = config.getBaseUrl() + "/api/v1/shop/relation";
        log.info("[抖音] 调用关联店铺接口：{}", url);

        HttpResponse response = HttpRequest.post(url)
                .body(JSONUtil.toJsonStr(params))
                .contentType("application/json")
                .timeout(30000)
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        log.info("[抖音] 关联店铺返回结果：{}", result);

        int code = result.getInt("code");
        if (code != 10000 && code != 0) {
            return ExternalRsp.fail(code, result.getStr("message"));
        }

        JSONObject data = result.getJSONObject("result");
        ShopAuthInfoRsp authInfo = ShopAuthInfoRsp.builder()
                .shopName(shopName)
                .ownerId(data.getStr("ownerId"))
                .authToken(bindCode)
                .build();

        return ExternalRsp.ok(authInfo);
    }

    @Override
    public ExternalRsp<ProductGetRecommendCategoryData> getRecommendCategory(String ownerId, ProductGetRecommendCategoryParam req) {
        return callDouyinRouter(ownerId, "/product/GetRecommendCategory", req, ProductGetRecommendCategoryData.class);
    }

    @Override
    public ExternalRsp<ProductAddV2Data> addProduct(String ownerId, ProductAddV2Param req) {
        return callDouyinRouter(ownerId, "/product/addV2", req, ProductAddV2Data.class);
    }

    @Override
    public ExternalRsp<ProductListV2Data> listProduct(String ownerId, ProductListV2Param req) {
        return callDouyinRouter(ownerId, "/product/listV2", req, ProductListV2Data.class);
    }

    @Override
    public ExternalRsp<ProductSetOnlineData> setProductOnline(String ownerId, ProductSetOnlineParam req) {
        return callDouyinRouter(ownerId, "/product/setOnline", req, ProductSetOnlineData.class);
    }

    @Override
    public ExternalRsp<ProductSetOfflineData> setProductOffline(String ownerId, ProductSetOfflineParam req) {
        return callDouyinRouter(ownerId, "/product/setOffline", req, ProductSetOfflineData.class);
    }

    @Override
    public ExternalRsp<ProductDetailData> getProductDetail(String ownerId, ProductDetailParam req) {
        return callDouyinRouter(ownerId, "/product/detail", req, ProductDetailData.class);
    }

    @Override
    public ExternalRsp<FreightTemplateListData> listFreightTemplate(String ownerId, FreightTemplateListParam req) {
        return callDouyinRouter(ownerId, "/freightTemplate/list", req, FreightTemplateListData.class);
    }

    @Override
    public ExternalRsp<FreightTemplateCreateData> createFreightTemplate(String ownerId, FreightTemplateCreateParam req) {
        return callDouyinRouter(ownerId, "/freightTemplate/create", req, FreightTemplateCreateData.class);
    }

    @Override
    public ExternalRsp<FreightTemplateUpdateData> updateFreightTemplate(String ownerId, FreightTemplateUpdateParam req) {
        return callDouyinRouter(ownerId, "/freightTemplate/update", req, FreightTemplateUpdateData.class);
    }

    @Override
    public ExternalRsp<FreightTemplateDetailData> getFreightTemplateDetail(String ownerId, FreightTemplateDetailParam req) {
        return callDouyinRouter(ownerId, "/freightTemplate/detail", req, FreightTemplateDetailData.class);
    }

    @Override
    public ExternalRsp<MaterialBatchUploadImageSyncData> batchUploadImage(String ownerId, MaterialBatchUploadImageSyncParam req) {
        return callDouyinRouter(ownerId, "/material/batchUploadImageSync", req, MaterialBatchUploadImageSyncData.class);
    }

    @Override
    public ExternalRsp<MaterialSearchFolderData> searchMaterialFolder(String ownerId, MaterialSearchFolderParam req) {
        return callDouyinRouter(ownerId, "/material/searchFolder", req, MaterialSearchFolderData.class);
    }

    @Override
    public ExternalRsp<MaterialSearchMaterialData> searchMaterial(String ownerId, MaterialSearchMaterialParam req) {
        return callDouyinRouter(ownerId, "/material/searchMaterial", req, MaterialSearchMaterialData.class);
    }

    @Override
    public ExternalRsp<BrandListData> listBrand(String ownerId, BrandListParam req) {
        return callDouyinRouter(ownerId, "/brand/list", req, BrandListData.class);
    }

    @Override
    public ExternalRsp<ShopGetShopCategoryData> listShopCategory(String ownerId, ShopGetShopCategoryParam req) {
        return callDouyinRouter(ownerId, "/shop/getShopCategory", req, ShopGetShopCategoryData.class);
    }

    @Override
    public ExternalRsp<ProductGetCatePropertyV2Data> getCateProperty(String ownerId, ProductGetCatePropertyV2Param req) {
        return callDouyinRouter(ownerId, "/product/getCatePropertyV2", req, ProductGetCatePropertyV2Data.class);
    }

    @Override
    public String getOrderUrl() {
        return properties.getDouyin().getOrderUrl();
    }

    @Override
    public String getPlatformName() {
        return "抖音";
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
     * 调用抖音中转接口
     */
    private <T, P> ExternalRsp<T> callDouyinRouter(String ownerId, String method, P params, Class<T> dataClass) {
        ShopExternalProperties.DouyinConfig config = properties.getDouyin();

        String sendTime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = generateDouyinSign(sendTime, config.getSecret());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("method", method);
        requestBody.put("appKey", config.getAppKey());
        requestBody.put("ownerId", ownerId);
        requestBody.put("params", JsonUtil.toJson(params));
        requestBody.put("sendTime", sendTime);
        requestBody.put("sign", sign);

        String url = config.getBaseUrl() + "/api/v1/router";
        log.info("[抖音] 调用中转接口：{} - {}", method, url);
        HttpResponse response = HttpRequest.post(url)
                .body(JsonUtil.toJson(requestBody))
                .contentType("application/json")
                .timeout(30000)
                .execute();

        String resultJson = response.body();
        log.info("[抖音] 中转接口返回：{}", resultJson);

        JSONObject result = JSONUtil.parseObj(resultJson);

        int code = result.getInt("code", -1);
        if (code != 10000 && code != 0) {
            return ExternalRsp.fail(code, result.getStr("message", "接口调用失败"));
        }

        JSONObject jsonObject = result.getJSONObject("data").getJSONObject("data");
        T data = JsonUtil.fromJson(JSONUtil.toJsonStr(jsonObject), dataClass);
        return ExternalRsp.ok(data);
    }

    /**
     * 抖音签名：MD5(sendTime + secret).toUpperCase()
     */
    private String generateDouyinSign(String sendTime, String secret) {
        String str = sendTime + secret;
        return DigestUtil.md5Hex(str).toUpperCase();
    }

}
