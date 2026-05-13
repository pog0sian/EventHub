<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'
import { Gift, ShieldCheck, Store, UsersRound } from 'lucide-vue-next'

import { getAdminOrganizations } from '@/entities/organization/api'
import type { OrganizationResponse } from '@/entities/organization/types'
import { getAdminRewardPurchases, getAdminRewards } from '@/entities/reward/api'
import type { RewardPurchaseResponse, RewardResponse } from '@/entities/reward/types'
import { getAdminUsers } from '@/entities/user/api'
import type { UserResponse } from '@/entities/auth/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

const isLoading = ref(true)
const organizations = ref<OrganizationResponse[]>([])
const users = ref<UserResponse[]>([])
const rewards = ref<RewardResponse[]>([])
const purchases = ref<RewardPurchaseResponse[]>([])

const activeOrganizations = computed(() => organizations.value.filter((organization) => organization.active))
const enabledUsers = computed(() => users.value.filter((user) => user.enabled))
const requestedPurchases = computed(() => purchases.value.filter((purchase) => purchase.status === 'REQUESTED'))

async function loadDashboard(): Promise<void> {
  isLoading.value = true

  try {
    const [
      organizationsResponse,
      usersResponse,
      rewardsResponse,
      purchasesResponse,
    ] = await Promise.all([
      getAdminOrganizations(),
      getAdminUsers(),
      getAdminRewards(),
      getAdminRewardPurchases(),
    ])

    organizations.value = organizationsResponse
    users.value = usersResponse
    rewards.value = rewardsResponse
    purchases.value = purchasesResponse
  } catch (error) {
    toast.error('Не удалось загрузить кабинет администратора', {
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
          Кабинет администратора
        </h1>
        <p class="text-muted-foreground">
          Управление EventHub: организации, пользователи и награды.
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
              {{ organizations.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              активных: {{ activeOrganizations.length }}
            </p>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <UsersRound class="size-4" />
              Пользователи
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ users.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              активных: {{ enabledUsers.length }}
            </p>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <Gift class="size-4" />
              Награды
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ rewards.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              в каталоге
            </p>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <ShieldCheck class="size-4" />
              Заявки
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ requestedPurchases.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              ожидают выдачи
            </p>
          </CardContent>
        </Card>
      </div>

      <Card class="rounded-lg">
        <CardHeader>
          <CardTitle>Новые заявки на награды</CardTitle>
        </CardHeader>
        <CardContent>
          <div v-if="isLoading" class="space-y-3">
            <Skeleton v-for="index in 5" :key="index" class="h-20 rounded-md" />
          </div>

          <div v-else-if="requestedPurchases.length === 0" class="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
            Сейчас нет заявок, ожидающих выдачи.
          </div>

          <div v-else class="space-y-3">
            <div
                v-for="purchase in requestedPurchases.slice(0, 5)"
                :key="purchase.id"
                class="flex flex-col gap-3 rounded-md border p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div class="min-w-0 space-y-1">
                <h2 class="font-medium">
                  {{ purchase.rewardTitle }}
                </h2>
                <p class="text-sm text-muted-foreground">
                  Пользователь #{{ purchase.userId }} · {{ formatDateTime(purchase.createdAt) }}
                </p>
              </div>

              <Badge>
                {{ purchase.cost }} баллов
              </Badge>
            </div>
          </div>
        </CardContent>
      </Card>
    </section>
  </AppLayout>
</template>
