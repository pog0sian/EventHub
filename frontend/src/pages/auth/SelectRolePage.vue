<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { GraduationCap, ShieldCheck, UsersRound } from 'lucide-vue-next'

import { useAuthStore } from '@/entities/auth/model/auth.store'
import type { RoleName } from '@/entities/auth/types'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const roleCards: Record<RoleName, {
  title: string
  description: string
  path: string
  icon: typeof GraduationCap
}> = {
  STUDENT: {
    title: 'Студент',
    description: 'Мероприятия, баллы и награды.',
    path: '/student',
    icon: GraduationCap,
  },
  ORG_MANAGER: {
    title: 'Менеджер организации',
    description: 'События, регистрации и посещаемость.',
    path: '/manager',
    icon: UsersRound,
  },
  ADMIN: {
    title: 'Администратор',
    description: 'Организации, пользователи и заявки.',
    path: '/admin',
    icon: ShieldCheck,
  },
}

const availableRoles = computed(() => authStore.roles.map((role) => ({
  role,
  ...roleCards[role],
})))

function selectRole(role: RoleName): void {
  authStore.setActiveRole(role)

  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : roleCards[role].path
  router.push(redirect)
}
</script>

<template>
  <main class="flex min-h-screen items-center justify-center bg-muted/30 p-6">
    <div class="w-full max-w-4xl space-y-6">
      <div class="space-y-2 text-center">
        <h1 class="text-3xl font-semibold">
          Выберите роль
        </h1>
        <p class="text-muted-foreground">
          Рабочий контекст будет отправляться в API как X-Active-Role.
        </p>
      </div>

      <div class="grid gap-4 md:grid-cols-3">
        <Card v-for="item in availableRoles" :key="item.role" class="rounded-lg">
          <CardHeader>
            <component :is="item.icon" class="mb-2 size-6 text-primary" />
            <CardTitle class="text-lg">
              {{ item.title }}
            </CardTitle>
            <CardDescription>
              {{ item.description }}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <Button class="w-full" @click="selectRole(item.role)">
              Продолжить
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  </main>
</template>
