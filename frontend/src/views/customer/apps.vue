<template>
  <div class="apps-page">
    <div class="page-header">
      <h2>应用列表</h2>
      <el-button type="primary" icon="el-icon-plus" @click="handleAdd">创建应用</el-button>
    </div>

    <!-- 应用卡片列表 -->
    <div class="app-cards">
      <el-card v-for="app in appList" :key="app.id" class="app-card" :class="{ disabled: !app.status }">
        <div class="card-header">
          <div class="app-icon" :style="{ background: app.color }">
            <i :class="app.icon"></i>
          </div>
          <div class="app-info">
            <h3>{{ app.name }}</h3>
            <p>{{ app.description || '暂无描述' }}</p>
          </div>
          <el-dropdown @command="handleCommand($event, app)">
            <span class="el-dropdown-link">
              <i class="el-icon-more"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="edit">编辑</el-dropdown-item>
              <el-dropdown-item command="detail">查看详情</el-dropdown-item>
              <el-dropdown-item command="toggle">{{ app.status ? '禁用' : '启用' }}</el-dropdown-item>
              <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
        <div class="card-body">
          <div class="stat-item">
            <span class="label">创建时间</span>
            <span class="value">{{ formatTime(app.createTime) }}</span>
          </div>
          <div class="stat-item">
            <span class="label">已授权接口</span>
            <span class="value highlight">{{ app.apiCount }} 个</span>
          </div>
          <div class="stat-item">
            <span class="label">今日调用</span>
            <span class="value">{{ app.todayCall }} 次</span>
          </div>
        </div>
        <div class="card-footer">
          <el-tag :type="app.status ? 'success' : 'info'" size="small">
            {{ app.status ? '运行中' : '已禁用' }}
          </el-tag>
          <el-button type="text" size="small" @click="goDetail(app)">
            管理凭证 <i class="el-icon-arrow-right"></i>
          </el-button>
        </div>
      </el-card>

      <!-- 空状态 -->
      <el-card class="app-card add-card" @click.native="handleAdd" v-if="appList.length === 0">
        <div class="add-content">
          <i class="el-icon-plus"></i>
          <p>创建第一个应用</p>
        </div>
      </el-card>
    </div>

    <!-- 创建/编辑应用弹窗 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="500px">
      <el-form :model="form" :rules="rules" ref="form" label-width="100px">
        <el-form-item label="应用名称" prop="name">
          <el-input v-model="form.name" placeholder="如：订单系统"></el-input>
        </el-form-item>
        <el-form-item label="应用描述">
          <el-input v-model="form.description" type="textarea" :rows="3"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <div class="icon-select">
            <div
              v-for="icon in iconOptions"
              :key="icon.value"
              class="icon-item"
              :class="{ active: form.icon === icon.value }"
              @click="form.icon = icon.value">
              <i :class="icon.value"></i>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="主题色">
          <el-color-picker v-model="form.color"></el-color-picker>
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
import { getAppList, createApp, updateApp, deleteApp, updateAppStatus } from '@/api/customer'

