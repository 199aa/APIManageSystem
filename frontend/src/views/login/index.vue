<template>
  <div class="login-container">
    <!-- 网格背景 -->
    <div class="grid-background"></div>

    <div class="login-box">
      <!-- Logo和标题 -->
      <div class="logo-section">
        <div class="logo-icon">
          <svg viewBox="0 0 48 48" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M24 8L14 14L24 20L34 14L24 8Z" fill="url(#grad1)" />
            <path d="M14 20L14 30L24 36L24 26L14 20Z" fill="url(#grad2)" />
            <path d="M34 20L34 30L24 36L24 26L34 20Z" fill="url(#grad3)" />
            <defs>
              <linearGradient id="grad1" x1="14" y1="8" x2="34" y2="20">
                <stop offset="0%" style="stop-color:#667eea;stop-opacity:1" />
                <stop offset="100%" style="stop-color:#764ba2;stop-opacity:1" />
              </linearGradient>
              <linearGradient id="grad2" x1="14" y1="20" x2="24" y2="36">
                <stop offset="0%" style="stop-color:#667eea;stop-opacity:0.8" />
                <stop offset="100%" style="stop-color:#764ba2;stop-opacity:0.8" />
              </linearGradient>
              <linearGradient id="grad3" x1="34" y1="20" x2="24" y2="36">
                <stop offset="0%" style="stop-color:#764ba2;stop-opacity:0.8" />
                <stop offset="100%" style="stop-color:#667eea;stop-opacity:0.8" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1>API Hub</h1>
        <p class="subtitle">一站式平台综合接口管理平台</p>
      </div>

      <!-- 登录表单 -->
      <div v-if="!isRegister" class="form-section">
        <el-form ref="loginForm" :model="loginForm" :rules="loginRules">
          <el-form-item prop="username">
            <el-input v-model="loginForm.username" placeholder="用户名/邮箱" prefix-icon="el-icon-user"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="el-icon-lock" show-password @keyup.enter.native="handleLogin"></el-input>
          </el-form-item>
          <el-button type="primary" class="submit-btn" @click="handleLogin" :loading="loading">
            登录
          </el-button>
        </el-form>
        <div class="form-footer">
          <span class="link-text" @click="toggleMode">申请帐号或返回号</span>
        </div>
      </div>

      <!-- 注册表单 -->
      <div v-else class="form-section">
        <h2 class="form-title">注册帐号或账号</h2>
        <el-form ref="registerForm" :model="registerForm" :rules="registerRules">
          <el-form-item prop="email">
            <el-input v-model="registerForm.email" placeholder="Email" prefix-icon="el-icon-message"></el-input>
          </el-form-item>
          <el-form-item prop="username">
            <el-input v-model="registerForm.username" placeholder="输入用户名" prefix-icon="el-icon-user"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="密码" prefix-icon="el-icon-lock" show-password></el-input>
          </el-form-item>
          <el-form-item prop="phone">
            <el-input v-model="registerForm.phone" placeholder="请输入联系号" prefix-icon="el-icon-phone" class="code-input">
              <!-- <el-button slot="append" @click="sendCode" :disabled="codeCountdown > 0" class="code-btn">
                {{ codeCountdown > 0 ? `${codeCountdown}秒` : '60秒' }}
              </el-button> -->
            </el-input>
          </el-form-item>
          <el-button type="primary" class="submit-btn" @click="handleRegister" :loading="loading">
            注册
          </el-button>
        </el-form>
        <div class="form-footer">
          <span class="link-text" @click="toggleMode">已有账号？去登录</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { login, register } from '@/api/user'

export default {
  name: 'Login',
  data() {
    return {
      isRegister: false,
      loginForm: {
        username: '',
        password: ''
      },
      registerForm: {
        username: '',
        email: '',
        password: '',
        phone: ''
      },
      loginRules: {
        username: [{ required: true, message: '请输入邮箱', trigger: 'blur' }],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ]
      },
      registerRules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 20, message: '用户名长度在3-20个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
        ],
        phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }]
      },
      loading: false,
      codeCountdown: 0,
      codeTimer: null
    }
  },
  beforeDestroy() {
    if (this.codeTimer) {
      clearInterval(this.codeTimer)
    }
  },
  methods: {
    toggleMode() {
      this.isRegister = !this.isRegister
      this.resetForms()
    },
    resetForms() {
      this.loginForm = { username: '', password: '' }
      this.registerForm = { username: '', email: '', password: '', phone: '' }
      this.$nextTick(() => {
        this.$refs.loginForm && this.$refs.loginForm.clearValidate()
        this.$refs.registerForm && this.$refs.registerForm.clearValidate()
      })
    },
    handleLogin() {
      this.$refs.loginForm.validate((valid) => {
        if (valid) {
          this.loading = true
          login(this.loginForm)
            .then((res) => {
              if (res.code === 200) {
                this.$store.dispatch('user/setToken', res.data.token)
                this.$store.dispatch('user/setUserInfo', res.data.userInfo)
                this.$store.dispatch('user/setPermissions', res.data.permissions || [])
                this.$message.success('登录成功')
                this.$router.push('/dashboard')
              }
            })
            .catch((err) => {
              this.$message.error(err.message || '登录失败')
            })
            .finally(() => {
              this.loading = false
            })
        }
      })
    },
    handleRegister() {
      this.$refs.registerForm.validate((valid) => {
        if (valid) {
          this.loading = true
          const data = {
            username: this.registerForm.username,
            email: this.registerForm.email,
            password: this.registerForm.password
          }
          register(data)
            .then((res) => {
              if (res.code === 200) {
                this.$message.success('注册成功，请登录')
                this.isRegister = false
                this.loginForm.username = this.registerForm.email
                this.resetForms()
              }
            })
            .catch((err) => {
              this.$message.error(err.message || '注册失败')
            })
            .finally(() => {
              this.loading = false
            })
        }
      })
    },
    sendCode() {
      if (!this.registerForm.phone) {
        this.$message.warning('请先输入手机号')
        return
      }
      if (!/^1[3-9]\d{9}$/.test(this.registerForm.phone)) {
        this.$message.warning('请输入正确的手机号')
        return
      }

      // this.$message.success('验证码已发送到手机')
      // this.codeCountdown = 60
      // this.codeTimer = setInterval(() => {
      //   this.codeCountdown--
      //   if (this.codeCountdown <= 0) {
      //     clearInterval(this.codeTimer)
      //   }
      // }, 1000)
    }
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #1a1a2e;
  position: relative;
  overflow: hidden;

  .grid-background {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image: linear-gradient(rgba(102, 126, 234, 0.05) 1px, transparent 1px), linear-gradient(90deg, rgba(102, 126, 234, 0.05) 1px, transparent 1px);
    background-size: 50px 50px;
    z-index: 0;
  }

  .login-box {
    position: relative;
    z-index: 1;
    width: 400px;
    padding: 40px 35px;
    background: rgba(35, 35, 55, 0.95);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    border: 1px solid rgba(102, 126, 234, 0.15);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.5);

    @media screen and (max-width: 768px) {
      width: 90%;
      padding: 30px 25px;
    }

    .logo-section {
      text-align: center;
      margin-bottom: 35px;

      .logo-icon {
        width: 56px;
        height: 56px;
        margin: 0 auto 16px;
        svg {
          width: 100%;
          height: 100%;
        }
      }

      h1 {
        margin: 0 0 8px;
        font-size: 28px;
        font-weight: 600;
        color: #fff;
      }

      .subtitle {
        margin: 0;
        font-size: 13px;
        color: #8b8ba7;
      }
    }

    .form-section {
      .form-title {
        text-align: center;
        color: #fff;
        font-size: 18px;
        margin: 0 0 25px;
        font-weight: 500;
      }

      ::v-deep .el-form-item {
        margin-bottom: 20px;

        .el-form-item__error {
          color: #f56c6c;
          font-size: 12px;
          padding-top: 4px;
        }
      }

      ::v-deep .el-input {
        .el-input__inner {
          height: 44px;
          line-height: 44px;
          background: rgba(25, 25, 40, 0.8);
          border: 1px solid rgba(102, 126, 234, 0.15);
          border-radius: 6px;
          color: #fff;
          font-size: 14px;
          padding-left: 40px;
          transition: all 0.3s;

          &::placeholder {
            color: #666;
          }

          &:hover,
          &:focus {
            background: rgba(25, 25, 40, 0.95);
            border-color: rgba(102, 126, 234, 0.4);
          }
        }

        .el-input__prefix {
          left: 12px;
          color: #8b8ba7;
        }

        .el-input__suffix {
          right: 12px;

          .el-icon-view,
          .el-icon-error {
            color: #8b8ba7;

            &:hover {
              color: #667eea;
            }
          }
        }
      }

      .code-input {
        ::v-deep .el-input-group__append {
          background: transparent;
          border: 1px solid rgba(102, 126, 234, 0.15);
          border-left: none;
          padding: 0;

          .code-btn {
            background: transparent;
            color: #667eea;
            border: none;
            font-size: 13px;
            padding: 0 15px;
            height: 42px;

            &:hover:not(.is-disabled) {
              color: #764ba2;
              background: rgba(102, 126, 234, 0.1);
            }

            &.is-disabled {
              color: #555;
              cursor: not-allowed;
            }
          }
        }
      }

      .submit-btn {
        width: 100%;
        height: 44px;
        font-size: 15px;
        font-weight: 500;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        border-radius: 6px;
        margin-top: 10px;
        color: #fff;
        transition: all 0.3s;
        letter-spacing: 0.5px;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 20px rgba(102, 126, 234, 0.35);
        }

        &:active {
          transform: translateY(0);
        }

        &.is-loading {
          opacity: 0.8;
        }
      }

      .form-footer {
        text-align: center;
        margin-top: 20px;

        .link-text {
          color: #667eea;
          font-size: 13px;
          cursor: pointer;
          transition: color 0.3s;

          &:hover {
            color: #764ba2;
          }
        }
      }
    }
  }
}
</style>
