<template>
  <div class="app-detail">
    <div class="page-header">
      <div class="header-left">
        <el-button icon="el-icon-arrow-left" @click="goBack">返回</el-button>
        <h2>{{ appInfo.name }}</h2>
        <el-tag :type="appInfo.status ? 'success' : 'info'" size="small">
          {{ appInfo.status ? '运行中' : '已禁用' }}
        </el-tag>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 凭证信息 -->
      <el-col :span="12">
        <el-card class="info-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-key"></i> 凭证信息</span>
          </div>
          <div class="credential-item">
            <label>Access Key</label>
            <div class="credential-value">
              <code>{{ appInfo.appKey }}</code>
              <el-button type="text" icon="el-icon-document-copy" @click="copyToClipboard(appInfo.appKey)">复制</el-button>
            </div>
          </div>
          <div class="credential-item">
            <label>Secret Key</label>
            <div class="credential-value">
              <code v-if="showSecret">{{ appInfo.appSecret }}</code>
              <code v-else>********************************</code>
              <el-button type="text" :icon="showSecret ? 'el-icon-view' : 'el-icon-hide'" @click="showSecret = !showSecret">
                {{ showSecret ? '隐藏' : '显示' }}
              </el-button>
              <el-button type="text" icon="el-icon-document-copy" @click="copyToClipboard(appInfo.appSecret)">复制</el-button>
            </div>
          </div>
          <div class="credential-actions">
            <el-button type="warning" size="small" @click="resetSecret">
              <i class="el-icon-refresh"></i> 重置密钥
            </el-button>
            <p class="warning-tip">重置后原密钥将立即失效，请及时更新客户端配置</p>
          </div>
        </el-card>
      </el-col>

      <!-- 应用统计 -->
      <el-col :span="12">
        <el-card class="info-card">
          <div slot="header" class="card-header">
            <span><i class="el-icon-s-data"></i> 调用统计</span>
          </div>
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ appInfo.todayCall }}</div>
                <div class="stat-label">今日调用</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value success">{{ appInfo.successRate > 0 ? appInfo.successRate + '%' : '-' }}</div>
                <div class="stat-label">成功率</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-box">
                <div class="stat-value">{{ appInfo.avgTime > 0 ? appInfo.avgTime + 'ms' : '-' }}</div>
                <div class="stat-label">平均耗时</div>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </el-col>
    </el-row>

    <!-- 权限分配 -->
    <el-card class="permission-card">
      <div slot="header" class="card-header">
        <span><i class="el-icon-setting"></i> 接口权限分配</span>
        <div class="header-actions">
          <el-checkbox v-model="checkAll" @change="handleCheckAllChange">全选</el-checkbox>
          <el-button type="primary" size="small" @click="savePermissions">保存权限</el-button>
        </div>
      </div>

      <el-tree
        ref="tree"
        :data="apiTree"
        :props="treeProps"
        show-checkbox
        node-key="id"
        default-expand-all
        :default-checked-keys="checkedKeys"
        @check-change="handleCheckChange">
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <span>
            <i :class="data.children ? 'el-icon-folder' : 'el-icon-connection'"></i>
            {{ node.label }}
          </span>
          <span v-if="!data.children" class="tree-extra">
            <el-tag :type="getMethodType(data.method)" size="mini">{{ data.method }}</el-tag>
            <span class="path">{{ data.path }}</span>
          </span>
        </span>
      </el-tree>
    </el-card>
  </div>
</template>

<script>
import { getAppById, resetAppSecret, getAuthorizedApis, savePermissions } from '@/api/customer'
import { getApiList } from '@/api/api'

