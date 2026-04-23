package com.mars.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.biz.entity.DouyinProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 抖音商品 Mapper
 *
 * @author Mars
 * @date 2026-04-18
 */
@Mapper
public interface DouyinProductMapper extends BaseMapper<DouyinProduct> {

    /**
     * 查询商品列表（分页）
     *
     * @param ownerId  店铺所有者 ID
     * @param name     商品名称（模糊查询）
     * @param status   状态
     * @param offset   偏移量
     * @param limit    每页数量
     * @return 商品列表
     */
    List<DouyinProduct> selectProductList(
            @Param("ownerId") String ownerId,
            @Param("name") String name,
            @Param("status") Integer status,
            @Param("offset") Integer offset,
            @Param("limit") Integer limit
    );

    /**
     * 查询商品总数
     *
     * @param ownerId  店铺所有者 ID
     * @param name     商品名称（模糊查询）
     * @param status   状态
     * @return 商品总数
     */
    long countProductList(
            @Param("ownerId") String ownerId,
            @Param("name") String name,
            @Param("status") Integer status
    );

    /**
     * 根据 ownerId 和 productId 查询商品
     *
     * @param ownerId   店铺所有者 ID
     * @param productId 抖音商品 ID
     * @return 商品信息
     */
    DouyinProduct selectByOwnerIdAndProductId(
            @Param("ownerId") String ownerId,
            @Param("productId") String productId
    );
}
