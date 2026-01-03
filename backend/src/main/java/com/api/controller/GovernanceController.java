package com.api.controller;

import com.api.common.Result;
import com.api.model.RateLimit;
import com.api.model.CacheRule;
import com.api.service.RateLimitService;
import com.api.service.BlacklistWhitelistService;
import com.api.service.CacheRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 治理中心Controller - 限流、黑白名单、缓存策略
 */
@RestController
@RequestMapping("/governance")
@CrossOrigin
public class GovernanceController {

    @Autowired
    private RateLimitService rateLimitService;

    @Autowired
    private BlacklistWhitelistService blacklistWhitelistService;

    @Autowired
    private CacheRuleService cacheRuleService;

    // ==================== 限流策略 ====================

    /**
     * 获取所有限流规则
     */
    @GetMapping("/rateLimit/list")
    public Result<List<RateLimit>> getRateLimitList() {
        try {
            List<RateLimit> list = rateLimitService.getAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取限流规则失败: " + e.getMessage());
        }
    }

    /**
     * 保存限流规则（新增或更新）
     */
    @PostMapping("/rateLimit/save")
    public Result<String> saveRateLimit(@RequestBody RateLimit rateLimit) {
        try {
            if (rateLimit.getStatus() == null) {
                rateLimit.setStatus(1); // 默认启用
            }
            rateLimitService.save(rateLimit);
            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除限流规则
     */
    @DeleteMapping("/rateLimit/delete/{id}")
    public Result<String> deleteRateLimit(@PathVariable Long id) {
        try {
            rateLimitService.delete(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 更新限流规则状态
     */
    @PutMapping("/rateLimit/status/{id}/{status}")
    public Result<String> updateRateLimitStatus(@PathVariable Long id, @PathVariable Integer status) {
        try {
            rateLimitService.updateStatus(id, status);
            return Result.success("状态更新成功");
        } catch (Exception e) {
            return Result.error("状态更新失败: " + e.getMessage());
        }
    }

    // ==================== 黑白名单 ====================

    /**
     * 获取黑白名单
     */
    @GetMapping("/blacklist/get")
    public Result<Map<String, Object>> getBlacklist() {
        try {
            Map<String, Object> result = new HashMap<>();
            List<String> blacklist = blacklistWhitelistService.getBlacklist();
            List<String> whitelist = blacklistWhitelistService.getWhitelist();
            result.put("blacklist", blacklist);
            result.put("whitelist", whitelist);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取黑白名单失败: " + e.getMessage());
        }
    }

    /**
     * 保存黑名单
     */
    @PostMapping("/blacklist/saveBlacklist")
    public Result<String> saveBlacklist(@RequestBody List<String> ips) {
        try {
            blacklistWhitelistService.saveBlacklist(ips);
            return Result.success("黑名单保存成功");
        } catch (Exception e) {
            return Result.error("黑名单保存失败: " + e.getMessage());
        }
    }

    /**
     * 保存白名单
     */
    @PostMapping("/blacklist/saveWhitelist")
    public Result<String> saveWhitelist(@RequestBody List<String> ips) {
        try {
            blacklistWhitelistService.saveWhitelist(ips);
            return Result.success("白名单保存成功");
        } catch (Exception e) {
            return Result.error("白名单保存失败: " + e.getMessage());
        }
    }

    // ==================== 缓存策略 ====================

    /**
     * 获取所有缓存规则
     */
    @GetMapping("/cache/list")
    public Result<List<CacheRule>> getCacheRuleList() {
        try {
            List<CacheRule> list = cacheRuleService.getAll();
            return Result.success(list);
        } catch (Exception e) {
            return Result.error("获取缓存规则失败: " + e.getMessage());
        }
    }

    /**
     * 保存缓存规则（新增或更新）
     */
    @PostMapping("/cache/save")
    public Result<String> saveCacheRule(@RequestBody CacheRule cacheRule) {
        try {
            cacheRuleService.save(cacheRule);
            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 删除缓存规则
     */
    @DeleteMapping("/cache/delete/{id}")
    public Result<String> deleteCacheRule(@PathVariable Long id) {
        try {
            cacheRuleService.delete(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 清除指定API的缓存
     */
    @PostMapping("/cache/clear/{id}")
    public Result<String> clearCache(@PathVariable Long id) {
        try {
            cacheRuleService.clearCache(id);
            return Result.success("缓存已清除");
        } catch (Exception e) {
            return Result.error("缓存清除失败: " + e.getMessage());
        }
    }
}
