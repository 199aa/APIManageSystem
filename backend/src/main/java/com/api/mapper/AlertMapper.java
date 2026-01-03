package com.api.mapper;

import com.api.model.Alert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Date;
import java.util.List;

@Mapper
public interface AlertMapper {
    
    /**
     * 插入告警
     */
    int insert(Alert alert);
    
    /**
     * 更新告警
     */
    int updateById(Alert alert);
    
    /**
     * 根据ID查询告警
     */
    Alert selectById(Long id);
    
    /**
     * 分页查询告警列表
     */
    List<Alert> selectPage(@Param("offset") int offset, 
                           @Param("pageSize") int pageSize,
                           @Param("level") String level,
                           @Param("status") String status,
                           @Param("startTime") Date startTime,
                           @Param("endTime") Date endTime);
    
    /**
     * 统计告警数量
     */
    int countByCondition(@Param("level") String level,
                        @Param("status") String status,
                        @Param("startTime") Date startTime,
                        @Param("endTime") Date endTime);
    
    /**
     * 获取活跃告警列表
     */
    List<Alert> selectActiveAlerts(@Param("limit") int limit);
    
    /**
     * 统计各级别告警数量
     */
    int countByLevel(@Param("level") String level, @Param("status") String status);
    
    /**
     * 批量确认告警
     */
    int batchAcknowledge(@Param("ids") List<Long> ids, 
                        @Param("acknowledgedBy") String acknowledgedBy,
                        @Param("acknowledgedTime") Date acknowledgedTime);
    
    /**
     * 更新告警状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
