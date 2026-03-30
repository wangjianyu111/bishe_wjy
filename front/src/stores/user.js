import { defineStore } from 'pinia'
import { ref } from 'vue'

const TOKEN_KEY = 'gd_token'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(null)
  const menus = ref([])
  const permissions = ref([])
  const dynamicRoutesAdded = ref(false)

  function setSession({ token: t, user: u, menus: m, permissions: p }) {
    token.value = t
    user.value = u
    menus.value = m || []
    permissions.value = p || []
    localStorage.setItem(TOKEN_KEY, t)
  }

  function clear() {
    token.value = ''
    user.value = null
    menus.value = []
    permissions.value = []
    dynamicRoutesAdded.value = false
    localStorage.removeItem(TOKEN_KEY)
  }

  function hasPerm(code) {
    return permissions.value.includes(code)
  }

  return { token, user, menus, permissions, dynamicRoutesAdded, setSession, clear, hasPerm }
})
