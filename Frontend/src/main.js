import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import App from './App.vue'
import router from './router'
import Multiselect from 'vue-multiselect'

import 'ant-design-vue/dist/reset.css'
import 'vue3-toastify/dist/index.css'
import './assets/css/app.scss'

const app = createApp(App)
app.component('VueMultiselect', Multiselect)
app.use(router)
app.use(Antd)
app.use(createPinia())
app.mount('#app')
