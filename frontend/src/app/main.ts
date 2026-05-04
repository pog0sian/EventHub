import '../style.css'

import { createApp } from 'vue'
import router from "./router";
import ui from '@nuxt/ui/vue-plugin'
import App from './App.vue'

const app = createApp(App)

app.use(ui)
app.use(router)

app.mount('#app')
