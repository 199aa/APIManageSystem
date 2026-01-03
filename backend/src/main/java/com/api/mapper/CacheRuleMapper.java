package com.api.mapper;

import com.api.model.CacheRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CacheRuleMapper {
    List<CacheRule> selectAll();
    
    CacheRule selectById(@Param("id") Long id);
    
    int insert(CacheRule cacheRule);
    
    int update(CacheRule cacheRule);
    
    int deleteById(@Param("id") Long id);
}
