<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'
import { CalendarDays, Pencil, Plus, Search } from 'lucide-vue-next'

import {
  cancelManagerEvent,
  completeManagerEvent,
  createManagerEvent,
  getManagerEventsByOrganization,
  publishManagerEvent,
  updateManagerEvent,
} from '@/entities/event/api'
import type { EventResponse } from '@/entities/event/types'
import { getMyManagerOrganizations } from '@/entities/organization/api'
import type { OrganizationResponse } from '@/entities/organization/types'
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
const pendingEventId = ref<number | null>(null)
const selectedEvent = ref<EventResponse | null>(null)
const actionDialog = reactive<{
  open: boolean
  event: EventResponse | null
  action: 'publish' | 'cancel' | 'complete' | null
}>({
  open: false,
  event: null,
  action: null,
})
const search = ref('')
const selectedOrganizationId = ref<string>('')
const organizations = ref<OrganizationResponse[]>([])
const events = ref<EventResponse[]>([])
const activeOrganizations = computed(() => organizations.value.filter((organization) => organization.active))

const createForm = reactive({
  title: '',
  description: '',
  location: '',
  startsAt: '',
  endsAt: '',
  pointsReward: 10,
  capacity: '',
})

const editForm = reactive({
  title: '',
  description: '',
  location: '',
  startsAt: '',
  endsAt: '',
  pointsReward: 10,
  capacity: '',
})

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

const actionLabels: Record<'publish' | 'cancel' | 'complete', string> = {
  publish: 'Опубликовать',
  cancel: 'Отменить',
  complete: 'Завершить',
}

const actionDialogTitles: Record<'publish' | 'cancel' | 'complete', string> = {
  publish: 'Опубликовать мероприятие?',
  cancel: 'Отменить мероприятие?',
  complete: 'Завершить мероприятие?',
}

const actionDialogDescriptions: Record<'publish' | 'cancel' | 'complete', string> = {
  publish: 'Мероприятие станет видимым студентам и доступным для записи.',
  cancel: 'Мероприятие будет отменено и станет недоступным для записи.',
  complete: 'Мероприятие будет завершено. После этого его нельзя будет отменить или редактировать как активное.',
}

const filteredEvents = computed(() => {
  const query = search.value.trim().toLowerCase()

  if (!query) {
    return events.value
  }

  return events.value.filter((event) => (
      event.title.toLowerCase().includes(query)
      || event.organizationName.toLowerCase().includes(query)
      || event.location?.toLowerCase().includes(query)
  ))
})

const createValidationMessage = computed(() => {
  if (!selectedOrganizationId.value) {
    return 'Выберите организацию'
  }

  if (!createForm.title.trim()) {
    return 'Введите название'
  }

  if (!createForm.startsAt) {
    return 'Укажите начало'
  }

  if (new Date(createForm.startsAt) <= new Date()) {
    return 'Начало должно быть в будущем'
  }

  if (!createForm.endsAt) {
    return 'Укажите окончание'
  }

  if (new Date(createForm.endsAt) <= new Date(createForm.startsAt)) {
    return 'Окончание должно быть позже начала'
  }

  if (Number(createForm.pointsReward) < 0) {
    return 'Баллы не могут быть отрицательными'
  }

  return null
})

const editValidationMessage = computed(() => {
  if (!selectedEvent.value) {
    return 'Выберите мероприятие'
  }

  if (!editForm.title.trim()) {
    return 'Введите название'
  }

  if (!editForm.startsAt) {
    return 'Укажите начало'
  }

  if (!editForm.endsAt) {
    return 'Укажите окончание'
  }

  if (new Date(editForm.endsAt) <= new Date(editForm.startsAt)) {
    return 'Окончание должно быть позже начала'
  }

  if (Number(editForm.pointsReward) < 0) {
    return 'Баллы не могут быть отрицательными'
  }

  if (editForm.capacity && Number(editForm.capacity) < 1) {
    return 'Количество мест должно быть больше 0'
  }

  return null
})

const canCreate = computed(() => createValidationMessage.value === null)
const canEdit = computed(() => editValidationMessage.value === null)

