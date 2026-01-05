const state = {
  token: localStorage.getItem('token') || '',
  userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
  permissions: JSON.parse(localStorage.getItem('permissions') || '[]')
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    localStorage.setItem('token', token)
  },
  SET_USER_INFO(state, userInfo) {
    state.userInfo = userInfo
    localStorage.setItem('userInfo', JSON.stringify(userInfo))
  },
  SET_PERMISSIONS(state, permissions) {
    state.permissions = permissions
    localStorage.setItem('permissions', JSON.stringify(permissions))
  },
  LOGOUT(state) {
    state.token = ''
    state.userInfo = {}
    state.permissions = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
  }
}

const actions = {
  setToken({ commit }, token) {
    commit('SET_TOKEN', token)
  },
  setUserInfo({ commit }, userInfo) {
    commit('SET_USER_INFO', userInfo)
  },
  setPermissions({ commit }, permissions) {
    commit('SET_PERMISSIONS', permissions)
  },
  logout({ commit }) {
    commit('LOGOUT')
  }
}

const getters = {
  hasPermission: (state) => (permission) => {
    // 超级管理员拥有所有权限
    if (state.userInfo.roleId === 1) {
      return true
    }
    return state.permissions.includes(permission)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions,
  getters
}
