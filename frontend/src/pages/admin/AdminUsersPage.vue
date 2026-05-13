<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'
import { Search, UserX, UsersRound } from 'lucide-vue-next'

import type { UserResponse } from '@/entities/auth/types'
import { deactivateAdminUser, getAdminUsers } from '@/entities/user/api'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog'

const isLoading = ref(true)
const pendingUserId = ref<number | null>(null)
const search = ref('')
const users = ref<UserResponse[]>([])

const deactivateDialog = reactive<{
  open: boolean
  user: UserResponse | null
}>({
  open: false,
  user: null,
})

function openDeactivateDialog(user: UserResponse): void {
  deactivateDialog.user = user
  deactivateDialog.open = true
}

const filteredUsers = computed(() => {
  const query = search.value.trim().toLowerCase()

  if (!query) {
    return users.value
  }

  return users.value.filter((user) => (
      user.email.toLowerCase().includes(query)
      || user.firstName.toLowerCase().includes(query)
      || user.lastName.toLowerCase().includes(query)
      || user.patronymic?.toLowerCase().includes(query)
  ))
})

async function loadUsers(): Promise<void> {
  isLoading.value = true

  try {
    users.value = await getAdminUsers()
  } catch (error) {
    toast.error('Не удалось загрузить пользователей', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

async function deactivateUser(): Promise<void> {
  const user = deactivateDialog.user

  if (!user) {
    return
  }

  pendingUserId.value = user.id

  try {
    await deactivateAdminUser(user.id)
    toast.success('Пользователь деактивирован')
    deactivateDialog.open = false
    deactivateDialog.user = null
    await loadUsers()
  } catch (error) {
    toast.error('Не удалось деактивировать пользователя', {
      description: getApiErrorMessage(error),
    })
  } finally {
    pendingUserId.value = null
  }
}

onMounted(loadUsers)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 class="text-2xl font-semibold">
            Пользователи
          </h1>
          <p class="text-muted-foreground">
            Просмотр пользователей и отключение доступа.
          </p>
        </div>

        <div class="relative w-full lg:max-w-sm">
          <Search class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input v-model="search" class="pl-9" placeholder="Поиск пользователя" />
        </div>
      </div>

      <div v-if="isLoading" class="space-y-3">
        <Skeleton v-for="index in 8" :key="index" class="h-20 rounded-lg" />
      </div>

      <div v-else-if="filteredUsers.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
        Пользователи не найдены.
      </div>

      <div v-else class="space-y-3">
        <Card v-for="user in filteredUsers" :key="user.id" class="rounded-lg">
          <CardContent class="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between">
            <div class="flex min-w-0 gap-3">
              <div class="flex size-10 shrink-0 items-center justify-center rounded-md bg-muted">
                <UsersRound class="size-5" />
              </div>

              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <h2 class="font-medium">
                    {{ user.lastName }} {{ user.firstName }} {{ user.patronymic || '' }}
                  </h2>
                  <Badge :variant="user.enabled ? 'default' : 'secondary'">
                    {{ user.enabled ? 'Активен' : 'Отключен' }}
                  </Badge>
                </div>

                <p class="truncate text-sm text-muted-foreground">
                  {{ user.email }}
                </p>
                <p class="text-xs text-muted-foreground">
                  Зарегистрирован: {{ formatDateTime(user.createdAt) }}
                </p>
              </div>
            </div>

            <Button
                v-if="user.enabled"
                :disabled="pendingUserId === user.id"
                size="sm"
                variant="outline"
                @click="openDeactivateDialog(user)"
            >
              <UserX class="mr-2 size-4" />
              Отключить
            </Button>
          </CardContent>
        </Card>
      </div>

      <AlertDialog v-model:open="deactivateDialog.open">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Отключить пользователя?</AlertDialogTitle>
            <AlertDialogDescription>
              Пользователь {{ deactivateDialog.user?.email }} больше не сможет войти в EventHub.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>
              Отмена
            </AlertDialogCancel>
            <AlertDialogAction
                :disabled="deactivateDialog.user ? pendingUserId === deactivateDialog.user.id : false"
                @click="deactivateUser"
            >
              Отключить
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

    </section>
  </AppLayout>
</template>