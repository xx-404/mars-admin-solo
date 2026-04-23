package com.mars.biz.excel;

import com.alibaba.excel.EasyExcel;
import com.mars.biz.config.ActivationCodeImportConfig;
import com.mars.biz.entity.ActivationCode;
import com.mars.biz.mapper.ActivationCodeMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("ActivationCodeImportListener 单元测试")
class ActivationCodeImportListenerTest {

    private ActivationCodeMapper mockMapper;
    private ActivationCodeImportConfig config;
    private List<ActivationCodeExcel> testData;

    @BeforeEach
    void setUp() {
        mockMapper = Mockito.mock(ActivationCodeMapper.class);
        config = new ActivationCodeImportConfig();
        testData = new ArrayList<>();
    }

    @Test
    @DisplayName("测试空行过滤")
    void testEmptyRowFilter(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        
        data.add(createExcelData("CODE001", "月", 30));
        
        data.add(createExcelData(null, null, null));
        
        data.add(createExcelData("CODE002", "季", 90));

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(3, listener.getTotalRows());
        assertEquals(1, listener.getEmptyRows());
    }

    @Test
    @DisplayName("测试激活码格式校验 - 包含特殊字符")
    void testInvalidCodeFormat_SpecialCharacters(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("CODE@#$", "月", 30));

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(1, listener.getFailCount());
        assertEquals(0, listener.getSuccessCount());
        assertTrue(listener.getErrorMessages().get(0).contains("格式不正确"));
    }

    @Test
    @DisplayName("测试激活码长度校验 - 超长")
    void testInvalidCodeLength_TooLong(@TempDir Path tempDir) throws IOException {
        String longCode = "A".repeat(50);
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData(longCode, "月", 30));

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(1, listener.getFailCount());
        assertTrue(listener.getErrorMessages().get(0).contains("长度不能超过"));
    }

    @Test
    @DisplayName("测试天数校验 - 负数")
    void testInvalidDays_Negative(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("CODE001", "月", -1));

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(1, listener.getFailCount());
        assertTrue(listener.getErrorMessages().get(0).contains("不能小于"));
    }

    @Test
    @DisplayName("测试天数校验 - 零")
    void testInvalidDays_Zero(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("CODE001", "月", 0));

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(1, listener.getFailCount());
        assertTrue(listener.getErrorMessages().get(0).contains("不能小于"));
    }

    @Test
    @DisplayName("测试天数校验 - 超出最大限制")
    void testInvalidDays_ExceedsMax(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("CODE001", "月", 20000));

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(1, listener.getFailCount());
        assertTrue(listener.getErrorMessages().get(0).contains("不能大于"));
    }

    @Test
    @DisplayName("测试天数为空时从类型解析 - 中文类型")
    void testDaysFromType_ChineseType(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("CODE001", "月", null));
        data.add(createExcelData("CODE002", "年", null));

        File testFile = createTestExcel(tempDir, data);

        when(mockMapper.selectByCode(anyString())).thenReturn(null);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(0, listener.getFailCount());
    }

    @Test
    @DisplayName("测试天数为空时从类型解析 - 英文类型")
    void testDaysFromType_EnglishType(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("CODE001", "MONTH", null));
        data.add(createExcelData("CODE002", "YEAR", null));

        File testFile = createTestExcel(tempDir, data);

        when(mockMapper.selectByCode(anyString())).thenReturn(null);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(0, listener.getFailCount());
    }

    @Test
    @DisplayName("测试天数和类型都为空")
    void testBothDaysAndTypeEmpty(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("CODE001", null, null));

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(1, listener.getFailCount());
        assertTrue(listener.getErrorMessages().get(0).contains("天数不能为空"));
    }

    @Test
    @DisplayName("测试同一文件内激活码重复")
    void testDuplicateCodeInSameFile(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("DUPLICATE001", "月", 30));
        data.add(createExcelData("DUPLICATE001", "季", 90));

        File testFile = createTestExcel(tempDir, data);

        when(mockMapper.selectByCode(anyString())).thenReturn(null);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(1, listener.getSuccessCount());
        assertEquals(1, listener.getFailCount());
        assertTrue(listener.getErrorMessages().get(0).contains("在文件内重复"));
    }

    @Test
    @DisplayName("测试激活码在数据库中已存在")
    void testDuplicateCodeInDatabase(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("EXISTING01", "月", 30));

        File testFile = createTestExcel(tempDir, data);

        ActivationCode existingCode = new ActivationCode();
        existingCode.setActivationCode("EXISTING01");
        when(mockMapper.selectByCode("EXISTING01")).thenReturn(existingCode);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(0, listener.getSuccessCount());
        assertEquals(1, listener.getFailCount());
        assertTrue(listener.getErrorMessages().get(0).contains("已存在"));
    }

    @Test
    @DisplayName("测试激活码自动生成 - 激活码为空")
    void testAutoGenerateCode(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData(null, "月", 30));
        data.add(createExcelData("", "季", 90));
        data.add(createExcelData("   ", "年", 365));

        File testFile = createTestExcel(tempDir, data);

        when(mockMapper.selectByCode(anyString())).thenReturn(null);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(3, listener.getSuccessCount());
        assertEquals(0, listener.getFailCount());
    }

    @Test
    @DisplayName("测试超过最大导入条数限制")
    void testExceedsMaxRows(@TempDir Path tempDir) throws IOException {
        ActivationCodeImportConfig customConfig = new ActivationCodeImportConfig();
        customConfig.setMaxRows(2);

        List<ActivationCodeExcel> data = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            data.add(createExcelData("CODE" + String.format("%03d", i), "月", 30));
        }

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, customConfig);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertTrue(listener.isHasFatalError());
        assertNotNull(listener.getFatalErrorMessage());
        assertTrue(listener.getFatalErrorMessage().contains("超过最大限制"));
    }

    @Test
    @DisplayName("测试正常导入成功")
    void testSuccessfulImport(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("VALID001", "月", 30));
        data.add(createExcelData("VALID002", "季", 90));
        data.add(createExcelData("VALID003", "年", 365));
        data.add(createExcelData(null, "日", 1));

        File testFile = createTestExcel(tempDir, data);

        when(mockMapper.selectByCode(anyString())).thenReturn(null);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(4, listener.getSuccessCount());
        assertEquals(0, listener.getFailCount());
        assertEquals(0, listener.getEmptyRows());
        assertFalse(listener.isHasFatalError());
    }

    @Test
    @DisplayName("测试激活码大小写不敏感 - 统一转为大写")
    void testCaseInsensitiveCode(@TempDir Path tempDir) throws IOException {
        List<ActivationCodeExcel> data = new ArrayList<>();
        data.add(createExcelData("lowercase001", "月", 30));
        data.add(createExcelData("MixedCase002", "季", 90));

        File testFile = createTestExcel(tempDir, data);

        when(mockMapper.selectByCode(anyString())).thenReturn(null);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, config);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(2, listener.getSuccessCount());
        assertEquals(0, listener.getFailCount());
    }

    @Test
    @DisplayName("测试错误信息数量限制")
    void testErrorMessageLimit(@TempDir Path tempDir) throws IOException {
        ActivationCodeImportConfig customConfig = new ActivationCodeImportConfig();
        customConfig.setMaxErrorMessages(3);

        List<ActivationCodeExcel> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(createExcelData("CODE" + i, "月", -1));
        }

        File testFile = createTestExcel(tempDir, data);

        ActivationCodeImportListener listener = new ActivationCodeImportListener(mockMapper, customConfig);
        EasyExcel.read(testFile, ActivationCodeExcel.class, listener).sheet().doRead();

        assertEquals(10, listener.getFailCount());
        assertTrue(listener.getErrorMessages().size() <= 4);
        assertTrue(listener.getErrorMessages().stream()
                .anyMatch(msg -> msg.contains("更多错误未显示")));
    }

    private ActivationCodeExcel createExcelData(String code, String type, Integer days) {
        ActivationCodeExcel excel = new ActivationCodeExcel();
        excel.setActivationCode(code);
        excel.setDurationTypeStr(type);
        excel.setDurationDays(days);
        return excel;
    }

    private File createTestExcel(Path tempDir, List<ActivationCodeExcel> data) throws IOException {
        File file = tempDir.resolve("test-activation-codes.xlsx").toFile();
        EasyExcel.write(file, ActivationCodeExcel.class).sheet("测试数据").doWrite(data);
        return file;
    }
}
