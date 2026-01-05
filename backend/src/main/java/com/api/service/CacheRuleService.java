package com.api.service;

import com.api.mapper.CacheRuleMapper;
import com.api.model.CacheRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Set;

@Service
public class CacheRuleService {

    @Autowired
    private CacheRuleMapper cacheRuleMapper;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public List<CacheRule> getAll() {
        return cacheRuleMapper.selectAll();
    }

    public CacheRule getById(Long id) {
        return cacheRuleMapper.selectById(id);
    }

    @Transactional
    public void save(CacheRule cacheRule) {
        if (cacheRule.getId() == null) {
            cacheRuleMapper.insert(cacheRule);
        } else {
            cacheRuleMapper.update(cacheRule);
        }
    }

    @Transactional
    public void delete(Long id) {
        cacheRuleMapper.deleteById(id);
    }

    /**
     * 清除指定API的缓存
     */
    public void clearCache(Long apiId) {
        if (redisTemplate != null) {
            try {
                // 根据缓存规则的key模式清除缓存
                CacheRule rule = cacheRuleMapper.selectById(apiId);
                if (rule != null && rule.getCacheKey() != null) {
                    // 这里简化处理，实际应该根据cacheKey的模式匹配删除
                    String pattern = rule.getCacheKey().replace("${apiPath}", "*")
                            .replace("${userId}", "*")
                            .replace("${params.*}", "*");
                    Set<String> keys = redisTemplate.keys(pattern);
                    if (keys != null && !keys.isEmpty()) {
                        redisTemplate.delete(keys);
                    }
                }
            } catch (Exception e) {
                // Redis未配置或连接失败时忽略错误
                System.err.println("清除缓存失败: " + e.getMessage());
            }
        }
    }
}
