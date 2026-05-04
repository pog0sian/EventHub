<script setup lang="ts">
import * as z from 'zod'
import type { FormSubmitEvent, AuthFormField } from '@nuxt/ui'

const toast = useToast()

const fields: AuthFormField[] = [
  {
    name: 'email',
    type: 'email',
    label: 'Email',
    placeholder: 'Введите ваш email',
    required: true
  },
  {
    name: 'password',
    label: 'Пароль',
    type: 'password',
    placeholder: 'Введите ваш пароль',
    required: true
  }
]

const schema = z.object({
  email: z.email('Некорректный email'),
  password: z.string('Пароль обязателен').min(8, 'Пароль должен быть не менее 8 символов')
})

type Schema = z.output<typeof schema>

</script>

<template>
  <div class="flex flex-col items-center justify-center gap-4 p-4">
    <UPageCard class="w-full max-w-md">
      <UAuthForm
          :schema="schema"
          title="Вход"
          description="Введите данные для входа"
          icon="i-lucide-user"
          :fields="fields"
          :submit="{
            label: 'Войти'
          }"
          @submit="onSubmit"
      />
    </UPageCard>
  </div>
</template>

<style scoped>

</style>