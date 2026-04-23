package com.mars.biz.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.doudian.open.api.material_batchUploadImageSync.data.MaterialBatchUploadImageSyncData;
import com.doudian.open.api.material_batchUploadImageSync.data.FailedMapItem;
import com.doudian.open.api.material_batchUploadImageSync.data.SuccessMapItem;
import com.doudian.open.api.material_batchUploadImageSync.param.MaterialBatchUploadImageSyncParam;
import com.doudian.open.api.material_batchUploadImageSync.param.MaterialsItem;
import com.doudian.open.api.material_searchFolder.data.MaterialSearchFolderData;
import com.doudian.open.api.material_searchFolder.data.FolderInfoListItem;
import com.doudian.open.api.material_searchFolder.param.MaterialSearchFolderParam;
import com.doudian.open.api.material_searchMaterial.data.MaterialSearchMaterialData;
import com.doudian.open.api.material_searchMaterial.data.MaterialInfoListItem;
import com.doudian.open.api.material_searchMaterial.param.MaterialSearchMaterialParam;
import com.doudian.open.api.brand_list.data.BrandListData;
import com.doudian.open.api.brand_list.data.BrandListItem;
import com.doudian.open.api.brand_list.data.BrandInfosItem;
import com.doudian.open.api.brand_list.data.AuthBrandListItem;
import com.doudian.open.api.brand_list.param.BrandListParam;
import com.doudian.open.api.shop_getShopCategory.data.ShopGetShopCategoryData;
import com.doudian.open.api.shop_getShopCategory.data.DataItem;
import com.doudian.open.api.shop_getShopCategory.param.ShopGetShopCategoryParam;
import com.doudian.open.api.freightTemplate_list.data.FreightTemplateListData;
import com.doudian.open.api.freightTemplate_list.data.ListItem;
import com.doudian.open.api.freightTemplate_list.param.FreightTemplateListParam;
import com.doudian.open.api.product_getCatePropertyV2.data.ProductGetCatePropertyV2Data;
import com.doudian.open.api.product_getCatePropertyV2.param.ProductGetCatePropertyV2Param;
import com.doudian.open.api.product_listV2.data.ProductListV2Data;
import com.doudian.open.api.product_listV2.param.ProductListV2Param;
import com.doudian.open.api.product_listV2.param.QueryOptions;
import com.mars.biz.dto.req.DouyinBatchUploadImageReq;
import com.mars.biz.dto.req.DouyinBrandListReq;
import com.mars.biz.dto.req.DouyinCatePropertyReq;
import com.mars.biz.dto.req.DouyinFreightTemplateListReq;
import com.mars.biz.dto.req.DouyinMaterialItem;
import com.mars.biz.dto.req.DouyinMaterialSearchFolderReq;
import com.mars.biz.dto.req.DouyinMaterialSearchMaterialReq;
import com.mars.biz.dto.req.DouyinProductListV2Req;
import com.mars.biz.dto.req.DouyinProductQueryReq;
import com.mars.biz.dto.req.DouyinProductSaveReq;
import com.mars.biz.dto.req.DouyinShopCategoryReq;
import com.mars.biz.dto.rsp.DouyinBatchUploadImageRsp;
import com.mars.biz.dto.rsp.DouyinBrandListRsp;
import com.mars.biz.dto.rsp.DouyinCatePropertyRsp;
import com.mars.biz.dto.rsp.DouyinFreightTemplateListRsp;
import com.mars.biz.dto.rsp.DouyinMaterialSearchFolderRsp;
import com.mars.biz.dto.rsp.DouyinMaterialSearchMaterialRsp;
import com.mars.biz.dto.rsp.DouyinProductListV2Rsp;
import com.mars.biz.dto.rsp.DouyinMaterialSearchFolderRsp;
import com.mars.biz.dto.rsp.DouyinProductInfoRsp;
import com.mars.biz.dto.rsp.DouyinProductListRsp;
import com.mars.biz.dto.rsp.DouyinShopCategoryRsp;
import com.mars.biz.dto.rsp.DouyinUploadFailedItem;
import com.mars.biz.dto.rsp.DouyinUploadSuccessItem;
import com.mars.biz.dto.rsp.ExternalRsp;
import com.mars.biz.entity.DouyinProduct;
import com.mars.biz.mapper.DouyinProductMapper;
import com.mars.biz.service.DouyinExternalService;
import com.mars.biz.service.DouyinProductService;
import com.mars.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抖音商品 Service 实现
 *
 * @author Mars
 * @date 2026-04-18
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DouyinProductServiceImpl implements DouyinProductService {

    private final DouyinProductMapper douyinProductMapper;
    private final DouyinExternalService douyinExternalService;

    @Override
    public Page<DouyinProduct> page(Integer page, Integer pageSize, DouyinProductQueryReq req) {
        Page<DouyinProduct> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<DouyinProduct> wrapper = buildQueryWrapper(req);
        wrapper.orderByDesc(DouyinProduct::getCreateTime);
        return douyinProductMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public DouyinProductListRsp list(DouyinProductQueryReq req) {
        Page<DouyinProduct> pageResult = page(req.getPage(), req.getPageSize(), req);

        List<DouyinProductInfoRsp> list = pageResult.getRecords().stream()
                .map(this::convertToInfoRsp)
                .collect(Collectors.toList());

        return DouyinProductListRsp.builder()
                .list(list)
                .total(pageResult.getTotal())
                .page(req.getPage())
                .pageSize(req.getPageSize())
                .build();
    }

    @Override
    public DouyinProduct getById(Long id) {
        return douyinProductMapper.selectById(id);
    }

    @Override
    public DouyinProductInfoRsp getInfoById(Long id) {
        DouyinProduct product = douyinProductMapper.selectById(id);
        if (product == null) {
            return null;
        }
        return convertToInfoRsp(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DouyinProduct create(DouyinProductSaveReq req) {
        DouyinProduct product = convertToEntity(req);
        product.setId(null);
        product.setStatus(req.getStatus() != null ? req.getStatus() : DouyinProduct.STATUS_DRAFT);
        product.setAuditStatus(DouyinProduct.AUDIT_STATUS_PENDING);

        douyinProductMapper.insert(product);
        log.info("创建抖音商品成功，id={}", product.getId());
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DouyinProduct update(DouyinProductSaveReq req) {
        if (req.getId() == null) {
            throw new IllegalArgumentException("商品 ID 不能为空");
        }

        DouyinProduct existing = douyinProductMapper.selectById(req.getId());
        if (existing == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        DouyinProduct product = convertToEntity(req);
        product.setId(req.getId());
        // 保留原有状态
        product.setStatus(existing.getStatus());
        product.setAuditStatus(existing.getAuditStatus());
        product.setAuditRemark(existing.getAuditRemark());

        douyinProductMapper.updateById(product);
        log.info("更新抖音商品成功，id={}", product.getId());
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DouyinProduct save(DouyinProductSaveReq req) {
        if (req.getId() == null) {
            return create(req);
        } else {
            return update(req);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return;
        }
        Arrays.stream(ids)
                .forEach(id -> douyinProductMapper.deleteById(id));
        log.info("批量删除抖音商品成功，ids={}", Arrays.toString(ids));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        douyinProductMapper.deleteById(id);
        log.info("删除抖音商品成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onSale(Long id) {
        DouyinProduct product = douyinProductMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        product.setStatus(DouyinProduct.STATUS_ON_SALE);
        product.setPublishTime(LocalDateTime.now());
        douyinProductMapper.updateById(product);
        log.info("商品上架成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offSale(Long id) {
        DouyinProduct product = douyinProductMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        product.setStatus(DouyinProduct.STATUS_OFF_SALE);
        douyinProductMapper.updateById(product);
        log.info("商品下架成功，id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(Long id) {
        DouyinProduct product = douyinProductMapper.selectById(id);
        if (product == null) {
            throw new IllegalArgumentException("商品不存在");
        }

        product.setStatus(DouyinProduct.STATUS_AUDITING);
        product.setAuditStatus(DouyinProduct.AUDIT_STATUS_PENDING);
        product.setCommit(true);
        douyinProductMapper.updateById(product);
        log.info("商品提交审核成功，id={}", id);
    }

    @Override
    public DouyinProduct getByOwnerIdAndProductId(String ownerId, String productId) {
        return douyinProductMapper.selectByOwnerIdAndProductId(ownerId, productId);
    }

    @Override
    public DouyinBatchUploadImageRsp batchUploadImage(DouyinBatchUploadImageReq req) {
        // 构建抖音 SDK 请求参数
        MaterialBatchUploadImageSyncParam param = new MaterialBatchUploadImageSyncParam();
        param.setNeedDistinct(req.getNeedDistinct());

        // 转换材料列表
        List<MaterialsItem> materialsItems = BeanUtil.copyToList(req.getMaterials(), MaterialsItem.class);
        param.setMaterials(materialsItems);

        // 调用抖音接口
        ExternalRsp<MaterialBatchUploadImageSyncData> externalRsp = douyinExternalService.batchUploadImage(req.getOwnerId(), param);

        if (!externalRsp.isSuccess()) {
            log.error("批量上传图片失败，ownerId={}, error={}", req.getOwnerId(), externalRsp.getError());
            throw new RuntimeException("批量上传图片失败：" + externalRsp.getError());
        }

        // 转换响应数据
        MaterialBatchUploadImageSyncData data = externalRsp.getData();
        return convertToBatchUploadImageRsp(data);
    }

    /**
     * 转换批量上传图片响应
     */
    private DouyinBatchUploadImageRsp convertToBatchUploadImageRsp(MaterialBatchUploadImageSyncData data) {
        Map<String, DouyinUploadSuccessItem> successMap = new HashMap<>();
        Map<String, DouyinUploadFailedItem> failedMap = new HashMap<>();

        if (data.getSuccessMap() != null) {
            for (Map.Entry<String, SuccessMapItem> entry : data.getSuccessMap().entrySet()) {
                successMap.put(entry.getKey(), convertToSuccessItem(entry.getValue()));
            }
        }

        if (data.getFailedMap() != null) {
            for (Map.Entry<String, FailedMapItem> entry : data.getFailedMap().entrySet()) {
                failedMap.put(entry.getKey(), convertToFailedItem(entry.getValue()));
            }
        }

        return DouyinBatchUploadImageRsp.builder()
                .successMap(successMap)
                .failedMap(failedMap)
                .build();
    }

    /**
     * 转换成功上传项
     */
    private DouyinUploadSuccessItem convertToSuccessItem(SuccessMapItem item) {
        return DouyinUploadSuccessItem.builder()
                .materialId(item.getMaterialId())
                .name(item.getName())
                .folderId(item.getFolderId())
                .originUrl(item.getOriginUrl())
                .byteUrl(item.getByteUrl())
                .auditStatus(item.getAuditStatus())
                .isNew(item.getIsNew())
                .build();
    }

    /**
     * 转换失败上传项
     */
    private DouyinUploadFailedItem convertToFailedItem(FailedMapItem item) {
        return DouyinUploadFailedItem.builder()
                .errCode(item.getErrCode())
                .errMsg(item.getErrMsg())
                .build();
    }

    @Override
    public DouyinBrandListRsp listBrand(DouyinBrandListReq req) {
        BrandListParam param = new BrandListParam();
        param.setCategories(req.getCategories());
        param.setCategoryId(req.getCategoryId());
        param.setBrandIds(req.getBrandIds());
        param.setQuery(req.getQuery());
        param.setOffset(req.getOffset());
        param.setSize(req.getSize());
        param.setSort(req.getSort());
        param.setStatus(req.getStatus());
        param.setFullBrandInfo(req.getFullBrandInfo());

        ExternalRsp<BrandListData> externalRsp = douyinExternalService.listBrand(req.getOwnerId(), param);
        if (!externalRsp.isSuccess()) {
            log.error("查询品牌列表失败，ownerId={}, error={}", req.getOwnerId(), externalRsp.getError());
            throw new BusinessException("查询品牌列表失败：" + externalRsp.getError());
        }

        return convertToBrandListRsp(externalRsp.getData());
    }

    @Override
    public DouyinShopCategoryRsp listShopCategory(DouyinShopCategoryReq req) {
        ShopGetShopCategoryParam param = new ShopGetShopCategoryParam();
        param.setCid(req.getCid());
        param.setChannel(req.getChannel());

        ExternalRsp<ShopGetShopCategoryData> externalRsp = douyinExternalService.listShopCategory(req.getOwnerId(), param);
        if (!externalRsp.isSuccess()) {
            log.error("查询店铺类目失败，ownerId={}, error={}", req.getOwnerId(), externalRsp.getError());
            throw new BusinessException("查询店铺类目失败：" + externalRsp.getError());
        }

        return convertToShopCategoryRsp(externalRsp.getData());
    }

    @Override
    public DouyinFreightTemplateListRsp listFreightTemplate(DouyinFreightTemplateListReq req) {
        FreightTemplateListParam param = new FreightTemplateListParam();
        param.setName(req.getName());
        param.setPage(req.getPage());
        param.setSize(req.getSize());

        ExternalRsp<FreightTemplateListData> externalRsp = douyinExternalService.listFreightTemplate(req.getOwnerId(), param);
        if (!externalRsp.isSuccess()) {
            log.error("查询运费模板列表失败，ownerId={}, error={}", req.getOwnerId(), externalRsp.getError());
            throw new BusinessException("查询运费模板列表失败：" + externalRsp.getError());
        }

        return convertToFreightTemplateListRsp(externalRsp.getData());
    }

    @Override
    public DouyinCatePropertyRsp getCateProperty(DouyinCatePropertyReq req) {
        ProductGetCatePropertyV2Param param = new ProductGetCatePropertyV2Param();
        param.setCategoryLeafId(req.getCategoryLeafId());

        ExternalRsp<ProductGetCatePropertyV2Data> externalRsp = douyinExternalService.getCateProperty(req.getOwnerId(), param);
        if (!externalRsp.isSuccess()) {
            log.error("查询商品类目属性失败，ownerId={}, error={}", req.getOwnerId(), externalRsp.getError());
            throw new BusinessException("查询商品类目属性失败：" + externalRsp.getError());
        }

        return convertToCatePropertyRsp(externalRsp.getData());
    }

    @Override
    public DouyinMaterialSearchFolderRsp searchMaterialFolder(DouyinMaterialSearchFolderReq req) {
        MaterialSearchFolderParam param = new MaterialSearchFolderParam();
        param.setFolderId(req.getFolderId());
        param.setParentFolderId(req.getParentFolderId());
        param.setName(req.getName());
        param.setCreateTimeStart(req.getCreateTimeStart());
        param.setCreateTimeEnd(req.getCreateTimeEnd());
        param.setPageNum(req.getPageNum());
        param.setPageSize(req.getPageSize());
        param.setOrderBy(req.getOrderBy());
        param.setOperateStatus(req.getOperateStatus());

        ExternalRsp<MaterialSearchFolderData> externalRsp = douyinExternalService.searchMaterialFolder(req.getOwnerId(), param);
        if (!externalRsp.isSuccess()) {
            log.error("查询素材文件夹失败，ownerId={}, error={}", req.getOwnerId(), externalRsp.getError());
            throw new BusinessException("查询素材文件夹失败：" + externalRsp.getError());
        }

        return convertToMaterialSearchFolderRsp(externalRsp.getData());
    }

    @Override
    public DouyinMaterialSearchMaterialRsp searchMaterial(DouyinMaterialSearchMaterialReq req) {
        MaterialSearchMaterialParam param = new MaterialSearchMaterialParam();
        param.setMaterialId(req.getMaterialId());
        param.setMaterialName(req.getMaterialName());
        param.setMaterialType(req.getMaterialType());
        param.setOperateStatus(req.getOperateStatus());
        param.setAuditStatus(req.getAuditStatus());
        param.setCreateTimeStart(req.getCreateTimeStart());
        param.setCreateTimeEnd(req.getCreateTimeEnd());
        param.setFolderId(req.getFolderId());
        param.setMaterialIdList(req.getMaterialIdList());
        param.setPageNum(req.getPageNum());
        param.setPageSize(req.getPageSize());
        param.setOrderType(req.getOrderType());

        ExternalRsp<MaterialSearchMaterialData> externalRsp = douyinExternalService.searchMaterial(req.getOwnerId(), param);
        if (!externalRsp.isSuccess()) {
            log.error("查询素材失败，ownerId={}, error={}", req.getOwnerId(), externalRsp.getError());
            throw new BusinessException("查询素材失败：" + externalRsp.getError());
        }

        return convertToMaterialSearchMaterialRsp(externalRsp.getData());
    }

    /**
     * 转换品牌列表响应
     */
    private DouyinBrandListRsp convertToBrandListRsp(BrandListData data) {
        List<DouyinBrandListRsp.BrandSimpleItem> brandList = null;
        if (data.getBrandList() != null) {
            brandList = data.getBrandList().stream()
                    .map(this::convertToBrandSimpleItem)
                    .collect(Collectors.toList());
        }

        List<DouyinBrandListRsp.BrandSimpleItem> authBrandList = null;
        if (data.getAuthBrandList() != null) {
            authBrandList = data.getAuthBrandList().stream()
                    .map(this::convertToAuthBrandSimpleItem)
                    .collect(Collectors.toList());
        }

        Map<Long, DouyinBrandListRsp.BrandInfoItem> brandInfos = null;
        if (data.getBrandInfos() != null) {
            brandInfos = new HashMap<>();
            for (Map.Entry<Long, BrandInfosItem> entry : data.getBrandInfos().entrySet()) {
                brandInfos.put(entry.getKey(), convertToBrandInfoItem(entry.getValue()));
            }
        }

        return DouyinBrandListRsp.builder()
                .brandIds(data.getBrandIds())
                .brandInfos(brandInfos)
                .total(data.getTotal())
                .hasMore(data.getHasMore())
                .authRequired(data.getAuthRequired())
                .authBrandList(authBrandList)
                .brandList(brandList)
                .build();
    }

    /**
     * 转换品牌简略信息
     */
    private DouyinBrandListRsp.BrandSimpleItem convertToBrandSimpleItem(BrandListItem item) {
        return DouyinBrandListRsp.BrandSimpleItem.builder()
                .brandId(item.getBrandId())
                .nameCn(item.getNameCn())
                .nameEn(item.getNameEn())
                .build();
    }

    /**
     * 转换已授权品牌简略信息
     */
    private DouyinBrandListRsp.BrandSimpleItem convertToAuthBrandSimpleItem(AuthBrandListItem item) {
        return DouyinBrandListRsp.BrandSimpleItem.builder()
                .brandId(item.getBrandId())
                .nameCn(item.getNameCn())
                .nameEn(item.getNameEn())
                .build();
    }

    /**
     * 转换品牌详细信息
     */
    private DouyinBrandListRsp.BrandInfoItem convertToBrandInfoItem(BrandInfosItem item) {
        return DouyinBrandListRsp.BrandInfoItem.builder()
                .brandId(item.getBrandId())
                .brandNameCN(item.getBrandNameCN())
                .brandNameEN(item.getBrandNameEN())
                .level(item.getLevel())
                .status(item.getStatus())
                .brandAlias(item.getBrandAlias())
                .createTimestamp(item.getCreateTimestamp())
                .updateTimestamp(item.getUpdateTimestamp())
                .auditStatus(item.getAuditStatus())
                .bizType(item.getBizType())
                .logo(item.getLogo())
                .build();
    }

    /**
     * 转换店铺类目响应
     */
    private DouyinShopCategoryRsp convertToShopCategoryRsp(ShopGetShopCategoryData data) {
        List<DouyinShopCategoryRsp.CategoryItem> list = null;
        if (data.getData() != null) {
            list = data.getData().stream()
                    .map(this::convertToCategoryItem)
                    .collect(Collectors.toList());
        }

        return DouyinShopCategoryRsp.builder()
                .list(list)
                .build();
    }

    /**
     * 转换类目项
     */
    private DouyinShopCategoryRsp.CategoryItem convertToCategoryItem(DataItem item) {
        return DouyinShopCategoryRsp.CategoryItem.builder()
                .id(item.getId())
                .name(item.getName())
                .level(item.getLevel())
                .parentId(item.getParentId())
                .isLeaf(item.getIsLeaf())
                .enable(item.getEnable())
                .channel(item.getChannel())
                .build();
    }

    /**
     * 转换运费模板列表响应
     */
    private DouyinFreightTemplateListRsp convertToFreightTemplateListRsp(FreightTemplateListData data) {
        List<DouyinFreightTemplateListRsp.TemplateItem> list = null;
        if (data.getList() != null) {
            list = data.getList().stream()
                    .map(this::convertToTemplateItem)
                    .collect(Collectors.toList());
        }

        return DouyinFreightTemplateListRsp.builder()
                .list(list)
                .count(data.getCount())
                .build();
    }

    /**
     * 转换运费模板项
     */
    private DouyinFreightTemplateListRsp.TemplateItem convertToTemplateItem(ListItem item) {
        if (item.getTemplate() == null) {
            return null;
        }
        var template = item.getTemplate();
        return DouyinFreightTemplateListRsp.TemplateItem.builder()
                .id(template.getId())
                .templateName(template.getTemplateName())
                .productProvince(template.getProductProvince())
                .productCity(template.getProductCity())
                .calculateType(template.getCalculateType())
                .transferType(template.getTransferType())
                .ruleType(template.getRuleType())
                .fixedAmount(template.getFixedAmount())
                .build();
    }

    /**
     * 转换商品类目属性响应
     */
    private DouyinCatePropertyRsp convertToCatePropertyRsp(ProductGetCatePropertyV2Data data) {
        List<DouyinCatePropertyRsp.PropertyItem> list = null;
        if (data.getData() != null) {
            list = data.getData().stream()
                    .map(this::convertToPropertyItem)
                    .collect(Collectors.toList());
        }

        return DouyinCatePropertyRsp.builder()
                .list(list)
                .tplType(data.getTplType())
                .build();
    }

    /**
     * 转换属性项
     */
    private DouyinCatePropertyRsp.PropertyItem convertToPropertyItem(com.doudian.open.api.product_getCatePropertyV2.data.DataItem item) {
        List<DouyinCatePropertyRsp.OptionItem> options = null;
        if (item.getOptions() != null) {
            options = item.getOptions().stream()
                    .map(this::convertToOptionItem)
                    .collect(Collectors.toList());
        }

        DouyinCatePropertyRsp.PicRule picRule = null;
        if (item.getPropertyPicRule() != null) {
            picRule = DouyinCatePropertyRsp.PicRule.builder()
                    .available(item.getPropertyPicRule().getAvailable())
                    .required(item.getPropertyPicRule().getRequired())
                    .build();
        }

        return DouyinCatePropertyRsp.PropertyItem.builder()
                .propertyId(item.getPropertyId())
                .propertyName(item.getPropertyName())
                .categoryId(item.getCategoryId())
                .propertyType(item.getPropertyType())
                .required(item.getRequired())
                .status(item.getStatus())
                .sequence(item.getSequence())
                .relationId(item.getRelationId())
                .type(item.getType())
                .hasSubProperty(item.getHasSubProperty())
                .multiSelectMax(item.getMultiSelectMax())
                .diyType(item.getDiyType())
                .importantType(item.getImportantType())
                .featureTypeList(item.getFeatureTypeList())
                .options(options)
                .propertyPicRule(picRule)
                .build();
    }

    /**
     * 转换属性选项
     */
    private DouyinCatePropertyRsp.OptionItem convertToOptionItem(com.doudian.open.api.product_getCatePropertyV2.data.OptionsItem item) {
        return DouyinCatePropertyRsp.OptionItem.builder()
                .name(item.getName())
                .value(item.getValue())
                .valueId(item.getValueId())
                .sequence(item.getSequence())
                .build();
    }

    /**
     * 转换素材文件夹响应
     */
    private DouyinMaterialSearchFolderRsp convertToMaterialSearchFolderRsp(MaterialSearchFolderData data) {
        List<DouyinMaterialSearchFolderRsp.FolderInfoItem> folderInfoList = null;
        if (data.getFolderInfoList() != null) {
            folderInfoList = data.getFolderInfoList().stream()
                    .map(this::convertToFolderInfoItem)
                    .collect(Collectors.toList());
        }

        return DouyinMaterialSearchFolderRsp.builder()
                .folderInfoList(folderInfoList)
                .total(data.getTotal())
                .build();
    }

    /**
     * 转换文件夹信息项
     */
    private DouyinMaterialSearchFolderRsp.FolderInfoItem convertToFolderInfoItem(FolderInfoListItem item) {
        return DouyinMaterialSearchFolderRsp.FolderInfoItem.builder()
                .folderId(item.getFolderId())
                .folderType(item.getFolderType())
                .name(item.getName())
                .operateStatus(item.getOperateStatus())
                .parentFolderId(item.getParentFolderId())
                .createTime(item.getCreateTime())
                .updateTime(item.getUpdateTime())
                .deleteTime(item.getDeleteTime())
                .folderAttr(item.getFolderAttr())
                .build();
    }

    /**
     * 转换素材搜索响应
     */
    private DouyinMaterialSearchMaterialRsp convertToMaterialSearchMaterialRsp(MaterialSearchMaterialData data) {
        List<DouyinMaterialSearchMaterialRsp.MaterialInfoItem> materialInfoList = null;
        if (data.getMaterialInfoList() != null) {
            materialInfoList = data.getMaterialInfoList().stream()
                    .map(this::convertToMaterialInfoItem)
                    .collect(Collectors.toList());
        }

        return DouyinMaterialSearchMaterialRsp.builder()
                .materialInfoList(materialInfoList)
                .total(data.getTotal())
                .build();
    }

    /**
     * 转换素材信息项
     */
    private DouyinMaterialSearchMaterialRsp.MaterialInfoItem convertToMaterialInfoItem(MaterialInfoListItem item) {
        DouyinMaterialSearchMaterialRsp.PhotoInfo photoInfo = null;
        if (item.getPhotoInfo() != null) {
            photoInfo = DouyinMaterialSearchMaterialRsp.PhotoInfo.builder()
                    .height(item.getPhotoInfo().getHeight())
                    .width(item.getPhotoInfo().getWidth())
                    .format(item.getPhotoInfo().getFormat())
                    .build();
        }

        DouyinMaterialSearchMaterialRsp.VideoInfo videoInfo = null;
        if (item.getVideoInfo() != null) {
            videoInfo = DouyinMaterialSearchMaterialRsp.VideoInfo.builder()
                    .format(item.getVideoInfo().getFormat())
                    .duration(item.getVideoInfo().getDuration())
                    .vid(item.getVideoInfo().getVid())
                    .videoCoverUrl(item.getVideoInfo().getVideoCoverUrl())
                    .height(item.getVideoInfo().getHeight())
                    .width(item.getVideoInfo().getWidth())
                    .size(item.getVideoInfo().getSize())
                    .build();
        }

        return DouyinMaterialSearchMaterialRsp.MaterialInfoItem.builder()
                .materialId(item.getMaterialId())
                .folderId(item.getFolderId())
                .originUrl(item.getOriginUrl())
                .byteUrl(item.getByteUrl())
                .materialName(item.getMaterialName())
                .materialType(item.getMaterialType())
                .operateStatus(item.getOperateStatus())
                .auditStatus(item.getAuditStatus())
                .auditRejectDesc(item.getAuditRejectDesc())
                .size(item.getSize())
                .createTime(item.getCreateTime())
                .updateTime(item.getUpdateTime())
                .deleteTime(item.getDeleteTime())
                .photoInfo(photoInfo)
                .videoInfo(videoInfo)
                .build();
    }

    @Override
    public DouyinProductListV2Rsp listProductV2(DouyinProductListV2Req req) {
        ProductListV2Param param = new ProductListV2Param();
        param.setName(req.getName());
        param.setProductId(req.getProductId());
        param.setSkuCodes(req.getSkuCodes());
        param.setStatus(req.getStatus());
        param.setCheckStatus(req.getCheckStatus());
        param.setProductType(req.getProductType());
        param.setStoreId(req.getStoreId());
        param.setStartTime(req.getStartTime());
        param.setEndTime(req.getEndTime());
        param.setUpdateStartTime(req.getUpdateStartTime());
        param.setUpdateEndTime(req.getUpdateEndTime());
        param.setPage(req.getPage());
        param.setSize(req.getSize());
        param.setUseCursor(req.getUseCursor());
        param.setCursorId(req.getCursorId());
        param.setCanCombineProduct(req.getCanCombineProduct());
        param.setNeedRectificationInfo(req.getNeedRectificationInfo());
        param.setNeedCheckOut(req.getNeedCheckOut());

        if (req.getQueryOptions() != null) {
            QueryOptions queryOptions = new QueryOptions();
            queryOptions.setExistCategoryMisplaced(req.getQueryOptions().getExistCategoryMisplaced());
            queryOptions.setExistAuditRejectSuggest(req.getQueryOptions().getExistAuditRejectSuggest());
            queryOptions.setNeedAuditRejectSuggest(req.getQueryOptions().getNeedAuditRejectSuggest());
            param.setQueryOptions(queryOptions);
        }

        ExternalRsp<ProductListV2Data> externalRsp = douyinExternalService.listProduct(req.getOwnerId(), param);
        if (!externalRsp.isSuccess()) {
            log.error("查询抖音商品列表失败，ownerId={}, error={}", req.getOwnerId(), externalRsp.getError());
            throw new BusinessException("查询抖音商品列表失败：" + externalRsp.getError());
        }

        return convertToProductListV2Rsp(externalRsp.getData());
    }

    /**
     * 转换商品列表响应
     */
    private DouyinProductListV2Rsp convertToProductListV2Rsp(ProductListV2Data data) {
        List<DouyinProductListV2Rsp.ProductItem> list = null;
        if (data.getData() != null) {
            list = data.getData().stream()
                    .map(this::convertToProductItem)
                    .collect(Collectors.toList());
        }

        return DouyinProductListV2Rsp.builder()
                .data(list)
                .total(data.getTotal())
                .page(data.getPage())
                .size(data.getSize())
                .cursorId(data.getCursorId())
                .build();
    }

    /**
     * 转换商品项
     */
    private DouyinProductListV2Rsp.ProductItem convertToProductItem(com.doudian.open.api.product_listV2.data.DataItem item) {
        DouyinProductListV2Rsp.CategoryDetail categoryDetail = null;
        if (item.getCategoryDetail() != null) {
            categoryDetail = DouyinProductListV2Rsp.CategoryDetail.builder()
                    .firstCid(item.getCategoryDetail().getFirstCid())
                    .secondCid(item.getCategoryDetail().getSecondCid())
                    .thirdCid(item.getCategoryDetail().getThirdCid())
                    .fourthCid(item.getCategoryDetail().getFourthCid())
                    .firstCname(item.getCategoryDetail().getFirstCname())
                    .secondCname(item.getCategoryDetail().getSecondCname())
                    .thirdCname(item.getCategoryDetail().getThirdCname())
                    .fourthCname(item.getCategoryDetail().getFourthCname())
                    .build();
        }

        DouyinProductListV2Rsp.ShopCategory shopCategory = null;
        if (item.getShopCategory() != null) {
            shopCategory = DouyinProductListV2Rsp.ShopCategory.builder()
                    .leafCategoryIds(item.getShopCategory().getLeafCategoryIds())
                    .build();
        }

        List<DouyinProductListV2Rsp.SpecPriceItem> specPrices = null;
        if (item.getSpecPrices() != null) {
            specPrices = item.getSpecPrices().stream()
                    .map(sp -> DouyinProductListV2Rsp.SpecPriceItem.builder()
                            .id(sp.getId())
                            .code(sp.getCode())
                            .barcodes(sp.getBarcodes())
                            .build())
                    .collect(Collectors.toList());
        }

        return DouyinProductListV2Rsp.ProductItem.builder()
                .productId(item.getProductId())
                .name(item.getName())
                .img(item.getImg())
                .description(item.getDescription())
                .recommendRemark(item.getRecommendRemark())
                .status(item.getStatus())
                .checkStatus(item.getCheckStatus())
                .productType(item.getProductType())
                .payType(item.getPayType())
                .marketPrice(item.getMarketPrice())
                .discountPrice(item.getDiscountPrice())
                .outerProductId(item.getOuterProductId())
                .freightId(item.getFreightId())
                .mobile(item.getMobile())
                .specPrices(specPrices)
                .categoryDetail(categoryDetail)
                .createTime(item.getCreateTime())
                .updateTime(item.getUpdateTime())
                .sellNum(item.getSellNum())
                .canCombine(item.getCanCombine())
                .isSecondHandDigital(item.getIsSecondHandDigital())
                .isPackageProduct(item.getIsPackageProduct())
                .shopCategory(shopCategory)
                .build();
    }

    // ==================== 辅助方法 ====================

    /**
     * 构建查询条件
     */
    private LambdaQueryWrapper<DouyinProduct> buildQueryWrapper(DouyinProductQueryReq req) {
        LambdaQueryWrapper<DouyinProduct> wrapper = new LambdaQueryWrapper<>();

        if (req == null) {
            return wrapper;
        }

        if (StrUtil.isNotBlank(req.getOwnerId())) {
            wrapper.eq(DouyinProduct::getOwnerId, req.getOwnerId());
        }
        if (StrUtil.isNotBlank(req.getProductId())) {
            wrapper.eq(DouyinProduct::getProductId, req.getProductId());
        }
        if (StrUtil.isNotBlank(req.getOuterProductId())) {
            wrapper.eq(DouyinProduct::getOuterProductId, req.getOuterProductId());
        }
        if (StrUtil.isNotBlank(req.getName())) {
            wrapper.like(DouyinProduct::getName, req.getName());
        }
        if (req.getCategoryLeafId() != null) {
            wrapper.eq(DouyinProduct::getCategoryLeafId, req.getCategoryLeafId());
        }
        if (req.getStatus() != null) {
            wrapper.eq(DouyinProduct::getStatus, req.getStatus());
        }
        if (req.getAuditStatus() != null) {
            wrapper.eq(DouyinProduct::getAuditStatus, req.getAuditStatus());
        }

        // 默认过滤已删除
        wrapper.ne(DouyinProduct::getStatus, DouyinProduct.STATUS_DELETED);

        return wrapper;
    }

    /**
     * 将请求转换为实体
     */
    private DouyinProduct convertToEntity(DouyinProductSaveReq req) {
        DouyinProduct product = new DouyinProduct();
        product.setOwnerId(req.getOwnerId());
        product.setProductId(req.getProductId());
        product.setOuterProductId(req.getOuterProductId());
        product.setProductType(req.getProductType());
        product.setCategoryLeafId(req.getCategoryLeafId());
        product.setName(req.getName());
        product.setPic(req.getPic());
        product.setDescription(req.getDescription());
        product.setRecommendRemark(req.getRecommendRemark());
        product.setPayType(req.getPayType());
        product.setPrice(req.getPrice());
        product.setOriginalPrice(req.getOriginalPrice());
        product.setCostPrice(req.getCostPrice());
        product.setStockNum(req.getStockNum());
        product.setReduceType(req.getReduceType());
        product.setDeliveryMethod(req.getDeliveryMethod());
        product.setFreightId(req.getFreightId());
        product.setWeight(req.getWeight());
        product.setWeightUnit(req.getWeightUnit());
        product.setDeliveryDelayDay(req.getDeliveryDelayDay());
        product.setPresellType(req.getPresellType());
        product.setPresellDelay(req.getPresellDelay());
        product.setPresellEndTime(req.getPresellEndTime());
        product.setPresellConfigLevel(req.getPresellConfigLevel());
        product.setPresellDeliveryType(req.getPresellDeliveryType());
        product.setSpecName(req.getSpecName());
        product.setSpecs(req.getSpecs());
        product.setSpecPrices(req.getSpecPrices());
        product.setSpecPic(req.getSpecPic());
        product.setMaximumPerOrder(req.getMaximumPerOrder());
        product.setMinimumPerOrder(req.getMinimumPerOrder());
        product.setLimitPerBuyer(req.getLimitPerBuyer());
        product.setProductFormatNew(req.getProductFormatNew());
        product.setSpuId(req.getSpuId());
        product.setStandardBrandId(req.getStandardBrandId());
        product.setQualityList(req.getQualityList());
        product.setCdfCategory(req.getCdfCategory());
        product.setAssocIds(req.getAssocIds());
        product.setMobile(req.getMobile());
        product.setSupply7dayReturn(req.getSupply7dayReturn());
        product.setCommit(req.getCommit());
        product.setRemark(req.getRemark());
        product.setStatus(req.getStatus());
        return product;
    }

    /**
     * 将实体转换为响应 DTO
     */
    private DouyinProductInfoRsp convertToInfoRsp(DouyinProduct product) {
        if (product == null) {
            return null;
        }

        DouyinProductInfoRsp rsp = new DouyinProductInfoRsp();
        rsp.setId(product.getId());
        rsp.setOwnerId(product.getOwnerId());
        rsp.setProductId(product.getProductId());
        rsp.setOuterProductId(product.getOuterProductId());
        rsp.setProductType(product.getProductType());
        rsp.setProductTypeName(getProductTypeName(product.getProductType()));
        rsp.setCategoryLeafId(product.getCategoryLeafId());
        rsp.setName(product.getName());
        rsp.setPic(product.getPic());
        rsp.setDescription(product.getDescription());
        rsp.setRecommendRemark(product.getRecommendRemark());
        rsp.setPayType(product.getPayType());
        rsp.setPrice(product.getPrice());
        rsp.setOriginalPrice(product.getOriginalPrice());
        rsp.setCostPrice(product.getCostPrice());
        rsp.setStockNum(product.getStockNum());
        rsp.setReduceType(product.getReduceType());
        rsp.setDeliveryMethod(product.getDeliveryMethod());
        rsp.setFreightId(product.getFreightId());
        rsp.setWeight(product.getWeight());
        rsp.setWeightUnit(product.getWeightUnit());
        rsp.setDeliveryDelayDay(product.getDeliveryDelayDay());
        rsp.setPresellType(product.getPresellType());
        rsp.setPresellDelay(product.getPresellDelay());
        rsp.setPresellEndTime(product.getPresellEndTime());
        rsp.setPresellConfigLevel(product.getPresellConfigLevel());
        rsp.setPresellDeliveryType(product.getPresellDeliveryType());
        rsp.setSpecName(product.getSpecName());
        rsp.setSpecs(product.getSpecs());
        rsp.setSpecPrices(product.getSpecPrices());
        rsp.setSpecPic(product.getSpecPic());
        rsp.setMaximumPerOrder(product.getMaximumPerOrder());
        rsp.setMinimumPerOrder(product.getMinimumPerOrder());
        rsp.setLimitPerBuyer(product.getLimitPerBuyer());
        rsp.setProductFormatNew(product.getProductFormatNew());
        rsp.setSpuId(product.getSpuId());
        rsp.setStandardBrandId(product.getStandardBrandId());
        rsp.setMobile(product.getMobile());
        rsp.setSupply7dayReturn(product.getSupply7dayReturn());
        rsp.setRemark(product.getRemark());
        rsp.setStatus(product.getStatus());
        rsp.setStatusName(product.getStatusName());
        rsp.setAuditStatus(product.getAuditStatus());
        rsp.setAuditStatusName(product.getAuditStatusName());
        rsp.setAuditRemark(product.getAuditRemark());
        rsp.setCreateTime(product.getCreateTime());
        rsp.setUpdateTime(product.getUpdateTime());
        rsp.setPublishTime(product.getPublishTime());
        return rsp;
    }

    /**
     * 获取商品类型名称
     */
    private String getProductTypeName(Long productType) {
        if (productType == null) {
            return "普通商品";
        }
        return productType == 1 ? "普通商品" : "电子面单商品";
    }
}
