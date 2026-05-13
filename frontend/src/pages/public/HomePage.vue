<script setup lang="ts">
import { CalendarDays, Gift, ShieldCheck, UsersRound } from 'lucide-vue-next'
import { RouterLink } from 'vue-router'

import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'

const roles = [
  {
    icon: CalendarDays,
    title: 'Студентам',
    text: 'Находите мероприятия, записывайтесь, посещайте события и получайте баллы.',
  },
  {
    icon: UsersRound,
    title: 'Менеджерам',
    text: 'Создавайте события организации, управляйте регистрациями и отмечайте посещаемость.',
  },
  {
    icon: ShieldCheck,
    title: 'Администраторам',
    text: 'Управляйте организациями, пользователями, наградами и заявками на выдачу.',
  },
]
</script>

<template>
  <main class="min-h-screen bg-background text-foreground">
    <section class="border-b">
      <div class="mx-auto flex max-w-6xl items-center justify-between px-6 py-5">
        <RouterLink to="/" class="text-xl font-semibold">
          ИвентХаб
        </RouterLink>

        <nav class="flex items-center gap-2">
          <Button as-child variant="ghost">
            <RouterLink to="/login">
              Войти
            </RouterLink>
          </Button>
          <Button as-child>
            <RouterLink to="/register">
              Регистрация
            </RouterLink>
          </Button>
        </nav>
      </div>
    </section>

    <section class="mx-auto grid max-w-6xl gap-10 px-6 py-16 lg:grid-cols-[1.1fr_0.9fr] lg:items-center">
      <div class="space-y-7">
        <Badge variant="secondary" class="w-fit">
          Университетская платформа мероприятий
        </Badge>

        <div class="space-y-5">
          <h1 class="max-w-3xl text-4xl font-semibold tracking-normal sm:text-5xl">
            ИвентХаб объединяет события, посещаемость, баллы и награды
          </h1>
          <p class="max-w-2xl text-lg leading-8 text-muted-foreground">
            Студенты записываются на мероприятия и получают баллы, менеджеры ведут события организаций, а администраторы управляют всей экосистемой.
          </p>
        </div>

        <div class="flex flex-col gap-3 sm:flex-row">
          <Button as-child size="lg">
            <RouterLink to="/register">
              Начать
            </RouterLink>
          </Button>
          <Button as-child size="lg" variant="outline">
            <RouterLink to="/login">
              Войти в кабинет
            </RouterLink>
          </Button>
        </div>
      </div>

      <Card class="rounded-lg">
        <CardHeader>
          <CardTitle class="flex items-center gap-2">
            <Gift class="size-5" />
            Как работает система
          </CardTitle>
        </CardHeader>
        <CardContent class="space-y-4 text-sm text-muted-foreground">
          <div class="rounded-md border p-4">
            Организации публикуют мероприятия и задают награду в баллах.
          </div>
          <div class="rounded-md border p-4">
            Студенты регистрируются, приходят на событие и получают баллы после отметки.
          </div>
          <div class="rounded-md border p-4">
            Баллы можно обменять на мерч и другие университетские награды.
          </div>
        </CardContent>
      </Card>
    </section>

    <section class="border-t bg-muted/35">
      <div class="mx-auto grid max-w-6xl gap-4 px-6 py-12 md:grid-cols-3">
        <Card v-for="role in roles" :key="role.title" class="rounded-lg">
          <CardHeader>
            <component :is="role.icon" class="mb-2 size-5 text-primary" />
            <CardTitle class="text-base">
              {{ role.title }}
            </CardTitle>
          </CardHeader>
          <CardContent class="text-sm leading-6 text-muted-foreground">
            {{ role.text }}
          </CardContent>
        </Card>
      </div>
    </section>
  </main>
</template>