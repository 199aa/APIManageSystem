package com.api.mapper;

import com.api.model.AlertRule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AlertRuleMapper {
    
    /**
     * 插入告警规则
     */
    int insert(AlertRule rule);
    
    /**
     * 更新告警规则
     */
    int updateById(AlertRule rule);
    
    /**
     * 删除告警规则
     */
    int deleteById(Long id);
    
    /**
     * 根据ID查询告警规则
     */
    AlertRule selectById(Long id);
    
    /**
     * 查询所有启用的告警规则
     */
    List<AlertRule> selectActiveRules();
    
    /**
     * 查询所有告警规则
     */
    List<AlertRule> selectAll();
    
    /**
     * 更新规则状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
