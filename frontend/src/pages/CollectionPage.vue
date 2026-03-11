<template>
  <q-page class="collection-page">
    <!-- Header -->
    <div class="page-header row items-center q-px-lg q-py-md">
      <div class="col">
        <div class="text-caption text-grey-6">
          <router-link :to="`/workspaces/${collection?.workspaceId}`" class="text-grey-6">
            {{ workspaceName }}
          </router-link>
          <q-icon name="chevron_right" size="xs" />
          {{ collection?.name ?? collectionId }}
        </div>
      </div>
      <q-btn color="primary" icon="add" label="New Request" unelevated dense @click="openCreate" />
    </div>

    <q-separator />

    <!-- Split pane -->
    <q-splitter v-model="splitterSize" class="collection-splitter" :limits="[20, 50]">
      <!-- Left: request list -->
      <template #before>
        <div class="column full-height">
          <div v-if="store.loading" class="flex flex-center q-pa-lg">
            <q-spinner color="primary" />
          </div>
          <div v-else-if="store.requests.length === 0" class="text-center text-grey-5 q-pa-lg">
            <q-icon name="http" size="3rem" class="q-mb-sm" />
            <div class="text-caption">No requests yet</div>
          </div>
          <q-list v-else class="col overflow-auto">
            <q-item
              v-for="req in store.requests"
              :key="req.id"
              clickable
              v-ripple
              :active="selectedRequest?.id === req.id"
              active-class="bg-blue-1"
              @click="selectRequest(req)"
            >
              <q-item-section avatar>
                <q-badge :color="methodColor(req.method)" class="method-badge">{{ req.method }}</q-badge>
              </q-item-section>
              <q-item-section>
                <q-item-label class="ellipsis">{{ req.name }}</q-item-label>
                <q-item-label caption class="text-mono ellipsis">{{ req.urlTemplate || '—' }}</q-item-label>
              </q-item-section>
              <q-item-section side>
                <q-btn flat round dense icon="more_vert" size="sm" color="grey-6" @click.stop="openMenu($event, req)" />
              </q-item-section>
            </q-item>
          </q-list>
        </div>
      </template>

      <!-- Right: request runner -->
      <template #after>
        <RequestRunner
          :request="selectedRequest"
          class="full-height"
          @send="onSend"
          @edit="openEdit"
        />
      </template>
    </q-splitter>

    <!-- Context menu -->
    <q-menu v-if="menuTarget !== null" v-model="menuOpen" :target="menuTarget" context-menu>
      <q-list dense style="min-width: 140px">
        <q-item clickable v-close-popup @click="openEdit(menuRequest!)">
          <q-item-section avatar><q-icon name="edit" /></q-item-section>
          <q-item-section>Edit</q-item-section>
        </q-item>
        <q-item clickable v-close-popup @click="confirmDelete(menuRequest!)">
          <q-item-section avatar><q-icon name="delete" color="negative" /></q-item-section>
          <q-item-section class="text-negative">Delete</q-item-section>
        </q-item>
      </q-list>
    </q-menu>

    <!-- Create / Edit dialog -->
    <q-dialog v-model="showDialog" full-width>
      <q-card style="max-width: 720px; width: 100%">
        <q-card-section class="row items-center">
          <div class="text-h6">{{ editing ? 'Edit Request' : 'New Request' }}</div>
          <q-space />
          <q-btn flat round dense icon="close" v-close-popup />
        </q-card-section>

        <q-card-section class="q-gutter-md">
          <q-input v-model="form.name" label="Name" outlined :rules="[v => !!v.trim() || 'Required']" />

          <div class="row q-gutter-sm">
            <q-select
              v-model="form.method"
              :options="HTTP_METHODS"
              label="Method"
              outlined
              style="width: 120px"
            >
              <template #selected>
                <span :class="`text-${methodColor(form.method)}`">{{ form.method }}</span>
              </template>
            </q-select>
            <q-input
              :model-value="form.urlTemplate"
              label="URL"
              outlined
              class="col text-mono"
              placeholder="https://api.example.com/users"
              @update:model-value="onUrlInput"
            />
          </div>

          <!-- Query Params -->
          <div>
            <div class="row items-center q-mb-sm">
              <div class="text-subtitle2 col">Query Params</div>
              <div class="text-caption text-grey-5 text-mono">{{ previewQueryString }}</div>
            </div>
            <div v-for="(p, i) in params" :key="i" class="row q-gutter-sm q-mb-xs items-center">
              <q-input v-model="p.key" label="Key" outlined dense class="col text-mono" @update:model-value="rebuildUrl" />
              <q-input v-model="p.value" label="Value" outlined dense class="col text-mono" @update:model-value="rebuildUrl" />
              <q-btn flat round dense icon="remove" color="grey-6" @click="removeParam(i)" />
            </div>
            <q-btn flat dense icon="add" label="Add param" color="primary" @click="addParam" />
          </div>

          <!-- Headers -->
          <div>
            <div class="text-subtitle2 q-mb-sm">Headers</div>
            <div v-for="(_, key) in form.headers" :key="key" class="row q-gutter-sm q-mb-xs">
              <q-input :model-value="key" @update:model-value="renameHeader(key, $event as string)" label="Key" outlined dense class="col" />
              <q-input v-model="form.headers[key]" label="Value" outlined dense class="col" />
              <q-btn flat round dense icon="remove" color="grey-6" @click="removeHeader(key)" />
            </div>
            <q-btn flat dense icon="add" label="Add header" color="primary" @click="addHeader" />
          </div>

          <!-- Body -->
          <q-input
            v-model="form.body"
            label="Body"
            outlined
            type="textarea"
            rows="6"
            class="text-mono"
            placeholder='{"key": "value"}'
          />
        </q-card-section>

        <q-card-actions align="right">
          <q-btn flat label="Cancel" v-close-popup />
          <q-btn :label="editing ? 'Save' : 'Create'" color="primary" unelevated :loading="saving" @click="onSave" />
        </q-card-actions>
      </q-card>
    </q-dialog>
  </q-page>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useQuasar } from 'quasar'
