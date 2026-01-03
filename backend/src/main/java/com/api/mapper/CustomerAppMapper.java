package com.api.mapper;

import com.api.model.CustomerApp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CustomerAppMapper {
    
    /**
     * 插入应用
     */
    int insert(CustomerApp app);
    
    /**
     * 根据ID更新应用
     */
    int updateById(CustomerApp app);
    
    /**
     * 根据ID删除应用
     */
    int deleteById(Long id);
    
    /**
     * 根据ID查询应用
     */
    CustomerApp selectById(Long id);
    
    /**
     * 根据用户ID查询应用列表
     */
    List<CustomerApp> selectByUserId(Long userId);
    
    /**
     * 查询所有应用列表
     */
    List<CustomerApp> selectAll();
    
    /**
     * 根据应用Key查询应用
     */
    CustomerApp selectByAppKey(String appKey);
    
    /**
     * 更新应用状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
