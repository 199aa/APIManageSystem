package com.api.service;

import com.api.model.User;
import java.util.List;
import java.util.Map;

public interface UserService {

  User login(String username, String password);

  User getUserById(Long id);

  User register(User user);

  /**
   * 分页查询用户
   */
  Map<String, Object> getUsersByPage(Map<String, Object> params);

  /**
   * 创建用户
   */
  User createUser(User user);

  /**
   * 更新用户信息
   */
  User updateUser(User user);

  /**
   * 删除用户
   */
  void deleteUser(Long id);

  /**
   * 批量删除用户
   */
  void deleteBatch(List<Long> ids);

  /**
   * 更新用户状态
   */
  void updateStatus(Long id, Integer status);

  /**
   * 重置用户密码
   */
  void resetPassword(Long id, String newPassword);
}
