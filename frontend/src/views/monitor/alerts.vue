<template>
  <div class="alerts-page">
    <div class="page-header">
      <h2>告警管理</h2>
      <el-button type="primary" icon="el-icon-plus" size="small" @click="showRuleDialog()">新建告警规则</el-button>
    </div>

    <!-- 告警统计 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <div class="stat-card critical">
          <div class="stat-icon"><i class="el-icon-warning"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ alertStats.critical }}</div>
            <div class="stat-label">严重告警</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card warning">
          <div class="stat-icon"><i class="el-icon-warning-outline"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ alertStats.warning }}</div>
            <div class="stat-label">警告</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card info">
          <div class="stat-icon"><i class="el-icon-info"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ alertStats.info }}</div>
            <div class="stat-label">通知</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card resolved">
          <div class="stat-icon"><i class="el-icon-success"></i></div>
          <div class="stat-info">
            <div class="stat-value">{{ alertStats.resolved }}</div>
            <div class="stat-label">已解决</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 活跃告警列表 -->
    <el-card class="alerts-card">
      <div slot="header" class="card-header">
        <span>活跃告警</span>
        <el-button type="text" size="small" @click="handleBatchAck" :disabled="!selectedAlerts.length">
          <i class="el-icon-check"></i> 批量确认
        </el-button>
      </div>

      <el-table :data="alertList" v-loading="loading" style="width: 100%" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="55"></el-table-column>
        <el-table-column prop="level" label="级别" width="100">
          <template slot-scope="scope">
            <el-tag :type="getLevelType(scope.row.level)" size="small" effect="dark">
              {{ getLevelLabel(scope.row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertTime" label="告警时间" width="180">
          <template slot-scope="scope">
            {{ formatTime(scope.row.alertTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="ruleName" label="规则名称" width="150"></el-table-column>
        <el-table-column prop="content" label="告警内容" min-width="200"></el-table-column>
        <el-table-column prop="target" label="告警对象" width="150"></el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 'firing' ? 'danger' : 'success'" size="small">
              {{ scope.row.status === 'firing' ? '触发中' : '已恢复' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="handleAck(scope.row)">
              <i class="el-icon-check"></i> 确认
            </el-button>
            <el-button type="text" size="small" @click="showAlertDetail(scope.row)">
              <i class="el-icon-view"></i> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 告警规则 -->
    <el-card class="rules-card">
      <div slot="header" class="card-header">
        <span>告警规则</span>
      </div>

      <el-table :data="ruleList" style="width: 100%">
        <el-table-column prop="name" label="规则名称" width="150"></el-table-column>
        <el-table-column prop="type" label="类型" width="120">
          <template slot-scope="scope">
            <span>{{ getRuleTypeLabel(scope.row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="condition" label="触发条件" min-width="200">
          <template slot-scope="scope">
            <code class="condition-code">{{ scope.row.condition }}</code>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="告警级别" width="100">
          <template slot-scope="scope">
            <el-tag :type="getLevelType(scope.row.level)" size="small">{{ getLevelLabel(scope.row.level) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="notify" label="通知方式" width="150">
          <template slot-scope="scope">
            <span v-for="(n, i) in scope.row.notify" :key="i" class="notify-tag">
              <i :class="getNotifyIcon(n)"></i>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="状态" width="80">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.enabled" @change="toggleRule(scope.row)"></el-switch>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template slot-scope="scope">
            <el-button type="text" size="small" @click="showRuleDialog(scope.row)">编辑</el-button>
            <el-button type="text" size="small" class="danger-btn" @click="deleteRule(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新建/编辑规则对话框 -->
    <el-dialog :title="ruleForm.id ? '编辑规则' : '新建告警规则'" :visible.sync="ruleDialogVisible" width="600px">
      <el-form :model="ruleForm" :rules="ruleRules" ref="ruleFormRef" label-width="100px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="ruleForm.name" placeholder="请输入规则名称"></el-input>
        </el-form-item>
        <el-form-item label="监控类型" prop="type">
          <el-select v-model="ruleForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="错误率" value="error_rate"></el-option>
            <el-option label="响应时间" value="response_time"></el-option>
            <el-option label="调用量" value="call_count"></el-option>
            <el-option label="成功率" value="success_rate"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="触发条件" prop="condition">
          <el-row :gutter="10">
            <el-col :span="8">
              <el-select v-model="ruleForm.operator" style="width: 100%">
                <el-option label="大于" value=">"></el-option>
                <el-option label="大于等于" value=">="></el-option>
                <el-option label="小于" value="<"></el-option>
                <el-option label="等于" value="="></el-option>
              </el-select>
            </el-col>
            <el-col :span="8">
              <el-input v-model="ruleForm.threshold" placeholder="阈值"></el-input>
            </el-col>
            <el-col :span="8">
              <el-select v-model="ruleForm.unit" style="width: 100%">
                <el-option label="%" value="%"></el-option>
                <el-option label="ms" value="ms"></el-option>
                <el-option label="次/分" value="rpm"></el-option>
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="持续时间">
          <el-input v-model="ruleForm.duration" placeholder="持续多长时间后触发">
            <template slot="append">分钟</template>
          </el-input>
        </el-form-item>
        <el-form-item label="告警级别" prop="level">
          <el-radio-group v-model="ruleForm.level">
            <el-radio label="critical">严重</el-radio>
            <el-radio label="warning">警告</el-radio>
            <el-radio label="info">通知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知方式">
          <el-checkbox-group v-model="ruleForm.notify">
            <el-checkbox label="email">邮件</el-checkbox>
            <el-checkbox label="sms">短信</el-checkbox>
            <el-checkbox label="webhook">Webhook</el-checkbox>
            <el-checkbox label="dingtalk">钉钉</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="通知人">
          <el-select v-model="ruleForm.receivers" multiple placeholder="选择接收人" style="width: 100%">
            <el-option label="管理员" value="admin"></el-option>
            <el-option label="运维组" value="ops"></el-option>
            <el-option label="开发组" value="dev"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveRule">保存</el-button>
      </div>
    </el-dialog>

    <!-- 告警详情抽屉 -->
    <el-drawer title="告警详情" :visible.sync="alertDetailVisible" size="500px">
      <div class="alert-detail" v-if="currentAlert">
        <div class="alert-detail-header" :class="currentAlert.level">
          <i :class="getLevelIcon(currentAlert.level)"></i>
          <div>
            <div class="alert-title">{{ currentAlert.ruleName }}</div>
            <div class="alert-time">{{ formatTime(currentAlert.alertTime) }}</div>
          </div>
        </div>

        <div class="alert-detail-section">
          <h4>告警内容</h4>
          <p>{{ currentAlert.content }}</p>
        </div>

        <div class="alert-detail-section">
          <h4>告警对象</h4>
          <p>{{ currentAlert.target }}</p>
        </div>

        <div class="alert-detail-section">
          <h4>相关指标</h4>
          <div class="metrics-list">
            <div class="metric-item">
              <label>当前值</label>
              <span class="metric-value danger">{{ currentAlert.currentValue }}</span>
            </div>
            <div class="metric-item">
              <label>阈值</label>
              <span class="metric-value">{{ currentAlert.threshold }}</span>
            </div>
            <div class="metric-item">
              <label>持续时间</label>
              <span class="metric-value">{{ currentAlert.duration }}</span>
            </div>
          </div>
        </div>

        <div class="alert-detail-section">
          <h4>处理记录</h4>
          <el-timeline>
            <el-timeline-item v-for="(item, index) in currentAlert.history" :key="index" :timestamp="item.time" placement="top">
              {{ item.action }}
            </el-timeline-item>
          </el-timeline>
        </div>

        <div class="alert-detail-actions">
          <el-button type="primary" @click="handleAck(currentAlert)">确认告警</el-button>
          <el-button @click="handleResolve(currentAlert)">标记解决</el-button>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { getAlertList, getActiveAlerts, getAlertStats, acknowledgeAlert, batchAcknowledgeAlerts, resolveAlert, getAlertRules, createAlertRule, updateAlertRule, deleteAlertRule, updateRuleStatus } from '@/api/monitor'

export default {
  name: 'AlertManagement',
  data() {
    return {
      loading: false,
      alertStats: {
        critical: 0,
        warning: 0,
        info: 0,
        resolved: 0
      },
      alertList: [],
      selectedAlerts: [],
      ruleList: [],
      ruleDialogVisible: false,
      ruleForm: {
        id: null,
        name: '',
        type: '',
        operator: '>',
        threshold: '',
        unit: '%',
        duration: '5',
        level: 'warning',
        notify: ['email'],
        receivers: []
      },
      ruleRules: {
        name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
        type: [{ required: true, message: '请选择监控类型', trigger: 'change' }],
        level: [{ required: true, message: '请选择告警级别', trigger: 'change' }]
      },
      alertDetailVisible: false,
      currentAlert: null
    }
  },
  created() {
    this.loadAlerts()
    this.loadRules()
    this.loadStats()
  },
  methods: {
    // 加载告警列表
    async loadAlerts() {
      this.loading = true
      try {
        const res = await getAlertList({ page: 1, pageSize: 100, status: 'firing' })
        if (res.code === 200) {
          this.alertList = res.data?.list || []
        } else {
          this.$message.error(res.message || '加载告警列表失败')
        }
      } catch (error) {
        console.error('加载告警列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    
    // 加载告警规则
    async loadRules() {
      try {
        const res = await getAlertRules()
        if (res.code === 200) {
          this.ruleList = (res.data || []).map(rule => {
            // 解析condition字段
            let conditionData = {}
            try {
              conditionData = rule.condition ? JSON.parse(rule.condition) : {}
            } catch (e) {
              console.error('解析condition失败:', e)
            }
            
            return {
              ...rule,
              enabled: rule.status === 1,
              notify: rule.notifyChannels ? JSON.parse(rule.notifyChannels) : [],
              operator: conditionData.operator || '>',
              threshold: conditionData.threshold || '',
              unit: conditionData.unit || '%',
              duration: conditionData.duration || '5'
            }
          })
        }
      } catch (error) {
        console.error('加载告警规则失败:', error)
      }
    },
    
    // 加载统计信息
    async loadStats() {
      try {
        const res = await getAlertStats()
        if (res.code === 200) {
          this.alertStats = res.data || { critical: 0, warning: 0, info: 0, resolved: 0 }
        }
      } catch (error) {
        console.error('加载统计信息失败:', error)
      }
    },
    formatTime(time) {
      return new Date(time).toLocaleString()
    },
    getLevelType(level) {
      const types = { critical: 'danger', warning: 'warning', info: 'info' }
      return types[level] || 'info'
    },
    getLevelLabel(level) {
      const labels = { critical: '严重', warning: '警告', info: '通知' }
      return labels[level] || level
    },
    getLevelIcon(level) {
      const icons = { critical: 'el-icon-warning', warning: 'el-icon-warning-outline', info: 'el-icon-info' }
      return icons[level] || 'el-icon-info'
    },
    getRuleTypeLabel(type) {
      const labels = { error_rate: '错误率', response_time: '响应时间', call_count: '调用量', success_rate: '成功率' }
      return labels[type] || type
    },
    getNotifyIcon(type) {
      const icons = { email: 'el-icon-message', sms: 'el-icon-phone', webhook: 'el-icon-connection', dingtalk: 'el-icon-chat-dot-round' }
      return icons[type] || 'el-icon-bell'
    },
    handleSelectionChange(val) {
      this.selectedAlerts = val
    },
    
    // 确认告警
    async handleAck(alert) {
      try {
        await this.$confirm('确认该告警？', '提示', { type: 'warning' })
        const res = await acknowledgeAlert(alert.id, { acknowledgedBy: '系统管理员' })
        if (res.code === 200) {
          this.$message.success('告警已确认')
          this.loadAlerts()
          this.loadStats()
          if (this.alertDetailVisible) {
            this.alertDetailVisible = false
          }
        } else {
          this.$message.error(res.message || '确认失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('确认失败: ' + error.message)
        }
      }
    },
    
    // 批量确认告警
    async handleBatchAck() {
      if (this.selectedAlerts.length === 0) {
        this.$message.warning('请选择要确认的告警')
        return
      }
      
      try {
        await this.$confirm(`确认选中的 ${this.selectedAlerts.length} 条告警？`, '提示', { type: 'warning' })
        const ids = this.selectedAlerts.map(alert => alert.id)
        const res = await batchAcknowledgeAlerts({ ids, acknowledgedBy: '系统管理员' })
        if (res.code === 200) {
          this.$message.success('批量确认成功')
          this.loadAlerts()
          this.loadStats()
        } else {
          this.$message.error(res.message || '批量确认失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('批量确认失败: ' + error.message)
        }
      }
    },
    
    // 解决告警
    async handleResolve(alert) {
      try {
        await this.$confirm('标记该告警为已解决？', '提示', { type: 'warning' })
        const res = await resolveAlert(alert.id)
        if (res.code === 200) {
          this.$message.success('已标记为解决')
          this.alertDetailVisible = false
          this.loadAlerts()
          this.loadStats()
        } else {
          this.$message.error(res.message || '标记失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('标记失败: ' + error.message)
        }
      }
    },
    
    showAlertDetail(row) {
      this.currentAlert = row
      this.alertDetailVisible = true
    },
    
    showRuleDialog(rule) {
      if (rule) {
        // 编辑模式：从已解析的规则中获取数据
        this.ruleForm = { 
          id: rule.id,
          name: rule.name,
          type: rule.type,
          operator: rule.operator || '>',
          threshold: rule.threshold || '',
          unit: rule.unit || '%',
          duration: rule.duration || '5',
          level: rule.level,
          notify: rule.notify || [],
          receivers: []
        }
      } else {
        // 新建模式：使用默认值
        this.ruleForm = { 
          id: null, 
          name: '', 
          type: '', 
          operator: '>', 
          threshold: '', 
          unit: '%', 
          duration: '5', 
          level: 'warning', 
          notify: ['email'], 
          receivers: [] 
        }
      }
      this.ruleDialogVisible = true
      
      // 重置表单验证状态
      this.$nextTick(() => {
        if (this.$refs.ruleFormRef) {
          this.$refs.ruleFormRef.clearValidate()
        }
      })
    },
    
    // 保存规则
    async saveRule() {
      try {
        const valid = await this.$refs.ruleFormRef.validate()
        if (!valid) return
        
        const ruleData = {
          ...this.ruleForm,
          condition: JSON.stringify({
            operator: this.ruleForm.operator,
            threshold: this.ruleForm.threshold,
            unit: this.ruleForm.unit,
            duration: this.ruleForm.duration
          }),
          notifyChannels: JSON.stringify(this.ruleForm.notify),
          target: 'api',
          status: 1
        }
        
        const res = this.ruleForm.id 
          ? await updateAlertRule(ruleData)
          : await createAlertRule(ruleData)
          
        if (res.code === 200) {
          this.$message.success('规则保存成功')
          this.ruleDialogVisible = false
          this.loadRules()
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (error) {
        this.$message.error('保存失败: ' + error.message)
      }
    },
    
    // 删除规则
    async deleteRule(rule) {
      try {
        await this.$confirm('确定删除该规则？', '警告', { type: 'warning' })
        const res = await deleteAlertRule(rule.id)
        if (res.code === 200) {
          this.$message.success('规则已删除')
          this.loadRules()
        } else {
          this.$message.error(res.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('删除失败: ' + error.message)
        }
      }
    },
    
    // 切换规则状态
    async toggleRule(rule) {
      try {
        const res = await updateRuleStatus({ id: rule.id, status: rule.enabled ? 1 : 0 })
        if (res.code === 200) {
          this.$message.success(`规则已${rule.enabled ? '启用' : '禁用'}`)
        } else {
          this.$message.error(res.message || '操作失败')
          rule.enabled = !rule.enabled // 恢复状态
        }
      } catch (error) {
        this.$message.error('操作失败: ' + error.message)
        rule.enabled = !rule.enabled // 恢复状态
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.alerts-page {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    h2 { color: #fff; margin: 0; font-size: 20px; }
  }

  .stats-row {
    margin-bottom: 20px;
  }

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    border-radius: 10px;
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);

    &.critical { border-left: 4px solid #f56c6c; .stat-icon { color: #f56c6c; } }
    &.warning { border-left: 4px solid #e6a23c; .stat-icon { color: #e6a23c; } }
    &.info { border-left: 4px solid #409eff; .stat-icon { color: #409eff; } }
    &.resolved { border-left: 4px solid #67c23a; .stat-icon { color: #67c23a; } }

    .stat-icon {
      font-size: 36px;
      margin-right: 15px;
    }

    .stat-info {
      .stat-value { font-size: 28px; font-weight: 600; color: #fff; }
      .stat-label { font-size: 14px; color: #8b8ba7; margin-top: 5px; }
    }
  }

  .alerts-card,
  .rules-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;
    margin-bottom: 20px;

    ::v-deep .el-card__header {
      border-bottom: 1px solid rgba(102, 126, 234, 0.2);
      padding: 15px 20px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    color: #fff;
  }

  ::v-deep .el-table {
    background: transparent;
    color: #fff;
    &::before { display: none; }
    th { background: rgba(102, 126, 234, 0.1); color: #8b8ba7; border-bottom: 1px solid rgba(102, 126, 234, 0.2); }
    td { border-bottom: 1px solid rgba(102, 126, 234, 0.1); }
    tr { background: transparent; &:hover > td { background: rgba(102, 126, 234, 0.1); } }
  }

  .condition-code {
    background: rgba(102, 126, 234, 0.1);
    padding: 4px 8px;
    border-radius: 4px;
    font-size: 12px;
    color: #a8b2d1;
  }

  .notify-tag {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 28px;
    height: 28px;
    margin-right: 5px;
    background: rgba(102, 126, 234, 0.2);
    border-radius: 50%;
    color: #667eea;
  }

  .danger-btn { color: #f56c6c; }
}

::v-deep .el-dialog {
  background: #1a1a2e;
  .el-dialog__header { border-bottom: 1px solid rgba(102, 126, 234, 0.2); }
  .el-dialog__title { color: #fff; }
  .el-form-item__label { color: #8b8ba7; }
  .el-input__inner,
  .el-select .el-input__inner { background: rgba(35, 35, 55, 0.8); border-color: rgba(102, 126, 234, 0.3); color: #fff; }
}

::v-deep .el-drawer {
  background: #1a1a2e;
  .el-drawer__header { color: #fff; border-bottom: 1px solid rgba(102, 126, 234, 0.2); }
}

.alert-detail {
  padding: 20px;

  .alert-detail-header {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 20px;
    border-radius: 10px;
    margin-bottom: 20px;

    &.critical { background: rgba(245, 108, 108, 0.2); i { color: #f56c6c; font-size: 36px; } }
    &.warning { background: rgba(230, 162, 60, 0.2); i { color: #e6a23c; font-size: 36px; } }
    &.info { background: rgba(64, 158, 255, 0.2); i { color: #409eff; font-size: 36px; } }

    .alert-title { color: #fff; font-size: 16px; font-weight: 600; }
    .alert-time { color: #8b8ba7; font-size: 13px; margin-top: 5px; }
  }

  .alert-detail-section {
    margin-bottom: 25px;

    h4 {
      color: #fff;
      margin-bottom: 15px;
      padding-bottom: 10px;
      border-bottom: 1px solid rgba(102, 126, 234, 0.1);
    }

    p { color: #a8b2d1; line-height: 1.6; }
  }

  .metrics-list {
    display: flex;
    gap: 30px;

    .metric-item {
      label { display: block; color: #6b6b80; font-size: 12px; margin-bottom: 5px; }
      .metric-value { font-size: 18px; font-weight: 600; color: #fff; &.danger { color: #f56c6c; } }
    }
  }

  ::v-deep .el-timeline-item__timestamp { color: #6b6b80; }
  ::v-deep .el-timeline-item__content { color: #a8b2d1; }

  .alert-detail-actions {
    margin-top: 30px;
    padding-top: 20px;
    border-top: 1px solid rgba(102, 126, 234, 0.2);
    display: flex;
    gap: 10px;
  }
}
</style>
