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
          <!-- 动态菜单 -->
          <template v-for="menu in visibleMenus">
            <!-- 无子菜单 -->
            <el-menu-item v-if="!menu.children || menu.children.length === 0" :key="menu.path" :index="menu.path">
              <i :class="menu.icon"></i>
              <span slot="title">{{ menu.title }}</span>
            </el-menu-item>

            <!-- 有子菜单 -->
            <el-submenu v-else :key="menu.path" :index="menu.name">
              <template slot="title">
                <i :class="menu.icon"></i>
                <span>{{ menu.title }}</span>
              </template>
              <el-menu-item v-for="child in menu.children" :key="child.path" :index="child.path">
                {{ child.title }}
              </el-menu-item>
            </el-submenu>
          </template>
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
import { menuConfig, filterMenus } from '@/config/menu'

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
    permissions() {
      return this.$store.state.user.permissions
    },
    roleId() {
      return this.$store.state.user.userInfo.roleId
    },
    // 根据权限过滤菜单
    visibleMenus() {
      return filterMenus(menuConfig, this.permissions, this.roleId)
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
