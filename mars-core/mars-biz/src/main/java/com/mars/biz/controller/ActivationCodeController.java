package com.mars.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.mars.biz.dto.req.ActivationCodeQueryReq;
import com.mars.biz.dto.rsp.ActivationCodeRsp;
import com.mars.biz.entity.ActivationCode;
import com.mars.biz.service.ActivationCodeService;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 激活码管理 Controller
 *
 * @author Mars
 * @date 2026-04-17
 */
@RestController
@RequestMapping("/biz/activation-code")
@RequiredArgsConstructor
public class ActivationCodeController {

    private final ActivationCodeService activationCodeService;

    // ==================== 激活码 CRUD ====================

    /**
     * 分页查询激活码列表
     */
    @GetMapping("/page")
    @SaCheckPermission("biz:activation-code:list")
    public Result<PageResult<ActivationCode>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            ActivationCodeQueryReq req) {
        var result = activationCodeService.page(page, pageSize, req);
        return Result.ok(PageResult.of(result));
    }

    /**
     * 查询激活码列表
     */
    @GetMapping("/list")
    @SaCheckPermission("biz:activation-code:list")
    public Result<List<ActivationCode>> list(ActivationCodeQueryReq req) {
        List<ActivationCode> codes = activationCodeService.list(req);
        return Result.ok(codes);
    }

    /**
     * 获取激活码详情
     */
    @GetMapping("/{id}")
    @SaCheckPermission("biz:activation-code:query")
    public Result<ActivationCode> getInfo(@PathVariable Long id) {
        ActivationCode code = activationCodeService.getById(id);
        return Result.ok(code);
    }

    /**
     * 新增激活码
     */
    @PostMapping
    @SaCheckPermission("biz:activation-code:add")
    @Log(title = "激活码管理", businessType = BusinessType.INSERT)
    public Result<ActivationCode> add(@RequestBody ActivationCode activationCode) {
        ActivationCode created = activationCodeService.create(activationCode);
        return Result.ok(created);
    }

    /**
     * 更新激活码基本信息
     */
    @PutMapping("/{id}")
    @SaCheckPermission("biz:activation-code:edit")
    @Log(title = "激活码管理", businessType = BusinessType.UPDATE)
    public Result<Void> update(@PathVariable Long id, @RequestBody ActivationCode activationCode) {
        activationCode.setId(id);
        activationCodeService.update(activationCode);
        return Result.ok();
    }

    /**
     * 删除激活码
     */
    @DeleteMapping("/{ids}")
    @SaCheckPermission("biz:activation-code:remove")
    @Log(title = "激活码管理", businessType = BusinessType.DELETE)
    public Result<Void> remove(@PathVariable Long[] ids) {
        activationCodeService.delete(ids);
        return Result.ok();
    }

    // ==================== 激活管理 ====================

    /**
     * 激活激活码
     */
    @PostMapping("/activate")
    @SaCheckPermission("biz:activation-code:activate")
    @Log(title = "激活码激活", businessType = BusinessType.UPDATE)
    public Result<ActivationCodeRsp> activate(
            @RequestParam String code,
            @RequestParam Long shopId) {
        Long userId = StpUtil.getLoginIdAsLong();
        ActivationCodeRsp result = activationCodeService.activate(code, userId, shopId);
        return Result.ok(result);
    }
}
