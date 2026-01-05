<template>
  <div class="cache-page">
    <div class="page-header">
      <h2>缓存策略管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增缓存规则</el-button>
    </div>

    <el-card class="table-card">
      <el-table :data="cacheList" stripe style="width: 100%">
        <el-table-column prop="apiName" label="聚合API名称" min-width="180"></el-table-column>
        <el-table-column prop="cacheKey" label="缓存Key规则" min-width="220">
          <template slot-scope="scope">
            <code>{{ scope.row.cacheKey }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="ttl" label="缓存时长(TTL)" width="140">
          <template slot-scope="scope">
            <span class="highlight">{{ scope.row.ttl }}</span> 秒
          </template>
        </el-table-column>
        <el-table-column prop="hitRate" label="命中率" width="120">
          <template slot-scope="scope">
            <el-progress :percentage="scope.row.hitRate" :color="getHitRateColor(scope.row.hitRate)"></el-progress>
          </template>
        </el-table-column>
        <el-table-column prop="cacheSize" label="缓存大小" width="100">
          <template slot-scope="scope">{{ scope.row.cacheSize }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="text" size="small" style="color: #e6a23c" @click="clearCache(scope.row)">清除缓存</el-button>
            <el-button type="text" size="small" class="danger-btn" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="550px">
      <el-form :model="form" :rules="rules" ref="form" label-width="110px">
        <el-form-item label="聚合API" prop="apiId">
          <el-select v-model="form.apiId" style="width: 100%" placeholder="请选择">
            <el-option v-for="item in apiOptions" :key="item.id" :label="item.name" :value="item.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="缓存Key规则" prop="cacheKey">
          <el-input v-model="form.cacheKey" placeholder="${apiPath}:${params.userId}"></el-input>
          <div class="form-tip">支持变量：${apiPath}, ${userId}, ${appId}, ${params.xxx}</div>
        </el-form-item>
        <el-form-item label="缓存时长(秒)" prop="ttl">
          <el-input-number v-model="form.ttl" :min="1" :max="86400" style="width: 200px"></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getCacheRuleList, saveCacheRule, deleteCacheRule, clearCache } from '@/api/governance'
import { getAggregateList } from '@/api/api'

export default {
  name: 'CachePolicy',
  data() {
    return {
      cacheList: [],
      dialogVisible: false,
      dialogTitle: '新增缓存规则',
      form: { apiId: '', cacheKey: '', ttl: 300 },
      rules: {
        apiId: [{ required: true, message: '请选择聚合API', trigger: 'change' }],
        cacheKey: [{ required: true, message: '请输入缓存Key规则', trigger: 'blur' }],
        ttl: [{ required: true, message: '请设置缓存时长', trigger: 'blur' }]
      },
      apiOptions: [],
      loading: false
    }
  },
  mounted() {
    this.loadData()
    this.loadApiOptions()
  },
  methods: {
    async loadData() {
      try {
        this.loading = true
        const res = await getCacheRuleList()
        if (res.code === 200) {
          this.cacheList = res.data
        }
      } catch (error) {
        console.error('加载缓存规则失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadApiOptions() {
      try {
        const res = await getAggregateList()
        if (res.code === 200) {
          this.apiOptions = res.data.map((item) => ({ id: item.id, name: item.name }))
        }
      } catch (error) {
        console.error('加载API列表失败:', error)
      }
    },
    getHitRateColor(rate) {
      if (rate >= 80) return '#67c23a'
      if (rate >= 50) return '#e6a23c'
      return '#f56c6c'
    },
    handleAdd() {
      this.dialogTitle = '新增缓存规则'
      this.form = { apiId: '', cacheKey: '', ttl: 300 }
      if (this.$refs.form) {
        this.$refs.form.resetFields()
      }
      this.dialogVisible = true
    },
    handleEdit(row) {
      this.dialogTitle = '编辑缓存规则'
      this.form = { ...row }
      this.dialogVisible = true
    },
    handleSubmit() {
      this.$refs.form.validate(async (valid) => {
        if (valid) {
          try {
            const res = await saveCacheRule(this.form)
            if (res.code === 200) {
              this.$message.success('保存成功')
              this.dialogVisible = false
              this.loadData()
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
      this.$confirm(`确定要清除"${row.apiName}"的所有缓存吗？`, '提示', { type: 'warning' })
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
    handleDelete(row) {
      this.$confirm('确定要删除该规则吗？', '提示', { type: 'warning' })
        .then(async () => {
          try {
            const res = await deleteCacheRule(row.id)
            if (res.code === 200) {
              this.$message.success('删除成功')
              this.loadData()
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
.cache-page {
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
  }

  code {
    background: rgba(102, 126, 234, 0.1);
    padding: 2px 8px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 12px;
    color: #a8b2d1;
  }

  .highlight {
    color: #667eea;
    font-weight: 600;
  }
  .danger-btn {
    color: #f56c6c !important;
  }
}

::v-deep .el-dialog {
  background: #1a1a2e;
  border: 1px solid rgba(102, 126, 234, 0.3);
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
  .form-tip {
    color: #6b6b80;
    font-size: 12px;
    margin-top: 5px;
  }
}
</style>