export default {
  name: 'AppList',
  data() {
    return {
      appList: [],
      loading: false,
      dialogVisible: false,
      dialogTitle: '创建应用',
      form: {
        name: '',
        description: '',
        icon: 'el-icon-s-platform',
        color: '#667eea'
      },
      rules: {
        name: [{ required: true, message: '请输入应用名称', trigger: 'blur' }]
      },
      iconOptions: [
        { value: 'el-icon-s-platform' },
        { value: 'el-icon-s-order' },
        { value: 'el-icon-s-data' },
        { value: 'el-icon-user' },
        { value: 'el-icon-s-goods' },
        { value: 'el-icon-s-marketing' },
        { value: 'el-icon-s-finance' },
        { value: 'el-icon-s-claim' }
      ]
    }
  },
  mounted() {
    this.loadAppList()
  },
  methods: {
    // 加载应用列表
    async loadAppList() {
      this.loading = true
      try {
        const res = await getAppList()
        if (res.code === 200) {
          this.appList = res.data || []
        } else {
          this.$message.error(res.message || '加载应用列表失败')
        }
      } catch (error) {
        this.$message.error('加载应用列表失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleDateString()
    },
    handleAdd() {
      this.dialogTitle = '创建应用'
      this.form = { name: '', description: '', icon: 'el-icon-s-platform', color: '#667eea' }
      this.dialogVisible = true
    },
    async handleCommand(command, app) {
      switch (command) {
        case 'edit':
          this.dialogTitle = '编辑应用'
          this.form = { ...app }
          this.dialogVisible = true
          break
        case 'detail':
          this.goDetail(app)
          break
        case 'toggle':
          await this.toggleAppStatus(app)
          break
        case 'delete':
          this.$confirm('确定要删除该应用吗？删除后凭证将失效', '提示', { type: 'warning' })
            .then(async () => { 
              await this.handleDelete(app.id)
            })
            .catch(() => {})
          break
      }
    },
    // 切换应用状态
    async toggleAppStatus(app) {
      try {
        const newStatus = app.status ? 0 : 1
        const res = await updateAppStatus({ id: app.id, status: newStatus })
        if (res.code === 200) {
          app.status = newStatus
          this.$message.success(newStatus ? '已启用' : '已禁用')
        } else {
          this.$message.error(res.message || '状态更新失败')
        }
      } catch (error) {
        this.$message.error('状态更新失败: ' + error.message)
      }
    },
    // 删除应用
    async handleDelete(id) {
      try {
        const res = await deleteApp(id)
        if (res.code === 200) {
          this.$message.success('删除成功')
          this.loadAppList()
        } else {
          this.$message.error(res.message || '删除失败')
        }
      } catch (error) {
        this.$message.error('删除失败: ' + error.message)
      }
    },
    // 提交表单
    async handleSubmit() {
      this.$refs.form.validate(async valid => {
        if (valid) {
          try {
            let res
            if (this.form.id) {
              // 更新应用
              res = await updateApp(this.form)
            } else {
              // 创建应用
              res = await createApp(this.form)
            }
            
            if (res.code === 200) {
              this.$message.success(this.form.id ? '更新成功' : '创建成功')
              this.dialogVisible = false
              this.loadAppList()
            } else {
              this.$message.error(res.message || '保存失败')
            }
          } catch (error) {
            this.$message.error('保存失败: ' + error.message)
          }
        }
      })
    },
    goDetail(app) {
      this.$router.push(`/customer/app-detail/${app.id}`)
    }
  }
}
</script>

<style lang="scss" scoped>
.apps-page {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    h2 { color: #fff; margin: 0; font-size: 20px; }
  }

  .app-cards {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
    gap: 20px;
  }

  .app-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 12px;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
      border-color: rgba(102, 126, 234, 0.4);
    }

    &.disabled {
      opacity: 0.6;
    }

    ::v-deep .el-card__body {
      padding: 0;
    }

    .card-header {
      display: flex;
      align-items: flex-start;
      padding: 20px;
      border-bottom: 1px solid rgba(102, 126, 234, 0.1);

      .app-icon {
        width: 50px;
        height: 50px;
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 15px;
        i { font-size: 24px; color: #fff; }
      }

      .app-info {
        flex: 1;
        h3 { color: #fff; margin: 0 0 5px; font-size: 16px; }
        p { color: #8b8ba7; margin: 0; font-size: 13px; }
      }

      .el-dropdown-link {
        color: #8b8ba7;
        cursor: pointer;
        padding: 5px;
        &:hover { color: #667eea; }
      }
    }

    .card-body {
      padding: 15px 20px;

      .stat-item {
        display: flex;
        justify-content: space-between;
        padding: 8px 0;
        border-bottom: 1px dashed rgba(102, 126, 234, 0.1);

        &:last-child { border-bottom: none; }

        .label { color: #8b8ba7; font-size: 13px; }
        .value { color: #fff; font-size: 13px; }
        .highlight { color: #667eea; font-weight: 600; }
      }
    }

    .card-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 20px;
      background: rgba(102, 126, 234, 0.05);
      border-radius: 0 0 12px 12px;
    }
  }

  .add-card {
    cursor: pointer;
    border-style: dashed;

    .add-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      height: 200px;
      color: #667eea;
      i { font-size: 48px; margin-bottom: 15px; }
    }
  }
}

.icon-select {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;

  .icon-item {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 1px solid rgba(102, 126, 234, 0.3);
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.2s;

    i { font-size: 20px; color: #8b8ba7; }

    &:hover { border-color: #667eea; }

    &.active {
      background: rgba(102, 126, 234, 0.2);
      border-color: #667eea;
      i { color: #667eea; }
    }
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
}
</style>
