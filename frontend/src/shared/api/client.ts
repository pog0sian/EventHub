import axios, { AxiosError } from 'axios'

import { API_BASE_URL } from '@/shared/config/env'
import type { ApiErrorResponse, RoleName } from '@/entities/auth/types'

const TOKEN_STORAGE_KEY = 'eventhub.accessToken'
const ACTIVE_ROLE_STORAGE_KEY = 'eventhub.activeRole'

function getCurrentLocation(): string {
    return `${window.location.pathname}${window.location.search}${window.location.hash}`
}

function isPublicAuthPage(): boolean {
    return window.location.pathname === '/login' || window.location.pathname === '/register'
}

function redirectToLogin(): void {
    if (isPublicAuthPage()) {
        return
    }

    const redirect = encodeURIComponent(getCurrentLocation())
    window.location.assign(`/login?redirect=${redirect}`)
}

export const authStorage = {
    tokenKey: TOKEN_STORAGE_KEY,
    activeRoleKey: ACTIVE_ROLE_STORAGE_KEY,

    getToken(): string | null {
        return localStorage.getItem(TOKEN_STORAGE_KEY)
    },

    setToken(token: string): void {
        localStorage.setItem(TOKEN_STORAGE_KEY, token)
    },

    clearToken(): void {
        localStorage.removeItem(TOKEN_STORAGE_KEY)
    },

    getActiveRole(): RoleName | null {
        return localStorage.getItem(ACTIVE_ROLE_STORAGE_KEY) as RoleName | null
    },

    setActiveRole(role: RoleName): void {
        localStorage.setItem(ACTIVE_ROLE_STORAGE_KEY, role)
    },

    clearActiveRole(): void {
        localStorage.removeItem(ACTIVE_ROLE_STORAGE_KEY)
    },

    clear(): void {
        this.clearToken()
        this.clearActiveRole()
    },
}

export const apiClient = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        Accept: 'application/json',
        'Content-Type': 'application/json',
    },
})

apiClient.interceptors.request.use((config) => {
    const token = authStorage.getToken()
    const activeRole = authStorage.getActiveRole()

    if (token) {
        config.headers.Authorization = `Bearer ${token}`
    }

    if (activeRole) {
        config.headers['X-Active-Role'] = activeRole
    }

    return config
})

apiClient.interceptors.response.use(
    (response) => response,
    (error: unknown) => {
        if (isUnauthorizedError(error)) {
            authStorage.clear()
            redirectToLogin()
        }

        return Promise.reject(error)
    },
)

export function getApiErrorMessage(error: unknown): string {
    if (axios.isAxiosError<ApiErrorResponse>(error)) {
        const data = error.response?.data

        if (data?.fieldErrors?.length) {
            return data.fieldErrors
                .map((fieldError) => `${fieldError.field}: ${fieldError.message}`)
                .join('; ')
        }

        return data?.message ?? error.message
    }

    if (error instanceof Error) {
        return error.message
    }

    return 'Неизвестная ошибка'
}

export function isUnauthorizedError(error: unknown): boolean {
    return axios.isAxiosError(error) && error.response?.status === 401
}

export type ApiAxiosError = AxiosError<ApiErrorResponse>