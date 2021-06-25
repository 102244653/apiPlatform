package com.vantop.apitest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vantop.apitest.report.model.Report;
import org.springframework.stereotype.Repository;

@Repository

public interface ReportMapper extends BaseMapper<Report> {
}
