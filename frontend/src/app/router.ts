import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/entities/auth/model/auth.store'
import type { RoleName } from '@/entities/auth/types'

declare module 'vue-router' {
    interface RouteMeta {
        requiresAuth?: boolean
        guestOnly?: boolean
        roles?: RoleName[]
    }
}

const routes: RouteRecordRaw[] = [
    {
        path: '/',
        name: 'home',
        component: () => import('@/pages/public/HomePage.vue'),
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@/pages/public/LoginPage.vue'),
        meta: { guestOnly: true },
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('@/pages/public/RegisterPage.vue'),
        meta: { guestOnly: true },
    },
    {
        path: '/select-role',
        name: 'select-role',
        component: () => import('@/pages/auth/SelectRolePage.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/student',
        name: 'student-dashboard',
        component: () => import('@/pages/student/StudentDashboardPage.vue'),
        meta: { requiresAuth: true, roles: ['STUDENT'] },
    },
    {
        path: '/student/events',
        name: 'student-events',
        component: () => import('@/pages/student/StudentEventsPage.vue'),
        meta: { requiresAuth: true, roles: ['STUDENT'] },
    },
    {
        path: '/student/my-events',
        name: 'student-my-events',
        component: () => import('@/pages/student/StudentMyEventsPage.vue'),
        meta: { requiresAuth: true, roles: ['STUDENT'] },
    },
    {
        path: '/student/points',
        name: 'student-points',
        component: () => import('@/pages/student/StudentPointsPage.vue'),
        meta: { requiresAuth: true, roles: ['STUDENT'] },
    },
    {
        path: '/student/rewards',
        name: 'student-rewards',
        component: () => import('@/pages/student/StudentRewardsPage.vue'),
        meta: { requiresAuth: true, roles: ['STUDENT'] },
    },
    {
        path: '/manager',
        name: 'manager-dashboard',
        component: () => import('@/pages/manager/ManagerDashboardPage.vue'),
        meta: { requiresAuth: true, roles: ['ORG_MANAGER'] },
    },
    {
        path: '/manager/organizations',
        name: 'manager-organizations',
        component: () => import('@/pages/manager/ManagerOrganizationsPage.vue'),
        meta: { requiresAuth: true, roles: ['ORG_MANAGER'] },
    },
    {
        path: '/manager/events',
        name: 'manager-events',
        component: () => import('@/pages/manager/ManagerEventsPage.vue'),
        meta: { requiresAuth: true, roles: ['ORG_MANAGER'] },
    },
    {
        path: '/manager/attendance',
        name: 'manager-attendance',
        component: () => import('@/pages/manager/ManagerAttendancePage.vue'),
        meta: { requiresAuth: true, roles: ['ORG_MANAGER'] },
    },
    {
        path: '/admin',
        name: 'admin-dashboard',
        component: () => import('@/pages/admin/AdminDashboardPage.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
        path: '/admin/organizations',
        name: 'admin-organizations',
        component: () => import('@/pages/admin/AdminOrganizationsPage.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
        path: '/admin/users',
        name: 'admin-users',
        component: () => import('@/pages/admin/AdminUsersPage.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
        path: '/admin/rewards',
        name: 'admin-rewards',
        component: () => import('@/pages/admin/AdminRewardsPage.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
        path: '/admin/purchases',
        name: 'admin-purchases',
        component: () => import('@/pages/admin/AdminPurchasesPage.vue'),
        meta: { requiresAuth: true, roles: ['ADMIN'] },
    },
    {
        path: '/:pathMatch(.*)*',
        redirect: '/',
    },
]

function getRoleHome(role: RoleName): string {
    const homeByRole: Record<RoleName, string> = {
        STUDENT: '/student',
        ORG_MANAGER: '/manager',
        ADMIN: '/admin',
    }

    return homeByRole[role]
}

export const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach(async (to) => {
    const authStore = useAuthStore()

    if (!authStore.isSessionReady) {
        try {
            await authStore.restoreSession()
        } catch {
            return { name: 'login' }
        }
    }

    if (to.meta.guestOnly && authStore.isAuthenticated) {
        return authStore.activeRole ? getRoleHome(authStore.activeRole) : { name: 'select-role' }
    }

    if (to.meta.requiresAuth && !authStore.isAuthenticated) {
        return { name: 'login', query: { redirect: to.fullPath } }
    }

    if (to.name === 'select-role') {
        if (!authStore.isAuthenticated) {
            return { name: 'login' }
        }

        if (authStore.activeRole && !authStore.hasMultipleRoles) {
            return getRoleHome(authStore.activeRole)
        }

        return true
    }

    const allowedRoles = to.meta.roles

    if (allowedRoles?.length) {
        const activeRole = authStore.activeRole

        if (!activeRole) {
            return { name: 'select-role', query: { redirect: to.fullPath } }
        }

        if (!allowedRoles.includes(activeRole)) {
            return getRoleHome(activeRole)
        }
    }

    return true
})