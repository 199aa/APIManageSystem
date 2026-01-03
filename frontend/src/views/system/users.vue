<template>
  <div class="users-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="showDialog()">新建用户</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="请输入用户名" size="small" clearable></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleId" placeholder="全部" clearable size="small" style="width: 150px">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable size="small" style="width: 100px">
            <el-option label="启用" :value="1"></el-option>
            <el-option label="禁用" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 用户表格 -->
    <el-card class="table-card">
      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="avatar" label="头像" width="80">
          <template slot-scope="scope">
            <el-avatar :size="40" :src="scope.row.avatar">
              <i class="el-icon-user"></i>
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="120"></el-table-column>
        <el-table-column prop="realName" label="真实姓名" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180"></el-table-column>
        <el-table-column prop="phone" label="手机号" width="130"></el-table-column>
        <el-table-column prop="roleName" label="角色" width="120">
          <template slot-scope="scope">
            <el-tag size="small" :type="getRoleType(scope.row.roleId)">{{ scope.row.roleName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="toggleStatus(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160">
          <template slot-scope="scope">
            {{ formatTime(scope.row.lastLoginTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="resetPassword(scope.row)">重置密码</el-button>
            <el-button type="text" size="small" class="danger-btn" @click="deleteUser(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" @size-change="handleSizeChange" @current-change="handlePageChange" :current-page="page" :page-sizes="[10, 20, 50]" :page-size="pageSize" layout="total, sizes, prev, pager, next" :total="total">
      </el-pagination>
    </el-card>

    <!-- 新建/编辑对话框 -->
    <el-dialog :title="form.id ? '编辑用户' : '新建用户'" :visible.sync="dialogVisible" width="550px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="form.username" placeholder="请输入用户名" :disabled="!!form.id"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20" v-if="!form.id">
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="请确认密码"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="form.phone" placeholder="请输入手机号"></el-input>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="角色" prop="roleId">
          <el-select v-model="form.roleId" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveUser">保存</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getUserList, getAllRoles, createUser, updateUser, deleteUser, updateUserStatus, resetUserPassword } from '@/api/system'

export default {
  name: 'UserManagement',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.form.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }
    return {
      loading: false,
      searchForm: { username: '', roleId: '', status: '' },
      userList: [],
      page: 1,
      pageSize: 10,
      total: 0,
      roleList: [],
      dialogVisible: false,
      form: {
        id: null,
        username: '',
        realName: '',
        password: '',
        confirmPassword: '',
        email: '',
        phone: '',
        roleId: '',
        status: 1
      },
      formRules: {
        username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
        realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ],
        email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
        roleId: [{ required: true, message: '请选择角色', trigger: 'change' }]
      }
    }
  },
  created() {
    this.loadRoles()
    this.loadData()
  },
  methods: {
    loadRoles() {
      getAllRoles()
        .then((res) => {
          if (res.code === 200) {
            this.roleList = res.data
          }
        })
        .catch((err) => {
          console.error('加载角色列表失败', err)
        })
    },
    loadData() {
      this.loading = true
      const params = {
        page: this.page,
        pageSize: this.pageSize,
        username: this.searchForm.username || undefined,
        roleId: this.searchForm.roleId || undefined,
        status: this.searchForm.status !== '' ? this.searchForm.status : undefined
      }
      getUserList(params)
        .then((res) => {
          if (res.code === 200) {
            this.userList = res.data.list || []
            this.total = res.data.total || 0
          } else {
            this.$message.error(res.message || '加载失败')
          }
        })
        .catch((err) => {
          this.$message.error('加载用户列表失败')
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    formatTime(time) {
      return time ? new Date(time).toLocaleString() : '-'
    },
    getRoleType(roleId) {
      const types = { 1: 'danger', 2: 'warning', 3: '', 4: 'success', 5: 'info' }
      return types[roleId] || ''
    },
    handleSearch() {
      this.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = { username: '', roleId: '', status: '' }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadData()
    },
    handlePageChange(page) {
      this.page = page
      this.loadData()
    },
    showDialog(row) {
      if (row) {
        this.form = { ...row, password: '', confirmPassword: '' }
      } else {
        this.form = { id: null, username: '', realName: '', password: '', confirmPassword: '', email: '', phone: '', roleId: '', status: 1 }
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.formRef && this.$refs.formRef.clearValidate()
      })
    },
    saveUser() {
      this.$refs.formRef.validate((valid) => {
        if (valid) {
          const apiMethod = this.form.id ? updateUser : createUser
          const data = { ...this.form }
          delete data.confirmPassword
          if (this.form.id) {
            delete data.password
          }

          apiMethod(data)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success(this.form.id ? '用户更新成功' : '用户创建成功')
                this.dialogVisible = false
                this.loadData()
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
    toggleStatus(row) {
      updateUserStatus(row.id, row.status)
        .then((res) => {
          if (res.code === 200) {
            this.$message.success(`用户已${row.status ? '启用' : '禁用'}`)
          } else {
            this.$message.error(res.message || '操作失败')
            row.status = row.status === 1 ? 0 : 1
          }
        })
        .catch((err) => {
          this.$message.error('状态更新失败')
          row.status = row.status === 1 ? 0 : 1
          console.error(err)
        })
    },
    resetPassword(row) {
      this.$confirm(`确定重置用户 "${row.username}" 的密码？密码将重置为 123456`, '提示', { type: 'warning' })
        .then(() => {
          resetUserPassword(row.id)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success('密码已重置为 123456')
              } else {
                this.$message.error(res.message || '重置失败')
              }
            })
            .catch((err) => {
              this.$message.error('密码重置失败')
              console.error(err)
            })
        })
        .catch(() => {})
    },
    deleteUser(row) {
      this.$confirm(`确定删除用户 "${row.username}"？此操作不可恢复`, '警告', { type: 'warning' })
        .then(() => {
          deleteUser(row.id)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success('用户已删除')
                this.loadData()
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
.users-page {
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

  .filter-card,
  .table-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;
    margin-bottom: 20px;
  }

  .search-form {
    ::v-deep .el-form-item__label {
      color: #8b8ba7;
    }
    ::v-deep .el-input__inner,
    ::v-deep .el-select .el-input__inner {
      background: rgba(35, 35, 55, 0.8);
      border-color: rgba(102, 126, 234, 0.3);
      color: #fff;
    }
  }

  ::v-deep .el-table {
    background: transparent;
    color: #fff;
    &::before {
      display: none;
    }
    th {
      background: rgba(102, 126, 234, 0.1);
      color: #8b8ba7;
      border-bottom: 1px solid rgba(102, 126, 234, 0.2);
    }
    td {
      border-bottom: 1px solid rgba(102, 126, 234, 0.1);
    }
    tr {
      background: transparent;
      &:hover > td {
        background: rgba(102, 126, 234, 0.1);
      }
    }
  }

  .danger-btn {
    color: #f56c6c;
  }

  .pagination {
    margin-top: 20px;
    text-align: right;
    ::v-deep .el-pagination__total,
    ::v-deep .btn-prev,
    ::v-deep .btn-next,
    ::v-deep .el-pager li {
      background: transparent;
      color: #8b8ba7;
    }
    ::v-deep .el-pager li.active {
      color: #667eea;
    }
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
  .el-input__inner {
    background: rgba(35, 35, 55, 0.8);
    border-color: rgba(102, 126, 234, 0.3);
    color: #fff;
  }
}
</style>
