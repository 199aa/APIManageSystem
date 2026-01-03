const state = {
  apiList: [],
  currentApi: null
}

const mutations = {
  SET_API_LIST(state, list) {
    state.apiList = list
  },
  SET_CURRENT_API(state, api) {
    state.currentApi = api
  }
}

const actions = {
  setApiList({ commit }, list) {
    commit('SET_API_LIST', list)
  },
  setCurrentApi({ commit }, api) {
    commit('SET_CURRENT_API', api)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
