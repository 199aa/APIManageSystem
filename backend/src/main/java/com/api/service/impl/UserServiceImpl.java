package com.api.service.impl;

import com.api.mapper.UserMapper;
import com.api.model.User;
import com.api.service.UserService;
import com.api.util.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

  @Autowired
  private UserMapper userMapper;

  @Override
  @Transactional
  public User login(String username, String password) {
    // 支持用户名或邮箱登录
    User user = userMapper.selectByUsername(username);
    if (user == null) {
      // 尝试通过邮箱查找
      user = userMapper.selectByEmail(username);
    }

    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    String encryptedPassword = MD5Util.encrypt(password);
    if (!encryptedPassword.equals(user.getPassword())) {
      throw new RuntimeException("密码错误");
    }

    // 更新最后登录时间
    userMapper.updateLastLoginTime(user.getId());

    // 清除密码信息
    user.setPassword(null);
    return user;
  }

  @Override
  public User getUserById(Long id) {
    User user = userMapper.selectById(id);
    if (user != null) {
      user.setPassword(null);
    }
    return user;
  }

  @Override
  @Transactional
  public User register(User user) {
    // 检查用户名是否已存在
    User existUser = userMapper.selectByUsername(user.getUsername());
    if (existUser != null) {
      throw new RuntimeException("用户名已存在");
    }

    // 验证邮箱
    if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
      throw new RuntimeException("邮箱不能为空");
    }

    // 密码加密
    user.setPassword(MD5Util.encrypt(user.getPassword()));
    user.setStatus(1);
    userMapper.insert(user);
    user.setPassword(null);
    return user;
  }

  @Override
  public Map<String, Object> getUsersByPage(Map<String, Object> params) {
    int page = params.get("page") != null ? (int) params.get("page") : 1;
    int pageSize = params.get("pageSize") != null ? (int) params.get("pageSize") : 10;
    int offset = (page - 1) * pageSize;

    params.put("offset", offset);
    params.put("limit", pageSize);

    List<User> users = userMapper.selectByPage(params);
    // 清除密码信息
    users.forEach(user -> user.setPassword(null));

    int total = userMapper.countByCondition(params);

    Map<String, Object> result = new HashMap<>();
    result.put("list", users);
    result.put("total", total);
    result.put("page", page);
    result.put("pageSize", pageSize);
    return result;
  }

  @Override
  @Transactional
  public User createUser(User user) {
    // 检查用户名是否已存在
    User existUser = userMapper.selectByUsername(user.getUsername());
    if (existUser != null) {
      throw new RuntimeException("用户名已存在");
    }

    // 检查邮箱是否已存在
    if (user.getEmail() != null && !user.getEmail().isEmpty()) {
      existUser = userMapper.selectByEmail(user.getEmail());
      if (existUser != null) {
        throw new RuntimeException("邮箱已被使用");
      }
    }

    // 密码加密
    if (user.getPassword() == null || user.getPassword().isEmpty()) {
      user.setPassword(MD5Util.encrypt("123456")); // 默认密码
    } else {
      user.setPassword(MD5Util.encrypt(user.getPassword()));
    }

    if (user.getStatus() == null) {
      user.setStatus(1);
    }

    userMapper.insert(user);
    user.setPassword(null);
    return user;
  }

  @Override
  @Transactional
  public User updateUser(User user) {
    User existUser = userMapper.selectById(user.getId());
    if (existUser == null) {
      throw new RuntimeException("用户不存在");
    }

    // 如果更新邮箱，检查邮箱是否被其他用户使用
    if (user.getEmail() != null && !user.getEmail().isEmpty()) {
      User emailUser = userMapper.selectByEmail(user.getEmail());
      if (emailUser != null && !emailUser.getId().equals(user.getId())) {
        throw new RuntimeException("邮箱已被使用");
      }
    }

    // 不允许通过此接口更新密码
    user.setPassword(null);

    userMapper.update(user);
    return getUserById(user.getId());
  }

  @Override
  @Transactional
  public void deleteUser(Long id) {
    User user = userMapper.selectById(id);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    userMapper.deleteById(id);
  }

  @Override
  @Transactional
  public void deleteBatch(List<Long> ids) {
    if (ids != null && !ids.isEmpty()) {
      userMapper.deleteBatch(ids);
    }
  }

  @Override
  @Transactional
  public void updateStatus(Long id, Integer status) {
    User user = userMapper.selectById(id);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }
    userMapper.updateStatus(id, status);
  }

  @Override
  @Transactional
  public void resetPassword(Long id, String newPassword) {
    User user = userMapper.selectById(id);
    if (user == null) {
      throw new RuntimeException("用户不存在");
    }

    String password = newPassword != null && !newPassword.isEmpty()
        ? MD5Util.encrypt(newPassword)
        : MD5Util.encrypt("123456");

    user.setPassword(password);
    userMapper.update(user);
  }
}
