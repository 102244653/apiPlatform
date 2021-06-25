package com.vantop.apitest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vantop.apitest.swagger.model.Swagger;
import org.springframework.stereotype.Repository;

@Repository
public interface SwaggerMapper extends BaseMapper<Swagger> {
}
