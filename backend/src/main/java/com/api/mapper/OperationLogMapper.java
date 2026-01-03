package com.api.mapper;

import com.api.model.OperationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface OperationLogMapper {

  /**
   * 插入操作日志
   */
  int insert(OperationLog log);

  /**
   * 根据ID查询日志
   */
  OperationLog selectById(@Param("id") Long id);

  /**
   * 分页查询日志（支持条件查询）
   */
  List<OperationLog> selectByPage(@Param("params") Map<String, Object> params);

  /**
   * 查询日志总数（支持条件查询）
   */
  int countByCondition(@Param("params") Map<String, Object> params);

  /**
   * 批量删除日志
   */
  int deleteBatch(@Param("ids") List<Long> ids);

  /**
   * 删除指定日期之前的日志
   */
  int deleteBeforeDate(@Param("date") Date date);
}
