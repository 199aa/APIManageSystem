package com.api.controller;

import com.api.common.Result;
import com.api.model.ApiInfo;
import com.api.mapper.ApiInfoMapper;
import com.api.mapper.PlatformMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api-info")
public class ApiInfoController {

  @Autowired
  private ApiInfoMapper apiInfoMapper;

  @Autowired
  private PlatformMapper platformMapper;

  /**
   * 获取API列表（分页）
   */
  @GetMapping("/list")
  public Result list(@RequestParam(defaultValue = "1") Integer page,
      @RequestParam(defaultValue = "10") Integer pageSize,
      @RequestParam(required = false) Long platformId,
      @RequestParam(required = false) String method,
      @RequestParam(required = false) String name) {
    int offset = (page - 1) * pageSize;
    List<ApiInfo> list = apiInfoMapper.selectPage(offset, pageSize, platformId, method, name);
    int total = apiInfoMapper.countByCondition(platformId, method, name);

    Map<String, Object> data = new HashMap<>();
    data.put("list", list);
    data.put("total", total);
    data.put("page", page);
    data.put("pageSize", pageSize);

    return Result.success(data);
  }

  /**
   * 获取API详情
   */
  @GetMapping("/{id}")
  public Result detail(@PathVariable Long id) {
    ApiInfo apiInfo = apiInfoMapper.selectById(id);
    if (apiInfo == null) {
      return Result.error("接口不存在");
    }
    return Result.success(apiInfo);
  }

  /**
   * 保存API（新增/编辑）
   */
  @PostMapping("/save")
  public Result save(@RequestBody ApiInfo apiInfo) {
    if (apiInfo.getId() == null) {
      apiInfoMapper.insert(apiInfo);
    } else {
      apiInfoMapper.update(apiInfo);
    }
    return Result.success("保存成功");
  }

  /**
   * 删除API
   */
  @DeleteMapping("/{id}")
  public Result delete(@PathVariable Long id) {
    apiInfoMapper.deleteById(id);
    return Result.success("删除成功");
  }

  /**
   * 克隆API
   */
  @PostMapping("/{id}/clone")
  public Result clone(@PathVariable Long id) {
    ApiInfo original = apiInfoMapper.selectById(id);
    if (original == null) {
      return Result.error("原接口不存在");
    }
    original.setId(null);
    original.setName(original.getName() + "_copy");
    apiInfoMapper.insert(original);
    return Result.success("克隆成功");
  }

  /**
   * 测试API（简单模拟）
   */
  @PostMapping("/test")
  public Result test(@RequestBody Map<String, Object> params) {
    // 这里可以实现真实的HTTP调用逻辑
    // 目前返回模拟数据
    Map<String, Object> response = new HashMap<>();
    response.put("statusCode", 200);
    response.put("statusText", "OK");

    Map<String, Object> data = new HashMap<>();
    data.put("id", 1);
    data.put("name", "测试数据");

    Map<String, Object> result = new HashMap<>();
    result.put("code", 200);
    result.put("message", "success");
    result.put("data", data);

    response.put("response", result);
    return Result.success(response);
  }
}
