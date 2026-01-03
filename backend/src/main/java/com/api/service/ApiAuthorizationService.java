package com.api.service;

import java.util.List;

public interface ApiAuthorizationService {
    
    /**
     * 为应用授权API
     */
    void authorize(Long appId, Long apiId);
    
    /**
     * 取消应用API授权
     */
    void revoke(Long appId, Long apiId);
    
    /**
     * 批量保存应用的API授权
     */
    void batchSave(Long appId, List<Long> apiIds);
    
    /**
     * 获取应用已授权的API ID列表
     */
    List<Long> getAuthorizedApiIds(Long appId);
    
    /**
     * 检查应用是否有某个API的权限
     */
    boolean hasPermission(Long appId, Long apiId);
}
