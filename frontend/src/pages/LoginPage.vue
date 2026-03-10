<template>
  <div class="flex flex-center bg-grey-2" style="min-height: 100vh">
    <q-card style="width: 380px" class="q-pa-md">
      <q-card-section>
        <div class="text-h5 text-weight-bold">Sign in to TICman</div>
      </q-card-section>

      <q-card-section class="q-pt-none">
        <q-form @submit="onSubmit" class="q-gutter-md">
          <q-input
            v-model="email"
            label="Email"
            type="email"
            outlined
            :rules="[val => !!val || 'Required']"
          />
          <q-input
            v-model="password"
            label="Password"
            :type="showPassword ? 'text' : 'password'"
            outlined
            :rules="[val => !!val || 'Required']"
          >
            <template #append>
              <q-icon
                :name="showPassword ? 'visibility_off' : 'visibility'"
                class="cursor-pointer"
                @click="showPassword = !showPassword"
              />
            </template>
          </q-input>

          <q-banner v-if="error" class="bg-red-1 text-red-9 rounded-borders">
            {{ error }}
          </q-banner>

          <q-btn
            type="submit"
            label="Sign in"
            color="primary"
            class="full-width"
            :loading="loading"
            unelevated
          />
        </q-form>
      </q-card-section>

      <q-card-section class="text-center q-pt-none">
        <span class="text-grey-7">No account? </span>
        <router-link to="/register" class="text-primary">Register</router-link>
      </q-card-section>
    </q-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from 'stores/auth'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

async function onSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    await router.push('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>
