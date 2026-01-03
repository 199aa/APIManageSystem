package com.api.controller;

import com.api.common.Result;
import com.api.model.CustomerApp;
import com.api.service.ApiAuthorizationService;
import com.api.service.CustomerAppService;
import com.api.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customer/app")
@CrossOrigin
public class CustomerAppController {
    
    @Autowired
    private CustomerAppService customerAppService;
    
    @Autowired
    private ApiAuthorizationService apiAuthorizationService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 获取当前用户的应用列表
     */
    @GetMapping("/list")
    public Result<List<CustomerApp>> getAppList(@RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtil.getUserIdFromToken(jwt);
            
            List<CustomerApp> apps = customerAppService.getAppsByUserId(Long.parseLong(userId));
            return Result.success(apps);
        } catch (Exception e) {
            return Result.error("获取应用列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取所有应用（管理员）
     */
    @GetMapping("/all")
    public Result<List<CustomerApp>> getAllApps() {
        try {
            List<CustomerApp> apps = customerAppService.getAllApps();
            return Result.success(apps);
        } catch (Exception e) {
            return Result.error("获取应用列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 根据ID获取应用详情
     */
    @GetMapping("/{id}")
    public Result<CustomerApp> getAppById(@PathVariable Long id) {
        try {
            CustomerApp app = customerAppService.getAppById(id);
            if (app == null) {
                return Result.error("应用不存在");
            }
            return Result.success(app);
        } catch (Exception e) {
            return Result.error("获取应用详情失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建应用
     */
    @PostMapping("/create")
    public Result<CustomerApp> createApp(@RequestHeader("Authorization") String token, 
                                         @RequestBody CustomerApp app) {
        try {
            String jwt = token.replace("Bearer ", "");
            String userId = jwtUtil.getUserIdFromToken(jwt);
            
            app.setUserId(Long.parseLong(userId));
            CustomerApp newApp = customerAppService.createApp(app);
            return Result.success(newApp);
        } catch (Exception e) {
            return Result.error("创建应用失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新应用
     */
    @PutMapping("/update")
    public Result<CustomerApp> updateApp(@RequestBody CustomerApp app) {
        try {
            if (app.getId() == null) {
                return Result.error("应用ID不能为空");
            }
            CustomerApp updatedApp = customerAppService.updateApp(app);
            return Result.success(updatedApp);
        } catch (Exception e) {
            return Result.error("更新应用失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除应用
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteApp(@PathVariable Long id) {
        try {
            customerAppService.deleteApp(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除应用失败: " + e.getMessage());
        }
    }
    
    /**
     * 更新应用状态
     */
    @PutMapping("/status")
    public Result<Void> updateAppStatus(@RequestBody CustomerApp app) {
        try {
            if (app.getId() == null || app.getStatus() == null) {
                return Result.error("应用ID和状态不能为空");
            }
            customerAppService.updateAppStatus(app.getId(), app.getStatus());
            return Result.success();
        } catch (Exception e) {
            return Result.error("更新应用状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 重置应用密钥
     */
    @PostMapping("/reset-secret/{id}")
    public Result<CustomerApp> resetAppSecret(@PathVariable Long id) {
        try {
            CustomerApp app = customerAppService.resetAppSecret(id);
            if (app == null) {
                return Result.error("应用不存在");
            }
            return Result.success(app);
        } catch (Exception e) {
            return Result.error("重置密钥失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取应用已授权的API ID列表
     */
    @GetMapping("/authorized-apis/{appId}")
    public Result<List<Long>> getAuthorizedApis(@PathVariable Long appId) {
        try {
            List<Long> apiIds = apiAuthorizationService.getAuthorizedApiIds(appId);
            return Result.success(apiIds);
        } catch (Exception e) {
            return Result.error("获取授权列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量保存应用的API授权
     */
    @PostMapping("/save-permissions")
    public Result<Void> savePermissions(@RequestBody Map<String, Object> data) {
        try {
            Long appId = Long.parseLong(data.get("appId").toString());
            @SuppressWarnings("unchecked")
            List<Integer> apiIdsInt = (List<Integer>) data.get("apiIds");
            List<Long> apiIds = apiIdsInt.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
            
            apiAuthorizationService.batchSave(appId, apiIds);
            return Result.success();
        } catch (Exception e) {
            return Result.error("保存权限失败: " + e.getMessage());
        }
    }
}
