<template>
  <q-page class="q-pa-lg">
    <div class="row items-center q-mb-lg">
      <div class="text-h5 col">Workspaces</div>
      <q-btn color="primary" icon="add" label="New Workspace" unelevated @click="showDialog = true" />
    </div>

    <div v-if="store.loading" class="flex flex-center q-pa-xl">
      <q-spinner size="2rem" color="primary" />
    </div>

    <div v-else-if="store.workspaces.length === 0" class="text-center text-grey-6 q-pa-xl">
      <q-icon name="workspaces" size="4rem" class="q-mb-md" />
      <div class="text-subtitle1">No workspaces yet. Create one to get started.</div>
    </div>

    <div v-else class="row q-gutter-md">
      <q-card
        v-for="ws in store.workspaces"
        :key="ws.id"
        class="col-12 col-sm-5 col-md-3 cursor-pointer workspace-card"
        flat
        bordered
        @click="$router.push(`/workspaces/${ws.id}`)"
      >
        <q-card-section>
          <div class="text-h6">{{ ws.name }}</div>
          <q-badge :color="roleColor(ws.role)" class="q-mt-sm">{{ ws.role }}</q-badge>
        </q-card-section>
      </q-card>
    </div>

    <q-dialog v-model="showDialog">
      <q-card style="min-width: 340px">
        <q-card-section>
          <div class="text-h6">New Workspace</div>
        </q-card-section>
        <q-card-section>
          <q-input
            v-model="newName"
            label="Workspace name"
            outlined
            autofocus
            :rules="[val => !!val.trim() || 'Name is required']"
            @keyup.enter="onCreate"
          />
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup @click="newName = ''" />
          <q-btn label="Create" color="primary" unelevated :loading="creating" @click="onCreate" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useQuasar } from 'quasar'
import { useWorkspacesStore } from 'stores/workspaces'

const $q = useQuasar()
const store = useWorkspacesStore()

const showDialog = ref(false)
const newName = ref('')
const creating = ref(false)

onMounted(() => store.fetchAll())

function roleColor(role: string) {
  return role === 'owner' ? 'primary' : role === 'editor' ? 'teal' : 'grey'
}

async function onCreate() {
  if (!newName.value.trim()) return
  creating.value = true
  try {
    await store.create(newName.value.trim())
    showDialog.value = false
    newName.value = ''
    $q.notify({ type: 'positive', message: 'Workspace created' })
  } catch (e) {
    $q.notify({ type: 'negative', message: e instanceof Error ? e.message : 'Failed to create workspace' })
  } finally {
    creating.value = false
  }
}
</script>

<style scoped>
.workspace-card:hover {
  border-color: var(--q-primary);
}
</style>
