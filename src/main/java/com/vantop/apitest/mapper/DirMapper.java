package com.vantop.apitest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vantop.apitest.system.model.ConfigKind;
import com.vantop.apitest.system.model.Dir;
import org.springframework.stereotype.Repository;

@Repository

public interface DirMapper extends BaseMapper<Dir> {
}
