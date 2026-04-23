package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.dto.req.ShopQueryReq;
import com.mars.biz.dto.rsp.AuthUrlRsp;
import com.mars.biz.dto.rsp.ExternalRsp;
import com.mars.biz.dto.rsp.ShopAuthInfoRsp;
import com.mars.biz.entity.Shop;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 店铺 Service
 *
 * @author Mars
 * @date 2026-04-16
 */
public interface ShopService {

    /**
     * 分页查询店铺列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param req      查询条件
     * @return 分页结果
     */
    Page<Shop> page(Integer page, Integer pageSize, ShopQueryReq req);

    /**
     * 查询用户的店铺列表
     *
     * @param userId 用户 ID
     * @return 店铺列表
     */
    List<Shop> listByUserId(Long userId);

    /**
     * 根据条件查询店铺列表
     *
     * @param userId           用户 ID
     * @param platform         平台类型
     * @param authStatus       授权状态
     * @param activationStatus 激活状态
     * @return 店铺列表
     */
    List<Shop> listByConditions(Long userId, String platform, Integer authStatus, Integer activationStatus);

    /**
     * 根据 ID 查询店铺
     *
     * @param id 店铺 ID
     * @return 店铺信息
     */
    Shop getById(Long id);

    /**
     * 根据 ID 和用户 ID 查询店铺（验证归属）
     *
     * @param id     店铺 ID
     * @param userId 用户 ID
     * @return 店铺信息
     */
    Shop getByIdAndUserId(Long id, Long userId);

    /**
     * 新增店铺
     *
     * @param shop 店铺信息
     * @return 新增后的店铺
     */
    Shop create(Shop shop);

    /**
     * 更新店铺基本信息
     *
     * @param shop 店铺信息
     */
    void update(Shop shop);

    /**
     * 更新店铺授权状态
     *
     * @param id             店铺 ID
     * @param shopName       店铺名称
     * @param shopIdExternal 外部店铺 ID
     * @param authToken      授权 Token
     * @param remark         备注
     */
    void updateAuthStatus(Long id, String shopName, String shopIdExternal, String authToken, String remark);

    /**
     * 更新店铺激活状态
     *
     * @param id               店铺 ID
     * @param activationStatus 激活状态
     * @param expiresAt        过期时间
     */
    void updateActivationStatus(Long id, Integer activationStatus, LocalDateTime expiresAt);

    /**
     * 删除店铺
     *
     * @param ids 店铺 ID 数组
     */
    void delete(Long[] ids);

    /**
     * 删除店铺（验证归属）
     *
     * @param id     店铺 ID
     * @param userId 用户 ID
     */
    void deleteByIdAndUserId(Long id, Long userId);

    // ==================== 授权相关 ====================

    /**
     * 获取授权链接
     *
     * @param id     店铺 ID
     * @param userId 用户 ID
     * @return 授权链接响应
     */
    ExternalRsp<AuthUrlRsp> getAuthUrl(Long id, Long userId);

    /**
     * 处理授权回调
     *
     * @param id       店铺 ID
     * @param platform 平台类型
     * @param state    授权状态
     * @return 店铺授权信息
     */
    ExternalRsp<ShopAuthInfoRsp> handleAuthCallback(Long id, String platform, String state);

    /**
     * 抖音平台绑定店铺（使用关联码）
     *
     * @param id       店铺 ID
     * @param userId   用户 ID
     * @param bindCode 关联码
     * @param shopName 店铺名称
     * @param state    授权 state（可选，用于验证）
     * @return 店铺授权信息
     */
    ExternalRsp<ShopAuthInfoRsp> bindDouyinShop(Long id, Long userId, String bindCode, String shopName, String state);

    /**
     * 获取授权状态
     *
     * @param id     店铺 ID
     * @param userId 用户 ID
     * @return 授权状态信息
     */
    ShopAuthInfoRsp getAuthStatus(Long id, Long userId);

    /**
     * 刷新授权状态
     *
     * @param id     店铺 ID
     * @param userId 用户 ID
     * @return 授权链接响应
     */
    ExternalRsp<AuthUrlRsp> refreshAuth(Long id, Long userId);


    /**
     * 激活店铺
     *
     * @param id             店铺 ID
     * @param userId         用户 ID
     * @param activationCode 激活码
     * @return 激活结果
     */
    ExternalRsp<ShopAuthInfoRsp> activate(Long id, Long userId, String activationCode);

}