function toOffsetDateTime(value: string): string {
  return new Date(value).toISOString()
}

function resetCreateForm(): void {
  createForm.title = ''
  createForm.description = ''
  createForm.location = ''
  createForm.startsAt = ''
  createForm.endsAt = ''
  createForm.pointsReward = 10
  createForm.capacity = ''
}

function openEditDialog(event: EventResponse): void {
  selectedEvent.value = event
  editForm.title = event.title
  editForm.description = event.description ?? ''
  editForm.location = event.location ?? ''
  editForm.startsAt = toDateTimeLocalValue(new Date(event.startsAt))
  editForm.endsAt = toDateTimeLocalValue(new Date(event.endsAt))
  editForm.pointsReward = event.pointsReward
  editForm.capacity = event.capacity === null ? '' : String(event.capacity)
  isEditOpen.value = true
}

async function loadEvents(): Promise<void> {
  isLoading.value = true

  try {
    organizations.value = await getMyManagerOrganizations()

    if (!activeOrganizations.value.some((organization) => String(organization.id) === selectedOrganizationId.value)) {
      selectedOrganizationId.value = activeOrganizations.value[0] ? String(activeOrganizations.value[0].id) : ''
    }

    if (!selectedOrganizationId.value) {
      events.value = []
      return
    }

    events.value = await getManagerEventsByOrganization(Number(selectedOrganizationId.value))
  } catch (error) {
    toast.error('Не удалось загрузить мероприятия', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

async function onOrganizationChange(): Promise<void> {
  await loadEvents()
}

function toDateTimeLocalValue(date: Date): string {
  const offset = date.getTimezoneOffset()
  const localDate = new Date(date.getTime() - offset * 60_000)

  return localDate.toISOString().slice(0, 16)
}

const minStartsAt = computed(() => toDateTimeLocalValue(new Date(Date.now() + 5 * 60_000)))

const minEndsAt = computed(() => {
  if (!createForm.startsAt) {
    return minStartsAt.value
  }

  return createForm.startsAt
})

async function createEvent(): Promise<void> {
  if (!canCreate.value || isCreating.value) {
    return
  }

  isCreating.value = true

  try {
    await createManagerEvent({
      organizationId: Number(selectedOrganizationId.value),
      title: createForm.title.trim(),
      description: createForm.description.trim() || null,
      location: createForm.location.trim() || null,
      startsAt: toOffsetDateTime(createForm.startsAt),
      endsAt: toOffsetDateTime(createForm.endsAt),
      pointsReward: Number(createForm.pointsReward),
      capacity: createForm.capacity ? Number(createForm.capacity) : null,
    })

    toast.success('Мероприятие создано')
    resetCreateForm()
    isCreateOpen.value = false
    await loadEvents()
  } catch (error) {
    toast.error('Не удалось создать мероприятие', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isCreating.value = false
  }
}

async function updateEvent(): Promise<void> {
  const event = selectedEvent.value

  if (!event || !canEdit.value || isUpdating.value) {
    return
  }

  isUpdating.value = true

  try {
    await updateManagerEvent(event.id, {
      title: editForm.title.trim(),
      description: editForm.description.trim() || null,
      location: editForm.location.trim() || null,
      startsAt: toOffsetDateTime(editForm.startsAt),
      endsAt: toOffsetDateTime(editForm.endsAt),
      pointsReward: Number(editForm.pointsReward),
      capacity: editForm.capacity ? Number(editForm.capacity) : null,
    })

    toast.success('Мероприятие обновлено')
    isEditOpen.value = false
    selectedEvent.value = null
    await loadEvents()
  } catch (error) {
    toast.error('Не удалось обновить мероприятие', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isUpdating.value = false
  }
}

function openActionDialog(event: EventResponse, action: 'publish' | 'cancel' | 'complete'): void {
  actionDialog.event = event
  actionDialog.action = action
  actionDialog.open = true
}

async function runEventAction(): Promise<void> {
  const event = actionDialog.event
  const action = actionDialog.action

  if (!event || !action) {
    return
  }

  pendingEventId.value = event.id

  try {
    if (action === 'publish') {
      await publishManagerEvent(event.id)
      toast.success('Мероприятие опубликовано')
    }

    if (action === 'cancel') {
      await cancelManagerEvent(event.id)
      toast.success('Мероприятие отменено')
    }

    if (action === 'complete') {
      await completeManagerEvent(event.id)
      toast.success('Мероприятие завершено')
    }

    actionDialog.open = false
    actionDialog.event = null
    actionDialog.action = null
    await loadEvents()
  } catch (error) {
    toast.error('Не удалось выполнить действие', {
      description: getApiErrorMessage(error),
    })
  } finally {
    pendingEventId.value = null
  }
}

onMounted(loadEvents)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div>
          <h1 class="text-2xl font-semibold">
            Мероприятия
          </h1>
          <p class="text-muted-foreground">
            Создавайте и управляйте событиями организации.
          </p>
        </div>

        <div class="flex flex-col gap-3 sm:flex-row">
          <Select v-model="selectedOrganizationId" @update:model-value="onOrganizationChange">
            <SelectTrigger class="w-full sm:w-64">
              <SelectValue placeholder="Организация" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem
                  v-for="organization in activeOrganizations"
                  :key="organization.id"
                  :value="String(organization.id)"
              >
                {{ organization.name }}
              </SelectItem>
            </SelectContent>
          </Select>

          <div class="relative w-full sm:w-72">
            <Search class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input v-model="search" class="pl-9" placeholder="Поиск мероприятия" />
          </div>

          <Dialog v-model:open="isCreateOpen">
            <DialogTrigger as-child>
              <Button>
                <Plus class="mr-2 size-4" />
                Создать
              </Button>
            </DialogTrigger>
            <DialogContent class="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
            <DialogHeader>
                <DialogTitle>Новое мероприятие</DialogTitle>
                <DialogDescription>
                  Мероприятие будет создано как черновик.
                </DialogDescription>
              </DialogHeader>

              <form class="space-y-6" @submit.prevent="createEvent">
                <div class="grid gap-4">
                  <div class="space-y-2">
                    <Label for="organization">Организация</Label>
                    <Select v-model="selectedOrganizationId">
                      <SelectTrigger id="organization">
                        <SelectValue placeholder="Выберите организацию" />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem
                            v-for="organization in activeOrganizations"
                            :key="organization.id"
                            :value="String(organization.id)"
                        >
                          {{ organization.name }}
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  <div class="space-y-2">
                    <Label for="title">Название</Label>
                    <Input
                        id="title"
                        v-model="createForm.title"
                        placeholder="Например, День карьеры в ИЦ"
                    />
                  </div>

                  <div class="space-y-2">
                    <Label for="description">Описание</Label>
                    <Textarea
                        id="description"
                        v-model="createForm.description"
                        class="min-h-24 resize-none"
                        placeholder="Кратко расскажите, что будет на мероприятии"
                    />
                  </div>
                </div>

                <div class="rounded-lg border bg-muted/25 p-4">
                  <h3 class="mb-4 text-sm font-medium">
                    Дата и место
                  </h3>

                  <div class="grid gap-4 sm:grid-cols-2">
                    <div class="space-y-2">
                      <Label for="startsAt">Начало</Label>
                      <Input id="startsAt" v-model="createForm.startsAt" :min="minStartsAt" type="datetime-local" />
                    </div>

                    <div class="space-y-2">
                      <Label for="endsAt">Окончание</Label>
                      <Input id="endsAt" v-model="createForm.endsAt" :min="minEndsAt" type="datetime-local" />
                    </div>

                    <div class="space-y-2 sm:col-span-2">
                      <Label for="location">Место</Label>
                      <Input
                          id="location"
                          v-model="createForm.location"
                          placeholder="Аудитория, корпус или онлайн-ссылка"
                      />
                    </div>
                  </div>
                </div>

                <div class="rounded-lg border bg-muted/25 p-4">
                  <h3 class="mb-4 text-sm font-medium">
                    Условия участия
                  </h3>

                  <div class="grid gap-4 sm:grid-cols-2">
                    <div class="space-y-2">
                      <Label for="pointsReward">Баллы за посещение</Label>
                      <Input
                          id="pointsReward"
                          v-model.number="createForm.pointsReward"
                          min="0"
                          type="number"
                      />
                    </div>

                    <div class="space-y-2">
                      <Label for="capacity">Количество мест</Label>
                      <Input
                          id="capacity"
                          v-model="createForm.capacity"
                          min="1"
                          placeholder="Без лимита"
                          type="number"
                      />
                    </div>
                  </div>
                </div>

                <div class="flex flex-col-reverse gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
                  <p v-if="createValidationMessage" class="text-sm text-muted-foreground">
                    {{ createValidationMessage }}
                  </p>
                  <p v-else class="text-sm text-muted-foreground">
                    Мероприятие будет создано в статусе черновика.
                  </p>

                  <div class="flex gap-2 sm:justify-end">
                    <Button type="button" variant="outline" @click="isCreateOpen = false">
                      Отмена
                    </Button>
                    <Button :disabled="!canCreate || isCreating" type="submit">
                      {{ isCreating ? 'Создаем...' : 'Создать' }}
                    </Button>
                  </div>
                </div>
              </form>

            </DialogContent>
          </Dialog>
        </div>
      </div>

      <div v-if="isLoading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Skeleton v-for="index in 6" :key="index" class="h-64 rounded-lg" />
      </div>

      <div v-else-if="filteredEvents.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
        Мероприятия не найдены.
      </div>

      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Card v-for="event in filteredEvents" :key="event.id" class="flex rounded-lg">
          <div class="flex w-full flex-col">
            <CardHeader>
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0">
                  <CalendarDays class="mb-2 size-5 text-primary" />
                  <CardTitle class="text-lg">
                    {{ event.title }}
                  </CardTitle>
                  <p class="mt-1 text-sm text-muted-foreground">
                    {{ event.organizationName }}
                  </p>
                </div>

                <Badge :variant="statusVariants[event.status]">
                  {{ statusLabels[event.status] }}
                </Badge>
              </div>
            </CardHeader>

            <CardContent class="flex flex-1 flex-col gap-4">
              <p class="line-clamp-3 text-sm leading-6 text-muted-foreground">
                {{ event.description || 'Описание мероприятия пока не добавлено.' }}
              </p>

              <div class="space-y-1 text-sm text-muted-foreground">
                <p>{{ formatDateTime(event.startsAt) }}</p>
                <p v-if="event.location">
                  {{ event.location }}
                </p>
                <p>{{ event.pointsReward }} баллов · {{ event.capacity ? `мест: ${event.capacity}` : 'без лимита мест' }}</p>
              </div>

              <div class="mt-auto flex flex-wrap gap-2 pt-2">
                <Button
                    v-if="event.status === 'DRAFT' || event.status === 'PUBLISHED'"
                    size="sm"
                    variant="outline"
                    @click="openEditDialog(event)"
                >
                  <Pencil class="mr-2 size-4" />
                  Редактировать
                </Button>

                <Button
                    v-if="event.status === 'DRAFT'"
                    :disabled="pendingEventId === event.id"
                    size="sm"
                    @click="openActionDialog(event, 'publish')"
                >
                  Опубликовать
                </Button>

                <Button
                    v-if="event.status === 'DRAFT' || event.status === 'PUBLISHED'"
                    :disabled="pendingEventId === event.id"
                    size="sm"
                    variant="outline"
                    @click="openActionDialog(event, 'cancel')"
                >
                  Отменить
                </Button>

                <Button
                    v-if="event.status === 'PUBLISHED'"
                    :disabled="pendingEventId === event.id"
                    size="sm"
                    variant="outline"
                    @click="openActionDialog(event, 'complete')"
                >
                  Завершить
                </Button>
              </div>
            </CardContent>
          </div>
        </Card>
      </div>

      <Dialog v-model:open="isEditOpen">
        <DialogContent class="max-h-[90vh] overflow-y-auto sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>Редактировать мероприятие</DialogTitle>
            <DialogDescription>
              Можно изменить черновик или опубликованное мероприятие.
            </DialogDescription>
          </DialogHeader>

          <form class="space-y-6" @submit.prevent="updateEvent">
            <div class="grid gap-4">
              <div class="space-y-2">
                <Label for="edit-event-title">Название</Label>
                <Input
                    id="edit-event-title"
                    v-model="editForm.title"
                    placeholder="Например, День карьеры в ИЦ"
                />
              </div>

              <div class="space-y-2">
                <Label for="edit-event-description">Описание</Label>
                <Textarea
                    id="edit-event-description"
                    v-model="editForm.description"
                    class="min-h-24 resize-none"
                    placeholder="Кратко расскажите, что будет на мероприятии"
                />
              </div>
            </div>

            <div class="rounded-lg border bg-muted/25 p-4">
              <h3 class="mb-4 text-sm font-medium">
                Дата и место
              </h3>

              <div class="grid gap-4 sm:grid-cols-2">
                <div class="space-y-2">
                  <Label for="edit-event-startsAt">Начало</Label>
                  <Input id="edit-event-startsAt" v-model="editForm.startsAt" type="datetime-local" />
                </div>

                <div class="space-y-2">
                  <Label for="edit-event-endsAt">Окончание</Label>
                  <Input id="edit-event-endsAt" v-model="editForm.endsAt" :min="editForm.startsAt" type="datetime-local" />
                </div>

                <div class="space-y-2 sm:col-span-2">
                  <Label for="edit-event-location">Место</Label>
                  <Input
                      id="edit-event-location"
                      v-model="editForm.location"
                      placeholder="Аудитория, корпус или онлайн-ссылка"
                  />
                </div>
              </div>
            </div>

            <div class="rounded-lg border bg-muted/25 p-4">
              <h3 class="mb-4 text-sm font-medium">
                Условия участия
              </h3>

              <div class="grid gap-4 sm:grid-cols-2">
                <div class="space-y-2">
                  <Label for="edit-event-pointsReward">Баллы за посещение</Label>
                  <Input
                      id="edit-event-pointsReward"
                      v-model.number="editForm.pointsReward"
                      min="0"
                      type="number"
                  />
                </div>

                <div class="space-y-2">
                  <Label for="edit-event-capacity">Количество мест</Label>
                  <Input
                      id="edit-event-capacity"
                      v-model="editForm.capacity"
                      min="1"
                      placeholder="Без лимита"
                      type="number"
                  />
                </div>
              </div>
            </div>

            <div class="flex flex-col-reverse gap-3 border-t pt-4 sm:flex-row sm:items-center sm:justify-between">
              <p v-if="editValidationMessage" class="text-sm text-muted-foreground">
                {{ editValidationMessage }}
              </p>
              <p v-else class="text-sm text-muted-foreground">
                Изменения будут применены к выбранному мероприятию.
              </p>

              <div class="flex gap-2 sm:justify-end">
                <Button type="button" variant="outline" @click="isEditOpen = false">
                  Отмена
                </Button>
                <Button :disabled="!canEdit || isUpdating" type="submit">
                  {{ isUpdating ? 'Сохраняем...' : 'Сохранить' }}
                </Button>
              </div>
            </div>
          </form>
        </DialogContent>
      </Dialog>

      <AlertDialog v-model:open="actionDialog.open">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>
              {{ actionDialog.action ? actionDialogTitles[actionDialog.action] : 'Подтвердить действие?' }}
            </AlertDialogTitle>
            <AlertDialogDescription>
              {{ actionDialog.action ? actionDialogDescriptions[actionDialog.action] : '' }}
              Мероприятие: {{ actionDialog.event?.title }}.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>
              Отмена
            </AlertDialogCancel>
            <AlertDialogAction
                :disabled="actionDialog.event ? pendingEventId === actionDialog.event.id : false"
                @click="runEventAction"
            >
              {{ actionDialog.action ? actionLabels[actionDialog.action] : 'Подтвердить' }}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

    </section>
  </AppLayout>
</template>
