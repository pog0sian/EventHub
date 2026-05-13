<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'
import { CalendarDays, ClipboardCheck, Store } from 'lucide-vue-next'

import { getManagerEventsByOrganization } from '@/entities/event/api'
import type { EventResponse } from '@/entities/event/types'
import { getMyManagerOrganizations } from '@/entities/organization/api'
import type { OrganizationResponse } from '@/entities/organization/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

const isLoading = ref(true)
const organizations = ref<OrganizationResponse[]>([])
const events = ref<EventResponse[]>([])

const activeOrganizations = computed(() => organizations.value.filter((organization) => organization.active))
const publishedEvents = computed(() => events.value.filter((event) => event.status === 'PUBLISHED'))
const draftEvents = computed(() => events.value.filter((event) => event.status === 'DRAFT'))
const visibleEvents = computed(() => events.value.slice(0, 5))

const statusLabels: Record<EventResponse['status'], string> = {
  DRAFT: 'Черновик',
  PUBLISHED: 'Опубликовано',
  CANCELLED: 'Отменено',
  COMPLETED: 'Завершено',
}

const statusVariants: Record<EventResponse['status'], 'default' | 'secondary' | 'outline' | 'destructive'> = {
  DRAFT: 'secondary',
  PUBLISHED: 'default',
  CANCELLED: 'destructive',
  COMPLETED: 'outline',
}

async function loadDashboard(): Promise<void> {
  isLoading.value = true

  try {
    organizations.value = await getMyManagerOrganizations()

    const eventLists = await Promise.all(
        activeOrganizations.value.map((organization) => getManagerEventsByOrganization(organization.id)),
    )

    events.value = eventLists.flat()
  } catch (error) {
    toast.error('Не удалось загрузить кабинет менеджера', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

onMounted(loadDashboard)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div>
        <h1 class="text-2xl font-semibold">
          Кабинет менеджера
        </h1>
        <p class="text-muted-foreground">
          Управление мероприятиями организации и посещаемостью.
        </p>
      </div>

      <div v-if="isLoading" class="grid gap-4 md:grid-cols-4">
        <Skeleton v-for="index in 4" :key="index" class="h-28 rounded-lg" />
      </div>

      <div v-else class="grid gap-4 md:grid-cols-4">
        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <Store class="size-4" />
              Организации
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ activeOrganizations.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              доступно менеджеру
            </p>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <CalendarDays class="size-4" />
              Мероприятия
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ events.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              всего
            </p>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <CalendarDays class="size-4" />
              Опубликовано
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ publishedEvents.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              видно студентам
            </p>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <ClipboardCheck class="size-4" />
              Черновики
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ draftEvents.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              ждут публикации
            </p>
          </CardContent>
        </Card>
      </div>

      <Card class="rounded-lg">
        <CardHeader>
          <CardTitle>Последние мероприятия</CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="isLoading" class="space-y-3">
            <Skeleton v-for="index in 5" :key="index" class="h-20 rounded-md" />
          </div>

          <div v-else-if="visibleEvents.length === 0" class="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
            У ваших организаций пока нет мероприятий.
          </div>

          <div v-else class="space-y-3">
            <div
                v-for="event in visibleEvents"
                :key="event.id"
                class="flex flex-col gap-3 rounded-md border p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div class="min-w-0 space-y-1">
                <div class="flex flex-wrap items-center gap-2">
                  <h2 class="font-medium">
                    {{ event.title }}
                  </h2>
                  <Badge :variant="statusVariants[event.status]">
                    {{ statusLabels[event.status] }}
                  </Badge>
                </div>
                <p class="text-sm text-muted-foreground">
                  {{ event.organizationName }} · {{ formatDateTime(event.startsAt) }}
                </p>
              </div>

              <Badge variant="secondary">
                {{ event.pointsReward }} баллов
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>
    </section>
  </AppLayout>
</template>