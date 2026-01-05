<template>
  <div class="aggregate-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>聚合接口管理</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleCreate">创建聚合接口</el-button>
    </div>

    <!-- 列表 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" label="聚合接口名称" min-width="180">
          <template slot-scope="scope">
            <span class="link-text" @click="handleEdit(scope.row)">{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="path" label="访问路径" min-width="200">
          <template slot-scope="scope">
            <code class="path-code">{{ scope.row.path }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="apiCount" label="包含原子API数" width="130">
          <template slot-scope="scope">
            <el-tag size="small">{{ scope.row.apiCount || 0 }} 个</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(scope.row)">
            </el-switch>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="160">
          <template slot-scope="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleEdit(scope.row)">编排设计</el-button>
            <el-button type="text" size="small" @click="handleTest(scope.row)">测试</el-button>
            <el-button type="text" size="small" class="danger-btn" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination class="pagination" @current-change="handlePageChange" :current-page="page" :page-size="pageSize" layout="total, prev, pager, next" :total="total">
      </el-pagination>
    </el-card>

    <!-- 创建弹窗 -->
    <el-dialog title="创建聚合接口" :visible.sync="createDialogVisible" width="500px">
      <el-form :model="createForm" :rules="rules" ref="createForm" label-width="100px">
        <el-form-item label="接口名称" prop="name">
          <el-input v-model="createForm.name" placeholder="如：用户完整信息查询"></el-input>
        </el-form-item>
        <el-form-item label="访问路径" prop="path">
          <el-input v-model="createForm.path" placeholder="如：/aggregate/user-info">
            <template slot="prepend">/api</template>
          </el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">创建并进入编排</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getAggregateList, createAggregate, deleteAggregate, publishAggregate, offlineAggregate } from '@/api/api'

export default {
  name: 'AggregateList',
  data() {
    return {
      loading: false,
      tableData: [],
      page: 1,
      pageSize: 10,
      total: 0,
      createDialogVisible: false,
      createForm: {
        name: '',
        path: '',
        description: ''
      },
      rules: {
        name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
        path: [
          { required: true, message: '请输入访问路径', trigger: 'blur' },
          { pattern: /^\/[a-zA-Z0-9/_-]+$/, message: '路径格式不正确，应以/开头', trigger: 'blur' }
        ]
      }
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.loading = true
      try {
        const res = await getAggregateList({
          page: this.page,
          pageSize: this.pageSize
        })

        if (res.code === 200) {
          const data = res.data
          this.tableData = data.list || []
          this.total = data.total || 0

          // 解析aggregate_config获取子API数量
          this.tableData.forEach((item) => {
            if (item.aggregateConfig) {
              try {
                const config = JSON.parse(item.aggregateConfig)
                item.apiCount = config.nodes ? config.nodes.length : 0
              } catch (e) {
                item.apiCount = 0
              }
            } else {
              item.apiCount = 0
            }
          })
        } else {
          this.$message.error(res.message || '加载失败')
        }
      } catch (error) {
        console.error(error)
        this.$message.error('加载数据失败')
      } finally {
        this.loading = false
      }
    },

    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    handleCreate() {
      this.createForm = { name: '', path: '', description: '' }
      this.createDialogVisible = true
    },

    async submitCreate() {
      this.$refs.createForm.validate(async (valid) => {
        if (valid) {
          try {
            const res = await createAggregate(this.createForm)
            if (res.code === 200) {
              this.$message.success('创建成功')
              this.createDialogVisible = false
              // 跳转到编排页面
              this.$router.push(`/orchestration/design/${res.data.id}`)
            } else {
              this.$message.error(res.message || '创建失败')
            }
          } catch (error) {
            console.error(error)
            this.$message.error('创建失败')
          }
        }
      })
    },

    handleEdit(row) {
      this.$router.push(`/orchestration/design/${row.id}`)
    },

    handleTest(row) {
      // 跳转到编排页面的测试模式
      this.$router.push(`/orchestration/design/${row.id}?mode=test`)
    },

    async handleStatusChange(row) {
      try {
        const api = row.status === 1 ? publishAggregate : offlineAggregate
        const res = await api(row.id)

        if (res.code === 200) {
          this.$message.success(res.message || `已${row.status === 1 ? '发布' : '下线'}`)
        } else {
          this.$message.error(res.message || '操作失败')
          // 恢复状态
          row.status = row.status === 1 ? 0 : 1
        }
      } catch (error) {
        console.error(error)
        this.$message.error('操作失败')
        // 恢复状态
        row.status = row.status === 1 ? 0 : 1
      }
    },

    handleDelete(row) {
      this.$confirm('确定要删除该聚合接口吗？删除后无法恢复。', '提示', {
        type: 'warning',
        confirmButtonText: '确定删除',
        cancelButtonText: '取消'
      })
        .then(async () => {
          try {
            const res = await deleteAggregate(row.id)
            if (res.code === 200) {
              this.$message.success('删除成功')
              this.loadData()
            } else {
              this.$message.error(res.message || '删除失败')
            }
          } catch (error) {
            console.error(error)
            this.$message.error('删除失败')
          }
        })
        .catch(() => {})
    },

    handlePageChange(page) {
      this.page = page
      this.loadData()
    }
  }
}
</script>

<style lang="scss" scoped>
.aggregate-list {
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

      .el-table__row--striped td {
        background: rgba(102, 126, 234, 0.05);
      }
    }
  }

  .link-text {
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

  .el-input__inner,
  .el-textarea__inner {
    background: rgba(35, 35, 55, 0.8);
    border-color: rgba(102, 126, 234, 0.3);
    color: #fff;
  }
}
</style>
