package com.mars.biz.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.mars.biz.config.ActivationCodeImportConfig;
import com.mars.biz.dto.req.ActivationCodeQueryReq;
import com.mars.biz.dto.rsp.ActivationCodeRsp;
import com.mars.biz.entity.ActivationCode;
import com.mars.biz.excel.ActivationCodeExcel;
import com.mars.biz.service.ActivationCodeService;
import com.mars.common.exception.BusinessException;
import com.mars.common.result.PageResult;
import com.mars.common.result.Result;
import com.mars.system.annotation.Log;
import com.mars.system.annotation.Log.BusinessType;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 激活码管理 Controller
 *
 * @author Mars
 * @date 2026-04-17
 */
@Slf4j
@RestController
@RequestMapping("/biz/activation-code")
@RequiredArgsConstructor
public class ActivationCodeController {

    private final ActivationCodeService activationCodeService;
    private final ActivationCodeImportConfig importConfig;

    private static final byte[] XLSX_MAGIC = new byte[]{0x50, 0x4B, 0x03, 0x04};
    private static final byte[] XLS_MAGIC = new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0};

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

    // ==================== 导入导出 ====================

    /**
     * 导入激活码（无权限注解，可直接测试）
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("请选择要导入的文件");
        }

        if (file.getSize() > importConfig.getMaxFileSizeBytes()) {
            throw new BusinessException("文件大小超过限制，最大允许 " + importConfig.getMaxFileSizeMB() + " MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.hasText(originalFilename)) {
            String lowerName = originalFilename.toLowerCase();
            if (!lowerName.endsWith(".xlsx") && !lowerName.endsWith(".xls")) {
                throw new BusinessException("文件格式不正确，仅支持 .xlsx 或 .xls 格式");
            }
        }

        validateFileFormat(file);

        Map<String, Object> result = activationCodeService.importActivationCodes(file);

        if (result.containsKey("totalRows") && result.containsKey("emptyRows")) {
            int totalRows = ((Number) result.getOrDefault("totalRows", 0)).intValue();
            int emptyRows = ((Number) result.getOrDefault("emptyRows", 0)).intValue();
            int successCount = ((Number) result.getOrDefault("successCount", 0)).intValue();
            int failCount = ((Number) result.getOrDefault("failCount", 0)).intValue();

            if (totalRows == 0 || (totalRows == emptyRows && successCount == 0 && failCount == 0)) {
                throw new BusinessException("Excel文件中没有有效数据，请检查文件格式是否正确");
            }
        }

        return Result.ok(result);
    }

    private void validateFileFormat(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[8];
            int bytesRead = is.read(header);
            if (bytesRead < 4) {
                throw new BusinessException("文件格式不正确，无法识别的文件类型");
            }

            boolean isXlsx = Arrays.equals(Arrays.copyOf(header, XLSX_MAGIC.length), XLSX_MAGIC);
            boolean isXls = Arrays.equals(Arrays.copyOf(header, XLS_MAGIC.length), XLS_MAGIC);

            if (!isXlsx && !isXls) {
                log.warn("文件魔数不匹配，header: {}", bytesToHex(header));
                throw new BusinessException("文件格式不正确，请确保上传的是真实的Excel文件");
            }
        } catch (IOException e) {
            log.error("读取文件失败", e);
            throw new BusinessException("读取文件失败：" + e.getMessage());
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    /**
     * 下载测试用的Excel文件（无权限注解，可直接测试）
     */
    @GetMapping("/test-template")
    public void downloadTestTemplate(HttpServletResponse response) throws IOException {
        List<ActivationCodeExcel> list = activationCodeService.getTestExcelData();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("激活码测试数据", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), ActivationCodeExcel.class).sheet("激活码数据").doWrite(list);
    }
}
