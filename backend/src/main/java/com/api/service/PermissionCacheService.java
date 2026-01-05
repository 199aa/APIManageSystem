package com.api.service;

import com.api.model.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 权限缓存服务
 * 使用Redis缓存用户权限，提高查询性能
 */
@Service
public class PermissionCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private PermissionService permissionService;

    private static final String PERMISSION_KEY_PREFIX = "user:permissions:";
    private static final String ROLE_PERMISSION_KEY_PREFIX = "role:permissions:";
    private static final long CACHE_EXPIRE_HOURS = 24; // 缓存24小时

    /**
     * 获取角色权限（带缓存）
     */
    public List<Permission> getRolePermissions(Long roleId) {
        String key = ROLE_PERMISSION_KEY_PREFIX + roleId;
        
        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<Permission> permissions = (List<Permission>) redisTemplate.opsForValue().get(key);
        
        if (permissions != null && !permissions.isEmpty()) {
            return permissions;
        }
        
        // 缓存未命中，从数据库查询
        permissions = permissionService.getPermissionsByRoleId(roleId);
        
        // 写入缓存
        if (permissions != null && !permissions.isEmpty()) {
            redisTemplate.opsForValue().set(key, permissions, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } else {
            permissions = new ArrayList<>();
        }
        
        return permissions;
    }

    /**
     * 清除用户权限缓存
     * 在修改角色权限时调用
     */
    public void clearUserPermissionCache(Long userId) {
        String key = PERMISSION_KEY_PREFIX + userId;
        redisTemplate.delete(key);
    }
    
    /**
     * 清除角色权限缓存
     */
    public void clearRolePermissionCache(Long roleId) {
        String key = ROLE_PERMISSION_KEY_PREFIX + roleId;
        redisTemplate.delete(key);
    }

    /**
     * 清除所有权限缓存
     * 在批量修改权限时调用
     */
    public void clearAllPermissionCache() {
        redisTemplate.delete(redisTemplate.keys(PERMISSION_KEY_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(ROLE_PERMISSION_KEY_PREFIX + "*"));
    }

    /**
     * 刷新角色权限缓存
     */
    public void refreshRolePermissionCache(Long roleId) {
        clearRolePermissionCache(roleId);
        getRolePermissions(roleId);
    }
}
