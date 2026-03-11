<template>
  <div class="request-runner column full-height">
    <!-- Empty state -->
    <div v-if="!request" class="flex flex-center full-height text-grey-5">
      <div class="text-center">
        <q-icon name="ads_click" size="4rem" class="q-mb-md" />
        <div class="text-subtitle1">Select a request to run it</div>
      </div>
    </div>

    <template v-else>
      <!-- Request bar -->
      <div class="request-bar row items-center q-pa-md q-gutter-sm">
        <q-badge :color="methodColor(request.method)" class="method-badge text-weight-bold">
          {{ request.method }}
        </q-badge>
        <div class="col text-mono text-body2 ellipsis text-grey-8">
          {{ request.urlTemplate || '(no URL)' }}
        </div>
        <q-btn
          color="primary"
          label="Send"
          icon="send"
          unelevated
          :loading="proxy.sending"
          :disable="!request.urlTemplate"
          @click="$emit('send', request)"
        />
        <q-btn flat round dense icon="edit" color="grey-6" @click="$emit('edit', request)">
          <q-tooltip>Edit request</q-tooltip>
        </q-btn>
      </div>

      <q-separator />

      <!-- Tabs: always visible when a request is selected -->
      <q-tabs v-model="tab" dense align="left" class="q-px-md" indicator-color="primary">
        <q-tab name="body" label="Body" />
        <q-tab name="headers" label="Response Headers" />
        <q-tab name="history" label="History" />
      </q-tabs>
      <q-separator />

      <q-tab-panels v-model="tab" class="col overflow-auto">
        <!-- Body panel -->
        <q-tab-panel name="body" class="q-pa-none">
          <q-banner v-if="proxy.error" class="bg-red-1 text-red-9 q-ma-md rounded-borders">
            <template #avatar><q-icon name="error" /></template>
            {{ proxy.error }}
          </q-banner>
          <template v-if="proxy.response">
            <div class="row items-center q-px-md q-py-sm q-gutter-sm">
              <q-badge :color="statusColor(proxy.response.statusCode)" class="text-weight-bold" style="font-size: 0.85rem">
                {{ proxy.response.statusCode }}
              </q-badge>
              <span class="text-grey-6 text-caption">{{ statusText(proxy.response.statusCode) }}</span>
              <q-space />
              <q-chip dense icon="timer" color="grey-3" text-color="grey-8">{{ proxy.response.durationMs }} ms</q-chip>
              <q-chip dense icon="data_usage" color="grey-3" text-color="grey-8">{{ bodySize(proxy.response.body) }}</q-chip>
            </div>
            <pre class="response-body">{{ prettyBody(proxy.response.body) }}</pre>
          </template>
          <div v-else-if="!proxy.sending && !proxy.error" class="flex flex-center text-grey-4 q-pa-xl">
            <div class="text-center">
              <q-icon name="north_east" size="3rem" />
              <div class="q-mt-sm">Hit Send to run the request</div>
            </div>
          </div>
        </q-tab-panel>

        <!-- Response Headers panel -->
        <q-tab-panel name="headers">
          <q-list v-if="proxy.response" dense>
            <q-item v-for="(val, key) in proxy.response.headers" :key="key" dense>
              <q-item-section>
                <q-item-label class="text-weight-medium">{{ key }}</q-item-label>
              </q-item-section>
              <q-item-section class="text-mono text-caption text-grey-7">{{ val }}</q-item-section>
            </q-item>
          </q-list>
          <div v-else class="text-center text-grey-4 q-pa-xl">No response yet</div>
        </q-tab-panel>

        <!-- History panel -->
        <q-tab-panel name="history" class="q-pa-none">
          <div v-if="proxy.loadingHistory" class="flex flex-center q-pa-lg">
            <q-spinner color="primary" />
          </div>
          <q-list v-else-if="proxy.history.length" dense separator>
            <q-item v-for="entry in proxy.history" :key="entry.id" clickable v-ripple @click="showHistoryEntry(entry)">
              <q-item-section avatar>
                <q-badge :color="statusColor(entry.statusCode)">{{ entry.statusCode }}</q-badge>
              </q-item-section>
              <q-item-section>
                <q-item-label class="text-caption text-grey-6">{{ formatDate(entry.executedAt) }}</q-item-label>
              </q-item-section>
              <q-item-section side class="row items-center no-wrap">
                <q-chip dense icon="timer" color="grey-2" text-color="grey-7" size="sm">{{ entry.durationMs }} ms</q-chip>
                <q-btn flat round dense icon="delete" color="grey-5" size="sm" @click.stop="confirmDeleteHistory(entry)" />
              </q-item-section>
            </q-item>
          </q-list>
          <div v-else class="text-center text-grey-5 q-pa-xl">No history yet</div>
        </q-tab-panel>
      </q-tab-panels>
    </template>

    <!-- History entry dialog -->
    <q-dialog v-model="historyDialog">
      <q-card style="min-width: 560px; max-width: 90vw">
        <q-card-section class="row items-center">
          <q-badge :color="statusColor(selectedEntry?.statusCode ?? 0)" class="q-mr-sm">
            {{ selectedEntry?.statusCode }}
          </q-badge>
          <span class="text-grey-6 text-caption">{{ formatDate(selectedEntry?.executedAt ?? '') }}</span>
          <q-space />
          <q-btn flat round dense icon="close" v-close-popup />
        </q-card-section>
        <q-card-section>
          <pre class="response-body" style="max-height: 400px; overflow: auto">{{ prettyBody(selectedEntry?.body ?? '') }}</pre>
        </q-card-section>
      </q-card>
    </q-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useQuasar } from 'quasar'
