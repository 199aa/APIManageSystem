package com.api.service;

import com.api.mapper.ApiCallLogMapper;
import com.api.mapper.ApiInfoMapper;
import com.api.mapper.PlatformMapper;
import com.api.model.Alert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private ApiInfoMapper apiInfoMapper;

    @Autowired
    private ApiCallLogMapper apiCallLogMapper;

    @Autowired
    private PlatformMapper platformMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AlertService alertService;

    private static final String REDIS_KEY_TOTAL_CALLS = "dashboard:total_calls";
    private static final String REDIS_KEY_TODAY_CALLS = "dashboard:today_calls:";
    private static final String REDIS_KEY_TODAY_SUCCESS = "dashboard:today_success:";

    /**
     * 获取核心统计指标
     */
    public Map<String, Object> getCoreStats() {
        Map<String, Object> stats = new HashMap<>();

        // API总数
        int apiTotal = apiInfoMapper.countTotal();
        stats.put("apiTotal", apiTotal);

        // 聚合接口数
        int aggregateTotal = apiInfoMapper.countAggregate();
        stats.put("aggregateTotal", aggregateTotal);

        // 接入平台数
        int platformTotal = platformMapper.countAll();
        stats.put("platformTotal", platformTotal);

        // 异常平台数
        int offlinePlatforms = platformMapper.countOfflinePlatforms();
        stats.put("offlinePlatforms", offlinePlatforms);

        // 累计调用量 - 优先从Redis读取
        long callTotal = getCallTotalFromRedis();
        stats.put("callTotal", formatNumber(callTotal));
        stats.put("callTotalRaw", callTotal);

        // 今日调用量 - 优先从Redis读取
        long todayCall = getTodayCallFromRedis();
        stats.put("todayCall", formatNumber(todayCall));
        stats.put("todayCallRaw", todayCall);

        // 今日成功调用量 - 优先从Redis读取
        long todaySuccess = getTodaySuccessFromRedis();
        stats.put("todaySuccess", todaySuccess);

        // 计算今日成功率
        double todaySuccessRate = todayCall > 0 ? (todaySuccess * 100.0 / todayCall) : 0;
        stats.put("successRate", String.format("%.1f%%", todaySuccessRate));

        // 昨日调用量（用于计算趋势） - 优先从Redis读取
        long yesterdayCall = getYesterdayCallFromRedis();
        long yesterdaySuccess = getYesterdaySuccessFromRedis();

        // 计算今日调用量趋势
        double todayCallTrend = calculateTrend(todayCall, yesterdayCall);
        stats.put("todayCallTrend", todayCallTrend);

        // 计算成功率趋势
        double yesterdaySuccessRate = yesterdayCall > 0 ? (yesterdaySuccess * 100.0 / yesterdayCall) : 0;
        double successRateTrend = todaySuccessRate - yesterdaySuccessRate;
        stats.put("successRateTrend", Double.parseDouble(String.format("%.1f", successRateTrend)));

        // 平均响应时间
        Double avgTime = apiCallLogMapper.avgResponseTime();
        int avgTimeValue = (avgTime != null ? avgTime.intValue() : 0);
        stats.put("avgResponseTime", avgTimeValue + "ms");
        stats.put("avgResponseTimeRaw", avgTimeValue);

        // 活跃告警数 - 从数据库查询真实告警
        List<Alert> activeAlerts = alertService.getActiveAlerts(100);
        int activeAlertCount = activeAlerts.size();
        stats.put("activeAlerts", activeAlertCount);

        // API总数趋势（暂时设为固定值，后续可扩展）
        stats.put("apiTotalTrend", 0);

        // 平均响应时间趋势（暂时设为负值表示改善，后续可扩展）
        stats.put("avgTimeTrend", -5.0);

        return stats;
    }

    /**
     * 获取调用趋势数据
     */
    public Map<String, Object> getCallTrend(String range) {
        Map<String, Object> data = new HashMap<>();
        List<String> labels = new java.util.ArrayList<>();
        List<Integer> values = new java.util.ArrayList<>();
        List<Integer> successValues = new java.util.ArrayList<>();

        if ("24h".equals(range)) {
            // 24小时数据
            List<Map<String, Object>> result = apiCallLogMapper.countByHour();
            Map<String, Integer> hourMap = new HashMap<>();
            for (Map<String, Object> item : result) {
                hourMap.put((String) item.get("label"), ((Number) item.get("value")).intValue());
            }

            // 填充完整的24小时数据
            for (int i = 0; i < 24; i++) {
                String hour = String.format("%02d:00", i);
                labels.add(hour);
                int count = hourMap.getOrDefault(hour, 0);
                values.add(count);
                // 假设成功率95%
                successValues.add((int) (count * 0.95));
            }
        } else if ("7d".equals(range)) {
            // 7天数据
            List<Map<String, Object>> result = apiCallLogMapper.countByDay(7);
            for (Map<String, Object> item : result) {
                labels.add((String) item.get("label"));
                int count = ((Number) item.get("value")).intValue();
                values.add(count);
                successValues.add((int) (count * 0.95));
            }
        } else if ("30d".equals(range)) {
            // 30天数据
            List<Map<String, Object>> result = apiCallLogMapper.countByDay(30);
            for (Map<String, Object> item : result) {
                labels.add((String) item.get("label"));
                int count = ((Number) item.get("value")).intValue();
                values.add(count);
                successValues.add((int) (count * 0.95));
            }
        }

        data.put("labels", labels);
        data.put("values", values);
        data.put("successValues", successValues);
        return data;
    }

    /**
     * 获取响应最慢的API列表
     */
    public List<Map<String, Object>> getSlowestApis(int limit) {
        return apiCallLogMapper.selectSlowestApis(limit);
    }

    /**
     * 获取错误率最高的API列表
     */
    public List<Map<String, Object>> getHighestErrorApis(int limit) {
        return apiCallLogMapper.selectHighestErrorApis(limit);
    }

    /**
     * 获取活跃告警列表
     * 从数据库查询真实的告警记录
     */
    public List<Map<String, Object>> getActiveAlerts(int limit) {
        // 使用AlertService获取真实的告警数据
        List<Alert> alerts = alertService.getActiveAlerts(limit);
        List<Map<String, Object>> result = new java.util.ArrayList<>();

        for (Alert alert : alerts) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", alert.getId());
            item.put("level", alert.getLevel());
            item.put("content", alert.getContent());
            item.put("time", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(alert.getAlertTime()));
            item.put("status", alert.getStatus());
            result.add(item);
        }

        return result;
    }

    /**
     * 计算趋势百分比
     */
    private double calculateTrend(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100.0 : 0;
        }
        double trend = ((current - previous) * 100.0) / previous;
        return Double.parseDouble(String.format("%.1f", trend));
    }

    /**
     * 格式化数字
     */
    private String formatNumber(long num) {
        if (num >= 1000000) {
            return String.format("%.2fM", num / 1000000.0);
        } else if (num >= 1000) {
            return String.format("%.1fK", num / 1000.0);
        }
        return String.valueOf(num);
    }

    /**
     * 从Redis获取累计调用总数，如果Redis中没有则从数据库查询并缓存
     */
    private long getCallTotalFromRedis() {
        try {
            Object value = redisTemplate.opsForValue().get(REDIS_KEY_TOTAL_CALLS);
            if (value != null) {
                return Long.parseLong(value.toString());
            }
            // Redis中没有数据，从数据库查询并缓存
            long count = apiCallLogMapper.countTotal();
            redisTemplate.opsForValue().set(REDIS_KEY_TOTAL_CALLS, count);
            return count;
        } catch (Exception e) {
            // Redis操作失败，降级到数据库查询
            return apiCallLogMapper.countTotal();
        }
    }

    /**
     * 从Redis获取今日调用数
     */
    private long getTodayCallFromRedis() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String key = REDIS_KEY_TODAY_CALLS + today;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Long.parseLong(value.toString());
            }
            // Redis中没有数据，从数据库查询
            return apiCallLogMapper.countToday();
        } catch (Exception e) {
            return apiCallLogMapper.countToday();
        }
    }

    /**
     * 从Redis获取今日成功调用数
     */
    private long getTodaySuccessFromRedis() {
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String key = REDIS_KEY_TODAY_SUCCESS + today;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Long.parseLong(value.toString());
            }
            // Redis中没有数据，从数据库查询
            return apiCallLogMapper.countTodaySuccess();
        } catch (Exception e) {
            return apiCallLogMapper.countTodaySuccess();
        }
    }

    /**
     * 从Redis获取昨日调用数
     */
    private long getYesterdayCallFromRedis() {
        try {
            String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String key = REDIS_KEY_TODAY_CALLS + yesterday;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Long.parseLong(value.toString());
            }
            // Redis中没有数据，从数据库查询
            return apiCallLogMapper.countYesterday();
        } catch (Exception e) {
            return apiCallLogMapper.countYesterday();
        }
    }

    /**
     * 从Redis获取昨日成功调用数
     */
    private long getYesterdaySuccessFromRedis() {
        try {
            String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String key = REDIS_KEY_TODAY_SUCCESS + yesterday;
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return Long.parseLong(value.toString());
            }
            // Redis中没有数据，从数据库查询
            return apiCallLogMapper.countYesterdaySuccess();
        } catch (Exception e) {
            return apiCallLogMapper.countYesterdaySuccess();
        }
    }
}
