package com.mars.biz.service.impl;

import com.doudian.open.api.product_GetRecommendCategory.data.ProductGetRecommendCategoryData;
import com.doudian.open.api.product_GetRecommendCategory.param.PicItem;
import com.doudian.open.api.product_GetRecommendCategory.param.ProductFormatNewItem;
import com.doudian.open.api.product_GetRecommendCategory.param.ProductGetRecommendCategoryParam;
import com.mars.biz.dto.req.RecommendCategoryReq;
import com.mars.biz.dto.rsp.*;
import com.mars.biz.enums.PlatformType;
import com.mars.biz.service.DouyinExternalService;
import com.mars.biz.service.PddExternalService;
import com.mars.biz.service.ShopExternalService;
import com.mars.biz.service.XhsExternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 店铺外部接口服务（委托给各平台具体实现）
 *
 * @author Mars
 * @date 2026-04-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShopExternalServiceImpl implements ShopExternalService {

    private final DouyinExternalService doudianService;
    private final PddExternalService pddService;
    private final XhsExternalService xhsService;

    @Override
    public ExternalRsp<AuthUrlRsp> getAuthUrl(PlatformType platform) {
        return getAuthUrl(platform, cn.hutool.core.util.IdUtil.fastSimpleUUID().substring(0, 16));
    }

    @Override
    public ExternalRsp<AuthUrlRsp> getAuthUrl(PlatformType platform, String shopId) {
        switch (platform) {
            case DOUYIN:
                return doudianService.getAuthUrl(shopId);
            case PINDUODUO:
                return pddService.getAuthUrl(shopId);
            case XIAOHONGSHU:
                return xhsService.getAuthUrl(shopId);
            default:
                return ExternalRsp.fail("不支持的平台类型");
        }
    }

    @Override
    public ExternalRsp<ShopAuthInfoRsp> handleAuthCallback(PlatformType platform, String state) {
        switch (platform) {
            case DOUYIN:
                return doudianService.handleAuthCallback(state);
            case PINDUODUO:
                return pddService.handleAuthCallback(state);
            case XIAOHONGSHU:
                return xhsService.handleAuthCallback(state);
            default:
                return ExternalRsp.fail("不支持的平台类型");
        }
    }

    @Override
    public ExternalRsp<ShopAuthInfoRsp> bindDouyinShop(String bindCode, String shopName) {
        return doudianService.bindDouyinShop(bindCode, shopName);
    }

    @Override
    public String getOrderUrl(PlatformType platform) {
        switch (platform) {
            case DOUYIN:
                return doudianService.getOrderUrl();
            case PINDUODUO:
                return pddService.getOrderUrl();
            case XIAOHONGSHU:
                return xhsService.getOrderUrl();
            default:
                return null;
        }
    }

    /**
     * 将内部的 RecommendCategoryReq 转换为 SDK 的 ProductGetRecommendCategoryParam
     */
    private ProductGetRecommendCategoryParam convertToSdkParam(RecommendCategoryReq req) {
        ProductGetRecommendCategoryParam param = new ProductGetRecommendCategoryParam();
        param.setScene(req.getScene());

        if (req.getPic() != null && !req.getPic().isEmpty()) {
            List<PicItem> picItems = new ArrayList<>();
            for (String url : req.getPic()) {
                PicItem picItem = new PicItem();
                picItem.setUrl(url);
                picItems.add(picItem);
            }
            param.setPic(picItems);
        }

        param.setCategoryLeafId(req.getCategoryLeafId());
        param.setName(req.getName());

        if (req.getProductFormatNew() != null && !req.getProductFormatNew().isEmpty()) {
            Map<Long, List<ProductFormatNewItem>> productFormatNewMap = new HashMap<>();
            for (Map.Entry<Long, String> entry : req.getProductFormatNew().entrySet()) {
                List<ProductFormatNewItem> items = new ArrayList<>();
                ProductFormatNewItem item = new ProductFormatNewItem();
                item.setValue(entry.getKey());
                item.setName(entry.getValue());
                items.add(item);
                productFormatNewMap.put(entry.getKey(), items);
            }
            param.setProductFormatNew(productFormatNewMap);
        }

        param.setStandardBrandId(req.getStandardBrandId());
        param.setAsyncTaskId(req.getAsyncTaskId());
        return param;
    }

    /**
     * 将 SDK 的 ProductGetRecommendCategoryData 转换为内部的 RecommendCategoryRsp
     */
    private RecommendCategoryRsp convertToInternalRsp(ProductGetRecommendCategoryData data) {
        if (data == null) {
            return RecommendCategoryRsp.builder().build();
        }

        List<RecommendCategoryRsp.CategoryDetailInfo> categoryDetails = new ArrayList<>();
        List<com.doudian.open.api.product_GetRecommendCategory.data.CategoryDetailsItem> sdkDetails = data.getCategoryDetails();

        if (sdkDetails != null && !sdkDetails.isEmpty()) {
            for (com.doudian.open.api.product_GetRecommendCategory.data.CategoryDetailsItem sdkItem : sdkDetails) {
                com.doudian.open.api.product_GetRecommendCategory.data.CategoryDetail sdkCategory = sdkItem.getCategoryDetail();
                if (sdkCategory == null) {
                    continue;
                }

                RecommendCategoryRsp.CategoryInfo categoryInfo = RecommendCategoryRsp.CategoryInfo.builder()
                        .firstCid(String.valueOf(sdkCategory.getFirstCid()))
                        .firstCname(sdkCategory.getFirstCname())
                        .secondCid(String.valueOf(sdkCategory.getSecondCid()))
                        .secondCname(sdkCategory.getSecondCname())
                        .thirdCid(String.valueOf(sdkCategory.getThirdCid()))
                        .thirdCname(sdkCategory.getThirdCname())
                        .fourthCid(String.valueOf(sdkCategory.getFourthCid()))
                        .fourthCname(sdkCategory.getFourthCname())
                        .build();

                RecommendCategoryRsp.CategoryDetailInfo detailInfo = RecommendCategoryRsp.CategoryDetailInfo.builder()
                        .categoryDetail(categoryInfo)
                        .qualificationStatus(String.valueOf(sdkItem.getQualificationStatus()))
                        .build();

                categoryDetails.add(detailInfo);
            }
        }

        return RecommendCategoryRsp.builder()
                .asyncTaskId(data.getAsyncTaskId())
                .asyncTaskStatus(data.getAsyncTaskStatus() != null ? String.valueOf(data.getAsyncTaskStatus()) : null)
                .categoryDetails(categoryDetails)
                .interval(data.getInterval() != null ? String.valueOf(data.getInterval()) : null)
                .recommendId(data.getRecommendId())
                .timeout(data.getTimeout() != null ? String.valueOf(data.getTimeout()) : null)
                .build();
    }

    /**
     * 根据 ownerId 推断平台类型（简化实现）
     */
    private PlatformType detectPlatformFromOwnerId(String ownerId) {
        // 实际应用中可能需要从数据库查询店铺信息来确定平台
        // 这里作为示例，默认返回抖音
        return PlatformType.DOUYIN;
    }
}
