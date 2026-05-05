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

function onSubmit(payload: FormSubmitEvent<Schema>) {
  console.log('Submitted', payload)
}
</script>

<template>
  <div class="flex min-h-screen flex-col items-center justify-center gap-4 p-4">
    <UPageCard class="w-full max-w-md">
      <UAuthForm
          :schema="schema"
          title="C возвращением!"
          description="Введите данные для входа"
          icon="i-lucide-lock"
          :fields="fields"
          :submit="{
            label: 'Войти'
          }"
          @submit="onSubmit"
      >
        <template #description>
          Еще не аккаунта? <ULink to="/register" class="text-primary font-medium">Зарегистрируйтесь</ULink>
        </template>
        <template #footer>
          Выполняя вход, вы принимаете наши <ULink to="#" class="text-primary font-medium">Условиями использования</ULink>.
        </template>
      </UAuthForm>
    </UPageCard>
  </div>
</template>

<style scoped>

</style>