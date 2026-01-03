package com.api.mapper;

import com.api.model.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface RoleMapper {

  /**
   * 查询所有角色
   */
  List<Role> selectAll();

  /**
   * 分页查询角色
   */
  List<Role> selectByPage(@Param("offset") int offset, @Param("limit") int limit);

  /**
   * 查询角色总数
   */
  int count();

  /**
   * 根据ID查询角色
   */
  Role selectById(@Param("id") Long id);

  /**
   * 根据编码查询角色
   */
  Role selectByCode(@Param("code") String code);

  /**
   * 插入角色
   */
  int insert(Role role);

  /**
   * 更新角色
   */
  int update(Role role);

  /**
   * 删除角色
   */
  int deleteById(@Param("id") Long id);

  /**
   * 批量删除角色
   */
  int deleteBatch(@Param("ids") List<Long> ids);
}
