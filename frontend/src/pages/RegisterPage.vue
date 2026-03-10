<template>
  <div class="flex flex-center bg-grey-2" style="min-height: 100vh">
    <q-card style="width: 380px" class="q-pa-md">
      <q-card-section>
        <div class="text-h5 text-weight-bold">Create an account</div>
      </q-card-section>

      <q-card-section class="q-pt-none">
        <q-form @submit="onSubmit" class="q-gutter-md">
          <q-input
            v-model="displayName"
            label="Display name"
            outlined
            :rules="[val => !!val || 'Required']"
          />
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
            :rules="[val => val.length >= 6 || 'Min 6 characters']"
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
            label="Create account"
            color="primary"
            class="full-width"
            :loading="loading"
            unelevated
          />
        </q-form>
      </q-card-section>

      <q-card-section class="text-center q-pt-none">
        <span class="text-grey-7">Already have an account? </span>
        <router-link to="/login" class="text-primary">Sign in</router-link>
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

const displayName = ref('')
const email = ref('')
const password = ref('')
const showPassword = ref(false)
const loading = ref(false)
const error = ref('')

async function onSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.register(email.value, displayName.value, password.value)
    await router.push('/')
  } catch (e) {
    error.value = e instanceof Error ? e.message : 'Registration failed'
  } finally {
    loading.value = false
  }
}
</script>
