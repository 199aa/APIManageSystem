<template>
  <div class="logs-page">
    <div class="page-header">
      <h2>操作日志</h2>
      <el-button type="default" icon="el-icon-download" size="small" @click="handleExport">导出日志</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="时间范围">
          <el-date-picker v-model="searchForm.dateRange" type="daterange" range-separator="至" start-placeholder="开始日期" end-placeholder="结束日期" size="small" style="width: 240px">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="操作用户">
          <el-input v-model="searchForm.username" placeholder="用户名" size="small" style="width: 120px"></el-input>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="searchForm.operType" placeholder="全部" clearable size="small" style="width: 120px">
            <el-option label="登录" value="LOGIN"></el-option>
            <el-option label="新增" value="CREATE"></el-option>
            <el-option label="修改" value="UPDATE"></el-option>
            <el-option label="删除" value="DELETE"></el-option>
            <el-option label="导出" value="EXPORT"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="操作模块">
          <el-select v-model="searchForm.module" placeholder="全部" clearable size="small" style="width: 120px">
            <el-option label="用户管理" value="user"></el-option>
            <el-option label="平台管理" value="platform"></el-option>
            <el-option label="API管理" value="api"></el-option>
            <el-option label="应用管理" value="app"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable size="small" style="width: 100px">
            <el-option label="成功" :value="1"></el-option>
            <el-option label="失败" :value="0"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志表格 -->
    <el-card class="table-card">
      <el-table :data="logList" v-loading="loading" style="width: 100%" @row-click="showDetail">
        <el-table-column prop="operTime" label="操作时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.operTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="操作用户" width="120"></el-table-column>
        <el-table-column prop="operType" label="操作类型" width="100">
          <template slot-scope="scope">
            <el-tag :type="getOperTypeTag(scope.row.operType)" size="small">
              {{ getOperTypeLabel(scope.row.operType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="操作模块" width="120">
          <template slot-scope="scope">
            {{ getModuleLabel(scope.row.module) }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="操作描述" min-width="200"></el-table-column>
        <el-table-column prop="ip" label="操作IP" width="140"></el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status ? 'success' : 'danger'" size="small">
              {{ scope.row.status ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="100">
          <template slot-scope="scope">
            <span>{{ scope.row.costTime }}ms</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" @size-change="handleSizeChange" @current-change="handlePageChange" :current-page="page" :page-sizes="[20, 50, 100]" :page-size="pageSize" layout="total, sizes, prev, pager, next" :total="total">
      </el-pagination>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer title="操作日志详情" :visible.sync="detailVisible" size="550px">
      <div class="log-detail" v-if="currentLog">
        <div class="detail-header">
          <el-tag :type="currentLog.status ? 'success' : 'danger'" effect="dark">
            {{ currentLog.status ? '成功' : '失败' }}
          </el-tag>
          <span class="detail-time">{{ formatTime(currentLog.operTime) }}</span>
        </div>

        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <label>操作用户</label>
              <span>{{ currentLog.username }}</span>
            </div>
            <div class="info-item">
              <label>操作类型</label>
              <span>
                <el-tag :type="getOperTypeTag(currentLog.operType)" size="small">
                  {{ getOperTypeLabel(currentLog.operType) }}
                </el-tag>
              </span>
            </div>
            <div class="info-item">
              <label>操作模块</label>
              <span>{{ getModuleLabel(currentLog.module) }}</span>
            </div>
            <div class="info-item">
              <label>请求耗时</label>
              <span>{{ currentLog.costTime }}ms</span>
            </div>
            <div class="info-item">
              <label>操作IP</label>
              <span>{{ currentLog.ip }}</span>
            </div>
            <div class="info-item">
              <label>浏览器</label>
              <span>{{ currentLog.browser }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>操作描述</h4>
          <p>{{ currentLog.description }}</p>
        </div>

        <div class="detail-section">
          <h4>请求信息</h4>
          <div class="code-block-wrapper">
            <div class="code-label">请求URL</div>
            <pre class="code-block">{{ currentLog.requestMethod }} {{ currentLog.requestUrl }}</pre>
          </div>
          <div class="code-block-wrapper" v-if="currentLog.requestParams">
            <div class="code-label">请求参数</div>
            <pre class="code-block">{{ formatJson(currentLog.requestParams) }}</pre>
          </div>
        </div>

        <div class="detail-section" v-if="currentLog.responseResult">
          <h4>响应结果</h4>
          <pre class="code-block">{{ formatJson(currentLog.responseResult) }}</pre>
        </div>

        <div class="detail-section" v-if="currentLog.errorMsg">
          <h4>错误信息</h4>
          <pre class="code-block error">{{ currentLog.errorMsg }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getOperationLogList, getOperationLogById, exportOperationLogs } from '@/api/system'

export default {
  name: 'OperationLogs',
  data() {
    return {
      loading: false,
      searchForm: {
        dateRange: [],
        username: '',
        operType: '',
        module: '',
        status: ''
      },
      logList: [],
      page: 1,
      pageSize: 20,
      total: 0,
      detailVisible: false,
      currentLog: null
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    loadData() {
      this.loading = true
      const params = {
        page: this.page,
        pageSize: this.pageSize,
        username: this.searchForm.username || undefined,
        operType: this.searchForm.operType || undefined,
        module: this.searchForm.module || undefined,
        status: this.searchForm.status !== '' ? this.searchForm.status : undefined
      }

      // 处理日期范围
      if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
        params.startDate = this.formatDateParam(this.searchForm.dateRange[0])
        params.endDate = this.formatDateParam(this.searchForm.dateRange[1])
      }

      getOperationLogList(params)
        .then((res) => {
          if (res.code === 200) {
            this.logList = res.data.list || []
            this.total = res.data.total || 0
          } else {
            this.$message.error(res.message || '加载失败')
          }
        })
        .catch((err) => {
          this.$message.error('加载操作日志失败')
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    formatDateParam(date) {
      const d = new Date(date)
      const year = d.getFullYear()
      const month = String(d.getMonth() + 1).padStart(2, '0')
      const day = String(d.getDate()).padStart(2, '0')
      const hour = String(d.getHours()).padStart(2, '0')
      const minute = String(d.getMinutes()).padStart(2, '0')
      const second = String(d.getSeconds()).padStart(2, '0')
      return `${year}-${month}-${day} ${hour}:${minute}:${second}`
    },
    formatTime(time) {
      return time ? new Date(time).toLocaleString() : '-'
    },
    formatJson(str) {
      try {
        return JSON.stringify(JSON.parse(str), null, 2)
      } catch {
        return str
      }
    },
    getOperTypeLabel(type) {
      const labels = { LOGIN: '登录', CREATE: '新增', UPDATE: '修改', DELETE: '删除', EXPORT: '导出' }
      return labels[type] || type
    },
    getOperTypeTag(type) {
      const tags = { LOGIN: 'info', CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', EXPORT: '' }
      return tags[type] || ''
    },
    getModuleLabel(module) {
      const labels = { user: '用户管理', platform: '平台管理', api: 'API管理', app: '应用管理' }
      return labels[module] || module
    },
    showDetail(row) {
      getOperationLogById(row.id)
        .then((res) => {
          if (res.code === 200) {
            this.currentLog = res.data
            this.detailVisible = true
          } else {
            this.$message.error(res.message || '获取详情失败')
          }
        })
        .catch((err) => {
          this.$message.error('获取日志详情失败')
          console.error(err)
        })
    },
    handleSearch() {
      this.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = { dateRange: [], username: '', operType: '', module: '', status: '' }
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
    async handleExport() {
      try {
        this.$message.info('正在导出日志...')
        
        const params = {
          username: this.searchForm.username || undefined,
          operType: this.searchForm.operType || undefined,
          module: this.searchForm.module || undefined,
          status: this.searchForm.status !== '' ? this.searchForm.status : undefined
        }

        // 处理日期范围
        if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
          params.startDate = this.formatDateParam(this.searchForm.dateRange[0])
          params.endDate = this.formatDateParam(this.searchForm.dateRange[1])
        }

        const res = await exportOperationLogs(params)
        if (res.code === 200 && res.data) {
          // 添加BOM头以支持Excel正确显示中文
          const BOM = '\uFEFF'
          const csvData = BOM + res.data
          
          // 创建CSV文件并下载
          const blob = new Blob([csvData], { type: 'text/csv;charset=utf-8;' })
          const link = document.createElement('a')
          const url = URL.createObjectURL(blob)
          const timestamp = new Date().getTime()
          link.setAttribute('href', url)
          link.setAttribute('download', `操作日志_${timestamp}.csv`)
          link.style.visibility = 'hidden'
          document.body.appendChild(link)
          link.click()
          document.body.removeChild(link)
          URL.revokeObjectURL(url)
          this.$message.success('导出成功')
        } else {
          this.$message.error(res.message || '导出失败')
        }
      } catch (err) {
        this.$message.error('导出日志失败: ' + (err.message || ''))
        console.error('导出错误:', err)
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.logs-page {
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
      cursor: pointer;
    }
    tr {
      background: transparent;
      &:hover > td {
        background: rgba(102, 126, 234, 0.1);
      }
    }
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

::v-deep .el-drawer {
  background: #1a1a2e;
  .el-drawer__header {
    color: #fff;
    border-bottom: 1px solid rgba(102, 126, 234, 0.2);
  }
}

.log-detail {
  padding: 20px;

  .detail-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 25px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(102, 126, 234, 0.2);

    .detail-time {
      color: #8b8ba7;
    }
  }

  .detail-section {
    margin-bottom: 25px;

    h4 {
      color: #fff;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid rgba(102, 126, 234, 0.1);
    }

    p {
      color: #a8b2d1;
      line-height: 1.6;
    }
  }

  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 15px;

    .info-item {
      label {
        display: block;
        color: #6b6b80;
        font-size: 12px;
        margin-bottom: 5px;
      }
      span {
        color: #a8b2d1;
        font-size: 13px;
      }
    }
  }

  .code-block-wrapper {
    margin-bottom: 15px;

    .code-label {
      color: #6b6b80;
      font-size: 12px;
      margin-bottom: 8px;
    }
  }

  .code-block {
    background: rgba(0, 0, 0, 0.3);
    padding: 15px;
    border-radius: 8px;
    color: #a8b2d1;
    font-family: monospace;
    font-size: 12px;
    overflow: auto;
    max-height: 200px;
    white-space: pre-wrap;
    word-break: break-all;

    &.error {
      color: #f56c6c;
      border-left: 3px solid #f56c6c;
    }
  }
}
</style>
