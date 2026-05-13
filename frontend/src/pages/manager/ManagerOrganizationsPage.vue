<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'
import { Mail, Search, Store } from 'lucide-vue-next'

import { getMyManagerOrganizations } from '@/entities/organization/api'
import type { OrganizationResponse } from '@/entities/organization/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'

const isLoading = ref(true)
const search = ref('')
const organizations = ref<OrganizationResponse[]>([])

const filteredOrganizations = computed(() => {
  const query = search.value.trim().toLowerCase()

  if (!query) {
    return organizations.value
  }

  return organizations.value.filter((organization) => (
      organization.name.toLowerCase().includes(query)
      || organization.description?.toLowerCase().includes(query)
      || organization.contactEmail?.toLowerCase().includes(query)
  ))
})

async function loadOrganizations(): Promise<void> {
  isLoading.value = true

  try {
    organizations.value = await getMyManagerOrganizations()
  } catch (error) {
    toast.error('Не удалось загрузить организации', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

onMounted(loadOrganizations)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 class="text-2xl font-semibold">
            Организации
          </h1>
          <p class="text-muted-foreground">
            Организации, в которых вы назначены менеджером.
          </p>
        </div>

        <div class="relative w-full lg:max-w-sm">
          <Search class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input v-model="search" class="pl-9" placeholder="Поиск организации" />
        </div>
      </div>

      <div v-if="isLoading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Skeleton v-for="index in 6" :key="index" class="h-48 rounded-lg" />
      </div>

      <div v-else-if="filteredOrganizations.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
        Организации не найдены.
      </div>

      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Card v-for="organization in filteredOrganizations" :key="organization.id" class="rounded-lg">
          <CardHeader>
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <Store class="mb-2 size-5 text-primary" />
                <CardTitle class="text-lg">
                  {{ organization.name }}
                </CardTitle>
              </div>

              <Badge :variant="organization.active ? 'default' : 'secondary'">
                {{ organization.active ? 'Активна' : 'Неактивна' }}
              </Badge>
            </div>
          </CardHeader>

          <CardContent class="space-y-4">
            <p class="line-clamp-3 text-sm leading-6 text-muted-foreground">
              {{ organization.description || 'Описание организации пока не добавлено.' }}
            </p>

            <div class="space-y-2 text-sm text-muted-foreground">
              <div v-if="organization.contactEmail" class="flex items-center gap-2">
                <Mail class="size-4" />
                <span class="truncate">{{ organization.contactEmail }}</span>
              </div>

              <p>
                Добавлена: {{ formatDateTime(organization.createdAt) }}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    </section>
  </AppLayout>
</template>