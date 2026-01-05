/**
 * 权限指令
 * 使用方法: v-permission="'api:create'"
 * 或者: v-permission="['api:create', 'api:update']"
 */

import store from '@/store'

export default {
  inserted(el, binding) {
    const { value } = binding
    const permissions = store.state.user.permissions
    const roleId = store.state.user.userInfo.roleId

    // 超级管理员显示所有按钮
    if (roleId === 1) {
      return
    }

    if (value) {
      const requiredPermissions = Array.isArray(value) ? value : [value]
      const hasPermission = requiredPermissions.some(permission => {
        return permissions.includes(permission)
      })

      if (!hasPermission) {
        // 移除元素
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error('需要指定权限! 如: v-permission="\'api:create\'"')
    }
  }
}
