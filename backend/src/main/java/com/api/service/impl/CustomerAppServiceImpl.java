package com.api.service.impl;

import com.api.mapper.CustomerAppMapper;
import com.api.model.CustomerApp;
import com.api.service.CustomerAppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerAppServiceImpl implements CustomerAppService {
    
    @Autowired
    private CustomerAppMapper customerAppMapper;
    
    @Override
    @Transactional
    public CustomerApp createApp(CustomerApp app) {
        // 生成AppKey和AppSecret
        app.setAppKey(generateAppKey());
        app.setAppSecret(generateAppSecret());
        
        // 默认启用状态
        if (app.getStatus() == null) {
            app.setStatus(1);
        }
        
        customerAppMapper.insert(app);
        return app;
    }
    
    @Override
    @Transactional
    public CustomerApp updateApp(CustomerApp app) {
        customerAppMapper.updateById(app);
        return customerAppMapper.selectById(app.getId());
    }
    
    @Override
    @Transactional
    public void deleteApp(Long id) {
        customerAppMapper.deleteById(id);
    }
    
    @Override
    public CustomerApp getAppById(Long id) {
        return customerAppMapper.selectById(id);
    }
    
    @Override
    public List<CustomerApp> getAppsByUserId(Long userId) {
        return customerAppMapper.selectByUserId(userId);
    }
    
    @Override
    public List<CustomerApp> getAllApps() {
        return customerAppMapper.selectAll();
    }
    
    @Override
    @Transactional
    public void updateAppStatus(Long id, Integer status) {
        customerAppMapper.updateStatus(id, status);
    }
    
    @Override
    public CustomerApp getAppByAppKey(String appKey) {
        return customerAppMapper.selectByAppKey(appKey);
    }
    
    /**
     * 生成AppKey
     */
    private String generateAppKey() {
        return "app_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
    
    /**
     * 生成AppSecret
     */
    private String generateAppSecret() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    
    @Override
    @Transactional
    public CustomerApp resetAppSecret(Long id) {
        CustomerApp app = customerAppMapper.selectById(id);
        if (app == null) {
            return null;
        }
        
        // 生成新的密钥
        app.setAppSecret(generateAppSecret());
        customerAppMapper.updateById(app);
        
        return customerAppMapper.selectById(id);
    }
}
