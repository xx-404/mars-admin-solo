package com.mars.biz.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.doudian.open.api.freightTemplate_create.param.ColumnsItem;
import com.doudian.open.api.freightTemplate_create.param.FreightTemplateCreateParam;
import com.doudian.open.api.freightTemplate_create.param.Template;
import com.doudian.open.api.freightTemplate_detail.param.FreightTemplateDetailParam;
import com.doudian.open.api.freightTemplate_list.param.FreightTemplateListParam;
import com.doudian.open.api.freightTemplate_update.param.FreightTemplateUpdateParam;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mars.biz.dto.req.DouyinFreightTemplateQueryReq;
import com.mars.biz.dto.req.DouyinFreightTemplateSaveReq;
import com.mars.biz.dto.req.DouyinFreightTemplateSyncReq;
import com.mars.biz.dto.rsp.DouyinFreightTemplateInfoRsp;
import com.mars.biz.dto.rsp.ExternalRsp;
import com.mars.biz.entity.DouyinFreightTemplate;
import com.mars.biz.mapper.DouyinFreightTemplateMapper;
import com.mars.biz.service.DouyinExternalService;
import com.mars.biz.service.DouyinFreightTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 抖音运费模板服务实现
 *
 * @author Mars
 * @date 2026-04-19
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DouyinFreightTemplateServiceImpl implements DouyinFreightTemplateService {

    private final DouyinFreightTemplateMapper mapper;
    private final DouyinExternalService douyinExternalService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Page<DouyinFreightTemplate> page(Integer page, Integer pageSize, DouyinFreightTemplateQueryReq req) {
        Page<DouyinFreightTemplate> pageParam = new Page<>(page, pageSize);
        LambdaQueryWrapper<DouyinFreightTemplate> wrapper = buildQueryWrapper(req);
        wrapper.orderByDesc(DouyinFreightTemplate::getCreateTime);
        return mapper.selectPage(pageParam, wrapper);
    }

    @Override
    public List<DouyinFreightTemplate> list(DouyinFreightTemplateQueryReq req) {
        LambdaQueryWrapper<DouyinFreightTemplate> wrapper = buildQueryWrapper(req);
        wrapper.orderByDesc(DouyinFreightTemplate::getCreateTime);
        return mapper.selectList(wrapper);
    }

    @Override
    public DouyinFreightTemplate getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public DouyinFreightTemplateInfoRsp getInfoById(Long id) {
        DouyinFreightTemplate entity = mapper.selectById(id);
        return convertToInfoRsp(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DouyinFreightTemplate create(DouyinFreightTemplateSaveReq req) {
        DouyinFreightTemplate entity = convertToEntity(req);
        entity.setId(null);
        entity.setStatus(req.getStatus() != null ? req.getStatus() : DouyinFreightTemplate.STATUS_ENABLED);
        entity.setSyncStatus(DouyinFreightTemplate.SYNC_STATUS_NONE);
        mapper.insert(entity);
        log.info("创建抖音运费模板成功，id={}", entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DouyinFreightTemplate update(DouyinFreightTemplateSaveReq req) {
        if (req.getId() == null) {
            throw new IllegalArgumentException("更新时ID不能为空");
        }
        DouyinFreightTemplate entity = convertToEntity(req);
        mapper.updateById(entity);
        log.info("更新抖音运费模板成功，id={}", entity.getId());
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DouyinFreightTemplate save(DouyinFreightTemplateSaveReq req) {
        if (req.getId() != null) {
            return update(req);
        }
        return create(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long[] ids) {
        mapper.deleteBatchIds(java.util.Arrays.asList(ids));
        log.info("删除抖音运费模板成功，ids={}", ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void enable(Long id) {
        if (mapper.selectById(id) == null) {
            throw new IllegalArgumentException("运费模板不存在: " + id);
        }
        DouyinFreightTemplate entity = new DouyinFreightTemplate();
        entity.setId(id);
        entity.setStatus(DouyinFreightTemplate.STATUS_ENABLED);
        mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void disable(Long id) {
        if (mapper.selectById(id) == null) {
            throw new IllegalArgumentException("运费模板不存在: " + id);
        }
        DouyinFreightTemplate entity = new DouyinFreightTemplate();
        entity.setId(id);
        entity.setStatus(DouyinFreightTemplate.STATUS_DISABLED);
        mapper.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncFromDouyin(DouyinFreightTemplateSyncReq req) {
        String ownerId = req.getOwnerId();
        log.info("开始从抖音平台同步运费模板，ownerId={}", ownerId);

        // 调用抖音 API 获取模板列表
        FreightTemplateListParam listParam = new FreightTemplateListParam();
        listParam.setPage("1");
        listParam.setSize("100");

        ExternalRsp<com.doudian.open.api.freightTemplate_list.data.FreightTemplateListData> listRsp =
                douyinExternalService.listFreightTemplate(ownerId, listParam);

        if (!listRsp.isSuccess()) {
            log.error("从抖音获取运费模板列表失败：{}", listRsp.getError());
            throw new RuntimeException("从抖音获取运费模板列表失败：" + listRsp.getError());
        }

        com.doudian.open.api.freightTemplate_list.data.FreightTemplateListData listData = listRsp.getData();
        if (listData == null || listData.getList() == null) {
            log.info("抖音平台无运费模板数据");
            return;
        }

        // 遍历模板列表，逐个获取详情并保存
        for (com.doudian.open.api.freightTemplate_list.data.ListItem item : listData.getList()) {
            Long freightId = item.getTemplate().getId();
            if (freightId == null) continue;

            // 获取详情
            FreightTemplateDetailParam detailParam = new FreightTemplateDetailParam();
            detailParam.setFreightId(freightId);

            ExternalRsp<com.doudian.open.api.freightTemplate_detail.data.FreightTemplateDetailData> detailRsp =
                    douyinExternalService.getFreightTemplateDetail(ownerId, detailParam);

            if (!detailRsp.isSuccess()) {
                log.warn("获取运费模板详情失败，freightId={}: {}", freightId, detailRsp.getError());
                continue;
            }

            // 转换并保存到本地
            DouyinFreightTemplate localTemplate = convertFromDouyinDetail(detailRsp.getData(), ownerId);
            saveOrUpdateByFreightId(localTemplate);
        }

        log.info("从抖音平台同步运费模板完成，ownerId={}", ownerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pushToDouyin(Long id) {
        DouyinFreightTemplate local = mapper.selectById(id);
        if (local == null) {
            throw new IllegalArgumentException("运费模板不存在");
        }

        log.info("开始推送运费模板到抖音平台，id={}, ownerId={}", id, local.getOwnerId());

        try {
            if (local.getFreightId() == null) {
                // 创建新模板
                FreightTemplateCreateParam createParam = convertToCreateParam(local);
                ExternalRsp<com.doudian.open.api.freightTemplate_create.data.FreightTemplateCreateData> rsp =
                        douyinExternalService.createFreightTemplate(local.getOwnerId(), createParam);

                if (!rsp.isSuccess()) {
                    throw new RuntimeException("创建运费模板失败：" + rsp.getError());
                }

                // 更新本地记录
                local.setFreightId(rsp.getData().getTemplateId());
                local.setSyncStatus(DouyinFreightTemplate.SYNC_STATUS_DONE);
                local.setSyncTime(LocalDateTime.now());
                local.setSyncError(null);
                mapper.updateById(local);
            } else {
                // 更新现有模板
                FreightTemplateUpdateParam updateParam = convertToUpdateParam(local);
                ExternalRsp<com.doudian.open.api.freightTemplate_update.data.FreightTemplateUpdateData> rsp =
                        douyinExternalService.updateFreightTemplate(local.getOwnerId(), updateParam);

                if (!rsp.isSuccess()) {
                    throw new RuntimeException("更新运费模板失败：" + rsp.getError());
                }

                local.setSyncStatus(DouyinFreightTemplate.SYNC_STATUS_DONE);
                local.setSyncTime(LocalDateTime.now());
                local.setSyncError(null);
                mapper.updateById(local);
            }

            log.info("推送运费模板到抖音平台成功，id={}", id);
        } catch (Exception e) {
            // 更新同步失败状态
            local.setSyncStatus(DouyinFreightTemplate.SYNC_STATUS_FAILED);
            local.setSyncTime(LocalDateTime.now());
            local.setSyncError(e.getMessage());
            mapper.updateById(local);
            throw e;
        }
    }

    @Override
    public DouyinFreightTemplate getByOwnerIdAndFreightId(String ownerId, Long freightId) {
        return mapper.selectByOwnerIdAndFreightId(ownerId, freightId);
    }

    @Override
    public List<DouyinFreightTemplate> getByOwnerId(String ownerId) {
        return mapper.selectByOwnerId(ownerId);
    }

    // ==================== 辅助方法 ====================

    private LambdaQueryWrapper<DouyinFreightTemplate> buildQueryWrapper(DouyinFreightTemplateQueryReq req) {
        LambdaQueryWrapper<DouyinFreightTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(req.getOwnerId())) {
            wrapper.eq(DouyinFreightTemplate::getOwnerId, req.getOwnerId());
        }
        if (StrUtil.isNotBlank(req.getTemplateName())) {
            wrapper.like(DouyinFreightTemplate::getTemplateName, req.getTemplateName());
        }
        if (req.getStatus() != null) {
            wrapper.eq(DouyinFreightTemplate::getStatus, req.getStatus());
        }
        if (req.getSyncStatus() != null) {
            wrapper.eq(DouyinFreightTemplate::getSyncStatus, req.getSyncStatus());
        }
        return wrapper;
    }

    private DouyinFreightTemplate convertToEntity(DouyinFreightTemplateSaveReq req) {
        DouyinFreightTemplate entity = new DouyinFreightTemplate();
        entity.setId(req.getId());
        entity.setOwnerId(req.getOwnerId());
        entity.setFreightId(req.getFreightId());
        entity.setTemplateName(req.getTemplateName());
        entity.setProductProvince(req.getProductProvince());
        entity.setProductProvinceName(req.getProductProvinceName());
        entity.setProductCity(req.getProductCity());
        entity.setProductCityName(req.getProductCityName());
        entity.setCalculateType(req.getCalculateType());
        entity.setTransferType(req.getTransferType());
        entity.setRuleType(req.getRuleType());
        entity.setFixedAmount(req.getFixedAmount());
        entity.setColumns(req.getColumns());
        entity.setUpsertTransferRule(req.getUpsertTransferRule());
        entity.setStatus(req.getStatus());
        return entity;
    }

    private DouyinFreightTemplateInfoRsp convertToInfoRsp(DouyinFreightTemplate entity) {
        if (entity == null) return null;
        return DouyinFreightTemplateInfoRsp.builder()
                .id(entity.getId())
                .ownerId(entity.getOwnerId())
                .freightId(entity.getFreightId())
                .templateName(entity.getTemplateName())
                .productProvince(entity.getProductProvince())
                .productProvinceName(entity.getProductProvinceName())
                .productCity(entity.getProductCity())
                .productCityName(entity.getProductCityName())
                .calculateType(entity.getCalculateType())
                .calculateTypeName(entity.getCalculateTypeName())
                .transferType(entity.getTransferType())
                .transferTypeName(entity.getTransferTypeName())
                .ruleType(entity.getRuleType())
                .ruleTypeName(entity.getRuleTypeName())
                .fixedAmount(entity.getFixedAmount())
                .columns(entity.getColumns())
                .upsertTransferRule(entity.getUpsertTransferRule())
                .status(entity.getStatus())
                .statusName(entity.getStatusName())
                .syncStatus(entity.getSyncStatus())
                .syncStatusName(entity.getSyncStatusName())
                .syncTime(entity.getSyncTime())
                .syncError(entity.getSyncError())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private DouyinFreightTemplate convertFromDouyinDetail(
            com.doudian.open.api.freightTemplate_detail.data.FreightTemplateDetailData data, String ownerId) {
        DouyinFreightTemplate entity = new DouyinFreightTemplate();
        entity.setOwnerId(ownerId);

        com.doudian.open.api.freightTemplate_detail.data.Data dataObj = data.getData();
        if (dataObj != null) {
            com.doudian.open.api.freightTemplate_detail.data.Template template = dataObj.getTemplate();
            if (template != null) {
                entity.setFreightId(template.getId());
                entity.setTemplateName(template.getTemplateName());
                // productProvince 和 productCity 在 SDK 中是 String，转换为 Long
                if (template.getProductProvince() != null) {
                    try {
                        entity.setProductProvince(Long.parseLong(template.getProductProvince()));
                    } catch (NumberFormatException e) {
                        log.warn("productProvince 转换失败: {}", template.getProductProvince());
                    }
                }
                if (template.getProductCity() != null) {
                    try {
                        entity.setProductCity(Long.parseLong(template.getProductCity()));
                    } catch (NumberFormatException e) {
                        log.warn("productCity 转换失败: {}", template.getProductCity());
                    }
                }
                entity.setCalculateType(template.getCalculateType());
                entity.setTransferType(template.getTransferType());
                entity.setRuleType(template.getRuleType());
                entity.setFixedAmount(template.getFixedAmount());
            }

            // 将 columns 转为 JSON 存储
            if (dataObj.getColumns() != null) {
                try {
                    entity.setColumns(objectMapper.writeValueAsString(dataObj.getColumns()));
                } catch (Exception e) {
                    log.warn("序列化 columns 失败: {}", e.getMessage());
                }
            }
        }

        entity.setStatus(DouyinFreightTemplate.STATUS_ENABLED);
        entity.setSyncStatus(DouyinFreightTemplate.SYNC_STATUS_DONE);
        entity.setSyncTime(LocalDateTime.now());

        return entity;
    }

    private void saveOrUpdateByFreightId(DouyinFreightTemplate entity) {
        if (entity.getFreightId() == null) {
            mapper.insert(entity);
            return;
        }

        DouyinFreightTemplate existing = mapper.selectByOwnerIdAndFreightId(entity.getOwnerId(), entity.getFreightId());
        if (existing != null) {
            entity.setId(existing.getId());
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
    }

    private FreightTemplateCreateParam convertToCreateParam(DouyinFreightTemplate local) {
        FreightTemplateCreateParam param = new FreightTemplateCreateParam();

        Template template = new Template();
        template.setTemplateName(local.getTemplateName());
        template.setProductProvince(local.getProductProvince());
        template.setProductCity(local.getProductCity());
        template.setCalculateType(local.getCalculateType());
        template.setTransferType(local.getTransferType());
        template.setRuleType(local.getRuleType());
        template.setFixedAmount(local.getFixedAmount());

        // 解析 columns JSON
        if (StrUtil.isNotBlank(local.getColumns())) {
            try {
                List<ColumnsItem> columnsItems =
                        objectMapper.readValue(local.getColumns(),
                                new TypeReference<List<ColumnsItem>>() {});
                param.setColumns(columnsItems);
            } catch (Exception e) {
                log.warn("解析 columns JSON 失败: {}", e.getMessage());
            }
        }

        param.setTemplate(template);
        param.setUpsertTransferRule(local.getUpsertTransferRule());

        return param;
    }

    private FreightTemplateUpdateParam convertToUpdateParam(DouyinFreightTemplate local) {
        FreightTemplateUpdateParam param = new FreightTemplateUpdateParam();

        com.doudian.open.api.freightTemplate_update.param.Template template = new com.doudian.open.api.freightTemplate_update.param.Template();
        template.setId(local.getFreightId());
        template.setTemplateName(local.getTemplateName());
        template.setProductProvince(local.getProductProvince());
        template.setProductCity(local.getProductCity());
        template.setCalculateType(local.getCalculateType());
        template.setTransferType(local.getTransferType());
        template.setRuleType(local.getRuleType());
        template.setFixedAmount(local.getFixedAmount());

        if (StrUtil.isNotBlank(local.getColumns())) {
            try {
                List<com.doudian.open.api.freightTemplate_update.param.ColumnsItem> columnsItems =
                        objectMapper.readValue(local.getColumns(),
                                new TypeReference<List<com.doudian.open.api.freightTemplate_update.param.ColumnsItem>>() {});
                param.setColumns(columnsItems);
            } catch (Exception e) {
                log.warn("解析 columns JSON 失败: {}", e.getMessage());
            }
        }

        param.setTemplate(template);
        param.setUpsertTransferRule(local.getUpsertTransferRule());

        return param;
    }
}
