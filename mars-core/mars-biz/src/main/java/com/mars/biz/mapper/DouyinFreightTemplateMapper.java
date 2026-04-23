package com.mars.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.biz.entity.DouyinFreightTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 抖音运费模板 Mapper
 *
 * @author Mars
 * @date 2026-04-19
 */
@Mapper
public interface DouyinFreightTemplateMapper extends BaseMapper<DouyinFreightTemplate> {

    /**
     * 根据 ownerId 和 freightId 查询模板
     *
     * @param ownerId   店铺所有者ID
     * @param freightId 抖音运费模板ID
     * @return 运费模板
     */
    DouyinFreightTemplate selectByOwnerIdAndFreightId(@Param("ownerId") String ownerId, @Param("freightId") Long freightId);

    /**
     * 根据 ownerId 查询模板列表
     *
     * @param ownerId 店铺所有者ID
     * @return 运费模板列表
     */
    List<DouyinFreightTemplate> selectByOwnerId(@Param("ownerId") String ownerId);
}