import { defineStore } from 'pinia'
import { ref } from 'vue'

const TOKEN_KEY = 'gd_token'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) || '')
  const user = ref(null)
  const menus = ref([])
  const permissions = ref([])
  const dynamicRoutesAdded = ref(false)
  const unreadNoticeCount = ref(0)

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
    unreadNoticeCount.value = 0
    localStorage.removeItem(TOKEN_KEY)
  }

  function setUnreadCount(count) {
    unreadNoticeCount.value = count
  }

  function decrementUnread() {
    if (unreadNoticeCount.value > 0) {
      unreadNoticeCount.value--
    }
  }

  function hasPerm(code) {
    return permissions.value.includes(code)
  }

  return { token, user, menus, permissions, dynamicRoutesAdded, unreadNoticeCount, setSession, clear, setUnreadCount, decrementUnread, hasPerm }
})
