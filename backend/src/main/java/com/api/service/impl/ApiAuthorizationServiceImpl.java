package com.api.service.impl;

import com.api.mapper.ApiAuthorizationMapper;
import com.api.service.ApiAuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApiAuthorizationServiceImpl implements ApiAuthorizationService {
    
    @Autowired
    private ApiAuthorizationMapper apiAuthorizationMapper;
    
    @Override
    @Transactional
    public void authorize(Long appId, Long apiId) {
        // 检查是否已存在
        if (apiAuthorizationMapper.countByAppIdAndApiId(appId, apiId) == 0) {
            apiAuthorizationMapper.insert(appId, apiId);
        }
    }
    
    @Override
    @Transactional
    public void revoke(Long appId, Long apiId) {
        apiAuthorizationMapper.delete(appId, apiId);
    }
    
    @Override
    @Transactional
    public void batchSave(Long appId, List<Long> apiIds) {
        // 先删除该应用的所有授权
        apiAuthorizationMapper.deleteByAppId(appId);
        
        // 批量添加新授权
        if (apiIds != null && !apiIds.isEmpty()) {
            for (Long apiId : apiIds) {
                apiAuthorizationMapper.insert(appId, apiId);
            }
        }
    }
    
    @Override
    public List<Long> getAuthorizedApiIds(Long appId) {
        return apiAuthorizationMapper.selectApiIdsByAppId(appId);
    }
    
    @Override
    public boolean hasPermission(Long appId, Long apiId) {
        return apiAuthorizationMapper.countByAppIdAndApiId(appId, apiId) > 0;
    }
}
