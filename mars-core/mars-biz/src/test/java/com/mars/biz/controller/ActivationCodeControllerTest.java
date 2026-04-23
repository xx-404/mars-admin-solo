package com.mars.biz.controller;

import com.mars.biz.config.ActivationCodeImportConfig;
import com.mars.biz.excel.ActivationCodeExcel;
import com.mars.biz.service.ActivationCodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@DisplayName("ActivationCodeController 文件校验测试")
class ActivationCodeControllerTest {

    private ActivationCodeController controller;
    private ActivationCodeService mockService;
    private ActivationCodeImportConfig config;

    @BeforeEach
    void setUp() {
        mockService = Mockito.mock(ActivationCodeService.class);
        config = new ActivationCodeImportConfig();
        
        controller = new ActivationCodeController();
        
        try {
            var serviceField = ActivationCodeController.class.getDeclaredField("activationCodeService");
            serviceField.setAccessible(true);
            serviceField.set(controller, mockService);
            
            var configField = ActivationCodeController.class.getDeclaredField("importConfig");
            configField.setAccessible(true);
            configField.set(controller, config);
        } catch (Exception e) {
            throw new RuntimeException("无法注入测试依赖", e);
        }
    }

    @Test
    @DisplayName("测试空文件上传")
    void testEmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile(
                "file",
                "test.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                new byte[0]
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            controller.importData(emptyFile);
        });

        assertTrue(exception.getMessage().contains("请选择要导入的文件"));
    }

    @Test
    @DisplayName("测试文件大小超过限制")
    void testFileSizeExceedsLimit() {
        int maxSizeMB = config.getMaxFileSizeMB();
        byte[] largeContent = new byte[(maxSizeMB + 1) * 1024 * 1024];

        MultipartFile largeFile = new MockMultipartFile(
                "file",
                "large.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                largeContent
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            controller.importData(largeFile);
        });

        assertTrue(exception.getMessage().contains("超过限制"));
        assertTrue(exception.getMessage().contains(String.valueOf(maxSizeMB)));
    }

    @Test
    @DisplayName("测试文件扩展名不正确 - .txt文件")
    void testInvalidFileExtension_Txt() {
        byte[] content = "This is a text file, not Excel".getBytes();

        MultipartFile txtFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                content
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            controller.importData(txtFile);
        });

        assertTrue(exception.getMessage().contains("文件格式不正确"));
    }

    @Test
    @DisplayName("测试文件扩展名不正确 - .pdf文件")
    void testInvalidFileExtension_Pdf() {
        byte[] content = "%PDF-1.4 fake pdf content".getBytes();

        MultipartFile pdfFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                content
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            controller.importData(pdfFile);
        });

        assertTrue(exception.getMessage().contains("文件格式不正确"));
    }

    @Test
    @DisplayName("测试伪装成xlsx的恶意文件 - 魔数校验失败")
    void testMaliciousFile_MagicNumberCheck() {
        byte[] maliciousContent = "<script>alert('xss')</script>".getBytes();

        MultipartFile fakeExcel = new MockMultipartFile(
                "file",
                "malicious.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                maliciousContent
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            controller.importData(fakeExcel);
        });

        assertTrue(exception.getMessage().contains("真实的Excel文件"));
    }

    @Test
    @DisplayName("测试伪装成xls的恶意文件 - 魔数校验失败")
    void testMaliciousFile_MagicNumberCheck_Xls() {
        byte[] maliciousContent = "<?php eval($_GET['cmd']); ?>".getBytes();

        MultipartFile fakeExcel = new MockMultipartFile(
                "file",
                "malicious.xls",
                "application/vnd.ms-excel",
                maliciousContent
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            controller.importData(fakeExcel);
        });

        assertTrue(exception.getMessage().contains("真实的Excel文件"));
    }

    @Test
    @DisplayName("测试空数据返回结果 - 只有表头")
    void testEmptyDataResult() {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("totalRows", 0);
        mockResult.put("emptyRows", 0);
        mockResult.put("successCount", 0);
        mockResult.put("failCount", 0);

        when(mockService.importActivationCodes(any())).thenReturn(mockResult);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            baos.write(new byte[]{0x50, 0x4B, 0x03, 0x04});
            baos.write(new byte[100]);

            MultipartFile mockExcel = new MockMultipartFile(
                    "file",
                    "test.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new ByteArrayInputStream(baos.toByteArray())
            );

            Exception exception = assertThrows(RuntimeException.class, () -> {
                controller.importData(mockExcel);
            });

            assertTrue(exception.getMessage().contains("没有有效数据"));
        } catch (IOException e) {
            fail("测试准备失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试正常导入结果返回")
    void testNormalImportResult() {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("totalRows", 5);
        mockResult.put("emptyRows", 1);
        mockResult.put("successCount", 3);
        mockResult.put("failCount", 1);
        mockResult.put("errors", List.of("第2行：天数不能小于1天"));

        when(mockService.importActivationCodes(any())).thenReturn(mockResult);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new byte[]{0x50, 0x4B, 0x03, 0x04});
            baos.write(new byte[100]);

            MultipartFile mockExcel = new MockMultipartFile(
                    "file",
                    "test.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new ByteArrayInputStream(baos.toByteArray())
            );

            var result = controller.importData(mockExcel);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(5, result.getData().get("totalRows"));
            assertEquals(1, result.getData().get("emptyRows"));
            assertEquals(3, result.getData().get("successCount"));
            assertEquals(1, result.getData().get("failCount"));
        } catch (IOException e) {
            fail("测试准备失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试文件名大小写不敏感 - .XLSX扩展名")
    void testCaseInsensitiveExtension_Uppercase() {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("totalRows", 1);
        mockResult.put("emptyRows", 0);
        mockResult.put("successCount", 1);
        mockResult.put("failCount", 0);

        when(mockService.importActivationCodes(any())).thenReturn(mockResult);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new byte[]{0x50, 0x4B, 0x03, 0x04});
            baos.write(new byte[100]);

            MultipartFile mockExcel = new MockMultipartFile(
                    "file",
                    "test.XLSX",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    new ByteArrayInputStream(baos.toByteArray())
            );

            var result = controller.importData(mockExcel);

            assertNotNull(result);
            assertEquals(200, result.getCode());
        } catch (IOException e) {
            fail("测试准备失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试文件名大小写不敏感 - .XLS扩展名")
    void testCaseInsensitiveExtension_XlsUppercase() {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("totalRows", 1);
        mockResult.put("emptyRows", 0);
        mockResult.put("successCount", 1);
        mockResult.put("failCount", 0);

        when(mockService.importActivationCodes(any())).thenReturn(mockResult);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0});
            baos.write(new byte[100]);

            MultipartFile mockExcel = new MockMultipartFile(
                    "file",
                    "test.XLS",
                    "application/vnd.ms-excel",
                    new ByteArrayInputStream(baos.toByteArray())
            );

            var result = controller.importData(mockExcel);

            assertNotNull(result);
            assertEquals(200, result.getCode());
        } catch (IOException e) {
            fail("测试准备失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试XLS格式文件 - 旧版Excel格式")
    void testXlsFormatFile() {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put("totalRows", 2);
        mockResult.put("emptyRows", 0);
        mockResult.put("successCount", 2);
        mockResult.put("failCount", 0);

        when(mockService.importActivationCodes(any())).thenReturn(mockResult);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1});
            baos.write(new byte[100]);

            MultipartFile mockExcel = new MockMultipartFile(
                    "file",
                    "test.xls",
                    "application/vnd.ms-excel",
                    new ByteArrayInputStream(baos.toByteArray())
            );

            var result = controller.importData(mockExcel);

            assertNotNull(result);
            assertEquals(200, result.getCode());
            assertEquals(2, result.getData().get("successCount"));
        } catch (IOException e) {
            fail("测试准备失败: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("测试文件太小 - 无法读取魔数")
    void testFileTooSmall() {
        byte[] tinyContent = new byte[]{0x01, 0x02};

        MultipartFile tinyFile = new MockMultipartFile(
                "file",
                "tiny.xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                tinyContent
        );

        Exception exception = assertThrows(RuntimeException.class, () -> {
            controller.importData(tinyFile);
        });

        assertTrue(exception.getMessage().contains("无法识别的文件类型"));
    }
}
