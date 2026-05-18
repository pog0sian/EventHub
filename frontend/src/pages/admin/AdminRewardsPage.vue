<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'
import { Gift, Pencil, Plus, Search } from 'lucide-vue-next'

import {
  createAdminReward,
  deactivateAdminReward,
  getAdminRewards,
  updateAdminReward
} from '@/entities/reward/api'
import type { RewardResponse } from '@/entities/reward/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { Textarea } from '@/components/ui/textarea'
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
const isCreateOpen = ref(false)
const isEditOpen = ref(false)
const isCreating = ref(false)
const isUpdating = ref(false)
const pendingRewardId = ref<number | null>(null)
const selectedReward = ref<RewardResponse | null>(null)
const search = ref('')
const rewards = ref<RewardResponse[]>([])

const deactivateDialog = reactive<{
  open: boolean
  reward: RewardResponse | null
}>({
  open: false,
  reward: null,
})

function openDeactivateDialog(reward: RewardResponse): void {
  deactivateDialog.reward = reward
  deactivateDialog.open = true
}

const form = reactive({
  title: '',
  description: '',
  cost: 100,
  stock: 1,
})

const editForm = reactive({
  title: '',
  description: '',
  cost: 100,
  stock: 1,
  active: 'true',
})

const filteredRewards = computed(() => {
  const query = search.value.trim().toLowerCase()

  if (!query) {
    return rewards.value
  }

  return rewards.value.filter((reward) => (
      reward.title.toLowerCase().includes(query)
      || reward.description?.toLowerCase().includes(query)
  ))
})

const canCreate = computed(() => (
    form.title.trim().length > 0
    && Number(form.cost) >= 1
    && Number(form.stock) >= 0
))

const canEdit = computed(() => (
    selectedReward.value !== null
    && editForm.title.trim().length > 0
    && Number(editForm.cost) >= 1
    && Number(editForm.stock) >= 0
))

function resetForm(): void {
  form.title = ''
  form.description = ''
  form.cost = 100
  form.stock = 1
}

function openEditDialog(reward: RewardResponse): void {
  selectedReward.value = reward
  editForm.title = reward.title
  editForm.description = reward.description ?? ''
  editForm.cost = reward.cost
  editForm.stock = reward.stock
  editForm.active = String(reward.active)
  isEditOpen.value = true
}

