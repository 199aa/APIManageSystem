<template>
  <div class="api-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>原子接口列表</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增接口</el-button>
    </div>

    <!-- 搜索栏 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="所属平台">
          <el-select v-model="searchForm.platformId" placeholder="全部平台" clearable style="width: 150px">
            <el-option v-for="p in platforms" :key="p.id" :label="p.name" :value="p.id"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="请求方式">
          <el-select v-model="searchForm.method" placeholder="全部" clearable style="width: 120px">
            <el-option label="GET" value="GET"></el-option>
            <el-option label="POST" value="POST"></el-option>
            <el-option label="PUT" value="PUT"></el-option>
            <el-option label="DELETE" value="DELETE"></el-option>
            <el-option label="PATCH" value="PATCH"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="接口名称">
          <el-input v-model="searchForm.name" placeholder="模糊搜索" clearable style="width: 180px"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" label="接口名称" min-width="150">
          <template slot-scope="scope">
            <span class="api-name" @click="handleDetail(scope.row)">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="platformName" label="所属平台" width="120">
          <template slot-scope="scope">
            <el-tag size="small" type="info">{{ scope.row.platformName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="请求路径" min-width="200">
          <template slot-scope="scope">
            <code class="path-code">{{ scope.row.path }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="请求方式" width="100">
          <template slot-scope="scope">
            <el-tag :type="getMethodType(scope.row.method)" size="small" effect="dark">
              {{ scope.row.method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template slot-scope="scope">
            <el-tag :type="getStatusType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="最后更新" width="160">
          <template slot-scope="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleDetail(scope.row)">详情/编辑</el-button>
            <el-button type="text" size="small" @click="handleTest(scope.row)">调试</el-button>
            <el-button type="text" size="small" @click="handleClone(scope.row)">克隆</el-button>
            <el-button type="text" size="small" class="danger-btn" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pagination"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="pagination.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="pagination.pageSize"
        layout="total, sizes, prev, pager, next"
        :total="pagination.total">
      </el-pagination>
    </el-card>

    <!-- 接口调试抽屉 -->
    <el-drawer
      title="接口调试"
      :visible.sync="testDrawerVisible"
      size="600px"
      direction="rtl">
      <div class="test-panel" v-if="currentApi">
        <div class="test-header">
          <el-tag :type="getMethodType(currentApi.method)" effect="dark">{{ currentApi.method }}</el-tag>
          <span class="test-path">{{ currentApi.path }}</span>
        </div>

        <!-- 请求参数 -->
        <div class="test-section">
          <h4>请求参数</h4>
          <el-tabs v-model="testActiveTab">
            <el-tab-pane label="Query参数" name="query">
              <div v-for="(param, index) in testParams.query" :key="'q'+index" class="param-row">
                <el-input v-model="param.key" placeholder="参数名" size="small" style="width: 40%"></el-input>
                <el-input v-model="param.value" placeholder="值" size="small" style="width: 50%"></el-input>
                <el-button type="text" icon="el-icon-delete" @click="removeParam('query', index)"></el-button>
              </div>
              <el-button type="text" icon="el-icon-plus" @click="addParam('query')">添加参数</el-button>
            </el-tab-pane>
            <el-tab-pane label="Header" name="header">
              <div v-for="(param, index) in testParams.header" :key="'h'+index" class="param-row">
                <el-input v-model="param.key" placeholder="Header名" size="small" style="width: 40%"></el-input>
                <el-input v-model="param.value" placeholder="值" size="small" style="width: 50%"></el-input>
                <el-button type="text" icon="el-icon-delete" @click="removeParam('header', index)"></el-button>
              </div>
              <el-button type="text" icon="el-icon-plus" @click="addParam('header')">添加参数</el-button>
            </el-tab-pane>
            <el-tab-pane label="Body" name="body">
              <el-input
                type="textarea"
                v-model="testParams.body"
                :rows="8"
                placeholder='请输入JSON格式的请求体，如：{"key": "value"}'>
              </el-input>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 发送按钮 -->
        <div class="test-actions">
          <el-button type="primary" :loading="testing" @click="sendRequest">
            <i class="el-icon-s-promotion"></i> 发送请求
          </el-button>
        </div>

        <!-- 响应结果 -->
        <div class="test-section" v-if="testResponse">
          <h4>
            响应结果
            <el-tag :type="testResponse.success ? 'success' : 'danger'" size="small" style="margin-left: 10px">
              {{ testResponse.status }} {{ testResponse.statusText }}
            </el-tag>
            <span class="response-time">耗时: {{ testResponse.time }}ms</span>
          </h4>
          <pre class="response-body">{{ testResponse.data }}</pre>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getApiList, deleteApi, cloneApi, testApi } from '@/api/api'
import { getAllPlatforms } from '@/api/platform'

export default {
  name: 'ApiList',
  data() {
    return {
      loading: false,
      testing: false,
      platforms: [],
      searchForm: {
        platformId: '',
        method: '',
        name: ''
      },
      tableData: [],
      pagination: {
        page: 1,
        pageSize: 10,
        total: 0
      },
      testDrawerVisible: false,
      currentApi: null,
      testActiveTab: 'query',
      testParams: {
        query: [{ key: '', value: '' }],
        header: [{ key: '', value: '' }],
        body: ''
      },
      testResponse: null
    }
  },
  created() {
    this.loadPlatforms()
    this.loadData()
  },
  methods: {
    async loadPlatforms() {
      try {
        const res = await getAllPlatforms()
        if (res.code === 200) {
          this.platforms = res.data
        }
      } catch (e) {
        console.error(e)
      }
    },
    async loadData() {
      this.loading = true
      try {
        const params = {
          page: this.pagination.page,
          pageSize: this.pagination.pageSize,
          ...this.searchForm
        }
        const res = await getApiList(params)
        if (res.code === 200) {
          this.tableData = res.data.list || res.data
          this.pagination.total = res.data.total || this.tableData.length
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.loading = false
      }
    },
    getMethodType(method) {
      const types = {
        GET: 'success',
        POST: 'primary',
        PUT: 'warning',
        DELETE: 'danger',
        PATCH: 'info'
      }
      return types[method] || 'info'
    },
    getStatusType(status) {
      const types = { 1: 'success', 0: 'danger', 2: 'info' }
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = { 1: '正常', 0: '异常', 2: '离线' }
      return texts[status] || '未知'
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString()
    },
    handleSearch() {
      this.pagination.page = 1
      this.loadData()
    },
    handleReset() {
      this.searchForm = { platformId: '', method: '', name: '' }
      this.handleSearch()
    },
    handleAdd() {
      this.$router.push('/api/detail/new')
    },
    handleDetail(row) {
      this.$router.push(`/api/detail/${row.id}`)
    },
    handleTest(row) {
      this.currentApi = row
      this.testParams = {
        query: [{ key: '', value: '' }],
        header: [{ key: '', value: '' }],
        body: ''
      }
      this.testResponse = null
      this.testDrawerVisible = true
    },
    async handleClone(row) {
      try {
        await this.$confirm('确定要克隆该接口吗？', '提示', { type: 'info' })
        const res = await cloneApi(row.id)
        if (res.code === 200) {
          this.$message.success('克隆成功')
          this.loadData()
        }
      } catch (e) {
        if (e !== 'cancel') console.error(e)
      }
    },
    async handleDelete(row) {
      try {
        await this.$confirm('确定要删除该接口吗？', '提示', { type: 'warning' })
        const res = await deleteApi(row.id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadData()
        }
      } catch (e) {
        if (e !== 'cancel') console.error(e)
      }
    },
    handleSizeChange(val) {
      this.pagination.pageSize = val
      this.loadData()
    },
    handleCurrentChange(page) {
      this.pagination.page = page
      this.loadData()
    },
    addParam(type) {
      this.testParams[type].push({ key: '', value: '' })
    },
    removeParam(type, index) {
      this.testParams[type].splice(index, 1)
    },
    async sendRequest() {
      this.testing = true
      try {
        const params = {
          apiId: this.currentApi.id,
          queryParams: this.testParams.query.filter(p => p.key),
          headers: this.testParams.header.filter(p => p.key),
          body: this.testParams.body
        }
        const startTime = Date.now()
        const res = await testApi(params)
        const endTime = Date.now()
        this.testResponse = {
          success: res.code === 200,
          status: res.data?.statusCode || res.code,
          statusText: res.data?.statusText || (res.code === 200 ? 'OK' : 'Error'),
          time: endTime - startTime,
          data: JSON.stringify(res.data?.response || res.data, null, 2)
        }
      } catch (e) {
        this.testResponse = {
          success: false,
          status: 500,
          statusText: 'Error',
          time: 0,
          data: e.message || '请求失败'
        }
      } finally {
        this.testing = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.api-list {
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

    ::v-deep .el-card__body {
      padding: 20px;
    }
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

    .el-table__row--striped td {
      background: rgba(102, 126, 234, 0.05);
    }
  }

  .api-name {
    color: #667eea;
    cursor: pointer;

    &:hover {
      text-decoration: underline;
    }
  }

  .path-code {
    background: rgba(102, 126, 234, 0.1);
    padding: 2px 8px;
    border-radius: 4px;
    font-family: monospace;
    font-size: 12px;
    color: #a8b2d1;
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

.test-panel {
  padding: 20px;
  height: 100%;
  overflow-y: auto;

  .test-header {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    padding: 15px;
    background: rgba(102, 126, 234, 0.1);
    border-radius: 8px;

    .test-path {
      font-family: monospace;
      color: #a8b2d1;
    }
  }

  .test-section {
    margin-bottom: 20px;

    h4 {
      color: #fff;
      margin-bottom: 15px;
      display: flex;
      align-items: center;

      .response-time {
        margin-left: auto;
        font-size: 12px;
        color: #8b8ba7;
      }
    }
  }

  .param-row {
    display: flex;
    gap: 10px;
    margin-bottom: 10px;
    align-items: center;
  }

  .test-actions {
    margin: 20px 0;
  }

  .response-body {
    background: rgba(0, 0, 0, 0.3);
    padding: 15px;
    border-radius: 8px;
    color: #a8b2d1;
    font-family: monospace;
    font-size: 12px;
    max-height: 400px;
    overflow: auto;
    white-space: pre-wrap;
    word-break: break-all;
  }

  ::v-deep .el-tabs__item {
    color: #8b8ba7;

    &.is-active {
      color: #667eea;
    }
  }

  ::v-deep .el-input__inner,
  ::v-deep .el-textarea__inner {
    background: rgba(35, 35, 55, 0.8);
    border-color: rgba(102, 126, 234, 0.3);
    color: #fff;
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
