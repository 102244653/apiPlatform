package com.vantop.apitest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vantop.apitest.swagger.model.BlackList;
import com.vantop.apitest.system.model.DBConfig;
import org.springframework.stereotype.Repository;

@Repository
public interface DBConfigMapper extends BaseMapper<DBConfig> {

}
