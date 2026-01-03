package com.api.controller;

import com.api.common.Result;
import com.api.mapper.ApiInfoMapper;
import com.api.model.ApiInfo;
import com.api.service.AggregateExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 聚合接口管理Controller
 */
@RestController
@RequestMapping("/aggregate")
public class AggregateController {

    @Autowired
    private ApiInfoMapper apiInfoMapper;

    @Autowired
    private AggregateExecutionService aggregateExecutionService;

    /**
     * 获取聚合接口列表
     */
    @GetMapping("/list")
    public Result list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name
    ) {
        try {
            int offset = (page - 1) * pageSize;
            
            // 查询聚合接口 (is_aggregate = 1)
            Map<String, Object> params = new HashMap<>();
            params.put("offset", offset);
            params.put("limit", pageSize);
            params.put("is_aggregate", 1);
            if (name != null && !name.isEmpty()) {
                params.put("name", name);
            }

            List<ApiInfo> list = apiInfoMapper.selectPageByAggregate(params);
            int total = apiInfoMapper.countByAggregate(params);

            Map<String, Object> result = new HashMap<>();
            result.put("list", list);
            result.put("total", total);
            result.put("page", page);
            result.put("pageSize", pageSize);

            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 获取聚合接口详情
     */
    @GetMapping("/{id}")
    public Result detail(@PathVariable Long id) {
        try {
            ApiInfo apiInfo = apiInfoMapper.selectById(id);
            if (apiInfo == null || apiInfo.getIsAggregate() != 1) {
                return Result.error("聚合接口不存在");
            }
            return Result.success(apiInfo);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 创建聚合接口
     */
    @PostMapping("/create")
    public Result create(@RequestBody ApiInfo apiInfo) {
        try {
            // 设置基本信息
            apiInfo.setIsAggregate(1);
            apiInfo.setMethod("POST"); // 聚合接口统一使用POST
            apiInfo.setStatus(0); // 默认草稿状态
            
            if (apiInfo.getPlatformId() == null) {
                apiInfo.setPlatformId(0L); // 聚合接口不属于特定平台
            }

            apiInfoMapper.insert(apiInfo);
            return Result.success(apiInfo);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 保存聚合接口配置
     */
    @PostMapping("/save")
    public Result save(@RequestBody ApiInfo apiInfo) {
        try {
            ApiInfo existing = apiInfoMapper.selectById(apiInfo.getId());
            if (existing == null || existing.getIsAggregate() != 1) {
                return Result.error("聚合接口不存在");
            }

            // 更新配置
            apiInfoMapper.update(apiInfo);
            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error("保存失败: " + e.getMessage());
        }
    }

    /**
     * 发布聚合接口
     */
    @PostMapping("/{id}/publish")
    public Result publish(@PathVariable Long id) {
        try {
            ApiInfo apiInfo = apiInfoMapper.selectById(id);
            if (apiInfo == null || apiInfo.getIsAggregate() != 1) {
                return Result.error("聚合接口不存在");
            }

            if (apiInfo.getAggregateConfig() == null || apiInfo.getAggregateConfig().isEmpty()) {
                return Result.error("请先配置聚合接口编排");
            }

            // 发布：将状态改为启用
            apiInfo.setStatus(1);
            apiInfoMapper.update(apiInfo);

            return Result.success("发布成功");
        } catch (Exception e) {
            return Result.error("发布失败: " + e.getMessage());
        }
    }

    /**
     * 下线聚合接口
     */
    @PostMapping("/{id}/offline")
    public Result offline(@PathVariable Long id) {
        try {
            ApiInfo apiInfo = apiInfoMapper.selectById(id);
            if (apiInfo == null || apiInfo.getIsAggregate() != 1) {
                return Result.error("聚合接口不存在");
            }

            apiInfo.setStatus(0);
            apiInfoMapper.update(apiInfo);

            return Result.success("下线成功");
        } catch (Exception e) {
            return Result.error("下线失败: " + e.getMessage());
        }
    }

    /**
     * 删除聚合接口
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        try {
            ApiInfo apiInfo = apiInfoMapper.selectById(id);
            if (apiInfo == null || apiInfo.getIsAggregate() != 1) {
                return Result.error("聚合接口不存在");
            }

            apiInfoMapper.deleteById(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 测试执行聚合接口
     */
    @PostMapping("/{id}/test")
    public Result test(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            ApiInfo apiInfo = apiInfoMapper.selectById(id);
            if (apiInfo == null || apiInfo.getIsAggregate() != 1) {
                return Result.error("聚合接口不存在");
            }

            if (apiInfo.getAggregateConfig() == null || apiInfo.getAggregateConfig().isEmpty()) {
                return Result.error("请先配置聚合接口编排");
            }

            // 执行聚合接口
            long startTime = System.currentTimeMillis();
            Map<String, Object> result = aggregateExecutionService.execute(apiInfo, params);
            long costTime = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("result", result);
            response.put("costTime", costTime + "ms");

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("执行失败: " + e.getMessage());
        }
    }

    /**
     * 通过路径调用聚合接口
     */
    @PostMapping("/**")
    public Result invoke(
            @RequestBody(required = false) Map<String, Object> params,
            javax.servlet.http.HttpServletRequest request
    ) {
        try {
            // 提取路径
            String path = request.getRequestURI().replace("/aggregate", "");
            
            // 查找聚合接口
            ApiInfo apiInfo = apiInfoMapper.selectByPath(path);
            if (apiInfo == null || apiInfo.getIsAggregate() != 1 || apiInfo.getStatus() != 1) {
                return Result.error("聚合接口不存在或未发布");
            }

            // 执行
            Map<String, Object> inputParams = params != null ? params : new HashMap<>();
            Map<String, Object> result = aggregateExecutionService.execute(apiInfo, inputParams);

            return Result.success(result.get("data"));
        } catch (Exception e) {
            return Result.error("调用失败: " + e.getMessage());
        }
    }
}
