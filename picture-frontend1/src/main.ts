import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import App from './App.vue'
import router from './router'
import V3waterfall from 'v3-waterfall'
import 'v3-waterfall/dist/style.css'
import './access.ts'
import VueCropper from 'vue-cropper';
import 'vue-cropper/dist/index.css'

const app = createApp(App)
app.use(Antd)
app.use(createPinia())
app.use(router)
app.use(V3waterfall)
app.use(VueCropper)
app.mount('#app')


