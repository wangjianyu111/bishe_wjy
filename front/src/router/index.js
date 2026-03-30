import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const modules = import.meta.glob('../views/**/*.vue')

function loadComponent(componentPath) {
  if (!componentPath) return () => import('../views/Placeholder.vue')
  const key = `../views/${componentPath}.vue`
  const loader = modules[key]
  return loader ? loader : () => import('../views/Placeholder.vue')
}

function collectLeafRoutes(menuNodes, out = []) {
  for (const m of menuNodes || []) {
    if (m.children?.length) {
      collectLeafRoutes(m.children, out)
    } else if (m.path && m.component) {
      let p = m.path.startsWith('/') ? m.path.slice(1) : m.path
      out.push({
        path: p,
        name: String(m.permCode || `menu-${m.permId}`),
        component: loadComponent(m.component),
        meta: { title: m.permName },
      })
    }
  }
  return out
}

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('../views/Login.vue'),
      meta: { public: true },
    },
    {
      path: '/register',
      name: 'Register',
      component: () => import('../views/Register.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      name: 'main',
      component: () => import('../layout/MainLayout.vue'),
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('../views/Dashboard.vue'),
          meta: { title: '首页' },
        },
      ],
    },
  ],
})

router.beforeEach(async (to, from, next) => {
  if (to.meta.public) {
    next()
    return
  }
  const store = useUserStore()
  if (!store.token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }
  if (!store.dynamicRoutesAdded) {
    try {
      if (!store.user) {
        const { fetchInfo } = await import('@/api/auth')
        const data = await fetchInfo()
        store.setSession({
          token: store.token,
          user: data.user,
          menus: data.menus,
          permissions: data.permissions,
        })
      }
      const dynamic = collectLeafRoutes(store.menus)
      const names = new Set(router.getRoutes().map((r) => r.name))
      for (const r of dynamic) {
        if (!names.has(r.name)) {
          router.addRoute('main', r)
          names.add(r.name)
        }
      }
      store.dynamicRoutesAdded = true
    } catch {
      store.clear()
      next({ name: 'Login' })
      return
    }
  }
  next()
})

export default router
