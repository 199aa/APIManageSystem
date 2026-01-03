<template>
  <div class="logs-page">
    <div class="page-header">
      <h2>调用日志</h2>
    </div>

    <!-- 搜索栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="时间范围">
          <el-date-picker v-model="searchForm.dateRange" type="datetimerange" range-separator="至" start-placeholder="开始时间" end-placeholder="结束时间" size="small" style="width: 340px">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="TraceID">
          <el-input v-model="searchForm.traceId" placeholder="请输入TraceID" size="small" style="width: 200px"></el-input>
        </el-form-item>
        <el-form-item label="API路径">
          <el-input v-model="searchForm.apiPath" placeholder="请输入路径" size="small" style="width: 150px"></el-input>
        </el-form-item>
        <el-form-item label="状态码">
          <el-select v-model="searchForm.statusCode" placeholder="全部" clearable size="small" style="width: 100px">
            <el-option label="200" value="200"></el-option>
            <el-option label="400" value="400"></el-option>
            <el-option label="401" value="401"></el-option>
            <el-option label="500" value="500"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="耗时">
          <el-input v-model="searchForm.minTime" placeholder=">" size="small" style="width: 80px"></el-input>
          <span style="color: #8b8ba7; margin: 0 5px">ms</span>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" size="small" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" size="small" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 日志表格 -->
    <el-card class="table-card">
      <el-table :data="logList" v-loading="loading" stripe style="width: 100%" @row-click="showDetail">
        <el-table-column prop="callTime" label="请求时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.callTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="traceId" label="TraceID" width="200">
          <template slot-scope="scope">
            <span class="trace-id" @click.stop="copyTraceId(scope.row.traceId)">
              {{ scope.row.traceId }}
              <i class="el-icon-document-copy"></i>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="apiPath" label="请求API" min-width="200">
          <template slot-scope="scope">
            <el-tag :type="getMethodType(scope.row.method)" size="mini" effect="dark">{{ scope.row.method }}</el-tag>
            <code class="api-path">{{ scope.row.apiPath }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="appName" label="调用方" width="120"></el-table-column>
        <el-table-column prop="statusCode" label="状态码" width="100">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.statusCode)" size="small">
              {{ scope.row.statusCode }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responseTime" label="耗时" width="100">
          <template slot-scope="scope">
            <span :class="getTimeClass(scope.row.responseTime)">
              {{ scope.row.responseTime }}ms
            </span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" @size-change="handleSizeChange" @current-change="handlePageChange" :current-page="page" :page-sizes="[20, 50, 100]" :page-size="pageSize" layout="total, sizes, prev, pager, next" :total="total">
      </el-pagination>
    </el-card>

    <!-- 详情抽屉 -->
    <el-drawer title="调用详情" :visible.sync="detailVisible" size="650px" direction="rtl">
      <div class="detail-content" v-if="currentLog">
        <div class="detail-header">
          <el-tag :type="getStatusType(currentLog.statusCode)" effect="dark">{{ currentLog.statusCode }}</el-tag>
          <span class="detail-time">{{ formatTime(currentLog.callTime) }}</span>
          <span class="detail-duration">耗时: {{ currentLog.responseTime }}ms</span>
        </div>

        <div class="detail-section">
          <h4>基本信息</h4>
          <div class="info-grid">
            <div class="info-item">
              <label>TraceID</label>
              <span>{{ currentLog.traceId }}</span>
            </div>
            <div class="info-item">
              <label>调用方</label>
              <span>{{ currentLog.appName }}</span>
            </div>
            <div class="info-item">
              <label>客户端IP</label>
              <span>{{ currentLog.clientIp }}</span>
            </div>
            <div class="info-item">
              <label>请求路径</label>
              <span>{{ currentLog.apiPath }}</span>
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>请求报文</h4>
          <el-tabs v-model="requestTab" size="small">
            <el-tab-pane label="Headers" name="headers">
              <pre class="code-block">{{ currentLog.requestHeaders }}</pre>
            </el-tab-pane>
            <el-tab-pane label="Body" name="body">
              <pre class="code-block">{{ currentLog.requestBody }}</pre>
            </el-tab-pane>
          </el-tabs>
        </div>

        <div class="detail-section">
          <h4>响应报文</h4>
          <pre class="code-block">{{ currentLog.responseBody }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getApiCallLogs } from '@/api/monitor'

export default {
  name: 'CallLogs',
  data() {
    return {
      loading: false,
      searchForm: {
        dateRange: [],
        traceId: '',
        apiPath: '',
        statusCode: '',
        minTime: ''
      },
      logList: [],
      page: 1,
      pageSize: 20,
      total: 0,
      detailVisible: false,
      currentLog: null,
      requestTab: 'headers'
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
        traceId: this.searchForm.traceId,
        apiPath: this.searchForm.apiPath,
        statusCode: this.searchForm.statusCode,
        minTime: this.searchForm.minTime
      }

      if (this.searchForm.dateRange && this.searchForm.dateRange.length === 2) {
        params.startDate = this.searchForm.dateRange[0]
        params.endDate = this.searchForm.dateRange[1]
      }

      getApiCallLogs(params)
        .then((res) => {
          if (res.code === 200) {
            this.logList = res.data.list.map((item) => ({
              ...item,
              appName: item.platformName || 'Unknown',
              traceId: item.id, // Use ID as traceId for now
              method: 'GET', // Default to GET as it's missing
              clientIp: '127.0.0.1', // Default
              requestHeaders: '{}',
              requestBody: item.requestParams,
              responseBody: item.responseData
            }))
            this.total = res.data.total
          } else {
            this.$message.error(res.message || '获取日志失败')
          }
          this.loading = false
        })
        .catch((err) => {
          console.error(err)
          this.loading = false
          this.$message.error('获取日志失败')
        })
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString()
    },
    getMethodType(method) {
      const types = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
      return types[method] || 'info'
    },
    getStatusType(code) {
      if (code >= 200 && code < 300) return 'success'
      if (code >= 400 && code < 500) return 'warning'
      return 'danger'
    },
    getTimeClass(time) {
      if (time > 1000) return 'time-danger'
      if (time > 500) return 'time-warning'
      return 'time-normal'
    },
    copyTraceId(traceId) {
      navigator.clipboard.writeText(traceId).then(() => {
        this.$message.success('TraceID已复制')
      })
    },
    showDetail(row) {
      this.currentLog = row
      this.detailVisible = true
    },
    handleSearch() {
      this.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = { dateRange: [], traceId: '', apiPath: '', statusCode: '', minTime: '' }
      this.handleSearch()
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.loadData()
    },
    handlePageChange(page) {
      this.page = page
      this.loadData()
    }
  }
}
</script>

