package com.api.mapper;

import com.api.model.ApiInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ApiInfoMapper {

  List<ApiInfo> selectList(@Param("name") String name, @Param("method") String method);

  List<ApiInfo> selectPage(@Param("offset") int offset, @Param("limit") int limit,
      @Param("platformId") Long platformId,
      @Param("method") String method,
      @Param("name") String name);

  int countByCondition(@Param("platformId") Long platformId,
      @Param("method") String method,
      @Param("name") String name);

  ApiInfo selectById(@Param("id") Long id);

  int insert(ApiInfo apiInfo);

  int update(ApiInfo apiInfo);

  int deleteById(@Param("id") Long id);

  // 统计API总数
  int countTotal();

  // 统计聚合接口数
  int countAggregate();

  // 按状态统计API健康度
  Map<String, Object> countByStatus();

  // 根据路径和方法查询API（用于去重）
  ApiInfo selectByPathAndMethod(@Param("path") String path, @Param("method") String method);

  // 根据路径查询API
  ApiInfo selectByPath(@Param("path") String path);

  // 分页查询聚合接口
  List<ApiInfo> selectPageByAggregate(Map<String, Object> params);

  // 统计聚合接口数量
  int countByAggregate(Map<String, Object> params);
}
