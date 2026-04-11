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
      let c = m.component.startsWith('/') ? m.component.slice(1) : m.component
      out.push({
        path: p,
        name: String(m.permCode || `menu-${m.permId}`),
        component: loadComponent(c),
        meta: { title: m.permName },
      })
    }
  }
  return out
}

export function registerDynamicRoutes(menuNodes) {
  const dynamic = collectLeafRoutes(menuNodes)
  const names = new Set(router.getRoutes().map((r) => r.name))
  for (const r of dynamic) {
    if (!names.has(r.name)) {
      router.addRoute('main', r)
      names.add(r.name)
    }
  }
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
      children: [
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('../views/Profile.vue'),
          meta: { title: '个人资料' },
        },
      ],
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'NotFound',
      component: () => import('../views/NotFound.vue'),
      meta: { public: true },
    },
    {
      path: '/empty-home',
      name: 'EmptyHome',
      component: () => import('../views/EmptyHome.vue'),
      meta: { public: true },
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
      registerDynamicRoutes(store.menus)
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
