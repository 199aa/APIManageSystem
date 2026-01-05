<template>
  <div class="api-detail">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button icon="el-icon-arrow-left" @click="goBack">返回</el-button>
        <h2>{{ isNew ? '新增接口' : '编辑接口' }}</h2>
      </div>
      <div class="header-right">
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </div>
    </div>

    <!-- 多Tab表单 -->
    <el-card class="form-card">
      <el-tabs v-model="activeTab" type="border-card">
        <!-- 基础信息 -->
        <el-tab-pane label="基础信息" name="basic">
          <el-form :model="form" :rules="rules" ref="basicForm" label-width="120px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="接口名称" prop="name">
                  <el-input v-model="form.name" placeholder="请输入接口名称"></el-input>
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="归属平台" prop="platformId">
                  <el-select v-model="form.platformId" placeholder="请选择平台" style="width: 100%">
                    <el-option v-for="p in platforms" :key="p.id" :label="p.name" :value="p.id"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="20">
              <el-col :span="16">
                <el-form-item label="请求路径" prop="path">
                  <el-input v-model="form.path" placeholder="如：/api/v1/users">
                    <template slot="prepend">{{ getBaseUrl }}</template>
                  </el-input>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="请求方法" prop="method">
                  <el-select v-model="form.method" style="width: 100%">
                    <el-option label="GET" value="GET"></el-option>
                    <el-option label="POST" value="POST"></el-option>
                    <el-option label="PUT" value="PUT"></el-option>
                    <el-option label="DELETE" value="DELETE"></el-option>
                    <el-option label="PATCH" value="PATCH"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="接口描述">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入接口描述"></el-input>
            </el-form-item>
            <el-row :gutter="20">
              <el-col :span="8">
                <el-form-item label="是否聚合接口">
                  <el-switch v-model="form.isAggregate" :active-value="1" :inactive-value="0"></el-switch>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="状态">
                  <el-select v-model="form.status" style="width: 100%">
                    <el-option label="正常" :value="1"></el-option>
                    <el-option label="异常" :value="0"></el-option>
                    <el-option label="离线" :value="2"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-tab-pane>

        <!-- 入参定义 -->
        <el-tab-pane label="入参定义" name="params">
          <div class="section-header">
            <span>请求参数列表</span>
            <el-button type="primary" size="small" icon="el-icon-plus" @click="addParam">添加参数</el-button>
          </div>
          <el-table :data="form.requestParams" border style="width: 100%">
            <el-table-column label="参数名" min-width="150">
              <template slot-scope="scope">
                <el-input v-model="scope.row.name" size="small" placeholder="参数名"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="参数位置" width="120">
              <template slot-scope="scope">
                <el-select v-model="scope.row.position" size="small">
                  <el-option label="Header" value="header"></el-option>
                  <el-option label="Query" value="query"></el-option>
                  <el-option label="Body" value="body"></el-option>
                  <el-option label="Path" value="path"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="100">
              <template slot-scope="scope">
                <el-select v-model="scope.row.type" size="small">
                  <el-option label="String" value="string"></el-option>
                  <el-option label="Integer" value="integer"></el-option>
                  <el-option label="Boolean" value="boolean"></el-option>
                  <el-option label="Number" value="number"></el-option>
                  <el-option label="Array" value="array"></el-option>
                  <el-option label="Object" value="object"></el-option>
                </el-select>
              </template>
            </el-table-column>
            <el-table-column label="必填" width="80">
              <template slot-scope="scope">
                <el-switch v-model="scope.row.required" size="small"></el-switch>
              </template>
            </el-table-column>
            <el-table-column label="默认值" width="120">
              <template slot-scope="scope">
                <el-input v-model="scope.row.defaultValue" size="small" placeholder="默认值"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="示例值" width="120">
              <template slot-scope="scope">
                <el-input v-model="scope.row.example" size="small" placeholder="示例值"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="说明" min-width="150">
              <template slot-scope="scope">
                <el-input v-model="scope.row.description" size="small" placeholder="参数说明"></el-input>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="80" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" class="danger-btn" @click="removeParam(scope.$index)">
                  <i class="el-icon-delete"></i>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- 出参定义 -->
        <el-tab-pane label="出参定义" name="response">
          <div class="section-header">
            <span>响应结构定义</span>
            <el-button type="text" size="small" @click="parseResponseJson">从JSON解析</el-button>
          </div>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="code-section">
                <h4>响应示例 JSON</h4>
                <el-input type="textarea" v-model="form.responseExample" :rows="15" placeholder='粘贴响应JSON示例，如：
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "示例"
  }
}'></el-input>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="code-section">
                <h4>解析后的结构</h4>
                <div class="json-tree" v-if="parsedResponse.length">
                  <div v-for="(item, index) in parsedResponse" :key="index" class="tree-item" :style="{ paddingLeft: item.level * 20 + 'px' }">
                    <span class="field-name">{{ item.name }}</span>
                    <span class="field-type">({{ item.type }})</span>
                    <span class="field-desc" v-if="item.desc">: {{ item.desc }}</span>
                  </div>
                </div>
                <div v-else class="no-data">请在左侧输入JSON并点击"从JSON解析"</div>
              </div>
            </el-col>
          </el-row>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script>
import { getApiDetail, saveApi } from '@/api/api'
import { getAllPlatforms } from '@/api/platform'

