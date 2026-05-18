<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'
import { CheckCircle2, Gift, Search } from 'lucide-vue-next'

import {
  getAdminRewardPurchases,
  issueAdminRewardPurchase,
} from '@/entities/reward/api'
import type { RewardPurchaseResponse, RewardPurchaseStatus } from '@/entities/reward/types'
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
const pendingPurchaseId = ref<number | null>(null)
const search = ref('')
const activeTab = ref<'ALL' | RewardPurchaseStatus>('REQUESTED')
const purchases = ref<RewardPurchaseResponse[]>([])

const issueDialog = reactive<{
  open: boolean
  purchase: RewardPurchaseResponse | null
}>({
  open: false,
  purchase: null,
})

function openIssueDialog(purchase: RewardPurchaseResponse): void {
  issueDialog.purchase = purchase
  issueDialog.open = true
}

const statusLabels: Record<RewardPurchaseStatus, string> = {
  REQUESTED: 'Ожидает',
  ISSUED: 'Выдано',
  CANCELLED: 'Отменено',
}

const statusVariants: Record<RewardPurchaseStatus, 'default' | 'secondary' | 'outline' | 'destructive'> = {
  REQUESTED: 'default',
  ISSUED: 'outline',
  CANCELLED: 'secondary',
}

const filteredPurchases = computed(() => {
  const query = search.value.trim().toLowerCase()

  return purchases.value.filter((purchase) => {
    const matchesTab = activeTab.value === 'ALL' || purchase.status === activeTab.value
    const matchesSearch = !query || (
        purchase.rewardTitle.toLowerCase().includes(query)
        || String(purchase.userId).includes(query)
    )

    return matchesTab && matchesSearch
  })
})

async function loadPurchases(): Promise<void> {
  isLoading.value = true

  try {
    purchases.value = await getAdminRewardPurchases()
  } catch (error) {
    toast.error('Не удалось загрузить заявки', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

async function issuePurchase(): Promise<void> {
  const purchase = issueDialog.purchase

  if (!purchase) {
    return
  }

  pendingPurchaseId.value = purchase.id

  try {
    await issueAdminRewardPurchase(purchase.id)
    toast.success('Награда выдана')
    issueDialog.open = false
    issueDialog.purchase = null
    await loadPurchases()
  } catch (error) {
    toast.error('Не удалось выдать награду', {
      description: getApiErrorMessage(error),
    })
  } finally {
    pendingPurchaseId.value = null
  }
}

onMounted(loadPurchases)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 class="text-2xl font-semibold">
            Заявки на награды
          </h1>
          <p class="text-muted-foreground">
            Выдавайте награды студентам по созданным заявкам.
          </p>
        </div>

        <div class="relative w-full lg:max-w-sm">
          <Search class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input v-model="search" class="pl-9" placeholder="Поиск по награде или ID пользователя" />
        </div>
      </div>

      <Tabs v-model="activeTab">
        <TabsList>
          <TabsTrigger value="REQUESTED">
            Ожидают
          </TabsTrigger>
          <TabsTrigger value="ISSUED">
            Выданные
          </TabsTrigger>
          <TabsTrigger value="CANCELLED">
            Отмененные
          </TabsTrigger>
          <TabsTrigger value="ALL">
            Все
          </TabsTrigger>
        </TabsList>
      </Tabs>

      <div v-if="isLoading" class="space-y-3">
        <Skeleton v-for="index in 8" :key="index" class="h-24 rounded-lg" />
      </div>

      <div v-else-if="filteredPurchases.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
        Заявки не найдены.
      </div>

      <div v-else class="space-y-3">
        <Card v-for="purchase in filteredPurchases" :key="purchase.id" class="rounded-lg">
          <CardContent class="flex flex-col gap-4 p-4 sm:flex-row sm:items-center sm:justify-between">
            <div class="flex min-w-0 gap-3">
              <div class="flex size-10 shrink-0 items-center justify-center rounded-md bg-muted">
                <Gift class="size-5" />
              </div>

              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <h2 class="font-medium">
                    {{ purchase.rewardTitle }}
                  </h2>
                  <Badge :variant="statusVariants[purchase.status]">
                    {{ statusLabels[purchase.status] }}
                  </Badge>
                </div>

                <p class="text-sm text-muted-foreground">
                  Пользователь #{{ purchase.userId }} · {{ purchase.cost }} баллов
                </p>
                <p class="text-xs text-muted-foreground">
                  Создана: {{ formatDateTime(purchase.createdAt) }}
                </p>
              </div>
            </div>

            <Button
                v-if="purchase.status === 'REQUESTED'"
                :disabled="pendingPurchaseId === purchase.id"
                size="sm"
                @click="openIssueDialog(purchase)"
            >
              <CheckCircle2 class="mr-2 size-4" />
              {{ pendingPurchaseId === purchase.id ? 'Выдаем...' : 'Выдать' }}
            </Button>
          </CardContent>
        </Card>
      </div>

      <AlertDialog v-model:open="issueDialog.open">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Выдать награду?</AlertDialogTitle>
            <AlertDialogDescription>
              Заявка на {{ issueDialog.purchase?.rewardTitle }} будет отмечена как выданная.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>
              Отмена
            </AlertDialogCancel>
            <AlertDialogAction
                :disabled="issueDialog.purchase ? pendingPurchaseId === issueDialog.purchase.id : false"
                @click="issuePurchase"
            >
              Выдать
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

    </section>
  </AppLayout>
</template>