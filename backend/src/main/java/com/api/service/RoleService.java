package com.api.service;

import com.api.model.Role;
import java.util.List;
import java.util.Map;

public interface RoleService {

  /**
   * 查询所有角色
   */
  List<Role> getAllRoles();

  /**
   * 分页查询角色
   */
  Map<String, Object> getRolesByPage(int page, int pageSize);

  /**
   * 根据ID查询角色
   */
  Role getRoleById(Long id);

  /**
   * 创建角色
   */
  Role createRole(Role role);

  /**
   * 更新角色
   */
  Role updateRole(Role role);

  /**
   * 删除角色
   */
  void deleteRole(Long id);

  /**
   * 批量删除角色
   */
  void deleteBatch(List<Long> ids);
}
