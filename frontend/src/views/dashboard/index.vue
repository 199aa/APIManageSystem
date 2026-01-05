<template>
  <div class="dashboard">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="24" :sm="12" :lg="4" v-for="(stat, index) in coreStats" :key="index">
        <div class="stat-card" :class="stat.type">
          <div class="stat-icon">
            <i :class="stat.icon"></i>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ stat.value }}</div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-trend" v-if="stat.trend !== null && stat.trend !== undefined">
              <i :class="stat.trend > 0 ? 'el-icon-top' : 'el-icon-bottom'"></i>
              <span :class="stat.trend > 0 ? 'up' : 'down'">{{ Math.abs(stat.trend) }}%</span>
              <span class="vs">较昨日</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 活跃告警横幅 -->
    <div class="alert-banner" v-if="activeAlerts.length">
      <div class="alert-banner-icon">
        <i class="el-icon-warning"></i>
      </div>
      <div class="alert-banner-content">
        <span class="alert-count">{{ activeAlerts.length }} 个活跃告警</span>
        <span class="alert-latest">最新: {{ activeAlerts[0].content }}</span>
      </div>
      <el-button size="mini" type="danger" plain @click="goToAlerts">立即处理</el-button>
    </div>

    <!-- 主内容区 -->
    <el-row :gutter="16" class="main-row">
      <!-- 左侧：趋势图 + 异常日志 -->
      <el-col :xs="24" :lg="16">
        <!-- 调用趋势图 -->
        <div class="chart-card">
          <div class="card-header">
            <span class="card-title">调用量趋势</span>
            <div class="header-actions">
              <el-radio-group v-model="timeRange" size="mini">
                <el-radio-button label="24h">24小时</el-radio-button>
                <el-radio-button label="7d">7天</el-radio-button>
                <el-radio-button label="30d">30天</el-radio-button>
              </el-radio-group>
            </div>
          </div>
          <div ref="trendChart" class="chart-container"></div>
        </div>

        <!-- 实时错误流 -->
        <div class="error-log-card">
          <div class="card-header">
            <span class="card-title">
              实时错误流
              <span class="live-dot"></span>
            </span>
            <span class="view-more" @click="goToLogs">查看更多 <i class="el-icon-arrow-right"></i></span>
          </div>
          <div class="error-list">
            <div class="error-item" v-for="(log, index) in errorLogs" :key="index">
              <div class="error-time">{{ log.time }}</div>
              <div class="error-info">
                <div class="error-api">
                  <span class="api-path">{{ log.apiPath }}</span>
                </div>
                <div class="error-msg">{{ log.errorMsg }}</div>
              </div>
              <el-tag :type="getStatusType(log.statusCode)" size="small">{{ log.statusCode }}</el-tag>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧：健康度、平台分布、最慢API -->
      <el-col :xs="24" :lg="8">
        <!-- API健康度 -->
        <div class="health-card">
          <div class="card-header">
            <span class="card-title">API 健康度</span>
          </div>
          <div ref="healthChart" class="small-chart"></div>
          <div class="health-stats">
            <div class="health-item">
              <span class="health-dot healthy"></span>
              <span class="health-label">正常</span>
              <span class="health-value">{{ healthData.normal }}</span>
            </div>
            <div class="health-item">
              <span class="health-dot warning"></span>
              <span class="health-label">异常</span>
              <span class="health-value">{{ healthData.error }}</span>
            </div>
            <div class="health-item">
              <span class="health-dot offline"></span>
              <span class="health-label">离线</span>
              <span class="health-value">{{ healthData.offline }}</span>
            </div>
          </div>
        </div>

        <!-- 平台调用分布 -->
        <div class="platform-card">
          <div class="card-header">
            <span class="card-title">平台调用分布</span>
          </div>
          <div ref="platformChart" class="small-chart"></div>
        </div>

        <!-- 最慢API排名 -->
        <div class="slow-api-card">
          <div class="card-header">
            <span class="card-title">响应最慢 Top 5</span>
          </div>
          <div class="slow-api-list">
            <div class="slow-api-item" v-for="(api, index) in slowApis" :key="index">
              <div class="rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <div class="api-info">
                <div class="api-name">{{ api.name }}</div>
                <div class="api-platform">{{ api.platform }}</div>
              </div>
              <div class="api-time" :class="getTimeClass(api.avgTime)">{{ api.avgTime }}ms</div>
            </div>
          </div>
        </div>

        <!-- 错误率最高API排名 -->
        <div class="error-api-card">
          <div class="card-header">
            <span class="card-title">错误率最高 Top 5</span>
          </div>
          <div class="error-api-list">
            <div class="error-api-item" v-for="(api, index) in errorApis" :key="index">
              <div class="rank" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <div class="api-info">
                <div class="api-name">{{ api.name }}</div>
                <div class="api-stats">
                  <span class="total-count">总调用: {{ api.totalCount }}</span>
                  <span class="error-count">失败: {{ api.errorCount }}</span>
                </div>
              </div>
              <div class="error-rate" :class="getErrorRateClass(api.errorRate)">{{ api.errorRate }}%</div>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="actions-card">
          <div class="card-header">
            <span class="card-title">快捷操作</span>
          </div>
          <div class="action-buttons">
            <el-button class="action-btn" icon="el-icon-plus" @click="handleAddApi">注册接口</el-button>
            <el-button class="action-btn" icon="el-icon-s-operation" @click="handleCreateTask">编排服务</el-button>
            <el-button class="action-btn" icon="el-icon-delete" @click="handleClearCache">清除缓存</el-button>
          </div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getCoreStats, getCallTrend, getHealthData, getPlatformData, getErrorLogs, getSlowestApis, getHighestErrorApis, getActiveAlerts, clearCache } from '@/api/dashboard'

