<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { toast } from 'vue-sonner'
import { ArrowDownCircle, ArrowUpCircle, WalletCards } from 'lucide-vue-next'

import { getPointBalance, getPointTransactions } from '@/entities/points/api'
import type { PointTransactionResponse, PointTransactionType } from '@/entities/points/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'

const isLoading = ref(true)
const balance = ref(0)
const activeTab = ref<'ALL' | PointTransactionType>('ALL')
const transactions = ref<PointTransactionResponse[]>([])

const typeLabels: Record<PointTransactionType, string> = {
  EVENT_ATTENDANCE: 'Начисление за посещение',
  REWARD_PURCHASE: 'Покупка награды',
  MANUAL_ADJUSTMENT: 'Корректировка',
}

const filteredTransactions = computed(() => {
  if (activeTab.value === 'ALL') {
    return transactions.value
  }

  return transactions.value.filter((transaction) => transaction.type === activeTab.value)
})

async function loadPoints(): Promise<void> {
  isLoading.value = true

  try {
    const [balanceResponse, transactionsResponse] = await Promise.all([
      getPointBalance(),
      getPointTransactions(),
    ])

    balance.value = balanceResponse.balance
    transactions.value = transactionsResponse
  } catch (error) {
    toast.error('Не удалось загрузить баллы', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

onMounted(loadPoints)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div>
        <h1 class="text-2xl font-semibold">
          Баллы
        </h1>
        <p class="text-muted-foreground">
          Баланс и история начислений.
        </p>
      </div>

      <Card class="rounded-lg">
        <CardContent class="flex items-center gap-4 p-5">
          <div class="flex size-12 items-center justify-center rounded-md bg-muted">
            <WalletCards class="size-6" />
          </div>
          <div>
            <p class="text-sm text-muted-foreground">
              Текущий баланс
            </p>
            <p class="text-3xl font-semibold">
              {{ balance }}
            </p>
          </div>
        </CardContent>
      </Card>

      <div class="space-y-4">
        <div class="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <h2 class="text-lg font-semibold">
            История операций
          </h2>

          <Tabs v-model="activeTab">
            <TabsList>
              <TabsTrigger value="ALL">
                Все
              </TabsTrigger>
              <TabsTrigger value="EVENT_ATTENDANCE">
                Посещения
              </TabsTrigger>
              <TabsTrigger value="REWARD_PURCHASE">
                Покупки
              </TabsTrigger>
              <TabsTrigger value="MANUAL_ADJUSTMENT">
                Корректировки
              </TabsTrigger>
            </TabsList>
          </Tabs>
        </div>

        <div v-if="isLoading" class="space-y-3">
          <Skeleton v-for="index in 6" :key="index" class="h-20 rounded-lg" />
        </div>

        <div v-else-if="filteredTransactions.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
          Операций пока нет.
        </div>

        <div v-else class="space-y-3">
          <Card v-for="transaction in filteredTransactions" :key="transaction.id" class="rounded-lg">
            <CardHeader class="pb-2">
              <CardTitle class="flex items-center justify-between gap-3 text-base">
                <span class="flex min-w-0 items-center gap-2">
                  <ArrowUpCircle v-if="transaction.amount > 0" class="size-5 text-emerald-600" />
                  <ArrowDownCircle v-else class="size-5 text-destructive" />
                  <span class="truncate">
                    {{ transaction.description || typeLabels[transaction.type] }}
                  </span>
                </span>

                <Badge :variant="transaction.amount > 0 ? 'default' : 'secondary'">
                  {{ transaction.amount > 0 ? '+' : '' }}{{ transaction.amount }}
                </Badge>
              </CardTitle>
            </CardHeader>

            <CardContent class="text-sm text-muted-foreground">
              {{ typeLabels[transaction.type] }} · {{ formatDateTime(transaction.createdAt) }}
            </CardContent>
          </Card>
        </div>
      </div>
    </section>
  </AppLayout>
</template>
