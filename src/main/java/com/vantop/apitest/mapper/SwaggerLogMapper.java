package com.vantop.apitest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vantop.apitest.swagger.model.SwaggerLog;
import org.springframework.stereotype.Repository;

@Repository
public interface SwaggerLogMapper extends BaseMapper<SwaggerLog> {
}
