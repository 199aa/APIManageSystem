package com.api.mapper;

import com.api.model.BlacklistWhitelist;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface BlacklistWhitelistMapper {
    List<BlacklistWhitelist> selectByType(@Param("listType") String listType);
    
    int deleteByType(@Param("listType") String listType);
    
    int batchInsert(@Param("list") List<BlacklistWhitelist> list);
}
