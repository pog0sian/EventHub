import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import LoginPage from '../../pages/auth/LoginPage.vue';

const routes: Array<RouteRecordRaw> = [
    {
        path: '/login',
        component: LoginPage
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router;