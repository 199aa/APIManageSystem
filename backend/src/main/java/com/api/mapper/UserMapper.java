package com.api.mapper;

import com.api.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

  User selectByUsername(@Param("username") String username);

  User selectByEmail(@Param("email") String email);

  User selectById(@Param("id") Long id);

  int insert(User user);

  int update(User user);

  int deleteById(@Param("id") Long id);

  /**
   * 分页查询用户（支持条件查询）
   */
  List<User> selectByPage(@Param("params") Map<String, Object> params);

  /**
   * 查询用户总数（支持条件查询）
   */
  int countByCondition(@Param("params") Map<String, Object> params);

  /**
   * 批量删除用户
   */
  int deleteBatch(@Param("ids") List<Long> ids);

  /**
   * 更新用户状态
   */
  int updateStatus(@Param("id") Long id, @Param("status") Integer status);

  /**
   * 更新最后登录时间
   */
  int updateLastLoginTime(@Param("id") Long id);
}
