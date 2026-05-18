<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { toast } from 'vue-sonner'
import { Mail, Pencil, Plus, Search, Store, UserPlus } from 'lucide-vue-next'

import {
  assignAdminOrganizationManager,
  createAdminOrganization,
  deactivateAdminOrganization,
  getAdminOrganizationManagers,
  getAdminOrganizations,
  removeAdminOrganizationManager,
  updateAdminOrganization,
} from '@/entities/organization/api'
import type {
  OrganizationManagerDetailsResponse,
  OrganizationResponse,
} from '@/entities/organization/types'
import { getAdminUsers } from '@/entities/user/api'
import type { UserResponse } from '@/entities/auth/types'
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
const isCreating = ref(false)
const isUpdating = ref(false)
const isManagersOpen = ref(false)
const isCreateOpen = ref(false)
const isEditOpen = ref(false)
const pendingOrganizationId = ref<number | null>(null)
const selectedOrganization = ref<OrganizationResponse | null>(null)
const selectedEditOrganization = ref<OrganizationResponse | null>(null)
const selectedUserId = ref<string>('')
const search = ref('')

const organizations = ref<OrganizationResponse[]>([])
const users = ref<UserResponse[]>([])
const managers = ref<OrganizationManagerDetailsResponse[]>([])

const deactivateDialog = reactive<{
  open: boolean
  organization: OrganizationResponse | null
}>({
  open: false,
  organization: null,
})

function openDeactivateDialog(organization: OrganizationResponse): void {
  deactivateDialog.organization = organization
  deactivateDialog.open = true
}

const form = reactive({
  name: '',
  description: '',
  contactEmail: '',
})

const editForm = reactive({
  name: '',
  description: '',
  contactEmail: '',
  active: 'true',
})

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

const enabledUsers = computed(() => users.value.filter((user) => user.enabled))
const canCreate = computed(() => form.name.trim().length > 0)

const canEdit = computed(() => (
    selectedEditOrganization.value !== null
    && editForm.name.trim().length > 0
))

function resetForm(): void {
  form.name = ''
  form.description = ''
  form.contactEmail = ''
}

function openEditDialog(organization: OrganizationResponse): void {
  selectedEditOrganization.value = organization
  editForm.name = organization.name
  editForm.description = organization.description ?? ''
  editForm.contactEmail = organization.contactEmail ?? ''
  editForm.active = String(organization.active)
  isEditOpen.value = true
}

