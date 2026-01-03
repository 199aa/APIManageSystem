package com.api.mapper;

import com.api.model.ApiCallLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ApiCallLogMapper {

  int insert(ApiCallLog log);

  // 分页查询API调用日志
  List<ApiCallLog> selectList(Map<String, Object> params);

  // 统计符合条件的日志数量
  int countByCondition(Map<String, Object> params);

  // 获取最近的失败调用日志
  List<ApiCallLog> selectRecentErrors(@Param("limit") int limit);

  // 统计总调用量
  long countTotal();

  // 计算平均响应时间
  Double avgResponseTime();

  // 按小时统计调用量（最近24小时）
  List<Map<String, Object>> countByHour();

  // 按天统计调用量（最近7天）
  List<Map<String, Object>> countByDay(@Param("days") int days);

  // 按平台统计调用量
  List<Map<String, Object>> countByPlatform();

  // 统计今日调用量
  long countToday();

  // 统计今日成功调用量
  long countTodaySuccess();

  // 统计昨日调用量
  long countYesterday();

  // 统计昨日成功调用量
  long countYesterdaySuccess();

  // 获取响应最慢的Top N接口
  List<Map<String, Object>> selectSlowestApis(@Param("limit") int limit);

  // 获取错误率最高的Top N接口
  List<Map<String, Object>> selectHighestErrorApis(@Param("limit") int limit);
}
