package com.api.controller;

import com.api.common.Result;
import com.api.model.ApiInfo;
import com.api.service.ApiInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ApiController {

  @Autowired
  private ApiInfoService apiInfoService;

  @GetMapping("/list")
  public Result<List<ApiInfo>> getApiList(@RequestParam(required = false) String name,
      @RequestParam(required = false) String method) {
    try {
      List<ApiInfo> list = apiInfoService.getApiList(name, method);
      return Result.success(list);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  @GetMapping("/detail/{id}")
  public Result<ApiInfo> getApiDetail(@PathVariable Long id) {
    try {
      ApiInfo apiInfo = apiInfoService.getApiById(id);
      if (apiInfo == null) {
        return Result.error("API不存在");
      }
      return Result.success(apiInfo);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  @PostMapping("/create")
  public Result<ApiInfo> createApi(@RequestBody ApiInfo apiInfo) {
    try {
      ApiInfo newApi = apiInfoService.createApi(apiInfo);
      return Result.success(newApi);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  @PutMapping("/update/{id}")
  public Result<ApiInfo> updateApi(@PathVariable Long id, @RequestBody ApiInfo apiInfo) {
    try {
      ApiInfo updatedApi = apiInfoService.updateApi(id, apiInfo);
      return Result.success(updatedApi);
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }

  @DeleteMapping("/delete/{id}")
  public Result<String> deleteApi(@PathVariable Long id) {
    try {
      boolean success = apiInfoService.deleteApi(id);
      if (success) {
        return Result.success("删除成功");
      } else {
        return Result.error("删除失败");
      }
    } catch (Exception e) {
      return Result.error(e.getMessage());
    }
  }
}
