<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { toast } from 'vue-sonner'
import { CheckCircle2, ClipboardCheck, XCircle } from 'lucide-vue-next'

import { getManagerEventAttendance, markManagerEventAttendance } from '@/entities/attendance/api'
import type { AttendanceResponse } from '@/entities/attendance/types'
import { getManagerEventRegistrations, getManagerEventsByOrganization } from '@/entities/event/api'
import type { EventRegistrationResponse, EventResponse } from '@/entities/event/types'
import { getMyManagerOrganizations } from '@/entities/organization/api'
import type { OrganizationResponse } from '@/entities/organization/types'
import { getApiErrorMessage } from '@/shared/api/client'
import AppLayout from '@/shared/layouts/AppLayout.vue'
import { formatDateTime } from '@/shared/lib/date'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'

const isLoading = ref(true)
const isDetailsLoading = ref(false)
const pendingUserId = ref<number | null>(null)

const organizations = ref<OrganizationResponse[]>([])
const activeOrganizations = computed(() => organizations.value.filter((organization) => organization.active))
const events = ref<EventResponse[]>([])
const registrations = ref<EventRegistrationResponse[]>([])
const attendance = ref<AttendanceResponse[]>([])

const selectedOrganizationId = ref<string>('')
const selectedEventId = ref<string>('')

const attendanceByUserId = computed(() => {
  const map = new Map<number, AttendanceResponse>()

  attendance.value.forEach((item) => {
    map.set(item.userId, item)
  })

  return map
})

function isConfirmedAttendance(userId: number): boolean {
  return attendanceByUserId.value.get(userId)?.attended === true
}

const manageableEvents = computed(() => events.value.filter((event) => (
    event.status === 'PUBLISHED' || event.status === 'COMPLETED'
)))

const registeredStudents = computed(() => registrations.value.filter((registration) => (
    registration.status === 'REGISTERED'
)))

