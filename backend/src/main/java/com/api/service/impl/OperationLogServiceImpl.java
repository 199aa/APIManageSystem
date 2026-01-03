package com.api.service.impl;

import com.api.mapper.OperationLogMapper;
import com.api.model.OperationLog;
import com.api.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class OperationLogServiceImpl implements OperationLogService {

  @Autowired
  private OperationLogMapper operationLogMapper;

  @Override
  @Transactional
  public void saveLog(OperationLog log) {
    operationLogMapper.insert(log);
  }

  @Override
  public OperationLog getLogById(Long id) {
    return operationLogMapper.selectById(id);
  }

  @Override
  public Map<String, Object> getLogsByPage(Map<String, Object> params) {
    // 计算分页参数
    int page = params.get("page") != null ? (int) params.get("page") : 1;
    int pageSize = params.get("pageSize") != null ? (int) params.get("pageSize") : 20;
    int offset = (page - 1) * pageSize;

    params.put("offset", offset);
    params.put("limit", pageSize);

    List<OperationLog> logs = operationLogMapper.selectByPage(params);
    int total = operationLogMapper.countByCondition(params);

    Map<String, Object> result = new HashMap<>();
    result.put("list", logs);
    result.put("total", total);
    result.put("page", page);
    result.put("pageSize", pageSize);
    return result;
  }

  @Override
  @Transactional
  public void deleteBatch(List<Long> ids) {
    if (ids != null && !ids.isEmpty()) {
      operationLogMapper.deleteBatch(ids);
    }
  }

  @Override
  @Transactional
  public void cleanExpiredLogs(int days) {
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.DAY_OF_MONTH, -days);
    Date expiredDate = calendar.getTime();
    operationLogMapper.deleteBeforeDate(expiredDate);
  }
}
