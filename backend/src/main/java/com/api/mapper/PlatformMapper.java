package com.api.mapper;

import com.api.model.Platform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface PlatformMapper {

  List<Platform> selectAll();

  List<Platform> selectPage(@Param("offset") int offset, @Param("limit") int limit);

  Platform selectById(@Param("id") Long id);

  Platform selectByCode(@Param("code") String code);

  int countAll();

  int insert(Platform platform);

  int update(Platform platform);

  int deleteById(@Param("id") Long id);

  int updateStatus(@Param("id") Long id, @Param("status") Integer status);

  // 统计各平台API数量
  List<Map<String, Object>> countApiByPlatform();

  // 更新健康状态
  int updateHealthStatus(@Param("id") Long id, @Param("healthStatus") String healthStatus);

  // 查询所有启用的平台
  List<Platform> selectAllEnabled();

  // 统计异常平台数量
  int countOfflinePlatforms();
}
