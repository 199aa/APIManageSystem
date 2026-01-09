<template>
  <div class="api-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>原子接口列表</h2>
      <el-button v-permission="'api:create'" type="primary" icon="el-icon-plus" @click="handleAdd">新增接口</el-button>
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
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
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
            <el-button v-permission="'api:test'" type="text" size="small" @click="handleTest(scope.row)">调试</el-button>
            <el-button v-permission="'api:create'" type="text" size="small" @click="handleClone(scope.row)">克隆</el-button>
            <el-button v-permission="'api:delete'" type="text" size="small" class="danger-btn" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination class="pagination" @size-change="handleSizeChange" @current-change="handleCurrentChange" :current-page="pagination.page" :page-sizes="[10, 20, 50, 100]" :page-size="pagination.pageSize" layout="total, sizes, prev, pager, next" :total="pagination.total">
      </el-pagination>
    </el-card>

    <!-- 接口调试抽屉 -->
    <el-drawer title="接口调试" :visible.sync="testDrawerVisible" size="700px" direction="rtl">
      <div class="test-panel" v-if="currentApi">
        <div class="test-header">
          <el-tag :type="getMethodType(currentApi.method)" effect="dark" size="medium">{{ currentApi.method }}</el-tag>
          <div class="test-url">
            <el-input v-model="testUrl" placeholder="请输入完整的API地址" size="small">
              <template slot="prepend">URL</template>
            </el-input>
          </div>
        </div>

        <!-- URL提示 -->
        <el-alert v-if="!testUrl.startsWith('http')" title="请输入完整的URL地址（包含 http:// 或 https://）" type="warning" :closable="false" style="margin: 10px 0"></el-alert>

        <!-- 请求参数 -->
        <div class="test-section">
          <h4>请求参数</h4>
          <el-tabs v-model="testActiveTab">
            <!-- Path参数 -->
            <el-tab-pane name="path">
              <span slot="label">
                Path参数
                <el-badge v-if="testParams.path && testParams.path.length > 0" :value="testParams.path.length" class="param-badge"></el-badge>
              </span>
              <div v-if="!testParams.path || testParams.path.length === 0" style="color: #999; padding: 20px; text-align: center;">
                在URL中使用 {参数名} 格式来定义路径参数，例如: /api/users/{id}
              </div>
              <div v-for="(param, index) in testParams.path" :key="'p'+index" class="param-row">
                <el-input v-model="param.key" placeholder="参数名" size="small" style="width: 25%" disabled></el-input>
                <el-select v-model="param.type" placeholder="类型" size="small" style="width: 20%">
                  <el-option label="String" value="string"></el-option>
                  <el-option label="Integer" value="integer"></el-option>
                  <el-option label="Long" value="long"></el-option>
                  <el-option label="Double" value="double"></el-option>
                  <el-option label="Boolean" value="boolean"></el-option>
                </el-select>
                <el-input v-model="param.value" placeholder="值" size="small" style="width: 45%">
                  <template slot="append">{{ getTypeHint(param.type) }}</template>
                </el-input>
                <el-button type="text" icon="el-icon-refresh" @click="validateParamType(param)" title="验证类型"></el-button>
              </div>
            </el-tab-pane>

            <el-tab-pane label="Query参数" name="query">
              <div v-for="(param, index) in testParams.query" :key="'q'+index" class="param-row">
                <el-input v-model="param.key" placeholder="参数名" size="small" style="width: 25%"></el-input>
                <el-select v-model="param.type" placeholder="类型" size="small" style="width: 20%">
                  <el-option label="String" value="string"></el-option>
                  <el-option label="Integer" value="integer"></el-option>
                  <el-option label="Long" value="long"></el-option>
                  <el-option label="Double" value="double"></el-option>
                  <el-option label="Boolean" value="boolean"></el-option>
                </el-select>
                <el-input v-model="param.value" placeholder="值" size="small" style="width: 40%"></el-input>
                <el-button type="text" icon="el-icon-delete" @click="removeParam('query', index)"></el-button>
              </div>
              <el-button type="text" icon="el-icon-plus" @click="addParam('query')">添加参数</el-button>
            </el-tab-pane>
            <el-tab-pane label="Header" name="header">
              <div v-for="(param, index) in testParams.header" :key="'h'+index" class="param-row">
                <el-input v-model="param.key" placeholder="Header名" size="small" style="width: 30%"></el-input>
                <el-input v-model="param.value" placeholder="值" size="small" style="width: 60%"></el-input>
                <el-button type="text" icon="el-icon-delete" @click="removeParam('header', index)"></el-button>
              </div>
              <el-button type="text" icon="el-icon-plus" @click="addParam('header')">添加参数</el-button>
            </el-tab-pane>
            <el-tab-pane label="Body" name="body">
              <el-input type="textarea" v-model="testParams.body" :rows="8" placeholder='请输入JSON格式的请求体，如：{"key": "value"}'>
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
      testUrl: '', // 添加测试URL字段
      testActiveTab: 'path',
      testParams: {
        path: [], // 路径参数
        query: [{ key: '', value: '', type: 'string' }],
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
  watch: {
    // 监听URL变化，自动提取路径参数
    testUrl(newUrl) {
      this.extractPathParams(newUrl)
    }
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
      // 智能构建测试URL
      let url = row.path

      if (!url.startsWith('http://') && !url.startsWith('https://')) {
        // 判断是否为聚合接口
        if (row.isAggregate === 1 || url.startsWith('/aggregate')) {
          // 聚合接口使用后端地址 8081
          if (url.startsWith('/')) {
            url = 'http://localhost:8081' + url
          } else {
            url = 'http://localhost:8081/' + url
          }
        } else {
          // 普通接口：尝试从平台获取baseUrl
          let baseUrl = ''
          if (row.platformId && this.platforms.length > 0) {
            const platform = this.platforms.find((p) => p.id === row.platformId)
            if (platform && platform.baseUrl) {
              baseUrl = platform.baseUrl.replace(/\/$/, '') // 去掉末尾斜杠
            }
          }

          // 如果有平台baseUrl，使用平台地址
          if (baseUrl) {
            if (url.startsWith('/')) {
              url = baseUrl + url
            } else {
              url = baseUrl + '/' + url
            }
          } else {
            // 没有平台信息，使用默认的外部测试端口8083
            if (url.match(/^\d+\//)) {
              url = 'http://localhost:' + url
            } else if (url.startsWith('/')) {
              url = 'http://localhost:8083' + url
            } else {
              url = 'http://localhost:8083/' + url
            }
          }
        }
      }

      this.testUrl = url
      this.testParams = {
        path: [], // 添加path数组初始化
        query: [{ key: '', value: '', type: 'string' }],
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
      const newParam = { key: '', value: '' }
      if (type === 'query') {
        newParam.type = 'string'
      }
      this.testParams[type].push(newParam)
    },
    removeParam(type, index) {
      this.testParams[type].splice(index, 1)
    },
    // 提取URL中的路径参数
    extractPathParams(url) {
      if (!url) return

      // 匹配 {参数名} 格式
      const regex = /\{([^}]+)\}/g
      const matches = []
      let match

      while ((match = regex.exec(url)) !== null) {
        matches.push(match[1])
      }

      // 保留已存在参数的值和类型
      const existingParams = {}
      this.testParams.path.forEach((param) => {
        existingParams[param.key] = { value: param.value, type: param.type }
      })

      // 更新路径参数列表
      this.testParams.path = matches.map((key) => {
        return {
          key,
          value: existingParams[key]?.value || '',
          type: existingParams[key]?.type || 'string'
        }
      })

      // 如果有路径参数，自动切换到path标签页
      if (matches.length > 0 && this.testActiveTab === 'query') {
        this.testActiveTab = 'path'
      }
    },
    // 获取类型提示
    getTypeHint(type) {
      const hints = {
        string: '文本',
        integer: '整数',
        long: '长整数',
        double: '小数',
        boolean: 'true/false'
      }
      return hints[type] || ''
    },
    // 验证参数类型
    validateParamType(param) {
      if (!param.value) {
        this.$message.warning(`请输入 ${param.key} 的值`)
        return false
      }

      const value = param.value
      let isValid = true
      let errorMsg = ''

      switch (param.type) {
        case 'integer':
          isValid = /^-?\d+$/.test(value) && Number.isInteger(Number(value))
          errorMsg = '必须是整数'
          break
        case 'long':
          isValid = /^-?\d+$/.test(value)
          errorMsg = '必须是长整数'
          break
        case 'double':
          isValid = /^-?\d+(\.\d+)?$/.test(value) && !isNaN(parseFloat(value))
          errorMsg = '必须是数字'
          break
        case 'boolean':
          isValid = value === 'true' || value === 'false'
          errorMsg = '必须是 true 或 false'
          break
        case 'string':
          isValid = true
          break
      }

      if (isValid) {
        this.$message.success(`${param.key} 类型验证通过`)
      } else {
        this.$message.error(`${param.key} 类型错误：${errorMsg}`)
      }

      return isValid
    },
    // 验证所有参数类型
    validateAllParams() {
      // 验证路径参数
      for (const param of this.testParams.path) {
        if (!param.value) {
          this.$message.error(`路径参数 ${param.key} 不能为空`)
          this.testActiveTab = 'path'
          return false
        }
        if (!this.validateParamType(param)) {
          this.testActiveTab = 'path'
          return false
        }
      }

      // 验证Query参数
      for (const param of this.testParams.query) {
        if (param.key && param.value && param.type) {
          if (!this.validateParamType(param)) {
            this.testActiveTab = 'query'
            return false
          }
        }
      }

      return true
    },
    async sendRequest() {
      this.testing = true
      try {
        // 先验证参数类型
        if (!this.validateAllParams()) {
          this.testing = false
          return
        }

        // 替换URL中的路径参数
        let finalUrl = this.testUrl
        this.testParams.path.forEach((param) => {
          finalUrl = finalUrl.replace(`{${param.key}}`, param.value)
        })

        const params = {
          apiId: this.currentApi.id,
          url: finalUrl, // 传递处理后的URL
          queryParams: this.testParams.query.filter((p) => p.key),
          headers: this.testParams.header.filter((p) => p.key),
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
    flex-direction: column;

    .test-url {
      width: 100%;

      ::v-deep .el-input-group__prepend {
        background-color: #667eea;
        color: white;
        border: none;
        font-weight: bold;
      }
    }

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

    .el-input__inner {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(102, 126, 234, 0.3);
      color: #fff;

      &:focus {
        border-color: #667eea;
      }
    }

    .el-input-group__append {
      background: rgba(102, 126, 234, 0.2);
      border-color: rgba(102, 126, 234, 0.3);
      color: #8b8ba7;
      font-size: 12px;
    }
  }

  .param-badge {
    ::v-deep .el-badge__content {
      background-color: #667eea;
      border: none;
    }
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
