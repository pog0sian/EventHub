import { computed, ref } from 'vue'
import { defineStore } from 'pinia'

import { getCurrentUser, login, register } from '@/entities/auth/api'
import type {
    AuthResponse,
    LoginRequest,
    RegisterRequest,
    RoleName,
    UserResponse,
} from '@/entities/auth/types'
import { authStorage, isUnauthorizedError } from '@/shared/api/client'

function pickInitialRole(roles: RoleName[], preferredRole: RoleName | null): RoleName | null {
    if (preferredRole && roles.includes(preferredRole)) {
        return preferredRole
    }

    return roles[0] ?? null
}

export const useAuthStore = defineStore('auth', () => {
    const user = ref<UserResponse | null>(null)
    const roles = ref<RoleName[]>([])
    const activeRole = ref<RoleName | null>(authStorage.getActiveRole())
    const isSessionReady = ref(false)
    const isLoading = ref(false)

    const isAuthenticated = computed(() => Boolean(user.value && authStorage.getToken()))
    const hasMultipleRoles = computed(() => roles.value.length > 1)

    function applyAuth(response: AuthResponse): void {
        authStorage.setToken(response.accessToken)

        user.value = response.user
        roles.value = response.roles

        const nextRole = pickInitialRole(response.roles, activeRole.value)

        if (nextRole) {
            setActiveRole(nextRole)
        } else {
            clearActiveRole()
        }
    }

    function setActiveRole(role: RoleName): void {
        if (!roles.value.includes(role)) {
            return
        }

        activeRole.value = role
        authStorage.setActiveRole(role)
    }

    function clearActiveRole(): void {
        activeRole.value = null
        authStorage.clearActiveRole()
    }

    function clearSession(): void {
        user.value = null
        roles.value = []
        activeRole.value = null
        authStorage.clear()
    }

    async function loginWithEmail(payload: LoginRequest): Promise<void> {
        isLoading.value = true

        try {
            const response = await login(payload)
            applyAuth(response)
        } finally {
            isLoading.value = false
        }
    }

    async function registerStudent(payload: RegisterRequest): Promise<void> {
        isLoading.value = true

        try {
            const response = await register(payload)
            applyAuth(response)
        } finally {
            isLoading.value = false
        }
    }

    async function restoreSession(): Promise<void> {
        if (!authStorage.getToken()) {
            isSessionReady.value = true
            return
        }

        isLoading.value = true

        try {
            const response = await getCurrentUser()

            user.value = response.user
            roles.value = response.roles

            const restoredRole = pickInitialRole(response.roles, response.activeRole ?? activeRole.value)

            if (restoredRole) {
                setActiveRole(restoredRole)
            } else {
                clearActiveRole()
            }
        } catch (error) {
            if (isUnauthorizedError(error)) {
                clearSession()
            }

            throw error
        } finally {
            isLoading.value = false
            isSessionReady.value = true
        }
    }

    function logout(): void {
        clearSession()
    }

    return {
        user,
        roles,
        activeRole,
        isSessionReady,
        isLoading,
        isAuthenticated,
        hasMultipleRoles,
        loginWithEmail,
        registerStudent,
        restoreSession,
        setActiveRole,
        clearSession,
        logout,
    }
})
