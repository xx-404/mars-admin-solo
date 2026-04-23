package com.mars.biz.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mars.biz.dto.req.ActivationCodeQueryReq;
import com.mars.biz.dto.rsp.ActivationCodeRsp;
import com.mars.biz.entity.ActivationCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 激活码 Service
 *
 * @author Mars
 * @date 2026-04-17
 */
public interface ActivationCodeService {

    /**
     * 分页查询激活码列表
     *
     * @param page     页码
     * @param pageSize 每页数量
     * @param req      查询条件
     * @return 分页结果
     */
    Page<ActivationCode> page(Integer page, Integer pageSize, ActivationCodeQueryReq req);

    /**
     * 查询激活码列表
     *
     * @param req 查询条件
     * @return 激活码列表
     */
    List<ActivationCode> list(ActivationCodeQueryReq req);

    /**
     * 根据 ID 查询激活码
     *
     * @param id 激活码 ID
     * @return 激活码信息
     */
    ActivationCode getById(Long id);

    /**
     * 根据激活码查询
     *
     * @param code 激活码
     * @return 激活码信息
     */
    ActivationCode getByCode(String code);

    /**
     * 创建激活码
     *
     * @param activationCode 激活码信息
     * @return 创建后的激活码
     */
    ActivationCode create(ActivationCode activationCode);

    /**
     * 更新激活码
     *
     * @param activationCode 激活码信息
     */
    void update(ActivationCode activationCode);

    /**
     * 删除激活码
     *
     * @param ids 激活码 ID 数组
     */
    void delete(Long[] ids);

    /**
     * 激活激活码
     *
     * @param code     激活码
     * @param userId   用户 ID
     * @param shopId   店铺 ID
     * @return 激活码信息
     */
    ActivationCodeRsp activate(String code, Long userId, Long shopId);

    /**
     * 导入激活码
     *
     * @param file Excel 文件
     * @return 导入结果，包含 successCount, failCount, errors
     */
    Map<String, Object> importActivationCodes(MultipartFile file);

    /**
     * 获取测试用的Excel数据列表
     *
     * @return 测试数据列表
     */
    List<com.mars.biz.excel.ActivationCodeExcel> getTestExcelData();
}
