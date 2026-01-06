<template>
  <div class="platform-list">
    <!-- 页面标题和操作 -->
    <div class="page-header">
      <h2>平台列表</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增平台接入</el-button>
    </div>

    <!-- 平台列表表格 -->
    <el-card class="table-card">
      <el-table :data="platformList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" label="平台名称" min-width="120">
          <template slot-scope="scope">
            <div class="platform-name">
              <i :class="getPlatformIcon(scope.row.code)" class="platform-icon"></i>
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="baseUrl" label="官网地址" min-width="200">
          <template slot-scope="scope">
            <a :href="scope.row.baseUrl" target="_blank" class="link">{{ scope.row.baseUrl }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="contact" label="主要联系人" min-width="100">
          <template slot-scope="scope">
            {{ scope.row.contact || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="environment" label="当前环境" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.environment === 'prod' ? 'danger' : 'warning'" size="small">
              {{ scope.row.environment === 'prod' ? '生产' : '测试' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" @click="handleEnvConfig(scope.row)">环境配置</el-button>
            <el-button type="text" size="small" class="danger-btn" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination class="pagination" @current-change="handlePageChange" :current-page="page" :page-size="pageSize" layout="total, prev, pager, next" :total="total">
      </el-pagination>
    </el-card>

    <!-- 新增/编辑抽屉 -->
    <el-drawer :title="drawerTitle" :visible.sync="drawerVisible" size="600px" :before-close="handleDrawerClose">
      <div class="drawer-content">
        <el-tabs v-model="activeTab">
          <!-- 基础信息 -->
          <el-tab-pane label="基础信息" name="basic">
            <el-form :model="form" :rules="rules" ref="form" label-width="100px">
              <el-form-item label="平台名称" prop="name">
                <el-input v-model="form.name" placeholder="请输入平台名称"></el-input>
              </el-form-item>
              <el-form-item label="平台编码" prop="code">
                <el-input v-model="form.code" placeholder="请输入平台编码（唯一标识）"></el-input>
              </el-form-item>
              <el-form-item label="官网地址" prop="baseUrl">
                <el-input v-model="form.baseUrl" placeholder="请输入官网URL"></el-input>
              </el-form-item>
              <el-form-item label="联系人">
                <el-input v-model="form.contact" placeholder="请输入联系人姓名"></el-input>
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="form.phone" placeholder="请输入联系电话"></el-input>
              </el-form-item>
              <el-form-item label="平台描述">
                <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入平台描述"></el-input>
              </el-form-item>
              <el-form-item label="状态">
                <el-switch v-model="form.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="禁用"></el-switch>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <!-- 鉴权配置 -->
          <el-tab-pane label="鉴权配置" name="auth">
            <el-form :model="form" label-width="120px">
              <el-form-item label="鉴权模式">
                <el-select v-model="form.authType" placeholder="请选择鉴权模式" style="width: 100%">
                  <el-option label="无需鉴权" value="none"></el-option>
                  <el-option label="OAuth2" value="oauth2"></el-option>
                  <el-option label="Header传参" value="header"></el-option>
                  <el-option label="API Key" value="apikey"></el-option>
                </el-select>
              </el-form-item>

              <!-- OAuth2配置 -->
              <template v-if="form.authType === 'oauth2'">
                <el-form-item label="Client ID">
                  <el-input v-model="form.authConfig.clientId" placeholder="请输入Client ID"></el-input>
                </el-form-item>
                <el-form-item label="Client Secret">
                  <el-input v-model="form.authConfig.clientSecret" type="password" show-password placeholder="请输入Client Secret"></el-input>
                </el-form-item>
                <el-form-item label="Token URL">
                  <el-input v-model="form.authConfig.tokenUrl" placeholder="请输入Token获取地址"></el-input>
                </el-form-item>
                <el-form-item label="Scope">
                  <el-input v-model="form.authConfig.scope" placeholder="请输入授权范围"></el-input>
                </el-form-item>
              </template>

              <!-- Header传参配置 -->
              <template v-if="form.authType === 'header'">
                <el-form-item label="请求头参数">
                  <div v-for="(item, index) in form.authConfig.headers" :key="index" class="header-item">
                    <el-input v-model="item.key" placeholder="Key" style="width: 45%"></el-input>
                    <el-input v-model="item.value" placeholder="Value" style="width: 45%"></el-input>
                    <el-button type="text" icon="el-icon-delete" @click="removeHeader(index)"></el-button>
                  </div>
                  <el-button type="text" icon="el-icon-plus" @click="addHeader">添加参数</el-button>
                </el-form-item>
              </template>

              <!-- API Key配置 -->
              <template v-if="form.authType === 'apikey'">
                <el-form-item label="API Key 名称">
                  <el-input v-model="form.authConfig.keyName" placeholder="如：X-API-Key"></el-input>
                </el-form-item>
                <el-form-item label="API Key 值">
                  <el-input v-model="form.authConfig.keyValue" type="password" show-password placeholder="请输入API Key"></el-input>
                </el-form-item>
                <el-form-item label="传递位置">
                  <el-radio-group v-model="form.authConfig.keyPosition">
                    <el-radio label="header">Header</el-radio>
                    <el-radio label="query">Query参数</el-radio>
                  </el-radio-group>
                </el-form-item>
              </template>
            </el-form>
          </el-tab-pane>

          <!-- 环境管理 -->
          <el-tab-pane label="环境管理" name="env">
            <el-form :model="form" label-width="120px">
              <el-divider content-position="left">测试环境</el-divider>
              <el-form-item label="Base URL">
                <el-input v-model="form.envConfig.test.baseUrl" placeholder="测试环境API基础地址"></el-input>
              </el-form-item>
              <el-form-item label="超时时间(ms)">
                <el-input-number v-model="form.envConfig.test.timeout" :min="1000" :max="60000" :step="1000"></el-input-number>
              </el-form-item>

              <el-divider content-position="left">生产环境</el-divider>
              <el-form-item label="Base URL">
                <el-input v-model="form.envConfig.prod.baseUrl" placeholder="生产环境API基础地址"></el-input>
              </el-form-item>
              <el-form-item label="超时时间(ms)">
                <el-input-number v-model="form.envConfig.prod.timeout" :min="1000" :max="60000" :step="1000"></el-input-number>
              </el-form-item>

              <el-form-item label="当前使用环境">
                <el-radio-group v-model="form.environment">
                  <el-radio label="test">测试环境</el-radio>
                  <el-radio label="prod">生产环境</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="drawer-footer">
          <el-button @click="handleDrawerClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getPlatformList, savePlatform, deletePlatform } from '@/api/platform'

export default {
  name: 'PlatformList',
  data() {
    return {
      loading: false,
      submitting: false,
      platformList: [],
      page: 1,
      pageSize: 10,
      total: 0,
      drawerVisible: false,
      drawerTitle: '新增平台',
      activeTab: 'basic',
      form: this.getDefaultForm(),
      rules: {
        name: [{ required: true, message: '请输入平台名称', trigger: 'blur' }],
        code: [{ required: true, message: '请输入平台编码', trigger: 'blur' }],
        baseUrl: [{ required: true, message: '请输入官网地址', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    getDefaultForm() {
      return {
        id: null,
        name: '',
        code: '',
        baseUrl: '',
        contact: '',
        phone: '',
        description: '',
        status: 1,
        authType: 'none',
        authConfig: {
          clientId: '',
          clientSecret: '',
          tokenUrl: '',
          scope: '',
          headers: [{ key: '', value: '' }],
          keyName: '',
          keyValue: '',
          keyPosition: 'header'
        },
        envConfig: {
          test: { baseUrl: '', timeout: 5000 },
          prod: { baseUrl: '', timeout: 5000 }
        },
        environment: 'test'
      }
    },
    async loadData() {
      this.loading = true
      try {
        const res = await getPlatformList({ page: this.page, pageSize: this.pageSize })
        if (res.code === 200) {
          this.platformList = res.data.list || res.data
          this.total = res.data.total || this.platformList.length
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    getPlatformIcon(code) {
      const icons = {
        baidu: 'el-icon-search',
        aliyun: 'el-icon-cloudy',
        tencent: 'el-icon-chat-dot-round',
        gaode: 'el-icon-location',
        wechat: 'el-icon-chat-round',
        alipay: 'el-icon-wallet'
      }
      return icons[code] || 'el-icon-link'
    },
    handleAdd() {
      this.form = this.getDefaultForm()
      this.drawerTitle = '新增平台'
      this.activeTab = 'basic'
      this.drawerVisible = true
    },
    handleEdit(row) {
      this.form = {
        ...this.getDefaultForm(),
        ...row,
        authConfig: row.authConfig ? JSON.parse(row.authConfig) : this.getDefaultForm().authConfig,
        envConfig: row.envConfig ? JSON.parse(row.envConfig) : this.getDefaultForm().envConfig
      }
      this.drawerTitle = '编辑平台'
      this.activeTab = 'basic'
      this.drawerVisible = true
    },
    handleEnvConfig(row) {
      this.handleEdit(row)
      this.activeTab = 'env'
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该平台吗？删除后相关API配置也将受影响', '提示', {
          type: 'warning'
        })
        const res = await deletePlatform(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadData()
        }
      } catch (e) {
        if (e !== 'cancel') console.error(e)
      }
    },
    handleDrawerClose() {
      this.drawerVisible = false
      this.form = this.getDefaultForm()
    },
    addHeader() {
      this.form.authConfig.headers.push({ key: '', value: '' })
    },
    removeHeader(index) {
      this.form.authConfig.headers.splice(index, 1)
    },
    async handleSubmit() {
      try {
        await this.$refs.form.validate()
        this.submitting = true
        const data = {
          ...this.form,
          authConfig: JSON.stringify(this.form.authConfig),
          envConfig: JSON.stringify(this.form.envConfig)
        }
        const res = await savePlatform(data)
        if (res.code === 200) {
          this.$message.success(this.form.id ? '修改成功' : '添加成功')
          this.drawerVisible = false
          this.loadData()
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.submitting = false
      }
    },
    handlePageChange(page) {
      this.page = page
      this.loadData()
    }
  }
}
</script>

<style lang="scss" scoped>
.platform-list {
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

  .table-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;

    ::v-deep .el-card__body {
      padding: 20px;
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

      .el-table__row--striped td {
        background: rgba(102, 126, 234, 0.05);
      }
    }
  }

  .platform-name {
    display: flex;
    align-items: center;
    gap: 8px;

    .platform-icon {
      font-size: 18px;
      color: #667eea;
    }
  }

  .link {
    color: #667eea;
    text-decoration: none;

    &:hover {
      text-decoration: underline;
    }
  }

  .danger-btn {
    color: #f56c6c !important;
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

.drawer-content {
  padding: 20px;
  height: calc(100% - 60px);
  overflow-y: auto;

  ::v-deep .el-tabs__item {
    color: #8b8ba7;

    &.is-active {
      color: #667eea;
    }
  }

  ::v-deep .el-tabs__active-bar {
    background-color: #667eea;
  }

  ::v-deep .el-form-item__label {
    color: #8b8ba7;
  }

  ::v-deep .el-input__inner,
  ::v-deep .el-textarea__inner {
    background: rgba(35, 35, 55, 0.8);
    border-color: rgba(102, 126, 234, 0.3);
    color: #fff;

    &::placeholder {
      color: #6b6b80;
    }
  }

  ::v-deep .el-divider__text {
    background: #1a1a2e;
    color: #8b8ba7;
  }

  .header-item {
    display: flex;
    gap: 10px;
    margin-bottom: 10px;
    align-items: center;
  }

  .drawer-footer {
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    padding: 15px 20px;
    background: rgba(35, 35, 55, 0.98);
    border-top: 1px solid rgba(102, 126, 234, 0.2);
    text-align: right;
  }
}

::v-deep .el-drawer {
  background: #1a1a2e;

  .el-drawer__header {
    color: #fff;
    border-bottom: 1px solid rgba(102, 126, 234, 0.2);
    margin-bottom: 0;
    padding: 20px;
  }
}
</style>