import { useRequestsStore } from 'stores/requests'
import { useCollectionsStore } from 'stores/collections'
import { useWorkspacesStore } from 'stores/workspaces'
import { useProxyStore } from 'stores/proxy'
import RequestRunner from 'src/components/RequestRunner.vue'
import type { RequestResponse } from 'src/api/requests'

const route = useRoute()
const $q = useQuasar()
const store = useRequestsStore()
const collectionsStore = useCollectionsStore()
const workspacesStore = useWorkspacesStore()
const proxy = useProxyStore()

const collectionId = route.params.id as string
const collection = computed(() => collectionsStore.collections.find(c => c.id === collectionId))
const workspaceName = computed(() => {
  const wsId = collection.value?.workspaceId
  return workspacesStore.workspaces.find(w => w.id === wsId)?.name ?? 'Workspace'
})

const HTTP_METHODS = ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'HEAD', 'OPTIONS']
const splitterSize = ref(28)
const selectedRequest = ref<RequestResponse | null>(null)

// Context menu
const menuOpen = ref(false)
const menuTarget = ref<Element | null>(null)
const menuRequest = ref<RequestResponse | null>(null)

function openMenu(evt: MouseEvent, req: RequestResponse) {
  menuTarget.value = evt.currentTarget as Element
  menuRequest.value = req
  menuOpen.value = true
}

function selectRequest(req: RequestResponse) {
  selectedRequest.value = req
}

async function onSend(req: RequestResponse) {
  await proxy.send(req)
}

// --- Dialog ---
const showDialog = ref(false)
const saving = ref(false)
const editing = ref<RequestResponse | null>(null)

const emptyForm = () => ({ name: '', method: 'GET', urlTemplate: '', headers: {} as Record<string, string>, body: '' })
const form = reactive(emptyForm())
const params = ref<{ key: string; value: string }[]>([])