async function loadRewards(): Promise<void> {
  isLoading.value = true

  try {
    rewards.value = await getAdminRewards()
  } catch (error) {
    toast.error('Не удалось загрузить награды', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

async function createReward(): Promise<void> {
  if (!canCreate.value || isCreating.value) {
    return
  }

  isCreating.value = true

  try {
    await createAdminReward({
      title: form.title.trim(),
      description: form.description.trim() || null,
      cost: Number(form.cost),
      stock: Number(form.stock),
    })

    toast.success('Награда создана')
    resetForm()
    isCreateOpen.value = false
    await loadRewards()
  } catch (error) {
    toast.error('Не удалось создать награду', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isCreating.value = false
  }
}

async function updateReward(): Promise<void> {
  const reward = selectedReward.value

  if (!reward || !canEdit.value || isUpdating.value) {
    return
  }

  isUpdating.value = true

  try {
    await updateAdminReward(reward.id, {
      title: editForm.title.trim(),
      description: editForm.description.trim() || null,
      cost: Number(editForm.cost),
      stock: Number(editForm.stock),
      active: editForm.active === 'true',
    })

    toast.success('Награда обновлена')
    isEditOpen.value = false
    selectedReward.value = null
    await loadRewards()
  } catch (error) {
    toast.error('Не удалось обновить награду', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isUpdating.value = false
  }
}

async function deactivateReward(): Promise<void> {
  const reward = deactivateDialog.reward

  if (!reward) {
    return
  }

  pendingRewardId.value = reward.id

  try {
    await deactivateAdminReward(reward.id)
    toast.success('Награда деактивирована')
    deactivateDialog.open = false
    deactivateDialog.reward = null
    await loadRewards()
  } catch (error) {
    toast.error('Не удалось деактивировать награду', {
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
            Каталог наград, доступных студентам за баллы.
          </p>
        </div>

        <div class="flex flex-col gap-3 sm:flex-row">
          <div class="relative w-full sm:w-72">
            <Search class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input v-model="search" class="pl-9" placeholder="Поиск награды" />
          </div>

          <Dialog v-model:open="isCreateOpen">
            <DialogTrigger as-child>
              <Button>
                <Plus class="mr-2 size-4" />
                Создать
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Новая награда</DialogTitle>
                <DialogDescription>
                  Награда появится в каталоге студента, если активна и есть остаток.
                </DialogDescription>
              </DialogHeader>

              <form class="space-y-4" @submit.prevent="createReward">
                <div class="space-y-2">
                  <Label for="title">Название</Label>
                  <Input id="title" v-model="form.title" />
                </div>

                <div class="space-y-2">
                  <Label for="description">Описание</Label>
                  <Textarea id="description" v-model="form.description" class="min-h-24 resize-none" />
                </div>

                <div class="grid gap-4 sm:grid-cols-2">
                  <div class="space-y-2">
                    <Label for="cost">Стоимость</Label>
                    <Input id="cost" v-model.number="form.cost" min="1" type="number" />
                  </div>

                  <div class="space-y-2">
                    <Label for="stock">Остаток</Label>
                    <Input id="stock" v-model.number="form.stock" min="0" type="number" />
                  </div>
                </div>

                <div class="flex justify-end gap-2 border-t pt-4">
                  <Button type="button" variant="outline" @click="isCreateOpen = false">
                    Отмена
                  </Button>
                  <Button :disabled="!canCreate || isCreating" type="submit">
                    {{ isCreating ? 'Создаем...' : 'Создать' }}
                  </Button>
                </div>
              </form>
            </DialogContent>
          </Dialog>
        </div>
      </div>

      <div v-if="isLoading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Skeleton v-for="index in 6" :key="index" class="h-56 rounded-lg" />
      </div>

      <div v-else-if="filteredRewards.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
        Награды не найдены.
      </div>

      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Card v-for="reward in filteredRewards" :key="reward.id" class="flex rounded-lg">
          <div class="flex w-full flex-col">
            <CardHeader>
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0">
                  <Gift class="mb-2 size-5 text-primary" />
                  <CardTitle class="text-lg">
                    {{ reward.title }}
                  </CardTitle>
                </div>

                <Badge :variant="reward.active ? 'default' : 'secondary'">
                  {{ reward.active ? 'Активна' : 'Неактивна' }}
                </Badge>
              </div>
            </CardHeader>

            <CardContent class="flex flex-1 flex-col gap-4">
              <p class="line-clamp-3 text-sm leading-6 text-muted-foreground">
                {{ reward.description || 'Описание награды пока не добавлено.' }}
              </p>

              <div class="space-y-1 text-sm text-muted-foreground">
                <p>Стоимость: {{ reward.cost }} баллов</p>
                <p>Остаток: {{ reward.stock }}</p>
                <p>Создана: {{ formatDateTime(reward.createdAt) }}</p>
              </div>

              <div class="mt-auto flex flex-wrap gap-2 pt-2">
                <Button
                  size="sm"
                  variant="outline"
                  @click="openEditDialog(reward)"
                >
                  <Pencil class="mr-2 size-4" />
                  Редактировать
                </Button>

                <Button
                    v-if="reward.active"
                    :disabled="pendingRewardId === reward.id"
                    size="sm"
                    variant="outline"
                    @click="openDeactivateDialog(reward)"
                >
                  Деактивировать
                </Button>
              </div>
            </CardContent>
          </div>
        </Card>
      </div>

      <Dialog v-model:open="isEditOpen">
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Редактировать награду</DialogTitle>
            <DialogDescription>
              Изменения сразу отразятся в каталоге студента.
            </DialogDescription>
          </DialogHeader>

          <form class="space-y-4" @submit.prevent="updateReward">
            <div class="space-y-2">
              <Label for="edit-title">Название</Label>
              <Input id="edit-title" v-model="editForm.title" />
            </div>

            <div class="space-y-2">
              <Label for="edit-description">Описание</Label>
              <Textarea id="edit-description" v-model="editForm.description" class="min-h-24 resize-none" />
            </div>

            <div class="grid gap-4 sm:grid-cols-2">
              <div class="space-y-2">
                <Label for="edit-cost">Стоимость</Label>
                <Input id="edit-cost" v-model.number="editForm.cost" min="1" type="number" />
              </div>

              <div class="space-y-2">
                <Label for="edit-stock">Остаток</Label>
                <Input id="edit-stock" v-model.number="editForm.stock" min="0" type="number" />
              </div>
            </div>

            <div class="space-y-2">
              <Label for="edit-active">Статус</Label>
              <Select v-model="editForm.active">
                <SelectTrigger id="edit-active">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="true">
                    Активна
                  </SelectItem>
                  <SelectItem value="false">
                    Неактивна
                  </SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div class="flex justify-end gap-2 border-t pt-4">
              <Button type="button" variant="outline" @click="isEditOpen = false">
                Отмена
              </Button>
              <Button :disabled="!canEdit || isUpdating" type="submit">
                {{ isUpdating ? 'Сохраняем...' : 'Сохранить' }}
              </Button>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      <AlertDialog v-model:open="deactivateDialog.open">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Деактивировать награду?</AlertDialogTitle>
            <AlertDialogDescription>
              Награда {{ deactivateDialog.reward?.title }} больше не будет доступна студентам для покупки.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>
              Отмена
            </AlertDialogCancel>
            <AlertDialogAction
                :disabled="deactivateDialog.reward ? pendingRewardId === deactivateDialog.reward.id : false"
                @click="deactivateReward"
            >
              Деактивировать
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

    </section>
  </AppLayout>
</template>