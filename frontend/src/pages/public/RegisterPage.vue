<script setup lang="ts">
import { computed, reactive } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { toast } from 'vue-sonner'

import { useAuthStore } from '@/entities/auth/model/auth.store'
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

const form = reactive({
  email: '',
  lastName: '',
  firstName: '',
  patronymic: '',
  password: '',
})

const canSubmit = computed(() => (
    form.email.trim().length > 0
    && form.lastName.trim().length >= 2
    && form.firstName.trim().length >= 2
    && form.password.length >= 8
))

async function onSubmit(): Promise<void> {
  if (!canSubmit.value || authStore.isLoading) {
    return
  }

  try {
    await authStore.registerStudent({
      email: form.email.trim(),
      password: form.password,
      firstName: form.firstName.trim(),
      lastName: form.lastName.trim(),
      patronymic: form.patronymic.trim() || null,
    })

    toast.success('Аккаунт создан')
    await router.push('/student')
  } catch (error) {
    toast.error('Не удалось зарегистрироваться', {
      description: getApiErrorMessage(error),
    })
  }
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-muted/30 p-6">
    <Card class="w-full max-w-md rounded-lg">
      <CardHeader>
        <CardTitle class="text-2xl">
          Регистрация
        </CardTitle>
        <CardDescription>
          Новый пользователь получает роль студента.
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

          <div class="grid gap-4 sm:grid-cols-2">
            <div class="space-y-2">
              <Label for="lastName">Фамилия</Label>
              <Input id="lastName" v-model="form.lastName" autocomplete="family-name" placeholder="Иванов" />
            </div>

            <div class="space-y-2">
              <Label for="firstName">Имя</Label>
              <Input id="firstName" v-model="form.firstName" autocomplete="given-name" placeholder="Иван" />
            </div>
          </div>

          <div class="space-y-2">
            <Label for="patronymic">Отчество</Label>
            <Input id="patronymic" v-model="form.patronymic" autocomplete="additional-name" placeholder="Иванович" />
          </div>

          <div class="space-y-2">
            <Label for="password">Пароль</Label>
            <Input
                id="password"
                v-model="form.password"
                autocomplete="new-password"
                placeholder="Минимум 8 символов"
                type="password"
            />
          </div>

          <Button class="w-full" :disabled="!canSubmit || authStore.isLoading" type="submit">
            {{ authStore.isLoading ? 'Создаем...' : 'Зарегистрироваться' }}
          </Button>

          <p class="text-center text-sm text-muted-foreground">
            Уже есть аккаунт?
            <RouterLink class="font-medium text-primary underline-offset-4 hover:underline" to="/login">
              Войти
            </RouterLink>
          </p>
        </form>
      </CardContent>
    </Card>
  </main>
</template>