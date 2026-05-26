<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'
import { CalendarDays, MapPin, Search } from 'lucide-vue-next'

import {
  cancelEventRegistration,
  getMyEvents,
  getStudentEvents,
  registerForEvent,
} from '@/entities/event/api'
import type { EventRegistrationResponse, EventResponse } from '@/entities/event/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
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
const events = ref<EventResponse[]>([])
const registrations = ref<EventRegistrationResponse[]>([])
const cancelDialog = reactive<{
  open: boolean
  event: EventResponse | null
}>({
  open: false,
  event: null,
})

const activeRegistrationByEventId = computed(() => {
  const map = new Map<number, EventRegistrationResponse>()

  registrations.value
      .filter((registration) => registration.status === 'REGISTERED')
      .forEach((registration) => {
        map.set(registration.eventId, registration)
      })

  return map
})

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

function isRegistered(eventId: number): boolean {
  return activeRegistrationByEventId.value.has(eventId)
}

async function loadEvents(): Promise<void> {
  isLoading.value = true

  try {
    const [eventsResponse, registrationsResponse] = await Promise.all([
      getStudentEvents(),
      getMyEvents(),
    ])

    events.value = eventsResponse
    registrations.value = registrationsResponse
  } catch (error) {
    toast.error('Не удалось загрузить мероприятия', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

async function register(eventId: number): Promise<void> {
  pendingEventId.value = eventId

  try {
    await registerForEvent(eventId)
    toast.success('Вы записались на мероприятие')
    await loadEvents()
  } catch (error) {
    toast.error('Не удалось записаться', {
      description: getApiErrorMessage(error),
    })
  } finally {
    pendingEventId.value = null
  }
}

function openCancelDialog(event: EventResponse): void {
  cancelDialog.event = event
  cancelDialog.open = true
}

async function cancelRegistration(): Promise<void> {
  const event = cancelDialog.event

  if (!event) {
    return
  }

  pendingEventId.value = event.id

  try {
    await cancelEventRegistration(event.id)
    toast.success('Запись отменена')
    cancelDialog.open = false
    cancelDialog.event = null
    await loadEvents()
  } catch (error) {
    toast.error('Не удалось отменить запись', {
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
      <div class="flex flex-col gap-4 lg:flex-row lg:items-end lg:justify-between">
        <div>
          <h1 class="text-2xl font-semibold">
            Мероприятия
          </h1>
          <p class="text-muted-foreground">
            Выбирайте события и записывайтесь на участие.
          </p>
        </div>

        <div class="relative w-full lg:max-w-sm">
          <Search class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
          <Input v-model="search" class="pl-9" placeholder="Поиск по названию, организации или месту" />
        </div>
      </div>

      <div v-if="isLoading" class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Skeleton v-for="index in 6" :key="index" class="h-56 rounded-lg" />
      </div>

      <div v-else-if="filteredEvents.length === 0" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
        Ничего не найдено.
      </div>

      <div v-else class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <Card v-for="event in filteredEvents" :key="event.id" class="flex rounded-lg">
          <div class="flex w-full flex-col">
            <CardHeader>
              <div class="flex items-start justify-between gap-3">
                <div class="min-w-0">
                  <CardTitle class="text-lg">
                    {{ event.title }}
                  </CardTitle>
                  <p class="mt-1 text-sm text-muted-foreground">
                    {{ event.organizationName }}
                  </p>
                </div>
                <Badge variant="secondary">
                  {{ event.pointsReward }} баллов
                </Badge>
              </div>
            </CardHeader>

            <CardContent class="flex flex-1 flex-col gap-4">
              <p class="line-clamp-3 text-sm leading-6 text-muted-foreground">
                {{ event.description || 'Описание мероприятия пока не добавлено.' }}
              </p>

              <div class="space-y-2 text-sm">
                <div class="flex items-center gap-2 text-muted-foreground">
                  <CalendarDays class="size-4" />
                  <span>{{ formatDateTime(event.startsAt) }}</span>
                </div>
                <div v-if="event.location" class="flex items-center gap-2 text-muted-foreground">
                  <MapPin class="size-4" />
                  <span>{{ event.location }}</span>
                </div>
              </div>

              <div class="mt-auto flex items-center justify-between gap-3 pt-2">
                <span class="text-sm text-muted-foreground">
                  {{ event.capacity ? `Мест: ${event.capacity}` : 'Без лимита мест' }}
                </span>

                <Button
                    v-if="isRegistered(event.id)"
                    :disabled="pendingEventId === event.id"
                    variant="outline"
                    @click="openCancelDialog(event)"
                >
                  {{ pendingEventId === event.id ? 'Отмена...' : 'Отменить' }}
                </Button>

                <Button
                    v-else
                    :disabled="pendingEventId === event.id"
                    @click="register(event.id)"
                >
                  {{ pendingEventId === event.id ? 'Запись...' : 'Записаться' }}
                </Button>
              </div>
            </CardContent>
          </div>
        </Card>
      </div>

      <AlertDialog v-model:open="cancelDialog.open">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Отменить запись?</AlertDialogTitle>
            <AlertDialogDescription>
              Запись на мероприятие {{ cancelDialog.event?.title }} будет отменена.
              Если количество мест ограничено, место станет доступно другим студентам.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>
              Не отменять
            </AlertDialogCancel>
            <AlertDialogAction
                :disabled="cancelDialog.event ? pendingEventId === cancelDialog.event.id : false"
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