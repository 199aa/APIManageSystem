<template>
  <div class="blacklist-page">
    <div class="page-header">
      <h2>黑白名单管理</h2>
    </div>
    <el-card class="content-card">
      <el-row :gutter="30">
        <el-col :span="12">
          <div class="list-section blacklist">
            <div class="section-header">
              <i class="el-icon-warning-outline"></i>
              <span>IP黑名单</span>
            </div>
            <p class="tip">黑名单中的IP将被直接拒绝访问，每行一个IP地址</p>
            <el-input type="textarea" v-model="blacklistIPs" :rows="15" placeholder="192.168.1.100&#10;10.0.0.0/24"></el-input>
            <div class="section-footer">
              <span class="count">共 {{ blacklistCount }} 条</span>
              <el-button type="danger" size="small" @click="saveBlacklist">保存黑名单</el-button>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="list-section whitelist">
            <div class="section-header">
              <i class="el-icon-success"></i>
              <span>IP白名单</span>
            </div>
            <p class="tip">白名单中的IP将跳过所有安全检查和限流规则</p>
            <el-input type="textarea" v-model="whitelistIPs" :rows="15" placeholder="192.168.1.1&#10;127.0.0.1"></el-input>
            <div class="section-footer">
              <span class="count">共 {{ whitelistCount }} 条</span>
              <el-button type="success" size="small" @click="saveWhitelist">保存白名单</el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>
  </div>
</template>

<script>
import { getBlacklist, saveBlacklist, saveWhitelist } from '@/api/governance'

export default {
  name: 'Blacklist',
  data() {
    return {
      blacklistIPs: '',
      whitelistIPs: '',
      loading: false
    }
  },
  computed: {
    blacklistCount() {
      return this.blacklistIPs.split('\n').filter(ip => ip.trim()).length
    },
    whitelistCount() {
      return this.whitelistIPs.split('\n').filter(ip => ip.trim()).length
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        this.loading = true
        const res = await getBlacklist()
        if (res.code === 200) {
          this.blacklistIPs = res.data.blacklist ? res.data.blacklist.join('\n') : ''
          this.whitelistIPs = res.data.whitelist ? res.data.whitelist.join('\n') : ''
        }
      } catch (error) {
        console.error('加载黑白名单失败:', error)
      } finally {
        this.loading = false
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
    }
  }
}
</script>

<style lang="scss" scoped>
.blacklist-page {
  padding: 20px;

  .page-header {
    margin-bottom: 20px;
    h2 { color: #fff; margin: 0; font-size: 20px; }
  }

  .content-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;
  }

  .list-section {
    padding: 20px;
    border-radius: 8px;

    &.blacklist {
      background: rgba(245, 108, 108, 0.05);
      border: 1px solid rgba(245, 108, 108, 0.2);
    }

    &.whitelist {
      background: rgba(103, 194, 58, 0.05);
      border: 1px solid rgba(103, 194, 58, 0.2);
    }

    .section-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 10px;
      span { color: #fff; font-size: 16px; font-weight: 500; }
      i { font-size: 20px; }
    }

    &.blacklist .section-header i { color: #f56c6c; }
    &.whitelist .section-header i { color: #67c23a; }

    .tip { color: #8b8ba7; font-size: 13px; margin-bottom: 15px; }

    .section-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-top: 15px;
      .count { color: #8b8ba7; font-size: 13px; }
    }
  }

  ::v-deep .el-textarea__inner {
    background: rgba(35, 35, 55, 0.8);
    border-color: rgba(102, 126, 234, 0.3);
    color: #fff;
    font-family: monospace;
  }
}
</style>
