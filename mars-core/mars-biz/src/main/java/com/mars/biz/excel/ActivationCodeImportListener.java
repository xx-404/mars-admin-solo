package com.mars.biz.excel;

import cn.hutool.core.util.RandomUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.mars.biz.config.ActivationCodeImportConfig;
import com.mars.biz.entity.ActivationCode;
import com.mars.biz.mapper.ActivationCodeMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
public class ActivationCodeImportListener extends AnalysisEventListener<ActivationCodeExcel> {

    private final ActivationCodeMapper activationCodeMapper;
    private final ActivationCodeImportConfig config;
    private final Set<String> codesInCurrentFile = new HashSet<>();
    private final List<ActivationCodeExcel> dataList = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();
    private final List<ImportResultItem> successItems = new ArrayList<>();
    private final List<ImportResultItem> failItems = new ArrayList<>();

    private int totalRows = 0;
    private int emptyRows = 0;
    private int successCount = 0;
    private int failCount = 0;
    private boolean hasFatalError = false;
    private String fatalErrorMessage = null;

    private final Pattern codePattern;

    public ActivationCodeImportListener(ActivationCodeMapper activationCodeMapper, ActivationCodeImportConfig config) {
        this.activationCodeMapper = activationCodeMapper;
        this.config = config;
        this.codePattern = Pattern.compile(config.getCodePattern());
    }

    @Override
    public void invoke(ActivationCodeExcel data, AnalysisContext context) {
        int rowNum = context.readRowHolder().getRowIndex() + 1;
        totalRows++;

        if (hasFatalError) {
            return;
        }

        if (totalRows > config.getMaxRows()) {
            hasFatalError = true;
            fatalErrorMessage = "导入数据超过最大限制，最多允许 " + config.getMaxRows() + " 条数据";
            return;
        }

        if (isEmptyRow(data)) {
            emptyRows++;
            return;
        }

        ValidationResult validation = validateRow(data, rowNum);
        if (!validation.isValid()) {
            addError(rowNum, validation.getErrorMessage());
            failItems.add(new ImportResultItem(rowNum, null, validation.getErrorMessage()));
            failCount++;
            return;
        }

        if (StringUtils.hasText(data.getActivationCode())) {
            String trimmedCode = data.getActivationCode().trim().toUpperCase();

            if (codesInCurrentFile.contains(trimmedCode)) {
                addError(rowNum, "激活码[" + trimmedCode + "]在文件内重复");
                failItems.add(new ImportResultItem(rowNum, trimmedCode, "文件内重复"));
                failCount++;
                return;
            }

            ActivationCode existCode = activationCodeMapper.selectByCode(trimmedCode);
            if (existCode != null) {
                addError(rowNum, "激活码[" + trimmedCode + "]已存在");
                failItems.add(new ImportResultItem(rowNum, trimmedCode, "数据库已存在"));
                failCount++;
                return;
            }

            data.setActivationCode(trimmedCode);
            codesInCurrentFile.add(trimmedCode);
        } else {
            String generatedCode = generateUniqueCode();
            data.setActivationCode(generatedCode);
            codesInCurrentFile.add(generatedCode);
        }

        if (StringUtils.hasText(data.getDurationTypeStr())) {
            String type = parseType(data.getDurationTypeStr().trim());
            if (type != null) {
                data.setDurationType(type);
            } else {
                data.setDurationType(determineType(data.getDurationDays()));
            }
        } else {
            data.setDurationType(determineType(data.getDurationDays()));
        }

        dataList.add(data);

        if (dataList.size() >= config.getBatchSize()) {
            saveBatch();
            dataList.clear();
        }
    }

    private boolean isEmptyRow(ActivationCodeExcel data) {
        boolean codeEmpty = !StringUtils.hasText(data.getActivationCode()) 
                || data.getActivationCode().trim().isEmpty();
        boolean typeEmpty = !StringUtils.hasText(data.getDurationTypeStr())
                || data.getDurationTypeStr().trim().isEmpty();
        boolean daysEmpty = data.getDurationDays() == null;

        return codeEmpty && typeEmpty && daysEmpty;
    }

