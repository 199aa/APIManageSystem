// 菜单配置 - 定义所有菜单项及其权限要求
export const menuConfig = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    title: '首页',
    icon: 'el-icon-s-home',
    permission: null // 所有人都可以访问
  },
  {
    path: '/platform',
    name: 'Platform',
    title: '平台接入',
    icon: 'el-icon-connection',
    permission: 'platform',
    children: [
      {
        path: '/platform/list',
        name: 'PlatformList',
        title: '平台列表',
        permission: 'platform:list'
      }
    ]
  },
  {
    path: '/api',
    name: 'Api',
    title: 'API仓库',
    icon: 'el-icon-folder-opened',
    permission: 'api',
    children: [
      {
        path: '/api/list',
        name: 'ApiList',
        title: 'API列表',
        permission: 'api:list'
      }
    ]
  },
  {
    path: '/orchestration',
    name: 'Orchestration',
    title: '服务编排',
    icon: 'el-icon-share',
    permission: 'orchestration',
    children: [
      {
        path: '/orchestration/aggregate',
        name: 'AggregateList',
        title: '聚合接口',
        permission: 'orchestration:aggregate'
      }
    ]
  },
  {
    path: '/governance',
    name: 'Governance',
    title: '治理中心',
    icon: 'el-icon-setting',
    permission: 'governance',
    children: [
      {
        path: '/governance/rate-limit',
        name: 'RateLimit',
        title: '限流策略',
        permission: 'governance:ratelimit'
      },
      {
        path: '/governance/blacklist',
        name: 'Blacklist',
        title: '黑白名单',
        permission: 'governance:blacklist'
      },
      {
        path: '/governance/cache',
        name: 'CachePolicy',
        title: '缓存策略',
        permission: 'governance:cache'
      }
    ]
  },
  {
    path: '/customer',
    name: 'Customer',
    title: '客户管理',
    icon: 'el-icon-user-solid',
    permission: 'customer',
    children: [
      {
        path: '/customer/apps',
        name: 'AppList',
        title: '应用列表',
        permission: 'customer:list'
      }
    ]
  },
  {
    path: '/monitor',
    name: 'Monitor',
    title: '监控中心',
    icon: 'el-icon-monitor',
    permission: 'monitor',
    children: [
      {
        path: '/monitor/logs',
        name: 'CallLogs',
        title: '调用日志',
        permission: 'monitor:logs'
      },
      {
        path: '/monitor/alerts',
        name: 'Alerts',
        title: '告警中心',
        permission: 'monitor:alerts'
      }
    ]
  },
  {
    path: '/system',
    name: 'System',
    title: '系统管理',
    icon: 'el-icon-setting',
    permission: 'system:user:list',
    children: [
      {
        path: '/system/users',
        name: 'UserManage',
        title: '用户管理',
        permission: 'system:user:list'
      },
      {
        path: '/system/roles',
        name: 'RoleManage',
        title: '角色管理',
        permission: 'system:role:list'
      },
      {
        path: '/system/logs',
        name: 'OperationLogs',
        title: '操作日志',
        permission: 'system:log:list'
      }
    ]
  }
]

/**
 * 根据用户权限过滤菜单
 * @param {Array} menus - 菜单配置
 * @param {Array} permissions - 用户权限列表
 * @param {Number} roleId - 角色ID
 * @returns {Array} - 过滤后的菜单
 */
export function filterMenus(menus, permissions, roleId) {
  // 超级管理员显示所有菜单
  if (roleId === 1) {
    return menus
  }

  return menus.filter((menu) => {
    // 如果菜单不需要权限，则显示
    if (!menu.permission) {
      return true
    }

    // 检查用户是否有该菜单的权限
    if (permissions.includes(menu.permission)) {
      // 如果有子菜单，递归过滤
      if (menu.children && menu.children.length > 0) {
        menu.children = menu.children.filter((child) => {
          return !child.permission || permissions.includes(child.permission)
        })
      }
      return true
    }

    return false
  })
}
