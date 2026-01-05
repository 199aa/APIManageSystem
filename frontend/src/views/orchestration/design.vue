<template>
  <div class="aggregate-design">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button icon="el-icon-arrow-left" @click="goBack">返回</el-button>
        <h3>{{ isNew ? '新建聚合接口' : '编辑聚合接口' }}</h3>
        <el-tag v-if="!isNew" type="success" size="small">已保存</el-tag>
      </div>
      <div class="toolbar-right">
        <el-button @click="handleTest">测试运行</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        <el-button type="success" @click="handlePublish">发布</el-button>
      </div>
    </div>

    <div class="design-container">
      <!-- 左侧API列表 -->
      <div class="api-panel">
        <div class="panel-header">
          <span>原子API列表</span>
          <el-input v-model="apiSearch" size="small" placeholder="搜索API" prefix-icon="el-icon-search"></el-input>
        </div>
        <div class="api-list">
          <div v-for="api in filteredApis" :key="api.id" class="api-item" draggable="true" @dragstart="handleDragStart($event, api)" @dragend="handleDragEnd">
            <el-tag :type="getMethodType(api.method)" size="mini" effect="dark">{{ api.method }}</el-tag>
            <span class="api-name">{{ api.name }}</span>
          </div>
        </div>
      </div>

      <!-- 中间画布区域 -->
      <div class="canvas-panel" @drop="handleDrop" @dragover="handleDragOver">
        <div class="canvas-header">
          <span>编排画布</span>
          <el-radio-group v-model="executeMode" size="small">
            <el-radio-button label="serial">串行执行</el-radio-button>
            <el-radio-button label="parallel">并行执行</el-radio-button>
          </el-radio-group>
        </div>

        <div class="canvas-content">
          <!-- 开始节点 -->
          <div class="flow-node start-node">
            <div class="node-icon"><i class="el-icon-video-play"></i></div>
            <div class="node-info">
              <div class="node-title">开始</div>
              <div class="node-desc">定义入参</div>
            </div>
            <el-button type="text" size="mini" @click="editStartNode">配置</el-button>
          </div>

          <div class="flow-arrow" v-if="flowNodes.length"><i class="el-icon-arrow-down"></i></div>

          <!-- 流程节点 -->
          <template v-if="executeMode === 'serial'">
            <template v-for="(node, index) in flowNodes">
              <div :key="node.id" class="flow-node process-node" :class="{ active: activeNodeId === node.id }">
                <div class="node-icon"><i class="el-icon-connection"></i></div>
                <div class="node-info">
                  <div class="node-title">{{ node.name }}</div>
                  <div class="node-desc">
                    <el-tag :type="getMethodType(node.method)" size="mini">{{ node.method }}</el-tag>
                    {{ node.path }}
                  </div>
                </div>
                <div class="node-actions">
                  <el-button type="text" size="mini" @click="editNode(node)">配置</el-button>
                  <el-button type="text" size="mini" class="danger-btn" @click="removeNode(index)">
                    <i class="el-icon-delete"></i>
                  </el-button>
                </div>
              </div>
              <div :key="'arrow-'+node.id" class="flow-arrow" v-if="index < flowNodes.length - 1">
                <i class="el-icon-arrow-down"></i>
              </div>
            </template>
          </template>

          <!-- 并行模式 -->
          <template v-else>
            <div class="parallel-container" v-if="flowNodes.length">
              <div class="parallel-branch" v-for="(node, index) in flowNodes" :key="node.id">
                <div class="flow-node process-node">
                  <div class="node-icon"><i class="el-icon-connection"></i></div>
                  <div class="node-info">
                    <div class="node-title">{{ node.name }}</div>
                    <div class="node-desc">
                      <el-tag :type="getMethodType(node.method)" size="mini">{{ node.method }}</el-tag>
                    </div>
                  </div>
                  <el-button type="text" size="mini" class="danger-btn" @click="removeNode(index)">
                    <i class="el-icon-delete"></i>
                  </el-button>
                </div>
              </div>
            </div>
          </template>

          <div class="flow-arrow" v-if="flowNodes.length"><i class="el-icon-arrow-down"></i></div>

          <!-- 结果映射节点 -->
          <div class="flow-node mapping-node" v-if="flowNodes.length">
            <div class="node-icon"><i class="el-icon-s-grid"></i></div>
            <div class="node-info">
              <div class="node-title">结果映射</div>
              <div class="node-desc">配置输出结构</div>
            </div>
            <el-button type="text" size="mini" @click="openMappingEditor">配置</el-button>
          </div>

          <!-- 结束节点 -->
          <div class="flow-arrow" v-if="flowNodes.length"><i class="el-icon-arrow-down"></i></div>
          <div class="flow-node end-node">
            <div class="node-icon"><i class="el-icon-video-pause"></i></div>
            <div class="node-info">
              <div class="node-title">结束</div>
              <div class="node-desc">返回结果</div>
            </div>
          </div>

          <!-- 空状态 -->
          <div class="empty-tip" v-if="!flowNodes.length">
            <i class="el-icon-upload"></i>
            <p>拖拽左侧API到此处开始编排</p>
          </div>
        </div>
      </div>

      <!-- 右侧配置面板 -->
      <div class="config-panel">
        <div class="panel-header">
          <span>节点配置</span>
        </div>
        <div class="config-content" v-if="activeNode">
          <el-form label-width="80px" size="small">
            <el-form-item label="节点名称">
              <el-input v-model="activeNode.name"></el-input>
            </el-form-item>
            <el-form-item label="超时时间">
              <el-input-number v-model="activeNode.timeout" :min="1000" :max="60000" :step="1000"></el-input-number>
              <span class="unit">ms</span>
            </el-form-item>
            <el-form-item label="失败处理">
              <el-select v-model="activeNode.onError" style="width: 100%">
                <el-option label="终止流程" value="abort"></el-option>
                <el-option label="继续执行" value="continue"></el-option>
                <el-option label="使用默认值" value="default"></el-option>
              </el-select>
            </el-form-item>
            <el-divider>参数映射</el-divider>
            <p class="tip">将上游数据映射到当前节点入参</p>
            <div v-for="(param, idx) in activeNode.paramMappings" :key="idx" class="mapping-row">
              <el-input v-model="param.source" placeholder="来源路径" size="mini"></el-input>
              <span class="arrow">→</span>
              <el-input v-model="param.target" placeholder="目标参数" size="mini"></el-input>
            </div>
            <el-button type="text" icon="el-icon-plus" @click="addParamMapping">添加映射</el-button>
          </el-form>
        </div>
        <div class="empty-config" v-else>
          <i class="el-icon-setting"></i>
          <p>点击节点进行配置</p>
        </div>
      </div>
    </div>

    <!-- 结果映射编辑器弹窗 -->
    <el-dialog title="结果映射编辑器" :visible.sync="mappingDialogVisible" width="900px" top="5vh">
      <div class="mapping-editor">
        <div class="mapping-left">
          <h4>上游结果结构</h4>
          <div class="json-tree">
            <div v-for="(node, index) in flowNodes" :key="index" class="source-node">
              <div class="source-title">{{ node.name }}</div>
              <div class="source-fields">
                <div class="field-item" draggable="true">$.{{ node.name }}.data.id</div>
                <div class="field-item" draggable="true">$.{{ node.name }}.data.name</div>
                <div class="field-item" draggable="true">$.{{ node.name }}.data.value</div>
              </div>
            </div>
          </div>
        </div>
        <div class="mapping-center">
          <i class="el-icon-right"></i>
        </div>
        <div class="mapping-right">
          <h4>输出结构定义</h4>
          <el-input type="textarea" v-model="outputSchema" :rows="15" placeholder='定义输出JSON结构，如：
{
  "userId": "$.step1.data.id",
  "userName": "$.step1.data.name",
  "orderInfo": "$.step2.data"
}'></el-input>
        </div>
      </div>
      <div slot="footer">
        <el-button @click="mappingDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveMappingConfig">确定</el-button>
      </div>
    </el-dialog>

    <!-- 测试对话框 -->
    <el-dialog title="测试运行" :visible.sync="testDialogVisible" width="800px">
      <el-row :gutter="20">
        <el-col :span="12">
          <h4>输入参数 (JSON)</h4>
          <el-input type="textarea" v-model="testParams" :rows="12" placeholder='{"userId": "123", "orderId": "456"}'>
          </el-input>
          <div style="margin-top: 10px;">
            <el-button type="primary" :loading="testLoading" @click="executeTest">执行</el-button>
          </div>
        </el-col>
        <el-col :span="12">
          <h4>执行结果</h4>
          <div v-if="testResult" class="test-result">
            <el-alert :type="testResult.success ? 'success' : 'error'" :title="testResult.success ? '执行成功' : '执行失败'" :closable="false" style="margin-bottom: 10px;">
            </el-alert>
            <pre v-if="testResult.success" class="result-json">{{ JSON.stringify(testResult.data, null, 2) }}</pre>
            <el-alert v-else type="error" :title="testResult.error" :closable="false"></el-alert>
          </div>
          <el-empty v-else description="等待执行" :image-size="80"></el-empty>
        </el-col>
      </el-row>
    </el-dialog>
  </div>