import { useProxyStore } from 'stores/proxy'
import type { RequestResponse } from 'src/api/requests'
import type { HistoryEntry } from 'src/api/proxy'

const props = defineProps<{ request: RequestResponse | null }>()
defineEmits<{ send: [req: RequestResponse]; edit: [req: RequestResponse] }>()

const proxy = useProxyStore()
const $q = useQuasar()
const tab = ref('body')
const historyDialog = ref(false)
const selectedEntry = ref<HistoryEntry | null>(null)

watch(() => props.request, (req) => {
  proxy.clear()
  tab.value = 'body'
  if (req) proxy.fetchHistory(req.id)
})

function methodColor(method: string) {
  const c: Record<string, string> = { GET: 'positive', POST: 'primary', PUT: 'orange', PATCH: 'orange', DELETE: 'negative' }
  return c[method] ?? 'grey'
}

function statusColor(code: number) {
  if (code < 300) return 'positive'
  if (code < 400) return 'orange'
  return 'negative'
}

function statusText(code: number) {
  const texts: Record<number, string> = {
    200: 'OK', 201: 'Created', 204: 'No Content', 301: 'Moved Permanently',
    400: 'Bad Request', 401: 'Unauthorized', 403: 'Forbidden', 404: 'Not Found',
    500: 'Internal Server Error', 502: 'Bad Gateway', 503: 'Service Unavailable',
  }
  return texts[code] ?? ''
}

function prettyBody(body: string | null | undefined) {
  if (!body) return '(empty)'
  try { return JSON.stringify(JSON.parse(body), null, 2) }
  catch { return body }
}

function bodySize(body: string) {
  const bytes = new TextEncoder().encode(body).length
  return bytes < 1024 ? `${bytes} B` : `${(bytes / 1024).toFixed(1)} KB`
}

function formatDate(ts: string) {
  if (!ts) return ''
  return new Date(ts).toLocaleString()
}

function showHistoryEntry(entry: HistoryEntry) {
  selectedEntry.value = entry
  historyDialog.value = true
}

function confirmDeleteHistory(entry: HistoryEntry) {
  $q.dialog({
    title: 'Delete history entry',
    message: `Delete this entry from ${formatDate(entry.executedAt)}?`,
    cancel: true,
    ok: { label: 'Delete', color: 'negative', flat: true },
  }).onOk(async () => {
    try {
      await proxy.deleteHistory(entry.id)
    } catch (e) {
      $q.notify({ type: 'negative', message: e instanceof Error ? e.message : 'Failed' })
    }
  })
}
</script>

<style scoped>
.request-runner { background: #fafafa; }
.request-bar { background: white; border-bottom: 1px solid #e0e0e0; }
.method-badge { font-size: 0.75rem; padding: 4px 8px; }
.response-body {
  margin: 0;
  padding: 16px;
  font-family: monospace;
  font-size: 0.8rem;
  white-space: pre-wrap;
  word-break: break-all;
  background: #f5f5f5;
  min-height: 200px;
}
.text-mono { font-family: monospace; }
</style>
