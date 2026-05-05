<script setup lang="ts">
import * as z from 'zod'
import type { FormSubmitEvent, AuthFormField } from '@nuxt/ui'

const fields: AuthFormField[] = [
  {
    name: 'email',
    type: 'email',
    label: 'Email',
    placeholder: 'Введите ваш email',
    required: true
  },
  {
    name: 'lastName',
    type: 'text',
    label: 'Фамилия',
    placeholder: 'Введите вашу фамилию',
    required: true
  },
  {
    name: 'firstName',
    type: 'text',
    label: 'Имя',
    placeholder: 'Введите ваше имя',
    required: true
  },
  {
    name: 'middleName',
    type: 'text',
    label: 'Отчество',
    placeholder: 'Введите ваше отчество',
    required: false
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
  lastName: z.string('Фамилия обязательна').min(2, 'Фамилия должна быть не менее 2 символов'),
  firstName: z.string('Имя обязательно').min(2, 'Имя должно быть не менее 2 символов'),
  middleName: z.string().optional(),
  password: z.string('Пароль обязателен').min(8, 'Пароль должен быть не менее 8 символов')
})

type Schema = z.output<typeof schema>

function onSubmit(payload: FormSubmitEvent<Schema>) {
  console.log('Submitted', payload)
}
</script>

<template>
  <div class="flex min-h-screen flex-col items-center justify-center gap-4 p-4">
    <UPageCard class="w-full max-w-md">
      <UAuthForm
        :schema="schema"
        title="Регистрация"
        icon="i-lucide-user-plus"
        :fields="fields"
        :submit="{
          label: 'Зарегистрироваться'
        }"
        @submit="onSubmit"
      >
        <template #description>
          Уже есть аккаунт? <ULink to="/login" class="text-primary font-medium">Войдите</ULink>
        </template>
      </UAuthForm>
    </UPageCard>
  </div>
</template>

<style scoped>

</style>