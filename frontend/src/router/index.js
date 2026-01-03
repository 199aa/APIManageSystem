import Vue from 'vue'
import VueRouter from 'vue-router'
import Layout from '@/views/layout/index.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页' }
      },
      // 平台接入
      {
        path: 'platform/list',
        name: 'PlatformList',
        component: () => import('@/views/platform/list.vue'),
        meta: { title: '平台列表' }
      },
      // API仓库
      {
        path: 'api/list',
        name: 'ApiList',
        component: () => import('@/views/api/list.vue'),
        meta: { title: '原子接口列表' }
      },
      {
        path: 'api/detail/:id',
        name: 'ApiDetail',
        component: () => import('@/views/api/detail.vue'),
        meta: { title: 'API详情' }
      },
      // 服务编排
      {
        path: 'orchestration/aggregate',
        name: 'AggregateList',
        component: () => import('@/views/orchestration/aggregate.vue'),
        meta: { title: '聚合接口管理' }
      },
      {
        path: 'orchestration/design/:id?',
        name: 'AggregateDesign',
        component: () => import('@/views/orchestration/design.vue'),
        meta: { title: '聚合接口编排' }
      },
      // 治理中心
      {
        path: 'governance/rate-limit',
        name: 'RateLimit',
        component: () => import('@/views/governance/rate-limit.vue'),
        meta: { title: '限流策略' }
      },
      {
        path: 'governance/blacklist',
        name: 'Blacklist',
        component: () => import('@/views/governance/blacklist.vue'),
        meta: { title: '黑白名单' }
      },
      {
        path: 'governance/cache',
        name: 'CachePolicy',
        component: () => import('@/views/governance/cache.vue'),
        meta: { title: '缓存策略' }
      },
      // 客户管理
      {
        path: 'customer/apps',
        name: 'AppList',
        component: () => import('@/views/customer/apps.vue'),
        meta: { title: '应用列表' }
      },
      {
        path: 'customer/app-detail/:id',
        name: 'AppDetail',
        component: () => import('@/views/customer/app-detail.vue'),
        meta: { title: '应用详情' }
      },
      // 运维监控
      {
        path: 'monitor/logs',
        name: 'CallLogs',
        component: () => import('@/views/monitor/logs.vue'),
        meta: { title: '调用日志' }
      },
      {
        path: 'monitor/alerts',
        name: 'Alerts',
        component: () => import('@/views/monitor/alerts.vue'),
        meta: { title: '告警中心' }
      },
      // 系统管理
      {
        path: 'system/users',
        name: 'UserManage',
        component: () => import('@/views/system/users.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/roles',
        name: 'RoleManage',
        component: () => import('@/views/system/roles.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/logs',
        name: 'OperationLogs',
        component: () => import('@/views/system/logs.vue'),
        meta: { title: '操作日志' }
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login') {
    next()
  } else {
    if (token) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
