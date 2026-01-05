package com.api.mapper;

import com.api.model.RateLimit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RateLimitMapper {
    List<RateLimit> selectAll();

    RateLimit selectById(@Param("id") Long id);

    int insert(RateLimit rateLimit);

    int update(RateLimit rateLimit);

    int deleteById(@Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
