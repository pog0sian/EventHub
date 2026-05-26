<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'
import { CalendarCheck, Search } from 'lucide-vue-next'

import { cancelEventRegistration, getMyEvents } from '@/entities/event/api'
import type { EventRegistrationResponse, EventRegistrationStatus } from '@/entities/event/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
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
const pendingEventId = ref<number | null>(null)
const search = ref('')
const activeTab = ref<'ALL' | EventRegistrationStatus>('ALL')
const registrations = ref<EventRegistrationResponse[]>([])
const cancelDialog = reactive<{
  open: boolean
  registration: EventRegistrationResponse | null
}>({
  open: false,
  registration: null,
})

const statusLabels: Record<EventRegistrationStatus, string> = {
  REGISTERED: 'Записан',
  CANCELLED: 'Отменено',
}

const statusVariants: Record<EventRegistrationStatus, 'default' | 'secondary' | 'outline' | 'destructive'> = {
  REGISTERED: 'default',
  CANCELLED: 'secondary',
}

const filteredRegistrations = computed(() => {
  const query = search.value.trim().toLowerCase()

  return registrations.value.filter((registration) => {
    const matchesTab = activeTab.value === 'ALL' || registration.status === activeTab.value
    const matchesSearch = !query || (
        registration.eventTitle.toLowerCase().includes(query)
        || registration.userEmail.toLowerCase().includes(query)
    )

    return matchesTab && matchesSearch
  })
})

async function loadRegistrations(): Promise<void> {
  isLoading.value = true

  try {
    registrations.value = await getMyEvents()
  } catch (error) {
    toast.error('Не удалось загрузить мои события', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

function openCancelDialog(registration: EventRegistrationResponse): void {
  cancelDialog.registration = registration
  cancelDialog.open = true
}

async function cancelRegistration(): Promise<void> {
  const registration = cancelDialog.registration

  if (!registration) {
    return
  }

  pendingEventId.value = registration.eventId

  try {
    await cancelEventRegistration(registration.eventId)
    toast.success('Запись отменена')
    cancelDialog.open = false
    cancelDialog.registration = null
    await loadRegistrations()
  } catch (error) {
    toast.error('Не удалось отменить запись', {
      description: getApiErrorMessage(error),
    })
  } finally {
    pendingEventId.value = null
  }
}

onMounted(loadRegistrations)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 class="text-2xl font-semibold">
            Мои события
          </h1>
          <p class="text-muted-foreground">
            Активные и отмененные регистрации.
          </p>
        </div>

        <div class="relative w-full lg:max-w-sm">
          <Search class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input v-model="search" class="pl-9" placeholder="Поиск по названию" />
        </div>
      </div>

      <Tabs v-model="activeTab">
        <TabsList>
          <TabsTrigger value="ALL">
            Все
          </TabsTrigger>
          <TabsTrigger value="REGISTERED">
            Активные
          </TabsTrigger>
          <TabsTrigger value="CANCELLED">
            Отмененные
          </TabsTrigger>
        </TabsList>
      </Tabs>

      <div v-if="isLoading" class="space-y-3">
        <Skeleton v-for="index in 5" :key="index" class="h-24 rounded-lg" />
      </div>

      <div v-else-if="filteredRegistrations.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
        Здесь пока нет записей.
      </div>

      <div v-else class="space-y-3">
        <Card v-for="registration in filteredRegistrations" :key="registration.id" class="rounded-lg">
          <CardContent class="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between">
            <div class="min-w-0 space-y-2">
              <div class="flex flex-wrap items-center gap-2">
                <CalendarCheck class="size-4 text-muted-foreground" />
                <h2 class="font-medium">
                  {{ registration.eventTitle }}
                </h2>
                <Badge :variant="statusVariants[registration.status]">
                  {{ statusLabels[registration.status] }}
                </Badge>
              </div>

              <p class="text-sm text-muted-foreground">
                Запись создана: {{ formatDateTime(registration.createdAt) }}
              </p>
            </div>

            <Button
                v-if="registration.status === 'REGISTERED'"
                :disabled="pendingEventId === registration.eventId"
                variant="outline"
                @click="openCancelDialog(registration)"
            >
              {{ pendingEventId === registration.eventId ? 'Отмена...' : 'Отменить запись' }}
            </Button>
          </CardContent>
        </Card>
      </div>

      <AlertDialog v-model:open="cancelDialog.open">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Отменить запись?</AlertDialogTitle>
            <AlertDialogDescription>
              Запись на мероприятие {{ cancelDialog.registration?.eventTitle }} будет отменена.
              Если количество мест ограничено, место станет доступно другим студентам.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>
              Не отменять
            </AlertDialogCancel>
            <AlertDialogAction
                :disabled="cancelDialog.registration ? pendingEventId === cancelDialog.registration.eventId : false"
                @click="cancelRegistration"
            >
              Отменить запись
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

    </section>
  </AppLayout>
</template>