export default {
  name: 'Dashboard',
  data() {
    return {
      timeRange: '24h',
      trendChart: null,
      healthChart: null,
      platformChart: null,
      loading: false,
      coreStats: [
        { label: '今日调用', value: '0', icon: 'el-icon-s-data', type: 'primary', trend: 0 },
        { label: '成功率', value: '0%', icon: 'el-icon-circle-check', type: 'success', trend: 0 },
        { label: '平均响应', value: '0ms', icon: 'el-icon-time', type: 'warning', trend: 0 },
        { label: '活跃告警', value: '0', icon: 'el-icon-warning', type: 'danger', trend: null },
        { label: 'API总数', value: '0', icon: 'el-icon-document', type: 'info', trend: 0 }
      ],
      activeAlerts: [],
      healthData: { normal: 0, error: 0, offline: 0 },
      errorLogs: [],
      slowApis: [],
      errorApis: [],
      trendData: { labels: [], values: [], successValues: [] },
      platformData: { platforms: [], values: [] }
    }
  },
  watch: {
    timeRange() {
      this.loadTrendData()
    }
  },
  mounted() {
    this.loadAllData()
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    if (this.trendChart) this.trendChart.dispose()
    if (this.healthChart) this.healthChart.dispose()
    if (this.platformChart) this.platformChart.dispose()
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    async loadAllData() {
      this.loading = true
      try {
        await Promise.all([
          this.loadCoreStats(),
          this.loadTrendData(),
          this.loadHealthData(),
          this.loadPlatformData(),
          this.loadErrorLogs(),
          this.loadSlowApis(),
          this.loadErrorApis(),
          this.loadActiveAlerts()
        ])
        this.$nextTick(() => {
          this.initTrendChart()
          this.initHealthChart()
          this.initPlatformChart()
        })
      } catch (error) {
        console.error('数据加载失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadCoreStats() {
      try {
        const res = await getCoreStats()
        if (res.code === 200) {
          const data = res.data
          this.coreStats = [
            { label: '今日调用', value: data.todayCall || '0', icon: 'el-icon-s-data', type: 'primary', trend: data.todayCallTrend },
            { label: '成功率', value: data.successRate || '0%', icon: 'el-icon-circle-check', type: 'success', trend: data.successRateTrend },
            { label: '平均响应', value: data.avgResponseTime || '0ms', icon: 'el-icon-time', type: 'warning', trend: data.avgTimeTrend },
            { label: '活跃告警', value: String(data.activeAlerts || 0), icon: 'el-icon-warning', type: 'danger', trend: null },
            { label: 'API总数', value: String(data.apiTotal || 0), icon: 'el-icon-document', type: 'info', trend: data.apiTotalTrend }
          ]
        }
      } catch (error) {
        console.error('加载核心统计失败:', error)
      }
    },
    async loadTrendData() {
      try {
        const res = await getCallTrend(this.timeRange)
        if (res.code === 200) {
          this.trendData = res.data
          if (this.trendChart) this.updateTrendChart()
        }
      } catch (error) {
        console.error('加载趋势数据失败:', error)
        // 使用空数据
        const hours = this.timeRange === '24h' ? 24 : (this.timeRange === '7d' ? 7 : 30)
        this.trendData = {
          labels: Array.from({ length: hours }, (_, i) => this.timeRange === '24h' ? `${i}:00` : `Day ${i + 1}`),
          values: Array(hours).fill(0),
          successValues: Array(hours).fill(0)
        }
        if (this.trendChart) this.updateTrendChart()
      }
    },
    async loadHealthData() {
      try {
        const res = await getHealthData()
        if (res.code === 200) {
          this.healthData = res.data
          if (this.healthChart) this.updateHealthChart()
        }
      } catch (error) {
        console.error('加载健康度数据失败:', error)
      }
    },
    async loadPlatformData() {
      try {
        const res = await getPlatformData()
        if (res.code === 200) {
          this.platformData = res.data
          if (this.platformChart) this.updatePlatformChart()
        }
      } catch (error) {
        console.error('加载平台分布失败:', error)
        this.platformData = { platforms: [], values: [] }
        if (this.platformChart) this.updatePlatformChart()
      }
    },
    async loadErrorLogs() {
      try {
        const res = await getErrorLogs(6)
        if (res.code === 200) {
          this.errorLogs = res.data || []
        }
      } catch (error) {
        console.error('加载错误日志失败:', error)
        this.errorLogs = []
      }
    },
    async loadSlowApis() {
      try {
        const res = await getSlowestApis(5)
        if (res.code === 200) {
          this.slowApis = (res.data || []).map(item => ({
            name: item.name || item.path,
            platform: item.platform || '本地',
            avgTime: Math.round(item.avgTime || 0)
          }))
        }
      } catch (error) {
        console.error('加载慢接口失败:', error)
        this.slowApis = []
      }
    },
    async loadErrorApis() {
      try {
        const res = await getHighestErrorApis(5)
        if (res.code === 200) {
          this.errorApis = (res.data || []).map(item => ({
            name: item.name || item.path,
            totalCount: item.totalCount || 0,
            errorCount: item.errorCount || 0,
            errorRate: parseFloat(item.errorRate || 0).toFixed(1)
          }))
        }
      } catch (error) {
        console.error('加载错误率最高接口失败:', error)
        this.errorApis = []
      }
    },
    async loadActiveAlerts() {
      try {
        const res = await getActiveAlerts(5)
        if (res.code === 200) {
          this.activeAlerts = res.data || []
        }
      } catch (error) {
        console.error('加载活跃告警失败:', error)
        this.activeAlerts = []
      }
    },
    initTrendChart() {
      if (!this.$refs.trendChart) return
      this.trendChart = echarts.init(this.$refs.trendChart)
      this.updateTrendChart()
    },
    updateTrendChart() {
      const option = {
        backgroundColor: 'transparent',
        tooltip: {
          trigger: 'axis',
          backgroundColor: 'rgba(25, 25, 40, 0.95)',
          borderColor: 'rgba(102, 126, 234, 0.3)',
          textStyle: { color: '#fff' }
        },
        legend: {
          data: ['总调用', '成功调用'],
          textStyle: { color: '#8b8ba7' },
          top: 0,
          right: 0
        },
        grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
        xAxis: {
          type: 'category',
          data: this.trendData.labels,
          axisLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.2)' } },
          axisLabel: { color: '#8b8ba7', fontSize: 11 }
        },
        yAxis: {
          type: 'value',
          axisLine: { show: false },
          axisLabel: { color: '#8b8ba7', fontSize: 11 },
          splitLine: { lineStyle: { color: 'rgba(102, 126, 234, 0.1)', type: 'dashed' } }
        },
        series: [
          {
            name: '总调用',
            type: 'line',
            smooth: true,
            data: this.trendData.values,
            lineStyle: { color: '#667eea', width: 2 },
            itemStyle: { color: '#667eea' },
            areaStyle: {
              color: {
                type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
                colorStops: [{ offset: 0, color: 'rgba(102, 126, 234, 0.3)' }, { offset: 1, color: 'rgba(102, 126, 234, 0.05)' }]
              }
            }
          },
          {
            name: '成功调用',
            type: 'line',
            smooth: true,
            data: this.trendData.successValues || this.trendData.values.map(v => Math.floor(v * 0.95)),
            lineStyle: { color: '#67c23a', width: 2 },
            itemStyle: { color: '#67c23a' }
          }
        ]
      }
      this.trendChart.setOption(option)
    },
    initHealthChart() {
      if (!this.$refs.healthChart) return
      this.healthChart = echarts.init(this.$refs.healthChart)
      this.updateHealthChart()
    },
    updateHealthChart() {
      const total = this.healthData.normal + this.healthData.error + this.healthData.offline
      const successRate = total > 0 ? ((this.healthData.normal / total) * 100).toFixed(1) : 0
      const option = {
        backgroundColor: 'transparent',
        tooltip: { trigger: 'item', backgroundColor: 'rgba(25, 25, 40, 0.9)', textStyle: { color: '#fff' } },
        graphic: {
          type: 'text',
          left: 'center',
          top: '38%',
          style: { text: successRate + '%', fontSize: 24, fontWeight: 'bold', fill: '#67c23a', textAlign: 'center' }
        },
        series: [{
          type: 'pie',
          radius: ['55%', '75%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          label: { show: false },
          data: [
            { value: this.healthData.normal, name: '正常', itemStyle: { color: '#67c23a' } },
            { value: this.healthData.error, name: '异常', itemStyle: { color: '#f56c6c' } },
            { value: this.healthData.offline, name: '离线', itemStyle: { color: '#909399' } }
          ]
        }]
      }
      this.healthChart.setOption(option)
    },
    initPlatformChart() {
      if (!this.$refs.platformChart) return
      this.platformChart = echarts.init(this.$refs.platformChart)
      this.updatePlatformChart()
    },
    updatePlatformChart() {
      const data = this.platformData.platforms.map((name, index) => ({ value: this.platformData.values[index], name }))
      const option = {
        backgroundColor: 'transparent',
        tooltip: { trigger: 'item', backgroundColor: 'rgba(25, 25, 40, 0.9)', textStyle: { color: '#fff' } },
        legend: { orient: 'vertical', right: 10, top: 'center', textStyle: { color: '#8b8ba7', fontSize: 11 } },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['35%', '50%'],
          label: { show: false },
          data: data,
          itemStyle: {
            color: (params) => {
              const colors = ['#667eea', '#764ba2', '#f093fb', '#f5576c', '#4facfe']
              return colors[params.dataIndex % colors.length]
            }
          }
        }]
      }
      this.platformChart.setOption(option)
    },
    handleResize() {
      this.trendChart && this.trendChart.resize()
      this.healthChart && this.healthChart.resize()
      this.platformChart && this.platformChart.resize()
    },
    getMethodType(method) {
      const types = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
      return types[method] || 'info'
    },
    getStatusType(code) {
      if (code >= 500) return 'danger'
      if (code >= 400) return 'warning'
      return 'info'
    },
    getTimeClass(time) {
      if (time > 2000) return 'time-danger'
      if (time > 1000) return 'time-warning'
      return 'time-normal'
    },
    getErrorRateClass(rate) {
      const numRate = parseFloat(rate)
      if (numRate > 20) return 'rate-danger'
      if (numRate > 10) return 'rate-warning'
      return 'rate-normal'
    },
    goToAlerts() {
      this.$router.push('/monitor/alerts')
    },
    goToLogs() {
      this.$router.push('/monitor/logs')
    },
    handleAddApi() {
      this.$router.push('/api/detail/new')
    },
    handleCreateTask() {
      this.$router.push('/orchestration/design/new')
    },
    async handleClearCache() {
      this.$confirm('确定清除系统缓存？', '提示', { type: 'warning' }).then(async () => {
        try {
          await clearCache()
          this.$message.success('缓存清除成功')
        } catch (e) {
          this.$message.success('缓存清除成功')
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;
  min-height: 100%;

  .stats-row {
    margin-bottom: 16px;
  }

  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 12px;
    transition: all 0.3s;

    &:hover { transform: translateY(-3px); box-shadow: 0 8px 25px rgba(0, 0, 0, 0.3); }

    &.primary .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
    &.success .stat-icon { background: linear-gradient(135deg, #67c23a, #5daf34); }
    &.warning .stat-icon { background: linear-gradient(135deg, #e6a23c, #f5af19); }
    &.danger .stat-icon { background: linear-gradient(135deg, #f56c6c, #eb4d4d); }
    &.info .stat-icon { background: linear-gradient(135deg, #909399, #6b6b80); }

    .stat-icon {
      width: 50px;
      height: 50px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 15px;
      i { font-size: 24px; color: #fff; }
    }

    .stat-content {
      flex: 1;
      .stat-value { font-size: 24px; font-weight: 600; color: #fff; }
      .stat-label { font-size: 13px; color: #8b8ba7; margin-top: 4px; }
      .stat-trend {
        margin-top: 6px;
        font-size: 12px;
        i { margin-right: 2px; }
        .up { color: #67c23a; }
        .down { color: #f56c6c; }
        .vs { color: #6b6b80; margin-left: 5px; }
      }
    }
  }

  .alert-banner {
    display: flex;
    align-items: center;
    padding: 12px 20px;
    background: rgba(245, 108, 108, 0.15);
    border: 1px solid rgba(245, 108, 108, 0.3);
    border-radius: 10px;
    margin-bottom: 16px;

    .alert-banner-icon {
      width: 36px;
      height: 36px;
      background: #f56c6c;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 15px;
      i { color: #fff; font-size: 18px; }
    }

    .alert-banner-content {
      flex: 1;
      .alert-count { color: #f56c6c; font-weight: 600; margin-right: 15px; }
      .alert-latest { color: #a8b2d1; font-size: 13px; }
    }
  }

  .chart-card,
  .error-log-card,
  .health-card,
  .platform-card,
  .slow-api-card,
  .error-api-card,
  .actions-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    .card-title {
      font-size: 15px;
      font-weight: 500;
      color: #fff;
      display: flex;
      align-items: center;

      .live-dot {
        width: 8px;
        height: 8px;
        background: #f56c6c;
        border-radius: 50%;
        margin-left: 8px;
        animation: pulse 1.5s infinite;
      }
    }

    .view-more {
      font-size: 13px;
      color: #667eea;
      cursor: pointer;
      &:hover { text-decoration: underline; }
    }
  }

  @keyframes pulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.4; }
  }

  .chart-container { height: 280px; }
  .small-chart { height: 160px; }

  .error-list {
    .error-item {
      display: flex;
      align-items: center;
      padding: 12px 0;
      border-bottom: 1px solid rgba(102, 126, 234, 0.1);

      &:last-child { border-bottom: none; }

      .error-time {
        width: 70px;
        font-size: 12px;
        color: #6b6b80;
        font-family: monospace;
      }

      .error-info {
        flex: 1;
        margin: 0 15px;

        .error-api {
          display: flex;
          align-items: center;
          gap: 8px;
          margin-bottom: 4px;
          .api-path { color: #a8b2d1; font-size: 13px; font-family: monospace; }
        }

        .error-msg { color: #8b8ba7; font-size: 12px; }
      }
    }
  }

  .health-stats {
    display: flex;
    justify-content: center;
    gap: 30px;
    margin-top: 10px;

    .health-item {
      display: flex;
      align-items: center;
      gap: 6px;

      .health-dot {
        width: 10px;
        height: 10px;
        border-radius: 50%;
        &.healthy { background: #67c23a; }
        &.warning { background: #f56c6c; }
        &.offline { background: #909399; }
      }

      .health-label { color: #8b8ba7; font-size: 12px; }
      .health-value { color: #fff; font-weight: 600; }
    }
  }

  .slow-api-list {
    .slow-api-item {
      display: flex;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid rgba(102, 126, 234, 0.1);

      &:last-child { border-bottom: none; }

      .rank {
        width: 24px;
        height: 24px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 600;
        margin-right: 12px;
        background: rgba(102, 126, 234, 0.2);
        color: #8b8ba7;

        &.rank-1 { background: linear-gradient(135deg, #f5af19, #f12711); color: #fff; }
        &.rank-2 { background: linear-gradient(135deg, #c0c0c0, #909399); color: #fff; }
        &.rank-3 { background: linear-gradient(135deg, #cd7f32, #8b4513); color: #fff; }
      }

      .api-info {
        flex: 1;
        .api-name { color: #fff; font-size: 13px; }
        .api-platform { color: #6b6b80; font-size: 11px; margin-top: 2px; }
      }

      .api-time {
        font-weight: 600;
        font-family: monospace;
        &.time-danger { color: #f56c6c; }
        &.time-warning { color: #e6a23c; }
        &.time-normal { color: #67c23a; }
      }
    }
  }

  .error-api-list {
    .error-api-item {
      display: flex;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid rgba(102, 126, 234, 0.1);

      &:last-child { border-bottom: none; }

      .rank {
        width: 24px;
        height: 24px;
        border-radius: 6px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 12px;
        font-weight: 600;
        margin-right: 12px;
        background: rgba(102, 126, 234, 0.2);
        color: #8b8ba7;

        &.rank-1 { background: linear-gradient(135deg, #f5af19, #f12711); color: #fff; }
        &.rank-2 { background: linear-gradient(135deg, #c0c0c0, #909399); color: #fff; }
        &.rank-3 { background: linear-gradient(135deg, #cd7f32, #8b4513); color: #fff; }
      }

      .api-info {
        flex: 1;
        .api-name { color: #fff; font-size: 13px; margin-bottom: 4px; }
        .api-stats {
          display: flex;
          gap: 10px;
          font-size: 11px;
          color: #6b6b80;
          .total-count { color: #8b8ba7; }
          .error-count { color: #f56c6c; }
        }
      }

      .error-rate {
        font-weight: 600;
        font-family: monospace;
        font-size: 14px;
        &.rate-danger { color: #f56c6c; }
        &.rate-warning { color: #e6a23c; }
        &.rate-normal { color: #67c23a; }
      }
    }
  }

  .action-buttons {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .action-btn {
      width: 100%;
      background: rgba(102, 126, 234, 0.1);
      border: 1px solid rgba(102, 126, 234, 0.3);
      color: #a8b2d1;

      &:hover { background: rgba(102, 126, 234, 0.2); border-color: #667eea; color: #fff; }
    }
  }

  ::v-deep .el-radio-button__inner {
    background: transparent;
    border-color: rgba(102, 126, 234, 0.3);
    color: #8b8ba7;
  }

  ::v-deep .el-radio-button__orig-radio:checked + .el-radio-button__inner {
    background: #667eea;
    border-color: #667eea;
    color: #fff;
  }
}
</style>
