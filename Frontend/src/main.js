import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'
import Multiselect from 'vue-multiselect'

import 'ant-design-vue/dist/reset.css'
import 'vue3-toastify/dist/index.css'
import './assets/css/app.scss'
import { pinia } from './stores'

const app = createApp(App)
app.use(pinia)
app.component('VueMultiselect', Multiselect)
app.use(Antd)
app.use(router)
app.mount('#app')
