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
    handleSend() {
      if (!this.testForm.url) {
        this.$message.warning('请输入请求URL')
        return
      }

      this.loading = true
      const startTime = Date.now()

      // 模拟请求
      setTimeout(() => {
        this.response = {
          status: 200,
          time: Date.now() - startTime,
          data: JSON.stringify(
            {
              code: 200,
              message: 'success',
              data: {
                result: 'API测试成功'
              }
            },
            null,
            2
          ),
          headers: JSON.stringify(
            {
              'content-type': 'application/json',
              date: new Date().toUTCString(),
              server: 'nginx'
            },
            null,
            2
          )
        }
        this.loading = false
      }, 1000)
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
