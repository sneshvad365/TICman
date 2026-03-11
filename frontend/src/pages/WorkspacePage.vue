<template>
  <q-page class="q-pa-lg">
    <div class="row items-center q-mb-lg">
      <div class="col">
        <div class="text-caption text-grey-6">Workspace</div>
        <div class="text-h5">{{ workspace?.name ?? workspaceId }}</div>
      </div>
      <q-btn color="primary" icon="add" label="New Collection" unelevated @click="showDialog = true" />
    </div>

    <div v-if="store.loading" class="flex flex-center q-pa-xl">
      <q-spinner size="2rem" color="primary" />
    </div>

    <div v-else-if="store.collections.length === 0" class="text-center text-grey-6 q-pa-xl">
      <q-icon name="folder_open" size="4rem" class="q-mb-md" />
      <div class="text-subtitle1">No collections yet. Create one to organize your requests.</div>
    </div>

    <q-list v-else bordered separator class="rounded-borders">
      <q-item
        v-for="col in store.collections"
        :key="col.id"
        clickable
        v-ripple
        @click="$router.push(`/collections/${col.id}`)"
      >
        <q-item-section avatar>
          <q-icon name="folder" color="primary" />
        </q-item-section>
        <q-item-section>
          <q-item-label>{{ col.name }}</q-item-label>
          <q-item-label v-if="col.readme" caption>{{ col.readme }}</q-item-label>
        </q-item-section>
        <q-item-section side>
          <q-btn
            flat round dense
            icon="delete"
            color="grey-6"
            @click.stop="confirmDelete(col)"
          />
        </q-item-section>
      </q-item>
    </q-list>

    <!-- Create dialog -->
    <q-dialog v-model="showDialog">
      <q-card style="min-width: 380px">
        <q-card-section>
          <div class="text-h6">New Collection</div>
        </q-card-section>
        <q-card-section class="q-gutter-md">
          <q-input
            v-model="newName"
            label="Collection name"
            outlined
            autofocus
            :rules="[val => !!val.trim() || 'Required']"
            @keyup.enter="onCreate"
          />
          <q-input
            v-model="newReadme"
            label="Description (optional)"
            outlined
            type="textarea"
            rows="2"
          />
        </q-card-section>
        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup @click="resetForm" />
          <q-btn label="Create" color="primary" unelevated :loading="creating" @click="onCreate" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useQuasar } from 'quasar'
import { useCollectionsStore } from 'stores/collections'
import { useWorkspacesStore } from 'stores/workspaces'
import type { CollectionResponse } from 'src/api/collections'

const route = useRoute()
const $q = useQuasar()
const store = useCollectionsStore()
const workspacesStore = useWorkspacesStore()

const workspaceId = computed(() => route.params.id as string)
const workspace = computed(() => workspacesStore.workspaces.find(w => w.id === workspaceId.value))

const showDialog = ref(false)
const newName = ref('')
const newReadme = ref('')
const creating = ref(false)

watch(workspaceId, id => store.fetchForWorkspace(id), { immediate: true })

function resetForm() {
  newName.value = ''
  newReadme.value = ''
}

async function onCreate() {
  if (!newName.value.trim()) return
  creating.value = true
  try {
    await store.create(workspaceId.value, newName.value.trim(), newReadme.value)
    showDialog.value = false
    resetForm()
    $q.notify({ type: 'positive', message: 'Collection created' })
  } catch (e) {
    $q.notify({ type: 'negative', message: e instanceof Error ? e.message : 'Failed' })
  } finally {
    creating.value = false
  }
}

function confirmDelete(col: CollectionResponse) {
  $q.dialog({
    title: 'Delete collection',
    message: `Delete "${col.name}"? This cannot be undone.`,
    cancel: true,
    ok: { label: 'Delete', color: 'negative', flat: true },
  }).onOk(async () => {
    try {
      await store.delete(col.id)
      $q.notify({ type: 'positive', message: 'Collection deleted' })
    } catch (e) {
      $q.notify({ type: 'negative', message: e instanceof Error ? e.message : 'Failed' })
    }
  })
}
</script>
