<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'
import { CalendarDays, Gift, Medal, WalletCards } from 'lucide-vue-next'

import { getMyEvents, getStudentEvents, registerForEvent } from '@/entities/event/api'
import type { EventRegistrationResponse, EventResponse } from '@/entities/event/types'
import { getPointBalance } from '@/entities/points/api'
import { getMyRewardPurchases, getStudentRewards } from '@/entities/reward/api'
import type { RewardPurchaseResponse, RewardResponse } from '@/entities/reward/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'

const isLoading = ref(true)
const isRegistering = ref<number | null>(null)
const balance = ref(0)
const events = ref<EventResponse[]>([])
const myEvents = ref<EventRegistrationResponse[]>([])
const rewards = ref<RewardResponse[]>([])
const purchases = ref<RewardPurchaseResponse[]>([])

const registeredEventIds = computed(() => new Set(
    myEvents.value
        .filter((registration) => registration.status === 'REGISTERED')
        .map((registration) => registration.eventId),
))

const nearestEvents = computed(() => events.value.slice(0, 3))
const availableRewards = computed(() => rewards.value.filter((reward) => reward.active && reward.stock > 0).slice(0, 3))

async function loadDashboard(): Promise<void> {
  isLoading.value = true

  try {
    const [
      balanceResponse,
      eventsResponse,
      myEventsResponse,
      rewardsResponse,
      purchasesResponse,
    ] = await Promise.all([
      getPointBalance(),
      getStudentEvents(),
      getMyEvents(),
      getStudentRewards(),
      getMyRewardPurchases(),
    ])

    balance.value = balanceResponse.balance
    events.value = eventsResponse
    myEvents.value = myEventsResponse
    rewards.value = rewardsResponse
    purchases.value = purchasesResponse
  } catch (error) {
    toast.error('Не удалось загрузить кабинет', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

async function register(eventId: number): Promise<void> {
  isRegistering.value = eventId

  try {
    await registerForEvent(eventId)
    toast.success('Вы записались на мероприятие')
    await loadDashboard()
  } catch (error) {
    toast.error('Не удалось записаться', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isRegistering.value = null
  }
}

onMounted(loadDashboard)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div>
        <h1 class="text-2xl font-semibold">
          Кабинет студента
        </h1>
        <p class="text-muted-foreground">
          Мероприятия, баллы и награды в одном месте.
        </p>
      </div>

      <div v-if="isLoading" class="grid gap-4 md:grid-cols-4">
        <Skeleton v-for="index in 4" :key="index" class="h-28 rounded-lg" />
      </div>

      <div v-else class="grid gap-4 md:grid-cols-4">
        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <WalletCards class="size-4" />
              Баланс
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ balance }}
            </div>
            <p class="text-xs text-muted-foreground">
              баллов доступно
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
              опубликовано
            </p>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <Medal class="size-4" />
              Мои записи
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ myEvents.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              регистраций
            </p>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader class="space-y-0 pb-2">
            <CardTitle class="flex items-center gap-2 text-sm font-medium">
              <Gift class="size-4" />
              Покупки
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div class="text-2xl font-semibold">
              {{ purchases.length }}
            </div>
            <p class="text-xs text-muted-foreground">
              заявок на награды
            </p>
          </CardContent>
        </Card>
      </div>

      <div class="grid gap-4 lg:grid-cols-[1.3fr_0.7fr]">
        <Card class="rounded-lg">
          <CardHeader>
            <CardTitle>Ближайшие мероприятия</CardTitle>
          </CardHeader>
          <CardContent>
            <div v-if="isLoading" class="space-y-3">
              <Skeleton v-for="index in 3" :key="index" class="h-20 rounded-md" />
            </div>

            <div v-else-if="nearestEvents.length === 0" class="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
              Пока нет опубликованных мероприятий.
            </div>

            <div v-else class="space-y-3">
              <div
                  v-for="event in nearestEvents"
                  :key="event.id"
                  class="flex flex-col gap-4 rounded-md border p-4 sm:flex-row sm:items-center sm:justify-between"
              >
                <div class="min-w-0 space-y-1">
                  <div class="flex flex-wrap items-center gap-2">
                    <h2 class="font-medium">
                      {{ event.title }}
                    </h2>
                    <Badge variant="secondary">
                      {{ event.pointsReward }} баллов
                    </Badge>
                  </div>
                  <p class="text-sm text-muted-foreground">
                    {{ event.organizationName }} · {{ formatDateTime(event.startsAt) }}
                  </p>
                  <p v-if="event.location" class="text-sm text-muted-foreground">
                    {{ event.location }}
                  </p>
                </div>

                <Button
                    :disabled="registeredEventIds.has(event.id) || isRegistering === event.id"
                    @click="register(event.id)"
                >
                  <span v-if="registeredEventIds.has(event.id)">Вы записаны</span>
                  <span v-else-if="isRegistering === event.id">Запись...</span>
                  <span v-else>Записаться</span>
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card class="rounded-lg">
          <CardHeader>
            <CardTitle>Доступные награды</CardTitle>
          </CardHeader>
          <CardContent>
            <div v-if="isLoading" class="space-y-3">
              <Skeleton v-for="index in 3" :key="index" class="h-16 rounded-md" />
            </div>

            <div v-else-if="availableRewards.length === 0" class="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
              Пока нет доступных наград.
            </div>

            <div v-else class="space-y-3">
              <div v-for="reward in availableRewards" :key="reward.id" class="rounded-md border p-4">
                <div class="flex items-start justify-between gap-3">
                  <div class="min-w-0">
                    <h2 class="font-medium">
                      {{ reward.title }}
                    </h2>
                    <p class="text-sm text-muted-foreground">
                      Остаток: {{ reward.stock }}
                    </p>
                  </div>
                  <Badge>
                    {{ reward.cost }}
                  </Badge>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </section>
  </AppLayout>
</template>
