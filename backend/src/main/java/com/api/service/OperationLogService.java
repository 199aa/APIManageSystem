package com.api.service;

import com.api.model.OperationLog;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface OperationLogService {

  /**
   * 记录操作日志
   */
  void saveLog(OperationLog log);

  /**
   * 根据ID查询日志
   */
  OperationLog getLogById(Long id);

  /**
   * 分页查询日志
   */
  Map<String, Object> getLogsByPage(Map<String, Object> params);

  /**
   * 批量删除日志
   */
  void deleteBatch(List<Long> ids);

  /**
   * 清理过期日志
   */
  void cleanExpiredLogs(int days);
}
