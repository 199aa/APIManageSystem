<template>
  <div class="governance-page">
    <div class="page-header">
      <h2>访问策略管理</h2>
    </div>

    <el-card class="tabs-card">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 限流策略 -->
        <el-tab-pane label="限流策略" name="rateLimit">
          <div class="tab-header">
            <span>限流规则列表</span>
            <el-button type="primary" size="small" icon="el-icon-plus" @click="addRateLimit">新增规则</el-button>
          </div>
          <el-table :data="rateLimitList" stripe style="width: 100%">
            <el-table-column prop="name" label="规则名称" min-width="150"></el-table-column>
            <el-table-column prop="target" label="针对对象" min-width="200">
              <template slot-scope="scope">
                <el-tag size="small" type="info">{{ scope.row.targetType }}</el-tag>
                {{ scope.row.targetName }}
              </template>
            </el-table-column>
            <el-table-column prop="limitValue" label="限流阈值" width="120">
              <template slot-scope="scope">
                <span class="highlight">{{ scope.row.limitValue }}</span> QPS
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template slot-scope="scope">
                <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="updateStatus(scope.row)"></el-switch>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="editRateLimit(scope.row)">编辑</el-button>
                <el-button type="text" size="small" class="danger-btn" @click="deleteRateLimit(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 黑白名单 -->
        <el-tab-pane label="黑白名单" name="blacklist">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="list-section">
                <h4><i class="el-icon-close" style="color: #f56c6c"></i> IP黑名单</h4>
                <p class="tip">每行一个IP地址，支持CIDR格式</p>
                <el-input type="textarea" v-model="blacklistIPs" :rows="10" placeholder="192.168.1.100&#10;10.0.0.0/24"></el-input>
                <el-button type="primary" size="small" style="margin-top: 10px" @click="saveBlacklist">保存黑名单</el-button>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="list-section">
                <h4><i class="el-icon-check" style="color: #67c23a"></i> IP白名单</h4>
                <p class="tip">每行一个IP地址，白名单中的IP将跳过所有限制</p>
                <el-input type="textarea" v-model="whitelistIPs" :rows="10" placeholder="192.168.1.1&#10;127.0.0.1"></el-input>
                <el-button type="primary" size="small" style="margin-top: 10px" @click="saveWhitelist">保存白名单</el-button>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>

        <!-- 缓存策略 -->
        <el-tab-pane label="缓存策略" name="cache">
          <div class="tab-header">
            <span>缓存规则列表</span>
            <el-button type="primary" size="small" icon="el-icon-plus" @click="addCacheRule">新增规则</el-button>
          </div>
          <el-table :data="cacheList" stripe style="width: 100%">
            <el-table-column prop="apiName" label="聚合API名称" min-width="180"></el-table-column>
            <el-table-column prop="cacheKey" label="缓存Key规则" min-width="200">
              <template slot-scope="scope">
                <code>{{ scope.row.cacheKey }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="ttl" label="缓存时长(TTL)" width="130">
              <template slot-scope="scope">
                <span class="highlight">{{ scope.row.ttl }}</span> 秒
              </template>
            </el-table-column>
            <el-table-column prop="hitRate" label="命中率" width="100">
              <template slot-scope="scope">
                <span :class="scope.row.hitRate > 50 ? 'success-text' : 'warning-text'">
                  {{ scope.row.hitRate }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template slot-scope="scope">
                <el-button type="text" size="small" @click="editCacheRule(scope.row)">编辑</el-button>
                <el-button type="text" size="small" @click="clearCache(scope.row)">清除缓存</el-button>
                <el-button type="text" size="small" class="danger-btn" @click="deleteCacheRule(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 限流规则弹窗 -->
    <el-dialog :title="rateLimitDialogTitle" :visible.sync="rateLimitDialogVisible" width="500px">
      <el-form :model="rateLimitForm" :rules="rateLimitRules" ref="rateLimitForm" label-width="100px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="rateLimitForm.name" placeholder="如：高德API限流"></el-input>
        </el-form-item>
        <el-form-item label="对象类型" prop="targetType">
          <el-select v-model="rateLimitForm.targetType" style="width: 100%">
            <el-option label="特定API" value="api"></el-option>
            <el-option label="特定应用" value="app"></el-option>
            <el-option label="全局" value="global"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择对象" prop="targetId" v-if="rateLimitForm.targetType !== 'global'">
          <el-select v-model="rateLimitForm.targetId" style="width: 100%" placeholder="请选择">
            <el-option v-for="item in targetOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="限流阈值" prop="limitValue">
          <el-input-number v-model="rateLimitForm.limitValue" :min="1" :max="100000"></el-input-number>
          <span class="unit">QPS (每秒请求数)</span>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="rateLimitDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRateLimit">确定</el-button>
      </div>
    </el-dialog>

    <!-- 缓存规则弹窗 -->
    <el-dialog :title="cacheDialogTitle" :visible.sync="cacheDialogVisible" width="500px">
      <el-form :model="cacheForm" :rules="cacheRules" ref="cacheForm" label-width="100px">
        <el-form-item label="聚合API" prop="apiId">
          <el-select v-model="cacheForm.apiId" style="width: 100%" placeholder="请选择聚合API">
            <el-option v-for="item in aggregateApis" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="缓存Key" prop="cacheKey">
          <el-input v-model="cacheForm.cacheKey" placeholder="如：${apiPath}:${userId}"></el-input>
          <p class="tip">支持变量：${apiPath}, ${userId}, ${params.xxx}</p>
        </el-form-item>
        <el-form-item label="缓存时长" prop="ttl">
          <el-input-number v-model="cacheForm.ttl" :min="1" :max="86400"></el-input-number>
          <span class="unit">秒</span>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="cacheDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveCacheRule">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getRateLimitList, saveRateLimit, deleteRateLimit, updateRateLimitStatus } from '@/api/governance'
import { getBlacklist, saveBlacklist, saveWhitelist } from '@/api/governance'
import { getCacheRuleList, saveCacheRule, deleteCacheRule, clearCache } from '@/api/governance'
import { getAggregateList, getApiInfoList } from '@/api/api'

export default {
  name: 'RateLimit',
  data() {
    return {
      activeTab: 'rateLimit',
      loading: false,
      // 限流相关
      rateLimitList: [],
      rateLimitDialogVisible: false,
      rateLimitDialogTitle: '新增限流规则',
      rateLimitForm: { name: '', targetType: 'api', targetId: '', targetName: '', limitValue: 100, status: 1 },
      rateLimitRules: {
        name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
        limitValue: [{ required: true, message: '请设置限流阈值', trigger: 'blur' }]
      },
      targetOptions: [],
      // 黑白名单
      blacklistIPs: '',
      whitelistIPs: '',
      // 缓存相关
      cacheList: [],
      cacheDialogVisible: false,
      cacheDialogTitle: '新增缓存规则',
      cacheForm: { apiId: '', cacheKey: '', ttl: 300 },
      cacheRules: {
        apiId: [{ required: true, message: '请选择聚合API', trigger: 'change' }],
        ttl: [{ required: true, message: '请设置缓存时长', trigger: 'blur' }]
      },
      aggregateApis: []
    }
  },
  watch: {
    activeTab(val) {
      if (val === 'rateLimit') {
        this.loadRateLimitList()
      } else if (val === 'blacklist') {
        this.loadBlacklist()
      } else if (val === 'cache') {
        this.loadCacheList()
      }
    },
    'rateLimitForm.targetType'(val) {
      this.rateLimitForm.targetId = ''
      this.rateLimitForm.targetName = ''
      this.loadTargetOptions(val)
    }
  },
  mounted() {
    this.loadRateLimitList()
  },
  methods: {
    // ==================== 限流策略 ====================
    async loadRateLimitList() {
      try {
        this.loading = true
        const res = await getRateLimitList()
        if (res.code === 200) {
          this.rateLimitList = res.data
        }
      } catch (error) {
        console.error('加载限流规则失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadTargetOptions(type) {
      if (type === 'global') {
        this.targetOptions = []
        return
      }
      try {
        if (type === 'api') {
          const res = await getApiInfoList()
          if (res.code === 200) {
            this.targetOptions = res.data.map(item => ({ id: item.id, name: item.name }))
          }
        } else if (type === 'app') {
          this.targetOptions = [
            { id: 1, name: '示例应用1' },
            { id: 2, name: '示例应用2' }
          ]
        }
      } catch (error) {
        console.error('加载目标选项失败:', error)
      }
    },
    addRateLimit() {
      this.rateLimitDialogTitle = '新增限流规则'
      this.rateLimitForm = { name: '', targetType: 'api', targetId: '', targetName: '', limitValue: 100, status: 1 }
      if (this.$refs.rateLimitForm) {
        this.$refs.rateLimitForm.resetFields()
      }
      this.rateLimitDialogVisible = true
    },
    editRateLimit(row) {
      this.rateLimitDialogTitle = '编辑限流规则'
      this.rateLimitForm = { ...row }
      this.loadTargetOptions(row.targetType)
      this.rateLimitDialogVisible = true
    },
    saveRateLimit() {
      this.$refs.rateLimitForm.validate(async valid => {
        if (valid) {
          try {
            // 根据targetId查找targetName
            if (this.rateLimitForm.targetType !== 'global' && this.rateLimitForm.targetId) {
              const target = this.targetOptions.find(t => t.id === this.rateLimitForm.targetId)
              if (target) {
                this.rateLimitForm.targetName = target.name
              }
            } else if (this.rateLimitForm.targetType === 'global') {
              this.rateLimitForm.targetName = '全局'
              this.rateLimitForm.targetId = null
            }
            
            const res = await saveRateLimit(this.rateLimitForm)
            if (res.code === 200) {
              this.$message.success('保存成功')
              this.rateLimitDialogVisible = false
              this.loadRateLimitList()
            } else {
              this.$message.error(res.message || '保存失败')
            }
          } catch (error) {
            this.$message.error('保存失败')
          }
        }
      })
    },
    async deleteRateLimit(row) {
      this.$confirm('确定要删除该规则吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await deleteRateLimit(row.id)
            if (res.code === 200) {
              this.$message.success('删除成功')
              this.loadRateLimitList()
            } else {
              this.$message.error(res.message || '删除失败')
            }
          } catch (error) {
            this.$message.error('删除失败')
          }
        })
        .catch(() => {})
    },
    async updateStatus(row) {
      try {
        const res = await updateRateLimitStatus(row.id, row.status)
        if (res.code === 200) {
          this.$message.success('状态更新成功')
        } else {
          this.$message.error(res.message || '状态更新失败')
          row.status = row.status === 1 ? 0 : 1 // 回滚
        }
      } catch (error) {
        this.$message.error('状态更新失败')
        row.status = row.status === 1 ? 0 : 1 // 回滚
      }
    },
    
    // ==================== 黑白名单 ====================
    async loadBlacklist() {
      try {
        const res = await getBlacklist()
        if (res.code === 200) {
          this.blacklistIPs = res.data.blacklist ? res.data.blacklist.join('\n') : ''
          this.whitelistIPs = res.data.whitelist ? res.data.whitelist.join('\n') : ''
        }
      } catch (error) {
        console.error('加载黑白名单失败:', error)
      }
    },
    async saveBlacklist() {
      try {
        const ips = this.blacklistIPs.split('\n').filter(ip => ip.trim())
        const res = await saveBlacklist(ips)
        if (res.code === 200) {
          this.$message.success('黑名单保存成功')
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        this.$message.error('黑名单保存失败')
      }
    },
    async saveWhitelist() {
      try {
        const ips = this.whitelistIPs.split('\n').filter(ip => ip.trim())
        const res = await saveWhitelist(ips)
        if (res.code === 200) {
          this.$message.success('白名单保存成功')
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        this.$message.error('白名单保存失败')
      }
    },
    
    // ==================== 缓存策略 ====================
    async loadCacheList() {
      try {
        this.loading = true
        const res = await getCacheRuleList()
        if (res.code === 200) {
          this.cacheList = res.data
        }
        
        // 加载聚合API列表
        const apiRes = await getAggregateList()
        if (apiRes.code === 200) {
          this.aggregateApis = apiRes.data.map(item => ({ id: item.id, name: item.name }))
        }
      } catch (error) {
        console.error('加载缓存规则失败:', error)
      } finally {
        this.loading = false
      }
    },
    addCacheRule() {
      this.cacheDialogTitle = '新增缓存规则'
      this.cacheForm = { apiId: '', cacheKey: '', ttl: 300 }
      if (this.$refs.cacheForm) {
        this.$refs.cacheForm.resetFields()
      }
      this.cacheDialogVisible = true
    },
    editCacheRule(row) {
      this.cacheDialogTitle = '编辑缓存规则'
      this.cacheForm = { ...row }
      this.cacheDialogVisible = true
    },
    saveCacheRule() {
      this.$refs.cacheForm.validate(async valid => {
        if (valid) {
          try {
            const res = await saveCacheRule(this.cacheForm)
            if (res.code === 200) {
              this.$message.success('保存成功')
              this.cacheDialogVisible = false
              this.loadCacheList()
            } else {
              this.$message.error(res.message || '保存失败')
            }
          } catch (error) {
            this.$message.error('保存失败')
          }
        }
      })
    },
    clearCache(row) {
      this.$confirm('确定要清除该API的所有缓存吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await clearCache(row.id)
            if (res.code === 200) {
              this.$message.success('缓存已清除')
            } else {
              this.$message.error(res.message || '清除失败')
            }
          } catch (error) {
            this.$message.error('缓存清除失败')
          }
        })
        .catch(() => {})
    },
    deleteCacheRule(row) {
      this.$confirm('确定要删除该规则吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await deleteCacheRule(row.id)
            if (res.code === 200) {
              this.$message.success('删除成功')
              this.loadCacheList()
            } else {
              this.$message.error(res.message || '删除失败')
            }
          } catch (error) {
            this.$message.error('删除失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.governance-page {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
    h2 { color: #fff; margin: 0; font-size: 20px; }
  }

  .tabs-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;

    ::v-deep .el-tabs--border-card {
      background: transparent;
      border: none;

      .el-tabs__header {
        background: rgba(102, 126, 234, 0.1);
        border-bottom: 1px solid rgba(102, 126, 234, 0.2);
      }

      .el-tabs__item {
        color: #8b8ba7;
        border: none;
        &.is-active { color: #667eea; background: rgba(102, 126, 234, 0.2); }
        &:hover { color: #667eea; }
      }

      .el-tabs__content { padding: 20px; }
    }

    ::v-deep .el-table {
      background: transparent;
      color: #fff;
      &::before { display: none; }
      th { background: rgba(102, 126, 234, 0.1); color: #8b8ba7; border-bottom: 1px solid rgba(102, 126, 234, 0.2); }
      td { border-bottom: 1px solid rgba(102, 126, 234, 0.1); }
      tr { background: transparent; &:hover > td { background: rgba(102, 126, 234, 0.1); } }
      .el-table__row--striped td { background: rgba(102, 126, 234, 0.05); }
    }
  }

  .tab-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    span { color: #fff; font-weight: 500; }
  }

  .list-section {
    background: rgba(102, 126, 234, 0.05);
    padding: 20px;
    border-radius: 8px;
    h4 { color: #fff; margin: 0 0 10px; display: flex; align-items: center; gap: 8px; }
    .tip { color: #6b6b80; font-size: 12px; margin-bottom: 10px; }
  }

  .highlight { color: #667eea; font-weight: 600; }
  .success-text { color: #67c23a; }
  .warning-text { color: #e6a23c; }
  .danger-btn { color: #f56c6c !important; }
  .unit { color: #8b8ba7; margin-left: 10px; }

  code {
    background: rgba(102, 126, 234, 0.1);
    padding: 2px 8px;
    border-radius: 4px;
    font-family: monospace;
    color: #a8b2d1;
  }
}

::v-deep .el-dialog {
  background: #1a1a2e;
  border: 1px solid rgba(102, 126, 234, 0.3);
  .el-dialog__header { border-bottom: 1px solid rgba(102, 126, 234, 0.2); }
  .el-dialog__title { color: #fff; }
  .el-form-item__label { color: #8b8ba7; }
  .el-input__inner, .el-textarea__inner {
    background: rgba(35, 35, 55, 0.8);
    border-color: rgba(102, 126, 234, 0.3);
    color: #fff;
  }
  .tip { color: #6b6b80; font-size: 12px; margin-top: 5px; }
}

::v-deep .el-textarea__inner {
  background: rgba(35, 35, 55, 0.8);
  border-color: rgba(102, 126, 234, 0.3);
  color: #fff;
}
</style>
