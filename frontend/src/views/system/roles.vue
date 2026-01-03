<template>
  <div class="roles-page">
    <div class="page-header">
      <h2>角色管理</h2>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="showDialog()">新建角色</el-button>
    </div>

    <el-row :gutter="20">
      <!-- 角色列表 -->
      <el-col :span="8">
        <el-card class="role-list-card">
          <div slot="header" class="card-header">
            <span>角色列表</span>
          </div>
          <div class="role-list">
            <div v-for="role in roleList" :key="role.id" class="role-item" :class="{ active: selectedRole && selectedRole.id === role.id }" @click="selectRole(role)">
              <div class="role-info">
                <div class="role-name">{{ role.name }}</div>
                <div class="role-desc">{{ role.description }}</div>
              </div>
              <div class="role-actions">
                <el-tag size="mini" :type="role.status ? 'success' : 'info'">{{ role.status ? '启用' : '禁用' }}</el-tag>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 权限配置 -->
      <el-col :span="16">
        <el-card class="permission-card">
          <div slot="header" class="card-header">
            <span>权限配置 {{ selectedRole ? `- ${selectedRole.name}` : '' }}</span>
            <el-button type="primary" size="small" @click="savePermissions" v-if="selectedRole">保存权限</el-button>
          </div>

          <div class="permission-content" v-if="selectedRole">
            <div class="role-basic-info">
              <el-descriptions :column="2" size="small" border>
                <el-descriptions-item label="角色名称">{{ selectedRole.name }}</el-descriptions-item>
                <el-descriptions-item label="角色编码">{{ selectedRole.code }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatTime(selectedRole.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="状态">
                  <el-tag :type="selectedRole.status ? 'success' : 'info'" size="small">
                    {{ selectedRole.status ? '启用' : '禁用' }}
                  </el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">{{ selectedRole.description }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="permission-tree-wrapper">
              <h4>
                菜单权限
                <el-checkbox v-model="checkAll" @change="handleCheckAll">全选</el-checkbox>
              </h4>
              <el-tree ref="permissionTree" :data="menuTree" :props="{ label: 'name', children: 'children' }" show-checkbox node-key="id" :default-expand-all="true" :default-checked-keys="checkedKeys" @check-change="handleTreeCheck">
                <span class="custom-tree-node" slot-scope="{ node, data }">
                  <i :class="data.icon" v-if="data.icon"></i>
                  <span>{{ data.name }}</span>
                  <el-tag size="mini" v-if="data.type === 'button'" type="warning">按钮</el-tag>
                </span>
              </el-tree>
            </div>

            <div class="role-actions-bottom">
              <el-button type="text" size="small" @click="showDialog(selectedRole)">
                <i class="el-icon-edit"></i> 编辑角色
              </el-button>
              <el-button type="text" size="small" class="danger-btn" @click="deleteRole(selectedRole)" v-if="!selectedRole.isSystem">
                <i class="el-icon-delete"></i> 删除角色
              </el-button>
            </div>
          </div>

          <div class="no-selection" v-else>
            <i class="el-icon-info"></i>
            <p>请从左侧选择一个角色查看权限配置</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新建/编辑角色对话框 -->
    <el-dialog :title="form.id ? '编辑角色' : '新建角色'" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称"></el-input>
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="如: ROLE_ADMIN" :disabled="!!form.id"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" rows="3" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"></el-switch>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRole">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAllRoles, createRole, updateRole, deleteRole, getPermissionsTree, getPermissionsByRoleId, saveRolePermissions } from '@/api/system'

export default {
  name: 'RoleManagement',
  data() {
    return {
      roleList: [],
      selectedRole: null,
      menuTree: [],
      checkedKeys: [],
      checkAll: false,
      dialogVisible: false,
      form: {
        id: null,
        name: '',
        code: '',
        description: '',
        status: 1
      },
      formRules: {
        name: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
        code: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadRoles()
    this.loadMenuTree()
  },
  methods: {
    loadRoles() {
      getAllRoles()
        .then((res) => {
          if (res.code === 200) {
            this.roleList = res.data || []
          } else {
            this.$message.error(res.message || '加载角色失败')
          }
        })
        .catch((err) => {
          this.$message.error('加载角色列表失败')
          console.error(err)
        })
    },
    loadMenuTree() {
      getPermissionsTree().then((res) => {
        if (res.code === 200) {
          this.menuTree = res.data || []
        } else {
          this.$message.error(res.message || '加载权限树失败')
        }
      }).catch((err) => {
        console.error(err)
        // 如果API调用失败，使用默认数据
        this.menuTree = [
        {
          id: 1,
          name: '平台接入',
          icon: 'el-icon-connection',
          children: [
            { id: 11, name: '平台列表', type: 'menu' },
            { id: 12, name: '新增平台', type: 'button' },
            { id: 13, name: '编辑平台', type: 'button' },
            { id: 14, name: '删除平台', type: 'button' }
          ]
        },
        {
          id: 2,
          name: 'API仓库',
          icon: 'el-icon-folder-opened',
          children: [
            { id: 21, name: 'API列表', type: 'menu' },
            { id: 22, name: 'API详情', type: 'menu' },
            { id: 23, name: '新增API', type: 'button' },
            { id: 24, name: '编辑API', type: 'button' },
            { id: 25, name: '删除API', type: 'button' },
            { id: 26, name: '测试API', type: 'button' }
          ]
        },
        {
          id: 3,
          name: '服务编排',
          icon: 'el-icon-share',
          children: [
            { id: 31, name: '聚合API', type: 'menu' },
            { id: 32, name: '编排设计', type: 'menu' },
            { id: 33, name: '新增聚合', type: 'button' },
            { id: 34, name: '发布聚合', type: 'button' }
          ]
        },
        {
          id: 4,
          name: '治理中心',
          icon: 'el-icon-setting',
          children: [
            { id: 41, name: '限流策略', type: 'menu' },
            { id: 42, name: '黑白名单', type: 'menu' },
            { id: 43, name: '缓存策略', type: 'menu' }
          ]
        },
        {
          id: 5,
          name: '客户管理',
          icon: 'el-icon-user-solid',
          children: [
            { id: 51, name: '应用列表', type: 'menu' },
            { id: 52, name: '应用详情', type: 'menu' },
            { id: 53, name: '创建应用', type: 'button' },
            { id: 54, name: '管理凭证', type: 'button' }
          ]
        },
        {
          id: 6,
          name: '运维监控',
          icon: 'el-icon-data-line',
          children: [
            { id: 61, name: '调用日志', type: 'menu' },
            { id: 62, name: '告警管理', type: 'menu' }
          ]
        },
        {
          id: 7,
          name: '系统管理',
          icon: 'el-icon-s-tools',
          children: [
            { id: 71, name: '用户管理', type: 'menu' },
            { id: 72, name: '角色管理', type: 'menu' },
            { id: 73, name: '操作日志', type: 'menu' }
          ]
        }
        ]
      })
    },
    formatTime(time) {
      return time ? new Date(time).toLocaleDateString() : '-'
    },
    selectRole(role) {
      this.selectedRole = role
      // 加载该角色的权限
      getPermissionsByRoleId(role.id).then((res) => {
        if (res.code === 200) {
          // 只选中叶子节点，避免父节点自动被选中导致的问题
          const allIds = res.data.permissionIds || []
          this.checkedKeys = this.getLeafKeys(allIds)
          this.checkAll = allIds.length === this.getAllMenuIds().length
        } else {
          this.$message.error(res.message || '加载权限失败')
        }
      }).catch((err) => {
        console.error(err)
        this.$message.error('加载角色权限失败')
      })
      this.$nextTick(() => {
        this.$refs.permissionTree && this.$refs.permissionTree.setCheckedKeys(this.checkedKeys)
      })
    },
    getAllMenuIds() {
      const ids = []
      const traverse = (nodes) => {
        nodes.forEach((node) => {
          ids.push(node.id)
          if (node.children) traverse(node.children)
        })
      }
      traverse(this.menuTree)
      return ids
    },
    getLeafKeys(allIds) {
      // 获取叶子节点ID（没有子节点的节点）
      const leafIds = []
      const checkLeaf = (menus) => {
        menus.forEach((menu) => {
          if (allIds.includes(menu.id)) {
            if (!menu.children || menu.children.length === 0) {
              leafIds.push(menu.id)
            } else {
              checkLeaf(menu.children)
            }
          }
        })
      }
      checkLeaf(this.menuTree)
      return leafIds
    },
    handleCheckAll(val) {
      if (val) {
        this.$refs.permissionTree.setCheckedKeys(this.getAllMenuIds())
      } else {
        this.$refs.permissionTree.setCheckedKeys([])
      }
    },
    handleTreeCheck() {
      const allIds = this.getAllMenuIds()
      const checkedIds = this.$refs.permissionTree.getCheckedKeys()
      this.checkAll = checkedIds.length === allIds.length
    },
    savePermissions() {
      if (!this.selectedRole) return
      
      const checkedNodes = this.$refs.permissionTree.getCheckedNodes()
      const halfCheckedNodes = this.$refs.permissionTree.getHalfCheckedNodes()
      const permissionIds = [...checkedNodes, ...halfCheckedNodes].map(node => node.id)
      
      saveRolePermissions(this.selectedRole.id, permissionIds).then((res) => {
        if (res.code === 200) {
          this.$message.success('权限保存成功')
        } else {
          this.$message.error(res.message || '保存失败')
        }
      }).catch((err) => {
        console.error(err)
        this.$message.error('保存权限失败')
      })
    },
    showDialog(role) {
      if (role) {
        this.form = { ...role }
      } else {
        this.form = { id: null, name: '', code: '', description: '', status: 1 }
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef && this.$refs.formRef.clearValidate()
      })
    },
    saveRole() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          const apiMethod = this.form.id ? updateRole : createRole
          apiMethod(this.form)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success(this.form.id ? '角色更新成功' : '角色创建成功')
                this.dialogVisible = false
                this.loadRoles()
              } else {
                this.$message.error(res.message || '操作失败')
              }
            })
            .catch((err) => {
              this.$message.error('操作失败')
              console.error(err)
            })
        }
      })
    },
    deleteRole(role) {
      if (role.isSystem) {
        this.$message.warning('系统角色不允许删除')
        return
      }
      this.$confirm(`确定删除角色 "${role.name}"？`, '警告', { type: 'warning' })
        .then(() => {
          deleteRole(role.id)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success('角色已删除')
                this.selectedRole = null
                this.loadRoles()
              } else {
                this.$message.error(res.message || '删除失败')
              }
            })
            .catch((err) => {
              this.$message.error('删除失败')
              console.error(err)
            })
        })
        .catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.roles-page {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    h2 {
      color: #fff;
      margin: 0;
      font-size: 20px;
    }
  }

  .role-list-card,
  .permission-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;
    height: calc(100vh - 180px);

    ::v-deep .el-card__header {
      border-bottom: 1px solid rgba(102, 126, 234, 0.2);
      padding: 15px 20px;
    }

    ::v-deep .el-card__body {
      height: calc(100% - 60px);
      overflow: auto;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #fff;
  }

  .role-list {
    .role-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px;
      margin-bottom: 10px;
      background: rgba(102, 126, 234, 0.05);
      border: 1px solid rgba(102, 126, 234, 0.1);
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.3s;

      &:hover {
        background: rgba(102, 126, 234, 0.1);
      }
      &.active {
        background: rgba(102, 126, 234, 0.2);
        border-color: #667eea;
      }

      .role-info {
        .role-name {
          color: #fff;
          font-weight: 500;
          margin-bottom: 5px;
        }
        .role-desc {
          color: #8b8ba7;
          font-size: 12px;
        }
      }
    }
  }

  .permission-content {
    .role-basic-info {
      margin-bottom: 25px;

      ::v-deep .el-descriptions-item__label {
        background: rgba(102, 126, 234, 0.1);
        color: #8b8ba7;
      }
      ::v-deep .el-descriptions-item__content {
        background: transparent;
        color: #a8b2d1;
      }
      ::v-deep .el-descriptions {
        border-color: rgba(102, 126, 234, 0.2);
      }
    }

    .permission-tree-wrapper {
      h4 {
        color: #fff;
        margin-bottom: 15px;
        padding-bottom: 10px;
        border-bottom: 1px solid rgba(102, 126, 234, 0.1);
        display: flex;
        justify-content: space-between;
        align-items: center;

        ::v-deep .el-checkbox__label {
          color: #8b8ba7;
        }
      }

      ::v-deep .el-tree {
        background: transparent;
        color: #a8b2d1;

        .el-tree-node__content {
          height: 36px;
          &:hover {
            background: rgba(102, 126, 234, 0.1);
          }
        }
        .el-checkbox__inner {
          background: transparent;
          border-color: rgba(102, 126, 234, 0.5);
        }
        .el-checkbox__input.is-checked .el-checkbox__inner {
          background: #667eea;
          border-color: #667eea;
        }
      }

      .custom-tree-node {
        display: flex;
        align-items: center;
        gap: 8px;
        i {
          color: #667eea;
        }
      }
    }

    .role-actions-bottom {
      margin-top: 30px;
      padding-top: 20px;
      border-top: 1px solid rgba(102, 126, 234, 0.2);
    }
  }

  .no-selection {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #6b6b80;

    i {
      font-size: 48px;
      margin-bottom: 15px;
    }
    p {
      font-size: 14px;
    }
  }

  .danger-btn {
    color: #f56c6c;
  }
}

::v-deep .el-dialog {
  background: #1a1a2e;
  .el-dialog__header {
    border-bottom: 1px solid rgba(102, 126, 234, 0.2);
  }
  .el-dialog__title {
    color: #fff;
  }
  .el-form-item__label {
    color: #8b8ba7;
  }
  .el-input__inner,
  .el-textarea__inner {
    background: rgba(35, 35, 55, 0.8);
    border-color: rgba(102, 126, 234, 0.3);
    color: #fff;
  }
}
</style>
