import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";

import HomePage from '../../pages/public/HomePage.vue'
import LoginPage from '../../pages/public/LoginPage.vue';
import RegisterPage from '../../pages/public/RegisterPage.vue'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        component: HomePage
    },
    {
        path: '/login',
        component: LoginPage
    },
    {
        path: '/register',
        component: RegisterPage
    },

]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router;