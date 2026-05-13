<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import {
  CalendarDays,
  Gift,
  LayoutDashboard,
  LogOut,
  Medal,
  ShieldCheck,
  Store,
  UsersRound,
  WalletCards,
} from 'lucide-vue-next'

import { useAuthStore } from '@/entities/auth/model/auth.store'
import type { RoleName } from '@/entities/auth/types'
import { Button } from '@/components/ui/button'
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
} from '@/components/ui/sidebar'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const roleLabels: Record<RoleName, string> = {
  STUDENT: 'Студент',
  ORG_MANAGER: 'Менеджер',
  ADMIN: 'Администратор',
}

const navByRole = {
  STUDENT: [
    { title: 'Обзор', shortTitle: 'Обзор', to: '/student', icon: LayoutDashboard },
    { title: 'Мероприятия', shortTitle: 'События', to: '/student/events', icon: CalendarDays },
    { title: 'Мои события', shortTitle: 'Мои', to: '/student/my-events', icon: Medal },
    { title: 'Баллы', shortTitle: 'Баллы', to: '/student/points', icon: WalletCards },
    { title: 'Награды', shortTitle: 'Награды', to: '/student/rewards', icon: Gift },
  ],
  ORG_MANAGER: [
    { title: 'Обзор', shortTitle: 'Обзор', to: '/manager', icon: LayoutDashboard },
    { title: 'Организации', shortTitle: 'Орг.', to: '/manager/organizations', icon: Store },
    { title: 'Мероприятия', shortTitle: 'События', to: '/manager/events', icon: CalendarDays },
    { title: 'Посещаемость', shortTitle: 'Учет', to: '/manager/attendance', icon: UsersRound },
  ],
  ADMIN: [
    { title: 'Обзор', shortTitle: 'Обзор', to: '/admin', icon: LayoutDashboard },
    { title: 'Организации', shortTitle: 'Орг.', to: '/admin/organizations', icon: Store },
    { title: 'Пользователи', shortTitle: 'Люди', to: '/admin/users', icon: UsersRound },
    { title: 'Награды', shortTitle: 'Награды', to: '/admin/rewards', icon: Gift },
    { title: 'Заявки', shortTitle: 'Заявки', to: '/admin/purchases', icon: ShieldCheck },
  ],
} satisfies Record<RoleName, Array<{
  title: string
  shortTitle: string
  to: string
  icon: typeof LayoutDashboard
}>>

const activeRoleLabel = computed(() => (
    authStore.activeRole ? roleLabels[authStore.activeRole] : 'Роль не выбрана'
))

const userName = computed(() => {
  if (!authStore.user) {
    return 'Пользователь'
  }

  return `${authStore.user.firstName} ${authStore.user.lastName}`
})

const userInitials = computed(() => {
  if (!authStore.user) {
    return 'EH'
  }

  return `${authStore.user.firstName[0] ?? ''}${authStore.user.lastName[0] ?? ''}`.toUpperCase()
})

const navigationItems = computed(() => {
  if (!authStore.activeRole) {
    return []
  }

  return navByRole[authStore.activeRole]
})

function isActive(path: string): boolean {
  const rootPaths = ['/student', '/manager', '/admin']

  if (rootPaths.includes(path)) {
    return route.path === path
  }

  return route.path === path || route.path.startsWith(`${path}/`)
}

async function logout(): Promise<void> {
  authStore.logout()
  await router.push('/login')
}
</script>

<template>
  <SidebarProvider>
    <Sidebar class="hidden border-r lg:flex">
      <SidebarHeader>
        <RouterLink to="/" class="flex items-center gap-2 px-2 py-1.5">
          <div class="flex size-8 items-center justify-center rounded-md bg-primary text-sm font-semibold text-primary-foreground">
            EH
          </div>
          <div class="min-w-0">
            <div class="truncate text-sm font-semibold">
              EventHub
            </div>
            <div class="truncate text-xs text-muted-foreground">
              {{ activeRoleLabel }}
            </div>
          </div>
        </RouterLink>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>Навигация</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarMenuItem v-for="item in navigationItems" :key="item.to">
                <SidebarMenuButton as-child :is-active="isActive(item.to)">
                  <RouterLink :to="item.to">
                    <component :is="item.icon" />
                    <span>{{ item.title }}</span>
                  </RouterLink>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>

      <SidebarFooter>
        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <Button class="h-auto w-full justify-start gap-3 px-2 py-2" variant="ghost">
              <span class="flex size-8 shrink-0 items-center justify-center rounded-md bg-muted text-xs font-semibold">
                {{ userInitials }}
              </span>
              <span class="min-w-0 text-left">
                <span class="block truncate text-sm">{{ userName }}</span>
                <span class="block truncate text-xs text-muted-foreground">{{ authStore.user?.email }}</span>
              </span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" class="w-56">
            <DropdownMenuLabel>
              {{ activeRoleLabel }}
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem v-if="authStore.hasMultipleRoles" @click="router.push('/select-role')">
              Сменить роль
            </DropdownMenuItem>
            <DropdownMenuItem @click="logout">
              <LogOut class="mr-2 size-4" />
              Выйти
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </SidebarFooter>
    </Sidebar>

    <SidebarInset class="min-w-0">
      <header class="sticky top-0 z-20 flex h-14 items-center justify-between border-b bg-background/95 px-4 backdrop-blur lg:hidden">
        <RouterLink to="/" class="flex items-center gap-2">
          <div class="flex size-8 items-center justify-center rounded-md bg-primary text-xs font-semibold text-primary-foreground">
            EH
          </div>
          <div class="min-w-0">
            <div class="truncate text-sm font-semibold">
              EventHub
            </div>
            <div class="truncate text-xs text-muted-foreground">
              {{ activeRoleLabel }}
            </div>
          </div>
        </RouterLink>

        <DropdownMenu>
          <DropdownMenuTrigger as-child>
            <Button class="size-9 rounded-full p-0" variant="outline">
              {{ userInitials }}
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" class="w-56">
            <DropdownMenuLabel>
              <span class="block truncate">{{ userName }}</span>
              <span class="block truncate text-xs font-normal text-muted-foreground">
                {{ authStore.user?.email }}
              </span>
            </DropdownMenuLabel>
            <DropdownMenuSeparator />
            <DropdownMenuItem v-if="authStore.hasMultipleRoles" @click="router.push('/select-role')">
              Сменить роль
            </DropdownMenuItem>
            <DropdownMenuItem @click="logout">
              <LogOut class="mr-2 size-4" />
              Выйти
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </header>

      <main class="min-h-screen bg-muted/25 pb-20 lg:min-h-screen lg:pb-0">
        <slot />
      </main>

      <nav class="fixed inset-x-0 bottom-0 z-30 border-t bg-background/95 px-2 pb-[max(env(safe-area-inset-bottom),0.5rem)] pt-2 backdrop-blur lg:hidden">
        <div class="mx-auto grid max-w-md grid-cols-4 gap-1" :class="{ 'grid-cols-5': navigationItems.length === 5 }">
          <RouterLink
              v-for="item in navigationItems"
              :key="item.to"
              :to="item.to"
              class="flex min-w-0 flex-col items-center justify-center gap-1 rounded-md px-2 py-2 text-xs text-muted-foreground transition-colors"
              :class="{ 'bg-muted text-foreground': isActive(item.to) }"
          >
            <component :is="item.icon" class="size-5 shrink-0" />
            <span class="max-w-full truncate">{{ item.shortTitle }}</span>
          </RouterLink>
        </div>
      </nav>
    </SidebarInset>
  </SidebarProvider>
</template>