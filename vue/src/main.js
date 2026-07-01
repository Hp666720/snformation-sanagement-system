import './assets/css/global.css'

import ElementPlus from 'element-plus'

import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import 'element-plus/dist/index.css'

const app = createApp(App)
app.use(router)
app.mount('#app')

app.use(ElementPlus,{
    locale:zhCn
})
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}