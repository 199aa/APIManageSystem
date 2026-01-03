<template>
  <div class="layout-container">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
        <div class="logo">
          <span v-if="!isCollapse">API管理系统</span>
          <span v-else>API</span>
        </div>
        <el-menu :default-active="activeMenu" :collapse="isCollapse" :unique-opened="true" router text-color="#bfcbd9" active-text-color="#667eea">
          <el-menu-item index="/dashboard">
            <i class="el-icon-s-home"></i>
            <span slot="title">首页</span>
          </el-menu-item>
          
          <!-- 平台接入 -->
          <el-submenu index="platform">
            <template slot="title">
              <i class="el-icon-connection"></i>
              <span>平台接入</span>
            </template>
            <el-menu-item index="/platform/list">平台列表</el-menu-item>
          </el-submenu>
          
          <!-- API仓库 -->
          <el-submenu index="api">
            <template slot="title">
              <i class="el-icon-s-order"></i>
              <span>API仓库</span>
            </template>
            <el-menu-item index="/api/list">原子接口列表</el-menu-item>
          </el-submenu>
          
          <!-- 服务编排 -->
          <el-submenu index="orchestration">
            <template slot="title">
              <i class="el-icon-s-operation"></i>
              <span>服务编排</span>
            </template>
            <el-menu-item index="/orchestration/aggregate">聚合接口管理</el-menu-item>
          </el-submenu>
          
          <!-- 治理中心 -->
          <el-submenu index="governance">
            <template slot="title">
              <i class="el-icon-s-tools"></i>
              <span>治理中心</span>
            </template>
            <el-menu-item index="/governance/rate-limit">限流策略</el-menu-item>
            <el-menu-item index="/governance/blacklist">黑白名单</el-menu-item>
            <el-menu-item index="/governance/cache">缓存策略</el-menu-item>
          </el-submenu>
          
          <!-- 客户管理 -->
          <el-submenu index="customer">
            <template slot="title">
              <i class="el-icon-user"></i>
              <span>客户管理</span>
            </template>
            <el-menu-item index="/customer/apps">应用列表</el-menu-item>
          </el-submenu>
          
          <!-- 运维监控 -->
          <el-submenu index="monitor">
            <template slot="title">
              <i class="el-icon-data-line"></i>
              <span>运维监控</span>
            </template>
            <el-menu-item index="/monitor/logs">调用日志</el-menu-item>
            <el-menu-item index="/monitor/alerts">告警中心</el-menu-item>
          </el-submenu>
          
          <!-- 系统管理 -->
          <el-submenu index="system">
            <template slot="title">
              <i class="el-icon-setting"></i>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/system/users">用户管理</el-menu-item>
            <el-menu-item index="/system/roles">角色管理</el-menu-item>
            <el-menu-item index="/system/logs">操作日志</el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>

      <!-- 主体区域 -->
      <el-container>
        <!-- 头部 -->
        <el-header class="header">
          <div class="header-left">
            <i :class="isCollapse ? 'el-icon-s-unfold' : 'el-icon-s-fold'" @click="toggleSidebar"></i>
          </div>
          <div class="header-right">
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <i class="el-icon-user-solid"></i>
                {{ userInfo.username || '用户' }}
                <i class="el-icon-arrow-down"></i>
              </span>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 内容区 -->
        <el-main class="main-content">
          <transition name="fade" mode="out-in">
            <router-view />
          </transition>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
export default {
  name: 'Layout',
  data() {
    return {
      isCollapse: false
    }
  },
  computed: {
    userInfo() {
      return this.$store.state.user.userInfo
    },
    activeMenu() {
      const route = this.$route
      const { path } = route
      return path
    }
  },
  methods: {
    toggleSidebar() {
      this.isCollapse = !this.isCollapse
    },
    handleCommand(command) {
      if (command === 'logout') {
        this.$store.dispatch('user/logout')
        this.$router.push('/login')
        this.$message.success('退出成功')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.layout-container {
  height: 100vh;

  .el-container {
    height: 100%;
  }

  .sidebar {
    background-color: rgba(35, 35, 55, 0.98);
    transition: width 0.3s;
    border-right: 1px solid rgba(102, 126, 234, 0.15);

    .logo {
      height: 60px;
      line-height: 60px;
      text-align: center;
      color: #fff;
      font-size: 20px;
      font-weight: bold;
      background-color: rgba(25, 25, 40, 0.8);
      border-bottom: 1px solid rgba(102, 126, 234, 0.15);
    }

    .el-menu {
      border-right: none;
    }

    ::v-deep .el-menu {
      background-color: transparent;

      .el-menu-item {
        background-color: transparent;
        color: #8b8ba7;

        &:hover {
          background-color: rgba(102, 126, 234, 0.1);
          color: #fff;
        }

        &.is-active {
          background-color: rgba(102, 126, 234, 0.2);
          color: #667eea;
        }
      }
    }
  }

  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: rgba(35, 35, 55, 0.98);
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.3);
    padding: 0 20px;
    border-bottom: 1px solid rgba(102, 126, 234, 0.15);

    .header-left {
      i {
        font-size: 24px;
        cursor: pointer;
        color: #8b8ba7;

        &:hover {
          color: #667eea;
        }
      }
    }

    .header-right {
      .user-info {
        cursor: pointer;
        color: #fff;

        i {
          margin: 0 5px;
          color: #8b8ba7;
        }

        &:hover {
          color: #667eea;
        }
      }
    }
  }

  .main-content {
    background-color: #1a1a2e;
    padding: 0;

    @media screen and (max-width: 768px) {
      padding: 10px;
    }
  }
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}
</style>
