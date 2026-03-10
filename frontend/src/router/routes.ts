import { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  // Auth pages (no layout shell)
  {
    path: '/login',
    component: () => import('pages/LoginPage.vue'),
    meta: { public: true },
  },
  {
    path: '/register',
    component: () => import('pages/RegisterPage.vue'),
    meta: { public: true },
  },

  // App pages (with sidebar layout)
  {
    path: '/',
    component: () => import('layouts/MainLayout.vue'),
    children: [
      { path: '', component: () => import('pages/IndexPage.vue') },
      { path: 'workspaces/:id', component: () => import('pages/WorkspacePage.vue') },
      { path: 'collections/:id', component: () => import('pages/CollectionPage.vue') },
    ],
  },

  {
    path: '/:catchAll(.*)*',
    component: () => import('pages/ErrorNotFound.vue'),
  },
]

export default routes