<style lang="scss" scoped>
.logs-page {
  padding: 20px;

  .page-header {
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

  .trace-id {
    color: #667eea;
    font-family: monospace;
    font-size: 12px;
    cursor: pointer;
    i {
      margin-left: 5px;
      opacity: 0.5;
    }
    &:hover i {
      opacity: 1;
    }
  }

  .api-path {
    margin-left: 8px;
    background: rgba(102, 126, 234, 0.1);
    padding: 2px 6px;
    border-radius: 4px;
    font-size: 12px;
    color: #a8b2d1;
  }

  .time-normal {
    color: #67c23a;
  }
  .time-warning {
    color: #e6a23c;
  }
  .time-danger {
    color: #f56c6c;
    font-weight: 600;
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

.detail-content {
  padding: 20px;

  .detail-header {
    display: flex;
    align-items: center;
    gap: 15px;
    margin-bottom: 20px;
    padding-bottom: 15px;
    border-bottom: 1px solid rgba(102, 126, 234, 0.2);

    .detail-time {
      color: #8b8ba7;
    }
    .detail-duration {
      color: #667eea;
      margin-left: auto;
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
        word-break: break-all;
      }
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
  }

  ::v-deep .el-tabs__item {
    color: #8b8ba7;
    &.is-active {
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
</style>
