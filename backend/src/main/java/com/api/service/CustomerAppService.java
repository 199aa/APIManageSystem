package com.api.service;

import com.api.model.CustomerApp;
import java.util.List;

public interface CustomerAppService {
    
    /**
     * 创建应用
     */
    CustomerApp createApp(CustomerApp app);
    
    /**
     * 更新应用
     */
    CustomerApp updateApp(CustomerApp app);
    
    /**
     * 删除应用
     */
    void deleteApp(Long id);
    
    /**
     * 根据ID查询应用
     */
    CustomerApp getAppById(Long id);
    
    /**
     * 根据用户ID查询应用列表
     */
    List<CustomerApp> getAppsByUserId(Long userId);
    
    /**
     * 查询所有应用
     */
    List<CustomerApp> getAllApps();
    
    /**
     * 更新应用状态
     */
    void updateAppStatus(Long id, Integer status);
    
    /**
     * 根据AppKey查询应用
     */
    CustomerApp getAppByAppKey(String appKey);
    
    /**
     * 重置应用密钥
     */
    CustomerApp resetAppSecret(Long id);
}
