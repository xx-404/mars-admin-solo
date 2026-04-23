package com.mars.biz.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "activation-code.import")
public class ActivationCodeImportConfig {

    private int maxFileSizeMB = 5;
    private int maxRows = 1000;
    private int maxErrorMessages = 50;
    private int minDurationDays = 1;
    private int maxDurationDays = 10000;
    private int maxCodeLength = 32;
    private String codePattern = "^[A-Z0-9]+$";
    private int batchSize = 100;

    public long getMaxFileSizeBytes() {
        return (long) maxFileSizeMB * 1024 * 1024;
    }
}
