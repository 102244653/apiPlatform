package com.vantop.apitest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vantop.apitest.user.model.Department;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentMapper extends BaseMapper<Department> {
}