</template>

<script>
import { getApiList, getAggregateDetail, saveAggregate, publishAggregate, testAggregate } from '@/api/api'

export default {
  name: 'AggregateDesign',
  data() {
    return {
      aggregateId: null,
      aggregateInfo: null,
      isNew: true,
      saving: false,
      apiSearch: '',
      apiList: [],
      flowNodes: [],
      executeMode: 'serial',
      activeNodeId: null,
      activeNode: null,
      mappingDialogVisible: false,
      outputSchema: '',
      draggingApi: null,
      testDialogVisible: false,
      testParams: '{}',
      testResult: null,
      testLoading: false
    }
  },
  computed: {
    filteredApis() {
      if (!this.apiSearch) return this.apiList
      return this.apiList.filter((api) => api.name.toLowerCase().includes(this.apiSearch.toLowerCase()) || api.path.toLowerCase().includes(this.apiSearch.toLowerCase()))
    }
  },
  created() {
    const id = this.$route.params.id
    if (id && id !== 'new') {
      this.isNew = false
      this.aggregateId = parseInt(id)
      this.loadDesign(this.aggregateId)
    }
    this.loadApis()
  },
  methods: {
    async loadApis() {
      try {
        const res = await getApiList({ pageSize: 1000, status: 1 })
        if (res.code === 200) {
          // 过滤掉聚合接口，只显示原子接口
          const list = res.data.list || res.data || []
          this.apiList = list.filter((api) => api.isAggregate !== 1)
        } else {
          this.$message.error('加载API列表失败')
        }
      } catch (e) {
        console.error(e)
        this.$message.error('加载API列表失败')
      }
    },

    async loadDesign(id) {
      try {
        const res = await getAggregateDetail(id)
        if (res.code === 200) {
          this.aggregateInfo = res.data

          // 解析编排配置
          if (res.data.aggregateConfig) {
            const config = JSON.parse(res.data.aggregateConfig)
            this.executeMode = config.executeMode || 'serial'
            this.flowNodes = config.nodes || []
            this.outputSchema = JSON.stringify(config.outputSchema || {}, null, 2)
          }
        } else {
          this.$message.error('加载配置失败')
        }
      } catch (e) {
        console.error(e)
        this.$message.error('加载配置失败')
      }
    },

    goBack() {
      this.$router.push('/orchestration/aggregate')
    },

    getMethodType(method) {
      const types = { GET: 'success', POST: 'primary', PUT: 'warning', DELETE: 'danger' }
      return types[method] || 'info'
    },

    handleDragStart(e, api) {
      this.draggingApi = api
      e.dataTransfer.effectAllowed = 'copy'
    },

    handleDragEnd() {
      this.draggingApi = null
    },

    handleDragOver(e) {
      e.preventDefault()
      e.dataTransfer.dropEffect = 'copy'
    },

    handleDrop(e) {
      e.preventDefault()
      if (this.draggingApi) {
        const node = {
          apiId: this.draggingApi.id,
          apiName: this.draggingApi.name,
          apiPath: this.draggingApi.path,
          method: this.draggingApi.method,
          platformId: this.draggingApi.platformId,
          nodeId: Date.now(),
          timeout: 5000,
          onError: 'abort',
          retryCount: 0,
          paramMappings: [],
          responsePath: this.generateResponsePath(this.draggingApi.name)
        }
        this.flowNodes.push(node)
        this.$message.success(`已添加: ${this.draggingApi.name}`)
      }
    },

    generateResponsePath(apiName) {
      // 生成响应路径，去除特殊字符
      const path = apiName.replace(/[^a-zA-Z0-9\u4e00-\u9fa5]/g, '')
      return path.substring(0, 20).toLowerCase()
    },

    editStartNode() {
      this.$prompt('请输入入参定义(JSON格式)', '配置开始节点', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'textarea',
        inputValue: JSON.stringify({ userId: { type: 'string', required: true } }, null, 2)
      })
        .then(({ value }) => {
          try {
            JSON.parse(value)
            this.$message.success('入参配置已保存')
          } catch (e) {
            this.$message.error('JSON格式不正确')
          }
        })
        .catch(() => {})
    },

    editNode(node) {
      this.activeNodeId = node.nodeId
      this.activeNode = node
    },

    removeNode(index) {
      const removed = this.flowNodes.splice(index, 1)[0]
      if (this.activeNode && this.activeNode.nodeId === removed.nodeId) {
        this.activeNode = null
        this.activeNodeId = null
      }
      this.$message.success('已移除节点')
    },

    addParamMapping() {
      if (this.activeNode) {
        if (!this.activeNode.paramMappings) {
          this.activeNode.paramMappings = []
        }
        this.activeNode.paramMappings.push({
          source: 'input.',
          target: '',
          required: false
        })
      }
    },

    removeParamMapping(index) {
      if (this.activeNode && this.activeNode.paramMappings) {
        this.activeNode.paramMappings.splice(index, 1)
      }
    },

    openMappingEditor() {
      this.mappingDialogVisible = true
    },

    saveMappingConfig() {
      try {
        if (this.outputSchema) {
          JSON.parse(this.outputSchema)
        }
        this.mappingDialogVisible = false
        this.$message.success('映射配置已保存')
      } catch (e) {
        this.$message.error('JSON格式不正确')
      }
    },

    handleTest() {
      if (this.flowNodes.length === 0) {
        this.$message.warning('请先添加API节点')
        return
      }

      this.testParams = '{}'
      this.testResult = null
      this.testDialogVisible = true
    },

    async executeTest() {
      if (!this.aggregateId) {
        this.$message.warning('请先保存聚合接口')
        return
      }

      this.testLoading = true
      try {
        const params = JSON.parse(this.testParams)
        const res = await testAggregate(this.aggregateId, params)

        if (res.code === 200) {
          this.testResult = {
            success: true,
            data: res.data
          }
          this.$message.success('执行成功')
        } else {
          this.testResult = {
            success: false,
            error: res.message
          }
          this.$message.error('执行失败: ' + res.message)
        }
      } catch (e) {
        console.error(e)
        this.testResult = {
          success: false,
          error: e.message
        }
        this.$message.error('执行失败: ' + e.message)
      } finally {
        this.testLoading = false
      }
    },

    async handleSave() {
      if (!this.aggregateInfo && !this.isNew) {
        this.$message.error('聚合接口信息缺失')
        return
      }

      if (this.flowNodes.length === 0) {
        this.$message.warning('请至少添加一个API节点')
        return
      }

      this.saving = true
      try {
        // 构建配置
        const config = {
          executeMode: this.executeMode,
          timeout: 30000,
          nodes: this.flowNodes,
          outputSchema: this.outputSchema ? JSON.parse(this.outputSchema) : {}
        }

        const data = {
          id: this.aggregateId,
          name: this.aggregateInfo?.name,
          path: this.aggregateInfo?.path,
          description: this.aggregateInfo?.description,
          aggregateConfig: JSON.stringify(config)
        }

        const res = await saveAggregate(data)
        if (res.code === 200) {
          this.$message.success('保存成功')
          if (this.isNew) {
            this.isNew = false
            this.aggregateId = res.data.id
            // 更新路由
            this.$router.replace(`/orchestration/design/${res.data.id}`)
          }
        } else {
          this.$message.error(res.message || '保存失败')
        }
      } catch (e) {
        console.error(e)
        this.$message.error('保存失败: ' + e.message)
      } finally {
        this.saving = false
      }
    },

    handlePublish() {
      if (!this.aggregateId) {
        this.$message.warning('请先保存聚合接口')
        return
      }

      this.$confirm('确定要发布该聚合接口吗？发布后将可以被调用。', '确认发布', {
        type: 'info',
        confirmButtonText: '发布',
        cancelButtonText: '取消'
      })
        .then(async () => {
          try {
            const res = await publishAggregate(this.aggregateId)
            if (res.code === 200) {
              this.$message.success('发布成功')
              if (this.aggregateInfo) {
                this.aggregateInfo.status = 1
              }
            } else {
              this.$message.error(res.message || '发布失败')
            }
          } catch (e) {
            console.error(e)
            this.$message.error('发布失败')
          }
        })
        .catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.aggregate-design {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #1a1a2e;

  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    background: rgba(35, 35, 55, 0.98);
    border-bottom: 1px solid rgba(102, 126, 234, 0.2);

    .toolbar-left {
      display: flex;
      align-items: center;
      gap: 15px;

      h3 {
        color: #fff;
        margin: 0;
      }
    }

    .toolbar-right {
      display: flex;
      gap: 10px;
    }
  }

  .design-container {
    flex: 1;
    display: flex;
    overflow: hidden;
  }

  .api-panel {
    width: 250px;
    background: rgba(35, 35, 55, 0.95);
    border-right: 1px solid rgba(102, 126, 234, 0.2);
    display: flex;
    flex-direction: column;

    .panel-header {
      padding: 15px;
      border-bottom: 1px solid rgba(102, 126, 234, 0.2);

      span {
        display: block;
        color: #fff;
        font-weight: 500;
        margin-bottom: 10px;
      }
    }

    .api-list {
      flex: 1;
      overflow-y: auto;
      padding: 10px;

      .api-item {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px;
        background: rgba(102, 126, 234, 0.1);
        border-radius: 6px;
        margin-bottom: 8px;
        cursor: grab;
        transition: all 0.2s;

        &:hover {
          background: rgba(102, 126, 234, 0.2);
          transform: translateX(5px);
        }

        .api-name {
          color: #fff;
          font-size: 13px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }

  .canvas-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    overflow: hidden;

    .canvas-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 20px;
      background: rgba(35, 35, 55, 0.5);
      border-bottom: 1px solid rgba(102, 126, 234, 0.2);

      span {
        color: #fff;
        font-weight: 500;
      }
    }

    .canvas-content {
      flex: 1;
      overflow-y: auto;
      padding: 30px;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
  }

  .flow-node {
    display: flex;
    align-items: center;
    gap: 15px;
    padding: 15px 20px;
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.3);
    border-radius: 8px;
    min-width: 300px;
    transition: all 0.2s;

    &:hover {
      border-color: #667eea;
      box-shadow: 0 0 15px rgba(102, 126, 234, 0.3);
    }

    &.active {
      border-color: #667eea;
      background: rgba(102, 126, 234, 0.1);
    }

    .node-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 20px;
    }

    &.start-node .node-icon {
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: #fff;
    }

    &.process-node .node-icon {
      background: linear-gradient(135deg, #11998e, #38ef7d);
      color: #fff;
    }

    &.mapping-node .node-icon {
      background: linear-gradient(135deg, #f093fb, #f5576c);
      color: #fff;
    }

    &.end-node .node-icon {
      background: linear-gradient(135deg, #4facfe, #00f2fe);
      color: #fff;
    }

    .node-info {
      flex: 1;

      .node-title {
        color: #fff;
        font-weight: 500;
        margin-bottom: 4px;
      }

      .node-desc {
        color: #8b8ba7;
        font-size: 12px;
      }
    }

    .node-actions {
      display: flex;
      gap: 5px;
    }
  }

  .flow-arrow {
    padding: 10px 0;
    color: #667eea;
    font-size: 20px;
  }

  .parallel-container {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
    justify-content: center;

    .parallel-branch {
      position: relative;
    }
  }

  .empty-tip {
    color: #6b6b80;
    text-align: center;
    padding: 50px;

    i {
      font-size: 48px;
      margin-bottom: 15px;
    }
  }

  .config-panel {
    width: 300px;
    background: rgba(35, 35, 55, 0.95);
    border-left: 1px solid rgba(102, 126, 234, 0.2);
    display: flex;
    flex-direction: column;

    .panel-header {
      padding: 15px;
      border-bottom: 1px solid rgba(102, 126, 234, 0.2);

      span {
        color: #fff;
        font-weight: 500;
      }
    }

    .config-content {
      flex: 1;
      padding: 15px;
      overflow-y: auto;

      ::v-deep .el-form-item__label {
        color: #8b8ba7;
      }

      ::v-deep .el-input__inner,
      ::v-deep .el-select .el-input__inner {
        background: rgba(35, 35, 55, 0.8);
        border-color: rgba(102, 126, 234, 0.3);
        color: #fff;
      }

      .unit {
        color: #8b8ba7;
        margin-left: 10px;
      }

      .tip {
        color: #6b6b80;
        font-size: 12px;
        margin-bottom: 10px;
      }

      .mapping-row {
        display: flex;
        align-items: center;
        gap: 5px;
        margin-bottom: 8px;

        .arrow {
          color: #667eea;
        }
      }
    }

    .empty-config {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #6b6b80;

      i {
        font-size: 48px;
        margin-bottom: 15px;
      }
    }
  }

  .danger-btn {
    color: #f56c6c !important;
  }
}

.mapping-editor {
  display: flex;
  gap: 20px;
  min-height: 400px;

  .mapping-left,
  .mapping-right {
    flex: 1;

    h4 {
      color: #fff;
      margin-bottom: 15px;
    }
  }

  .mapping-center {
    display: flex;
    align-items: center;
    color: #667eea;
    font-size: 24px;
  }

  .json-tree {
    background: rgba(0, 0, 0, 0.3);
    border-radius: 8px;
    padding: 15px;
    height: 350px;
    overflow: auto;

    .source-node {
      margin-bottom: 15px;

      .source-title {
        color: #667eea;
        font-weight: 500;
        margin-bottom: 8px;
      }

      .field-item {
        padding: 5px 10px;
        color: #a8b2d1;
        font-family: monospace;
        font-size: 12px;
        cursor: grab;

        &:hover {
          background: rgba(102, 126, 234, 0.2);
          border-radius: 4px;
        }
      }
    }
  }
}

.test-result {
  .result-json {
    background: rgba(0, 0, 0, 0.3);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;
    padding: 15px;
    color: #a8b2d1;
    font-size: 12px;
    max-height: 400px;
    overflow: auto;
    font-family: 'Courier New', monospace;
    white-space: pre-wrap;
    word-break: break-all;
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

  .el-textarea__inner {
    background: rgba(35, 35, 55, 0.8);
    border-color: rgba(102, 126, 234, 0.3);
    color: #fff;
  }
}

::v-deep .el-input__inner {
  background: rgba(35, 35, 55, 0.8);
  border-color: rgba(102, 126, 234, 0.3);
  color: #fff;
}
</style>