async function loadOrganizationsAndEvents(): Promise<void> {
  isLoading.value = true

  try {
    organizations.value = await getMyManagerOrganizations()

    if (!activeOrganizations.value.some((organization) => String(organization.id) === selectedOrganizationId.value)) {
      selectedOrganizationId.value = activeOrganizations.value[0] ? String(activeOrganizations.value[0].id) : ''
    }

    await loadEventsForSelectedOrganization()
  } catch (error) {
    toast.error('Не удалось загрузить данные', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

async function loadEventsForSelectedOrganization(): Promise<void> {
  if (!selectedOrganizationId.value) {
    events.value = []
    selectedEventId.value = ''
    return
  }

  events.value = await getManagerEventsByOrganization(Number(selectedOrganizationId.value))

  const firstEvent = manageableEvents.value[0]
  selectedEventId.value = firstEvent ? String(firstEvent.id) : ''
}

async function loadEventDetails(): Promise<void> {
  if (!selectedEventId.value) {
    registrations.value = []
    attendance.value = []
    return
  }

  isDetailsLoading.value = true

  try {
    const eventId = Number(selectedEventId.value)

    const [registrationsResponse, attendanceResponse] = await Promise.all([
      getManagerEventRegistrations(eventId),
      getManagerEventAttendance(eventId),
    ])

    registrations.value = registrationsResponse
    attendance.value = attendanceResponse
  } catch (error) {
    toast.error('Не удалось загрузить посещаемость', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isDetailsLoading.value = false
  }
}

async function onOrganizationChange(): Promise<void> {
  try {
    await loadEventsForSelectedOrganization()
    await loadEventDetails()
  } catch (error) {
    toast.error('Не удалось сменить организацию', {
      description: getApiErrorMessage(error),
    })
  }
}

async function markAttendance(userId: number, attended: boolean): Promise<void> {
  if (!selectedEventId.value) {
    return
  }

  pendingUserId.value = userId

  try {
    await markManagerEventAttendance(Number(selectedEventId.value), {
      userId,
      attended,
    })

    toast.success(attended ? 'Посещение отмечено' : 'Отсутствие отмечено')
    await loadEventDetails()
  } catch (error) {
    toast.error('Не удалось отметить посещаемость', {
      description: getApiErrorMessage(error),
    })
  } finally {
    pendingUserId.value = null
  }
}

watch(selectedEventId, () => {
  void loadEventDetails()
})

onMounted(loadOrganizationsAndEvents)
</script>

<template>
  <AppLayout>
    <section class="space-y-6 p-4 sm:p-6">
      <div>
        <h1 class="text-2xl font-semibold">
          Посещаемость
        </h1>
        <p class="text-muted-foreground">
          Отмечайте присутствие студентов и начисляйте баллы за участие.
        </p>
      </div>

      <Card class="rounded-lg">
        <CardContent class="grid gap-4 p-4 lg:grid-cols-2">
          <div class="space-y-2">
            <p class="text-sm font-medium">
              Организация
            </p>
            <Select v-model="selectedOrganizationId" @update:model-value="onOrganizationChange">
              <SelectTrigger>
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
            <p class="text-sm font-medium">
              Мероприятие
            </p>
            <Select v-model="selectedEventId">
              <SelectTrigger>
                <SelectValue placeholder="Выберите мероприятие" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem
                    v-for="event in manageableEvents"
                    :key="event.id"
                    :value="String(event.id)"
                >
                  {{ event.title }}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      <div v-if="isLoading || isDetailsLoading" class="space-y-3">
        <Skeleton v-for="index in 6" :key="index" class="h-24 rounded-lg" />
      </div>

      <div v-else-if="!selectedEventId" class="rounded-lg border border-dashed bg-background p-10 text-center text-muted-foreground">
        Нет опубликованных или завершенных мероприятий для учета посещаемости.
      </div>

      <Card v-else class="rounded-lg">
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <ClipboardCheck class="size-5" />
            Список участников
          </CardTitle>
        </CardHeader>

        <CardContent>
          <div v-if="registeredStudents.length === 0" class="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
            На это мероприятие пока никто не записался.
          </div>

          <div v-else class="space-y-3">
            <div
                v-for="registration in registeredStudents"
                :key="registration.id"
                class="flex flex-col gap-4 rounded-md border p-4 sm:flex-row sm:items-center sm:justify-between"
            >
              <div class="min-w-0 space-y-1">
                <div class="flex flex-wrap items-center gap-2">
                  <h2 class="font-medium">
                    {{ registration.userLastName }} {{ registration.userFirstName }}
                  </h2>

                  <Badge
                      v-if="attendanceByUserId.get(registration.userId)?.attended === true"
                      variant="default"
                  >
                    Был
                  </Badge>

                  <Badge
                      v-else-if="attendanceByUserId.get(registration.userId)?.attended === false"
                      variant="secondary"
                  >
                    Не был
                  </Badge>

                  <Badge v-else variant="outline">
                    Не отмечен
                  </Badge>
                </div>

                <p class="text-sm text-muted-foreground">
                  {{ registration.userEmail }}
                </p>

                <p v-if="attendanceByUserId.get(registration.userId)" class="text-xs text-muted-foreground">
                  Отмечено: {{ formatDateTime(attendanceByUserId.get(registration.userId)!.markedAt) }}
                </p>
              </div>

              <div class="flex gap-2">
                <Button
                    :disabled="pendingUserId === registration.userId"
                    size="sm"
                    @click="markAttendance(registration.userId, true)"
                >
                  <CheckCircle2 class="mr-2 size-4" />
                  Был
                </Button>

                <Button
                    :disabled="pendingUserId === registration.userId || isConfirmedAttendance(registration.userId)"
                    size="sm"
                    variant="outline"
                    @click="markAttendance(registration.userId, false)"
                >
                  <XCircle class="mr-2 size-4" />
                  Не был
                </Button>
              </div>

              <p
                  v-if="isConfirmedAttendance(registration.userId)"
                  class="text-xs text-muted-foreground"
              >
                Подтвержденное посещение нельзя отменить после начисления баллов.
              </p>

            </div>
          </div>
        </CardContent>
      </Card>
    </section>
  </AppLayout>
</template>