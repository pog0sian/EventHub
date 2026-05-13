<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'
import { Gift, History, WalletCards } from 'lucide-vue-next'

import { getPointBalance } from '@/entities/points/api'
import { getMyRewardPurchases, getStudentRewards, purchaseReward } from '@/entities/reward/api'
import type { RewardPurchaseResponse, RewardPurchaseStatus, RewardResponse } from '@/entities/reward/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs'

const isLoading = ref(true)
const pendingRewardId = ref<number | null>(null)
const balance = ref(0)
const rewards = ref<RewardResponse[]>([])
const purchases = ref<RewardPurchaseResponse[]>([])

const activeRewards = computed(() => rewards.value.filter((reward) => reward.active))

const purchaseStatusLabels: Record<RewardPurchaseStatus, string> = {
  REQUESTED: 'Заявка',
  ISSUED: 'Выдано',
  CANCELLED: 'Отменено',
}

const purchaseStatusVariants: Record<RewardPurchaseStatus, 'default' | 'secondary' | 'outline' | 'destructive'> = {
  REQUESTED: 'default',
  ISSUED: 'outline',
  CANCELLED: 'secondary',
}

async function loadRewards(): Promise<void> {
  isLoading.value = true

  try {
    const [balanceResponse, rewardsResponse, purchasesResponse] = await Promise.all([
      getPointBalance(),
      getStudentRewards(),
      getMyRewardPurchases(),
    ])

    balance.value = balanceResponse.balance
    rewards.value = rewardsResponse
    purchases.value = purchasesResponse
  } catch (error) {
    toast.error('Не удалось загрузить награды', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

function canBuy(reward: RewardResponse): boolean {
  return reward.active && reward.stock > 0 && balance.value >= reward.cost
}

async function buyReward(reward: RewardResponse): Promise<void> {
  pendingRewardId.value = reward.id

  try {
    await purchaseReward(reward.id)
    toast.success('Заявка на награду создана')
    await loadRewards()
  } catch (error) {
    toast.error('Не удалось купить награду', {
      description: getApiErrorMessage(error),
    })
  } finally {
    pendingRewardId.value = null
  }
}

onMounted(loadRewards)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 class="text-2xl font-semibold">
            Награды
          </h1>
          <p class="text-muted-foreground">
            Обменивайте баллы на доступные университетские награды.
          </p>
        </div>

        <Card class="rounded-lg lg:min-w-64">
          <CardContent class="flex items-center gap-3 p-4">
            <div class="flex size-10 items-center justify-center rounded-md bg-muted">
              <WalletCards class="size-5" />
            </div>
            <div>
              <p class="text-sm text-muted-foreground">
                Баланс
              </p>
              <p class="text-2xl font-semibold">
                {{ balance }}
              </p>
            </div>
          </CardContent>
        </Card>
      </div>

      <Tabs default-value="catalog" class="space-y-4">
        <TabsList>
          <TabsTrigger value="catalog">
            Каталог
          </TabsTrigger>
          <TabsTrigger value="purchases">
            Мои заявки
          </TabsTrigger>
        </TabsList>

        <TabsContent value="catalog">
          <div v-if="isLoading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <Skeleton v-for="index in 6" :key="index" class="h-52 rounded-lg" />
          </div>

          <div v-else-if="activeRewards.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
            Пока нет доступных наград.
          </div>

          <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
            <Card v-for="reward in activeRewards" :key="reward.id" class="flex rounded-lg">
              <div class="flex w-full flex-col">
                <CardHeader>
                  <div class="flex items-start justify-between gap-3">
                    <div class="min-w-0">
                      <Gift class="mb-2 size-5 text-primary" />
                      <CardTitle class="text-lg">
                        {{ reward.title }}
                      </CardTitle>
                    </div>
                    <Badge>
                      {{ reward.cost }} баллов
                    </Badge>
                  </div>
                </CardHeader>

                <CardContent class="flex flex-1 flex-col gap-4">
                  <p class="line-clamp-3 text-sm leading-6 text-muted-foreground">
                    {{ reward.description || 'Описание награды пока не добавлено.' }}
                  </p>

                  <div class="mt-auto flex items-center justify-between gap-3 pt-2">
                    <span class="text-sm text-muted-foreground">
                      Остаток: {{ reward.stock }}
                    </span>

                    <Button
                        :disabled="!canBuy(reward) || pendingRewardId === reward.id"
                        @click="buyReward(reward)"
                    >
                      <span v-if="pendingRewardId === reward.id">Покупка...</span>
                      <span v-else-if="reward.stock <= 0">Нет в наличии</span>
                      <span v-else-if="balance < reward.cost">Не хватает баллов</span>
                      <span v-else>Купить</span>
                    </Button>
                  </div>
                </CardContent>
              </div>
            </Card>
          </div>
        </TabsContent>

        <TabsContent value="purchases">
          <div v-if="isLoading" class="space-y-3">
            <Skeleton v-for="index in 5" :key="index" class="h-20 rounded-lg" />
          </div>

          <div v-else-if="purchases.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
            У вас пока нет заявок на награды.
          </div>

          <div v-else class="space-y-3">
            <Card v-for="purchase in purchases" :key="purchase.id" class="rounded-lg">
              <CardContent class="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between">
                <div class="min-w-0 space-y-2">
                  <div class="flex flex-wrap items-center gap-2">
                    <History class="size-4 text-muted-foreground" />
                    <h2 class="font-medium">
                      {{ purchase.rewardTitle }}
                    </h2>
                    <Badge :variant="purchaseStatusVariants[purchase.status]">
                      {{ purchaseStatusLabels[purchase.status] }}
                    </Badge>
                  </div>
                  <p class="text-sm text-muted-foreground">
                    {{ purchase.cost }} баллов · {{ formatDateTime(purchase.createdAt) }}
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
    </section>
  </AppLayout>
</template>