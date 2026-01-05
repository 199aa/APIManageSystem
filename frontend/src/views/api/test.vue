<template>
  <div class="api-test">
    <el-card>
      <div slot="header">API测试工具</div>

      <el-form :model="testForm" label-width="100px">
        <el-form-item label="请求方法">
          <el-select v-model="testForm.method" style="width: 200px">
            <el-option label="GET" value="GET"></el-option>
            <el-option label="POST" value="POST"></el-option>
            <el-option label="PUT" value="PUT"></el-option>
            <el-option label="DELETE" value="DELETE"></el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="请求URL">
          <el-input v-model="testForm.url" placeholder="请输入完整的API地址"></el-input>
        </el-form-item>

        <el-form-item label="请求头">
          <el-input type="textarea" v-model="testForm.headers" rows="4" placeholder='JSON格式，例如: {"Content-Type": "application/json"}'></el-input>
        </el-form-item>

        <el-form-item label="请求体" v-if="testForm.method !== 'GET'">
          <el-input type="textarea" v-model="testForm.body" rows="6" placeholder='JSON格式，例如: {"name": "test"}'></el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSend" :loading="loading">
            <i class="el-icon-s-promotion"></i> 发送请求
          </el-button>
          <el-button @click="handleClear">清空</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px" v-if="response">
      <div slot="header">
        <span>响应结果</span>
        <el-tag :type="response.status >= 200 && response.status < 300 ? 'success' : 'danger'" style="margin-left: 10px">
          {{ response.status }}
        </el-tag>
        <span style="margin-left: 10px; color: #909399">耗时: {{ response.time }}ms</span>
      </div>

      <el-tabs>
        <el-tab-pane label="响应体">
          <pre>{{ response.data }}</pre>
        </el-tab-pane>
        <el-tab-pane label="响应头">
          <pre>{{ response.headers }}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'ApiTest',
  data() {
    return {
      testForm: {
        method: 'GET',
        url: '',
        headers: '{\n  "Content-Type": "application/json"\n}',
        body: ''
      },
      loading: false,
      response: null
    }
  },
  methods: {
    async handleSend() {
      if (!this.testForm.url) {
        this.$message.warning('请输入请求URL')
        return
      }

      this.loading = true
      const startTime = Date.now()

      try {
        // 解析请求头
        let headers = {}
        try {
          headers = JSON.parse(this.testForm.headers)
        } catch (e) {
          this.$message.error('请求头格式错误，请使用正确的JSON格式')
          this.loading = false
          return
        }

        // 解析请求体
        let body = null
        if (this.testForm.method !== 'GET' && this.testForm.body) {
          try {
            body = JSON.parse(this.testForm.body)
          } catch (e) {
            this.$message.error('请求体格式错误，请使用正确的JSON格式')
            this.loading = false
            return
          }
        }

        // 构建请求配置
        const config = {
          method: this.testForm.method.toLowerCase(),
          url: this.testForm.url,
          headers: headers
        }

        // 如果有请求体，添加到配置中
        if (body !== null) {
          config.data = body
        }

        // 发送真实的HTTP请求
        const response = await this.$http(config)
        const endTime = Date.now()

        // 处理响应
        this.response = {
          status: response.status,
          time: endTime - startTime,
          data: JSON.stringify(response.data, null, 2),
          headers: JSON.stringify(response.headers, null, 2)
        }

        this.$message.success('请求成功')
      } catch (error) {
        const endTime = Date.now()

        // 处理错误响应
        this.response = {
          status: error.response ? error.response.status : 0,
          time: endTime - startTime,
          data: error.response ? JSON.stringify(error.response.data, null, 2) : JSON.stringify({ error: error.message }, null, 2),
          headers: error.response ? JSON.stringify(error.response.headers, null, 2) : '{}'
        }

        this.$message.error('请求失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },
    handleClear() {
      this.testForm = {
        method: 'GET',
        url: '',
        headers: '{\n  "Content-Type": "application/json"\n}',
        body: ''
      }
      this.response = null
    }
  }
}
</script>

<style lang="scss" scoped>
.api-test {
  pre {
    background-color: #f5f7fa;
    padding: 15px;
    border-radius: 4px;
    overflow-x: auto;
    max-height: 400px;
  }
}
</style>
