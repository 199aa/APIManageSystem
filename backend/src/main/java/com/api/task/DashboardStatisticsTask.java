package com.api.task;

import com.api.mapper.ApiCallLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Dashboard数据汇总定时任务
 * 定期缓存统计数据到Redis，减少数据库查询压力
 * Redis不可用时自动跳过缓存操作，不影响系统运行
 */
@Component
public class DashboardStatisticsTask {

    @Autowired
    private ApiCallLogMapper apiCallLogMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_TREND_24H = "dashboard:trend:24h";
    private static final String REDIS_KEY_TREND_7D = "dashboard:trend:7d";
    private static final String REDIS_KEY_TREND_30D = "dashboard:trend:30d";
    
    private boolean redisAvailable = true;  // Redis可用性标记

    /**
     * 每小时执行一次，汇总趋势数据
     * cron表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void summarizeTrendData() {
        if (!redisAvailable) {
            return;  // Redis不可用，跳过
        }
        
        System.out.println("开始汇总趋势数据...");
        
        try {
            // 汇总24小时数据
            List<Map<String, Object>> hourData = apiCallLogMapper.countByHour();
            redisTemplate.opsForValue().set(REDIS_KEY_TREND_24H, hourData, 2, TimeUnit.HOURS);
            
            // 汇总7天数据
            List<Map<String, Object>> sevenDayData = apiCallLogMapper.countByDay(7);
            redisTemplate.opsForValue().set(REDIS_KEY_TREND_7D, sevenDayData, 1, TimeUnit.DAYS);
            
            // 汇总30天数据
            List<Map<String, Object>> thirtyDayData = apiCallLogMapper.countByDay(30);
            redisTemplate.opsForValue().set(REDIS_KEY_TREND_30D, thirtyDayData, 1, TimeUnit.DAYS);
            
            System.out.println("趋势数据汇总完成");
        } catch (Exception e) {
            System.err.println("趋势数据汇总失败: " + e.getMessage());
            redisAvailable = false;  // 标记Redis不可用
        }
    }

    /**
     * 每30分钟同步一次Redis总计数与数据库
     * 避免Redis数据丢失导致统计不准确
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void syncTotalCallsToRedis() {
        if (!redisAvailable) {
            return;  // Redis不可用，跳过
        }
        
        System.out.println("开始同步累计调用量到Redis...");
        
        try {
            long totalCalls = apiCallLogMapper.countTotal();
            redisTemplate.opsForValue().set("dashboard:total_calls", totalCalls);
            System.out.println("累计调用量同步完成: " + totalCalls);
        } catch (Exception e) {
            System.err.println("同步累计调用量失败: " + e.getMessage());
            redisAvailable = false;  // 标记Redis不可用
        }
    }

    /**
     * 每天凌晨1点，清理过期的Redis计数器
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void cleanupExpiredCounters() {
        if (!redisAvailable) {
            return;  // Redis不可用，跳过
        }
        
        System.out.println("开始清理过期的Redis计数器...");
        
        try {
            // 清理3天前的数据
            LocalDate threeDaysAgo = LocalDate.now().minusDays(3);
            String date = threeDaysAgo.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            String todayCallKey = "dashboard:today_calls:" + date;
            String todaySuccessKey = "dashboard:today_success:" + date;
            
            redisTemplate.delete(todayCallKey);
            redisTemplate.delete(todaySuccessKey);
            
            System.out.println("过期计数器清理完成");
        } catch (Exception e) {
            System.err.println("清理过期计数器失败: " + e.getMessage());
            redisAvailable = false;  // 标记Redis不可用
        }
    }

    /**
     * 启动时延迟10秒后执行一次数据初始化
     */
    @Scheduled(initialDelay = 10000, fixedDelay = Long.MAX_VALUE)
    public void initializeData() {
        System.out.println("初始化Dashboard统计数据...");
        
        try {
            // 测试Redis连接
            redisTemplate.opsForValue().get("test:connection");
            redisAvailable = true;
            
            // 同步总调用量
            syncTotalCallsToRedis();
            
            // 汇总趋势数据
            summarizeTrendData();
            
            System.out.println("Dashboard统计数据初始化完成");
        } catch (Exception e) {
            System.err.println("Redis不可用，将使用数据库模式: " + e.getMessage());
            redisAvailable = false;
        }
    }
}