export default {
  name: 'AppDetail',
  data() {
    return {
      appId: null,
      appInfo: {
        id: null,
        name: '',
        status: 1,
        appKey: '',
        appSecret: '',
        todayCall: 0,
        successRate: 0,
        avgTime: 0
      },
      showSecret: false,
      checkAll: false,
      checkedKeys: [],
      apiTree: [],
      treeProps: {
        children: 'children',
        label: 'label'
      },
      loading: false
    }
  },
  mounted() {
    this.appId = this.$route.params.id
    if (this.appId) {
      this.loadAppDetail()
      this.loadApiList()
      this.loadAuthorizedApis()
    }
  },
  methods: {
    // 加载应用详情
    async loadAppDetail() {
      this.loading = true
      try {
        const res = await getAppById(this.appId)
        if (res.code === 200) {
          this.appInfo = {
            ...res.data,
            todayCall: res.data.todayCall || 0,
            successRate: res.data.successRate || 0,
            avgTime: res.data.avgTime || 0
          }
        } else {
          this.$message.error(res.message || '获取应用详情失败')
        }
      } catch (error) {
        this.$message.error('获取应用详情失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    
    // 加载API列表
    async loadApiList() {
      try {
        // 不传分页参数，获取尽可能多的数据，或者传一个很大的pageSize
        const res = await getApiList({ page: 1, pageSize: 9999 })
        if (res.code === 200) {
          // 按平台分组API，注意数据在 data.list 中
          const apiList = res.data?.list || []
          this.apiTree = this.groupApisByPlatform(apiList)
        }
      } catch (error) {
        console.error('加载API列表失败:', error)
      }
    },
    
    // 按平台分组API
    groupApisByPlatform(apis) {
      const platformMap = {}
      apis.forEach(api => {
        const platformName = api.platformName || '未分类'
        if (!platformMap[platformName]) {
          platformMap[platformName] = {
            id: `platform_${api.platformId || 0}`,
            label: platformName,
            children: []
          }
        }
        platformMap[platformName].children.push({
          id: api.id,
          label: api.name,
          method: api.method,
          path: api.path
        })
      })
      return Object.values(platformMap)
    },
    
    // 加载已授权的API
    async loadAuthorizedApis() {
      try {
        const res = await getAuthorizedApis(this.appId)
        if (res.code === 200) {
          this.checkedKeys = res.data || []
          // 延迟设置选中状态，确保树已渲染
          this.$nextTick(() => {
            if (this.$refs.tree) {
              this.$refs.tree.setCheckedKeys(this.checkedKeys)
            }
          })
        }
      } catch (error) {
        console.error('加载授权列表失败:', error)
      }
    },
    
    goBack() {
      this.$router.push('/customer/apps')
    },
    
    copyToClipboard(text) {
      navigator.clipboard.writeText(text).then(() => {
        this.$message.success('已复制到剪贴板')
      })
    },
    
    // 重置密钥
    async resetSecret() {
      this.$confirm('确定要重置密钥吗？重置后原密钥将立即失效', '警告', {
        type: 'warning',
        confirmButtonText: '确定重置',
        cancelButtonText: '取消'
      }).then(async () => {
        try {
          const res = await resetAppSecret(this.appId)
          if (res.code === 200) {
            this.appInfo.appSecret = res.data.appSecret
            this.$message.success('密钥已重置')
            this.showSecret = true
          } else {
            this.$message.error(res.message || '重置失败')
          }
        } catch (error) {
          this.$message.error('重置失败: ' + error.message)
        }
      }).catch(() => {})
    },
    
    getMethodType(method) {
      const types = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
      return types[method] || 'info'
    },
    
    handleCheckAllChange(val) {
      if (val) {
        const allKeys = []
        this.getAllApiIds(this.apiTree, allKeys)
        this.$refs.tree.setCheckedKeys(allKeys)
      } else {
        this.$refs.tree.setCheckedKeys([])
      }
    },
    
    getAllApiIds(tree, result) {
      tree.forEach(node => {
        if (node.children) {
          this.getAllApiIds(node.children, result)
        } else {
          result.push(node.id)
        }
      })
    },
    
    handleCheckChange() {
      const checkedNodes = this.$refs.tree.getCheckedNodes()
      const checkedApiCount = checkedNodes.filter(node => !node.children).length
      const totalApiCount = this.getTotalApiCount()
      this.checkAll = checkedApiCount === totalApiCount
    },
    
    getTotalApiCount() {
      let count = 0
      const countApis = (nodes) => {
        nodes.forEach(node => {
          if (node.children) {
            countApis(node.children)
          } else {
            count++
          }
        })
      }
      countApis(this.apiTree)
      return count
    },
    
    // 保存权限
    async savePermissions() {
      try {
        const checkedNodes = this.$refs.tree.getCheckedNodes()
        // 只保存叶子节点（API），过滤掉分组节点
        const apiIds = checkedNodes.filter(node => !node.children).map(node => node.id)
        
        const res = await savePermissions({
          appId: this.appId,
          apiIds: apiIds
        })
        
        if (res.code === 200) {
          this.$message.success('权限保存成功')
          this.loadAppDetail() // 重新加载统计信息
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败: ' + error.message)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.app-detail {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 15px;

      h2 { color: #fff; margin: 0; font-size: 20px; }
    }
  }

  .info-card,
  .permission-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;
    margin-bottom: 20px;

    ::v-deep .el-card__header {
      padding: 15px 20px;
      border-bottom: 1px solid rgba(102, 126, 234, 0.2);
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      color: #fff;

      i { margin-right: 8px; color: #667eea; }

      .header-actions {
        display: flex;
        align-items: center;
        gap: 15px;
      }
    }
  }

  .credential-item {
    margin-bottom: 20px;

    label {
      display: block;
      color: #8b8ba7;
      font-size: 13px;
      margin-bottom: 8px;
    }

    .credential-value {
      display: flex;
      align-items: center;
      gap: 10px;

      code {
        flex: 1;
        background: rgba(0, 0, 0, 0.3);
        padding: 10px 15px;
        border-radius: 6px;
        font-family: monospace;
        color: #a8b2d1;
        font-size: 13px;
      }
    }
  }

  .credential-actions {
    padding-top: 15px;
    border-top: 1px solid rgba(102, 126, 234, 0.1);

    .warning-tip {
      color: #e6a23c;
      font-size: 12px;
      margin-top: 10px;
    }
  }

  .stat-box {
    text-align: center;
    padding: 20px;
    background: rgba(102, 126, 234, 0.05);
    border-radius: 8px;

    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #fff;
      margin-bottom: 5px;

      &.success { color: #67c23a; }
    }

    .stat-label {
      color: #8b8ba7;
      font-size: 13px;
    }
  }

  .permission-card {
    ::v-deep .el-tree {
      background: transparent;
      color: #fff;

      .el-tree-node__content {
        height: 40px;

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
      flex: 1;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding-right: 10px;

      i { margin-right: 8px; color: #667eea; }

      .tree-extra {
        display: flex;
        align-items: center;
        gap: 10px;

        .path {
          color: #6b6b80;
          font-size: 12px;
          font-family: monospace;
        }
      }
    }
  }
}

::v-deep .el-checkbox__label {
  color: #8b8ba7;
}
</style>
