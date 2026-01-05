package com.api.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ApiAuthorizationMapper {

    /**
     * 插入授权记录
     */
    int insert(@Param("appId") Long appId, @Param("apiId") Long apiId);

    /**
     * 删除授权记录
     */
    int delete(@Param("appId") Long appId, @Param("apiId") Long apiId);

    /**
     * 删除应用的所有授权
     */
    int deleteByAppId(Long appId);

    /**
     * 获取应用已授权的API ID列表
     */
    List<Long> selectApiIdsByAppId(Long appId);

    /**
     * 检查授权是否存在
     */
    int countByAppIdAndApiId(@Param("appId") Long appId, @Param("apiId") Long apiId);
}
