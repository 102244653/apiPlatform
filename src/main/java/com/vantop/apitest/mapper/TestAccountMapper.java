package com.vantop.apitest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vantop.apitest.user.model.TestAccount;
import org.springframework.stereotype.Repository;

@Repository
public interface TestAccountMapper extends BaseMapper<TestAccount> {
}
