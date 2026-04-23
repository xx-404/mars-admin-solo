package com.mars.biz.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mars.biz.entity.ActivationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ActivationCodeMapper extends BaseMapper<ActivationCode> {

    @Select("SELECT * FROM activation_code WHERE activation_code = #{code}")
    ActivationCode selectByCode(@Param("code") String code);
}