    private ValidationResult validateRow(ActivationCodeExcel data, int rowNum) {
        if (data.getDurationDays() == null) {
            if (StringUtils.hasText(data.getDurationTypeStr())) {
                Integer days = parseDaysFromType(data.getDurationTypeStr().trim());
                if (days != null) {
                    data.setDurationDays(days);
                } else {
                    return ValidationResult.fail("无法从类型[" + data.getDurationTypeStr() + "]解析天数，请直接填写天数");
                }
            } else {
                return ValidationResult.fail("天数不能为空");
            }
        }

        if (data.getDurationDays() < config.getMinDurationDays()) {
            return ValidationResult.fail("天数不能小于 " + config.getMinDurationDays() + " 天");
        }
        if (data.getDurationDays() > config.getMaxDurationDays()) {
            return ValidationResult.fail("天数不能大于 " + config.getMaxDurationDays() + " 天");
        }

        if (StringUtils.hasText(data.getActivationCode())) {
            String trimmedCode = data.getActivationCode().trim();
            
            if (trimmedCode.isEmpty()) {
                data.setActivationCode(null);
                return ValidationResult.ok();
            }

            if (trimmedCode.length() > config.getMaxCodeLength()) {
                return ValidationResult.fail("激活码长度不能超过 " + config.getMaxCodeLength() + " 个字符");
            }

            if (!codePattern.matcher(trimmedCode.toUpperCase()).matches()) {
                return ValidationResult.fail("激活码格式不正确，仅支持大写字母和数字");
            }
        }

        return ValidationResult.ok();
    }

    private void addError(int rowNum, String message) {
        if (errorMessages.size() < config.getMaxErrorMessages()) {
            errorMessages.add("第" + rowNum + "行：" + message);
        } else if (errorMessages.size() == config.getMaxErrorMessages()) {
            errorMessages.add("... 还有更多错误未显示");
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        if (hasFatalError) {
            log.error("导入存在致命错误: {}", fatalErrorMessage);
            return;
        }

        if (!dataList.isEmpty()) {
            saveBatch();
        }

        log.info("激活码导入完成，总行数: {}, 空行: {}, 成功: {}, 失败: {}", 
                totalRows, emptyRows, successCount, failCount);
    }

    private void saveBatch() {
        if (dataList.isEmpty()) {
            return;
        }

        for (ActivationCodeExcel excel : dataList) {
            try {
                ActivationCode code = new ActivationCode();
                code.setActivationCode(excel.getActivationCode());
                code.setDurationType(excel.getDurationType());
                code.setDurationDays(excel.getDurationDays());
                code.setActivationStatus(ActivationCode.STATUS_UNACTIVATED);
                code.setCreateTime(LocalDateTime.now());
                code.setUpdateTime(LocalDateTime.now());

                activationCodeMapper.insert(code);
                successCount++;
                successItems.add(new ImportResultItem(0, excel.getActivationCode(), "成功"));
            } catch (Exception e) {
                log.error("导入激活码失败: {}", excel.getActivationCode(), e);
                String errorMsg = e.getMessage();
                if (errorMsg != null && errorMsg.contains("Duplicate entry") 
                        || errorMsg.contains("唯一约束")) {
                    errorMsg = "激活码已存在";
                }
                addError(0, "激活码[" + excel.getActivationCode() + "]导入失败: " + errorMsg);
                failItems.add(new ImportResultItem(0, excel.getActivationCode(), errorMsg));
                failCount++;
            }
        }
    }

    private Integer parseDaysFromType(String typeStr) {
        return switch (typeStr.toUpperCase()) {
            case "DAY", "日" -> 1;
            case "MONTH", "月" -> 30;
            case "QUARTER", "季" -> 90;
            case "YEAR", "年" -> 365;
            default -> null;
        };
    }

    private String parseType(String typeStr) {
        return switch (typeStr.toUpperCase()) {
            case "DAY", "日" -> "DAY";
            case "MONTH", "月" -> "MONTH";
            case "QUARTER", "季" -> "QUARTER";
            case "YEAR", "年" -> "YEAR";
            default -> null;
        };
    }

    private String determineType(int days) {
        if (days == 1) return "DAY";
        if (days == 30) return "MONTH";
        if (days == 90) return "QUARTER";
        if (days == 365) return "YEAR";
        return "DAY";
    }

    private String generateUniqueCode() {
        String code;
        int maxAttempts = 100;
        int attempts = 0;
        do {
            code = RandomUtil.randomStringUpper(16);
            attempts++;
        } while ((codesInCurrentFile.contains(code) 
                || activationCodeMapper.selectByCode(code) != null) 
                && attempts < maxAttempts);
        
        if (attempts >= maxAttempts) {
            throw new RuntimeException("无法生成唯一的激活码，请稍后重试");
        }
        return code;
    }

    public boolean isHasFatalError() {
        return hasFatalError;
    }

    public String getFatalErrorMessage() {
        return fatalErrorMessage;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public int getEmptyRows() {
        return emptyRows;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
    }

    public List<ImportResultItem> getSuccessItems() {
        return successItems;
    }

    public List<ImportResultItem> getFailItems() {
        return failItems;
    }

    private static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        public static ValidationResult fail(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class ImportResultItem {
        private int rowNum;
        private String code;
        private String message;
    }
}
