package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.dto.req.DouyinFreightTemplateQueryReq;
import com.mars.biz.dto.req.DouyinFreightTemplateSaveReq;
import com.mars.biz.dto.req.DouyinFreightTemplateSyncReq;
import com.mars.biz.dto.rsp.DouyinFreightTemplateInfoRsp;
import com.mars.biz.entity.DouyinFreightTemplate;

import java.util.List;

/**
 * 抖音运费模板服务
 *
 * @author Mars
 * @date 2026-04-19
 */
public interface DouyinFreightTemplateService {

    /**
     * 分页查询
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param req      查询条件
     * @return 分页结果
     */
    Page<DouyinFreightTemplate> page(Integer page, Integer pageSize, DouyinFreightTemplateQueryReq req);

    /**
     * 列表查询
     *
     * @param req 查询条件
     * @return 列表结果
     */
    List<DouyinFreightTemplate> list(DouyinFreightTemplateQueryReq req);

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 运费模板
     */
    DouyinFreightTemplate getById(Long id);

    /**
     * 根据ID查询详情响应
     *
     * @param id 主键ID
     * @return 详情响应
     */
    DouyinFreightTemplateInfoRsp getInfoById(Long id);

    /**
     * 新增
     *
     * @param req 保存请求
     * @return 新增结果
     */
    DouyinFreightTemplate create(DouyinFreightTemplateSaveReq req);

    /**
     * 更新
     *
     * @param req 保存请求
     * @return 更新结果
     */
    DouyinFreightTemplate update(DouyinFreightTemplateSaveReq req);

    /**
     * 保存（新增或更新）
     *
     * @param req 保存请求
     * @return 保存结果
     */
    DouyinFreightTemplate save(DouyinFreightTemplateSaveReq req);

    /**
     * 批量删除
     *
     * @param ids 主键ID数组
     */
    void delete(Long[] ids);

    /**
     * 启用
     *
     * @param id 主键ID
     */
    void enable(Long id);

    /**
     * 禁用
     *
     * @param id 主键ID
     */
    void disable(Long id);

    /**
     * 从抖音平台同步模板
     *
     * @param req 同步请求
     */
    void syncFromDouyin(DouyinFreightTemplateSyncReq req);

    /**
     * 将本地模板推送到抖音平台
     *
     * @param id 主键ID
     */
    void pushToDouyin(Long id);

    /**
     * 根据店铺所有者ID和抖音模板ID查询
     *
     * @param ownerId   店铺所有者ID
     * @param freightId 抖音模板ID
     * @return 运费模板
     */
    DouyinFreightTemplate getByOwnerIdAndFreightId(String ownerId, Long freightId);

    /**
     * 根据店铺所有者ID查询列表
     *
     * @param ownerId 店铺所有者ID
     * @return 运费模板列表
     */
    List<DouyinFreightTemplate> getByOwnerId(String ownerId);
}