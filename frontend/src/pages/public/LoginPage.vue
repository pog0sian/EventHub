<script setup lang="ts">
import { computed, reactive } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { useAuthStore } from '@/entities/auth/model/auth.store'
import type { RoleName } from '@/entities/auth/types'
import { getApiErrorMessage } from '@/shared/api/client'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const form = reactive({
  email: '',
  password: '',
})

const canSubmit = computed(() => form.email.trim().length > 0 && form.password.length >= 8)

const homeByRole: Record<RoleName, string> = {
  STUDENT: '/student',
  ORG_MANAGER: '/manager',
  ADMIN: '/admin',
}

async function onSubmit(): Promise<void> {
  if (!canSubmit.value || authStore.isLoading) {
    return
  }

  try {
    await authStore.loginWithEmail({
      email: form.email.trim(),
      password: form.password,
    })

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : null

    if (redirect) {
      await router.push(redirect)
      return
    }

    if (authStore.hasMultipleRoles) {
      await router.push('/select-role')
      return
    }

    if (authStore.activeRole) {
      await router.push(homeByRole[authStore.activeRole])
      return
    }

    await router.push('/select-role')
  } catch (error) {
    const message = getApiErrorMessage(error)

    toast.error(message === 'User account is disabled' ? 'Аккаунт отключен' : 'Не удалось войти', {
      description: message === 'User account is disabled'
          ? 'Обратитесь к администратору ИвентХаб, чтобы восстановить доступ.'
          : message,
    })
  }
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-muted/30 p-6">
    <Card class="w-full max-w-md rounded-lg">
      <CardHeader>
        <CardTitle class="text-2xl">
          Вход
        </CardTitle>
        <CardDescription>
          Введите email и пароль от аккаунта ИвентХаб.
        </CardDescription>
      </CardHeader>

      <CardContent>
        <form class="space-y-5" @submit.prevent="onSubmit">
          <div class="space-y-2">
            <Label for="email">Email</Label>
            <Input
                id="email"
                v-model="form.email"
                autocomplete="email"
                placeholder="student@example.com"
                type="email"
            />
          </div>

          <div class="space-y-2">
            <Label for="password">Пароль</Label>
            <Input
                id="password"
                v-model="form.password"
                autocomplete="current-password"
                placeholder="Минимум 8 символов"
                type="password"
            />
          </div>

          <Button class="w-full" :disabled="!canSubmit || authStore.isLoading" type="submit">
            {{ authStore.isLoading ? 'Входим...' : 'Войти' }}
          </Button>

          <p class="text-center text-sm text-muted-foreground">
            Нет аккаунта?
            <RouterLink class="font-medium text-primary underline-offset-4 hover:underline" to="/register">
              Зарегистрироваться
            </RouterLink>
          </p>
        </form>
      </CardContent>
    </Card>
  </main>
</template>
