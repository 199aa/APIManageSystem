package com.api.service.impl;

import com.api.mapper.RoleMapper;
import com.api.model.Role;
import com.api.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {

  @Autowired
  private RoleMapper roleMapper;

  @Override
  public List<Role> getAllRoles() {
    return roleMapper.selectAll();
  }

  @Override
  public Map<String, Object> getRolesByPage(int page, int pageSize) {
    int offset = (page - 1) * pageSize;
    List<Role> roles = roleMapper.selectByPage(offset, pageSize);
    int total = roleMapper.count();

    Map<String, Object> result = new HashMap<>();
    result.put("list", roles);
    result.put("total", total);
    result.put("page", page);
    result.put("pageSize", pageSize);
    return result;
  }

  @Override
  public Role getRoleById(Long id) {
    return roleMapper.selectById(id);
  }

  @Override
  @Transactional
  public Role createRole(Role role) {
    // 检查角色编码是否已存在
    Role existRole = roleMapper.selectByCode(role.getCode());
    if (existRole != null) {
      throw new RuntimeException("角色编码已存在");
    }

    if (role.getStatus() == null) {
      role.setStatus(1);
    }
    if (role.getIsSystem() == null) {
      role.setIsSystem(0);
    }

    roleMapper.insert(role);
    return role;
  }

  @Override
  @Transactional
  public Role updateRole(Role role) {
    Role existRole = roleMapper.selectById(role.getId());
    if (existRole == null) {
      throw new RuntimeException("角色不存在");
    }

    // 系统角色不允许修改编码
    if (existRole.getIsSystem() == 1 && !existRole.getCode().equals(role.getCode())) {
      throw new RuntimeException("系统角色不允许修改编码");
    }

    roleMapper.update(role);
    return roleMapper.selectById(role.getId());
  }

  @Override
  @Transactional
  public void deleteRole(Long id) {
    Role role = roleMapper.selectById(id);
    if (role == null) {
      throw new RuntimeException("角色不存在");
    }
    if (role.getIsSystem() == 1) {
      throw new RuntimeException("系统角色不允许删除");
    }

    roleMapper.deleteById(id);
  }

  @Override
  @Transactional
  public void deleteBatch(List<Long> ids) {
    // 检查是否包含系统角色
    for (Long id : ids) {
      Role role = roleMapper.selectById(id);
      if (role != null && role.getIsSystem() == 1) {
        throw new RuntimeException("不能删除系统角色: " + role.getName());
      }
    }

    roleMapper.deleteBatch(ids);
  }
}