async function loadPage(): Promise<void> {
  isLoading.value = true

  try {
    const [organizationsResponse, usersResponse] = await Promise.all([
      getAdminOrganizations(),
      getAdminUsers(),
    ])

    organizations.value = organizationsResponse
    users.value = usersResponse
  } catch (error) {
    toast.error('Не удалось загрузить организации', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isLoading.value = false
  }
}

async function createOrganization(): Promise<void> {
  if (!canCreate.value || isCreating.value) {
    return
  }

  isCreating.value = true

  try {
    await createAdminOrganization({
      name: form.name.trim(),
      description: form.description.trim() || null,
      contactEmail: form.contactEmail.trim() || null,
    })

    toast.success('Организация создана')
    resetForm()
    isCreateOpen.value = false
    await loadPage()
  } catch (error) {
    toast.error('Не удалось создать организацию', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isCreating.value = false
  }
}

async function updateOrganization(): Promise<void> {
  const organization = selectedEditOrganization.value

  if (!organization || !canEdit.value || isUpdating.value) {
    return
  }

  isUpdating.value = true

  try {
    await updateAdminOrganization(organization.id, {
      name: editForm.name.trim(),
      description: editForm.description.trim() || null,
      contactEmail: editForm.contactEmail.trim() || null,
      active: editForm.active === 'true',
    })

    toast.success('Организация обновлена')
    isEditOpen.value = false
    selectedEditOrganization.value = null
    await loadPage()
  } catch (error) {
    toast.error('Не удалось обновить организацию', {
      description: getApiErrorMessage(error),
    })
  } finally {
    isUpdating.value = false
  }
}

async function deactivateOrganization(): Promise<void> {
  const organization = deactivateDialog.organization

  if (!organization) {
    return
  }

  pendingOrganizationId.value = organization.id

  try {
    await deactivateAdminOrganization(organization.id)
    toast.success('Организация деактивирована')
    deactivateDialog.open = false
    deactivateDialog.organization = null
    await loadPage()
  } catch (error) {
    toast.error('Не удалось деактивировать организацию', {
      description: getApiErrorMessage(error),
    })
  } finally {
    pendingOrganizationId.value = null
  }
}

async function openManagers(organization: OrganizationResponse): Promise<void> {
  selectedOrganization.value = organization
  selectedUserId.value = ''
  isManagersOpen.value = true

  try {
    managers.value = await getAdminOrganizationManagers(organization.id)
  } catch (error) {
    toast.error('Не удалось загрузить менеджеров', {
      description: getApiErrorMessage(error),
    })
  }
}

async function assignManager(): Promise<void> {
  if (!selectedOrganization.value || !selectedUserId.value) {
    return
  }

  try {
    await assignAdminOrganizationManager(selectedOrganization.value.id, {
      userId: Number(selectedUserId.value),
    })

    toast.success('Менеджер назначен')
    selectedUserId.value = ''
    managers.value = await getAdminOrganizationManagers(selectedOrganization.value.id)
  } catch (error) {
    toast.error('Не удалось назначить менеджера', {
      description: getApiErrorMessage(error),
    })
  }
}

async function removeManager(userId: number): Promise<void> {
  if (!selectedOrganization.value) {
    return
  }

  try {
    await removeAdminOrganizationManager(selectedOrganization.value.id, userId)
    toast.success('Менеджер снят')
    managers.value = await getAdminOrganizationManagers(selectedOrganization.value.id)
  } catch (error) {
    toast.error('Не удалось снять менеджера', {
      description: getApiErrorMessage(error),
    })
  }
}

onMounted(loadPage)
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
            Создание организаций и назначение менеджеров.
          </p>
        </div>

        <div class="flex flex-col gap-3 sm:flex-row">
          <div class="relative w-full sm:w-72">
            <Search class="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input v-model="search" class="pl-9" placeholder="Поиск организации" />
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
                <DialogTitle>Новая организация</DialogTitle>
                <DialogDescription>
                  После создания можно назначить менеджеров.
                </DialogDescription>
              </DialogHeader>

              <form class="space-y-4" @submit.prevent="createOrganization">
                <div class="space-y-2">
                  <Label for="name">Название</Label>
                  <Input id="name" v-model="form.name" />
                </div>

                <div class="space-y-2">
                  <Label for="contactEmail">Контактный email</Label>
                  <Input id="contactEmail" v-model="form.contactEmail" type="email" />
                </div>

                <div class="space-y-2">
                  <Label for="description">Описание</Label>
                  <Textarea id="description" v-model="form.description" class="min-h-24 resize-none" />
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
              <p>Создана: {{ formatDateTime(organization.createdAt) }}</p>
            </div>

            <div class="flex flex-wrap gap-2 pt-2">
              <Button size="sm" variant="outline" @click="openManagers(organization)">
                <UserPlus class="mr-2 size-4" />
                Менеджеры
              </Button>

              <Button size="sm" variant="outline" @click="openEditDialog(organization)">
                <Pencil class="mr-2 size-4" />
                Редактировать
              </Button>

              <Button
                  v-if="organization.active"
                  :disabled="pendingOrganizationId === organization.id"
                  size="sm"
                  variant="outline"
                  @click="openDeactivateDialog(organization)"
              >
                Деактивировать
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>

      <Dialog v-model:open="isEditOpen">
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Редактировать организацию</DialogTitle>
            <DialogDescription>
              Измените данные организации и ее активность.
            </DialogDescription>
          </DialogHeader>

          <form class="space-y-4" @submit.prevent="updateOrganization">
            <div class="space-y-2">
              <Label for="edit-organization-name">Название</Label>
              <Input id="edit-organization-name" v-model="editForm.name" />
            </div>

            <div class="space-y-2">
              <Label for="edit-organization-email">Контактный email</Label>
              <Input id="edit-organization-email" v-model="editForm.contactEmail" type="email" />
            </div>

            <div class="space-y-2">
              <Label for="edit-organization-description">Описание</Label>
              <Textarea
                  id="edit-organization-description"
                  v-model="editForm.description"
                  class="min-h-24 resize-none"
              />
            </div>

            <div class="space-y-2">
              <Label for="edit-organization-active">Статус</Label>
              <Select v-model="editForm.active">
                <SelectTrigger id="edit-organization-active">
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

      <Dialog v-model:open="isManagersOpen">
        <DialogContent class="sm:max-w-2xl">
          <DialogHeader>
            <DialogTitle>
              Менеджеры: {{ selectedOrganization?.name }}
            </DialogTitle>
            <DialogDescription>
              Назначайте пользователей менеджерами организации.
            </DialogDescription>
          </DialogHeader>

          <div class="space-y-5">
            <div class="grid gap-3 sm:grid-cols-[1fr_auto]">
              <Select v-model="selectedUserId">
                <SelectTrigger>
                  <SelectValue placeholder="Выберите пользователя" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem v-for="user in enabledUsers" :key="user.id" :value="String(user.id)">
                    {{ user.lastName }} {{ user.firstName }} · {{ user.email }}
                  </SelectItem>
                </SelectContent>
              </Select>

              <Button :disabled="!selectedUserId" @click="assignManager">
                Назначить
              </Button>
            </div>

            <div v-if="managers.length === 0" class="rounded-md border border-dashed p-6 text-sm text-muted-foreground">
              Менеджеры еще не назначены.
            </div>

            <div v-else class="space-y-2">
              <div
                  v-for="manager in managers"
                  :key="manager.id"
                  class="flex flex-col gap-3 rounded-md border p-3 sm:flex-row sm:items-center sm:justify-between"
              >
                <div class="min-w-0">
                  <p class="font-medium">
                    {{ manager.userLastName }} {{ manager.userFirstName }}
                  </p>
                  <p class="text-sm text-muted-foreground">
                    {{ manager.userEmail }}
                  </p>
                </div>

                <div class="flex items-center gap-2">
                  <Badge :variant="manager.active ? 'default' : 'secondary'">
                    {{ manager.active ? 'Активен' : 'Снят' }}
                  </Badge>

                  <Button
                      v-if="manager.active"
                      size="sm"
                      variant="outline"
                      @click="removeManager(manager.userId)"
                  >
                    Снять
                  </Button>
                </div>
              </div>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <AlertDialog v-model:open="deactivateDialog.open">
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Деактивировать организацию?</AlertDialogTitle>
            <AlertDialogDescription>
              Организация {{ deactivateDialog.organization?.name }} станет недоступна для новых мероприятий.
            </AlertDialogDescription>
          </AlertDialogHeader>

          <AlertDialogFooter>
            <AlertDialogCancel>
              Отмена
            </AlertDialogCancel>
            <AlertDialogAction
                :disabled="deactivateDialog.organization ? pendingOrganizationId === deactivateDialog.organization.id : false"
                @click="deactivateOrganization"
            >
              Деактивировать
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>

    </section>
  </AppLayout>
</template>