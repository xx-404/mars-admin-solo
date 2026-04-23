package com.mars.biz.excel;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

@Data
@ExcelIgnoreUnannotated
public class ActivationCodeExcel {

    @ExcelProperty("激活码")
    @ColumnWidth(25)
    private String activationCode;

    @ExcelProperty("类型")
    @ColumnWidth(15)
    private String durationTypeStr;

    @ExcelProperty("天数")
    @ColumnWidth(10)
    private Integer durationDays;

    private String durationType;
}
