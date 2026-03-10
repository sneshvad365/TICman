<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <q-btn flat dense round icon="menu" @click="leftDrawerOpen = !leftDrawerOpen" />
        <q-toolbar-title>TICman</q-toolbar-title>
        <q-btn flat round icon="logout" @click="logout">
          <q-tooltip>Sign out</q-tooltip>
        </q-btn>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" show-if-above bordered>
      <q-list>
        <q-item-label header>Workspaces</q-item-label>

        <q-item clickable v-ripple to="/" exact>
          <q-item-section avatar><q-icon name="home" /></q-item-section>
          <q-item-section>All Workspaces</q-item-section>
        </q-item>

        <q-separator class="q-my-sm" />

        <q-item
          v-for="ws in workspacesStore.workspaces"
          :key="ws.id"
          clickable
          v-ripple
          :to="`/workspaces/${ws.id}`"
        >
          <q-item-section avatar>
            <q-avatar color="primary" text-color="white" size="sm">
              {{ ws.name[0].toUpperCase() }}
            </q-avatar>
          </q-item-section>
          <q-item-section>{{ ws.name }}</q-item-section>
          <q-item-section side>
            <q-badge :color="roleColor(ws.role)">{{ ws.role }}</q-badge>
          </q-item-section>
        </q-item>

        <q-item v-if="workspacesStore.workspaces.length === 0 && !workspacesStore.loading">
          <q-item-section class="text-grey-5 text-caption">No workspaces yet</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <router-view />
    </q-page-container>
  </q-layout>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from 'stores/auth'
import { useWorkspacesStore } from 'stores/workspaces'

const router = useRouter()
const auth = useAuthStore()
const workspacesStore = useWorkspacesStore()
const leftDrawerOpen = ref(false)

function roleColor(role: string) {
  return role === 'owner' ? 'primary' : role === 'editor' ? 'teal' : 'grey'
}

function logout() {
  auth.logout()
  router.push('/login')
}
</script>