export default {
  name: 'ApiDetail',
  data() {
    return {
      isNew: true,
      activeTab: 'basic',
      saving: false,
      platforms: [],
      form: {
        id: null,
        name: '',
        platformId: null,
        path: '',
        method: 'GET',
        description: '',
        isAggregate: 0,
        status: 1,
        requestParams: [],
        responseExample: ''
      },
      rules: {
        name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
        platformId: [{ required: true, message: '请选择归属平台', trigger: 'change' }],
        path: [{ required: true, message: '请输入请求路径', trigger: 'blur' }],
        method: [{ required: true, message: '请选择请求方法', trigger: 'change' }]
      },
      parsedResponse: []
    }
  },
  computed: {
    getBaseUrl() {
      const platform = this.platforms.find((p) => p.id === this.form.platformId)
      return platform ? platform.baseUrl : 'https://'
    }
  },
  created() {
    this.loadPlatforms()
    const id = this.$route.params.id
    if (id && id !== 'new') {
      this.isNew = false
      this.loadDetail(id)
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
    async loadDetail(id) {
      try {
        const res = await getApiDetail(id)
        if (res.code === 200) {
          this.form = {
            ...res.data,
            requestParams: res.data.requestParams ? JSON.parse(res.data.requestParams) : []
          }
          if (this.form.responseExample) {
            this.parseResponseJson()
          }
        }
      } catch (e) {
        console.error(e)
      }
    },
    goBack() {
      this.$router.push('/api/list')
    },
    addParam() {
      this.form.requestParams.push({
        name: '',
        position: 'query',
        type: 'string',
        required: false,
        defaultValue: '',
        example: '',
        description: ''
      })
    },
    removeParam(index) {
      this.form.requestParams.splice(index, 1)
    },
    parseResponseJson() {
      try {
        const json = JSON.parse(this.form.responseExample)
        this.parsedResponse = this.flattenJson(json, '', 0)
      } catch (e) {
        this.$message.error('JSON格式错误，请检查')
      }
    },
    flattenJson(obj, prefix, level) {
      const result = []
      for (const key in obj) {
        const value = obj[key]
        const name = prefix ? `${prefix}.${key}` : key
        const type = Array.isArray(value) ? 'array' : typeof value

        result.push({ name: key, type, level, desc: '' })

        if (type === 'object' && value !== null) {
          result.push(...this.flattenJson(value, name, level + 1))
        } else if (type === 'array' && value.length > 0 && typeof value[0] === 'object') {
          result.push(...this.flattenJson(value[0], name + '[0]', level + 1))
        }
      }
      return result
    },
    async handleSave() {
      try {
        await this.$refs.basicForm.validate()
        this.saving = true
        const data = {
          ...this.form,
          requestParams: JSON.stringify(this.form.requestParams)
        }
        const res = await saveApi(data)
        if (res.code === 200) {
          this.$message.success('保存成功')
          this.$router.push('/api/list')
        }
      } catch (e) {
        console.error(e)
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.api-detail {
  padding: 20px;

  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    .header-left {
      display: flex;
      align-items: center;
      gap: 15px;

      h2 {
        color: #fff;
        margin: 0;
        font-size: 20px;
      }
    }
  }

  .form-card {
    background: rgba(35, 35, 55, 0.95);
    border: 1px solid rgba(102, 126, 234, 0.2);
    border-radius: 8px;

    ::v-deep .el-tabs--border-card {
      background: transparent;
      border: none;

      .el-tabs__header {
        background: rgba(102, 126, 234, 0.1);
        border-bottom: 1px solid rgba(102, 126, 234, 0.2);
      }

      .el-tabs__item {
        color: #8b8ba7;
        border: none;

        &.is-active {
          color: #667eea;
          background: rgba(102, 126, 234, 0.2);
        }

        &:hover {
          color: #667eea;
        }
      }

      .el-tabs__content {
        padding: 20px;
      }
    }

    ::v-deep .el-form-item__label {
      color: #8b8ba7;
    }

    ::v-deep .el-input__inner,
    ::v-deep .el-textarea__inner,
    ::v-deep .el-select .el-input__inner {
      background: rgba(35, 35, 55, 0.8);
      border-color: rgba(102, 126, 234, 0.3);
      color: #fff;

      &::placeholder {
        color: #6b6b80;
      }
    }

    ::v-deep .el-input-group__prepend {
      background: rgba(102, 126, 234, 0.2);
      border-color: rgba(102, 126, 234, 0.3);
      color: #8b8ba7;
    }
  }

  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;

    span {
      color: #fff;
      font-size: 14px;
      font-weight: 500;
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
      border: 1px solid rgba(102, 126, 234, 0.2);
    }

    td {
      border: 1px solid rgba(102, 126, 234, 0.1);
      background: transparent;
    }

    tr:hover > td {
      background: rgba(102, 126, 234, 0.1);
    }
  }

  .danger-btn {
    color: #f56c6c !important;
  }

  .code-section {
    h4 {
      color: #fff;
      margin-bottom: 10px;
      font-size: 14px;
    }
  }

  .json-tree {
    background: rgba(0, 0, 0, 0.3);
    border-radius: 8px;
    padding: 15px;
    min-height: 300px;
    max-height: 400px;
    overflow: auto;

    .tree-item {
      padding: 5px 0;
      font-family: monospace;
      font-size: 13px;

      .field-name {
        color: #667eea;
      }

      .field-type {
        color: #f5a623;
        margin-left: 5px;
      }

      .field-desc {
        color: #8b8ba7;
        margin-left: 5px;
      }
    }
  }

  .no-data {
    color: #6b6b80;
    text-align: center;
    padding: 50px;
  }
}
</style>