const previewQueryString = computed(() => {
  const active = params.value.filter(p => p.key.trim())
  if (!active.length) return ''
  return '?' + active.map(p => `${encodeURIComponent(p.key)}=${encodeURIComponent(p.value)}`).join('&')
})

let _syncing = false

function onUrlInput(val: string) {
  if (_syncing) return
  _syncing = true
  form.urlTemplate = val as string
  const qIdx = val.indexOf('?')
  params.value = qIdx === -1 ? [] : val.slice(qIdx + 1).split('&').filter(Boolean).map(seg => {
    const eq = seg.indexOf('=')
    return { key: decodeURIComponent(eq === -1 ? seg : seg.slice(0, eq)), value: decodeURIComponent(eq === -1 ? '' : seg.slice(eq + 1)) }
  })
  _syncing = false
}

function rebuildUrl() {
  if (_syncing) return
  _syncing = true
  form.urlTemplate = form.urlTemplate.split('?')[0] + previewQueryString.value
  _syncing = false
}

function addParam() { params.value.push({ key: '', value: '' }) }
function removeParam(i: number) { params.value.splice(i, 1); rebuildUrl() }
function addHeader() { form.headers[''] = '' }
function removeHeader(key: string) { delete form.headers[key] }
function renameHeader(oldKey: string, newKey: string) {
  const val = form.headers[oldKey]; delete form.headers[oldKey]; form.headers[newKey] = val
}

onMounted(() => store.fetchForCollection(collectionId))

function methodColor(method: string) {
  const c: Record<string, string> = { GET: 'positive', POST: 'primary', PUT: 'orange', PATCH: 'orange', DELETE: 'negative', HEAD: 'grey', OPTIONS: 'grey' }
  return c[method] ?? 'grey'
}

function openCreate() {
  editing.value = null
  Object.assign(form, emptyForm())
  params.value = []
  showDialog.value = true
}

function openEdit(req: RequestResponse) {
  editing.value = req
  form.name = req.name
  form.method = req.method
  form.headers = { ...req.headers }
  form.body = req.body ?? ''
  onUrlInput(req.urlTemplate)
  showDialog.value = true
}

async function onSave() {
  if (!form.name.trim()) return
  saving.value = true
  const payload = { name: form.name.trim(), method: form.method, urlTemplate: form.urlTemplate, headers: form.headers, body: form.body || null }
  try {
    if (editing.value) {
      const updated = await store.update(editing.value.id, payload)
      if (selectedRequest.value?.id === editing.value.id) selectedRequest.value = updated
      $q.notify({ type: 'positive', message: 'Request saved' })
    } else {
      await store.create(collectionId, payload)
      $q.notify({ type: 'positive', message: 'Request created' })
    }
    showDialog.value = false
  } catch (e) {
    $q.notify({ type: 'negative', message: e instanceof Error ? e.message : 'Failed' })
  } finally {
    saving.value = false
  }
}

function confirmDelete(req: RequestResponse) {
  $q.dialog({
    title: 'Delete request',
    message: `Delete "${req.name}"?`,
    cancel: true,
    ok: { label: 'Delete', color: 'negative', flat: true },
  }).onOk(async () => {
    try {
      await store.delete(req.id)
      if (selectedRequest.value?.id === req.id) { selectedRequest.value = null; proxy.clear() }
      $q.notify({ type: 'positive', message: 'Request deleted' })
    } catch (e) {
      $q.notify({ type: 'negative', message: e instanceof Error ? e.message : 'Failed' })
    }
  })
}
</script>

<style scoped>
.collection-page { display: flex; flex-direction: column; height: calc(100vh - 50px); }
.page-header { flex-shrink: 0; background: white; }
.collection-splitter { flex: 1; overflow: hidden; }
.method-badge { font-size: 0.65rem; padding: 2px 6px; min-width: 52px; text-align: center; }
.text-mono { font-family: monospace; }
</style>
