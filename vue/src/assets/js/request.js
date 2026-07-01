import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js";

const request = axios.create({
    baseURL: 'http://localhost:8083',
    timeout: 30000  // 后台接口超时时间
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    // 如果是文件上传（FormData），不设置Content-Type，让浏览器自动设置
    if (!(config.data instanceof FormData)) {
        config.headers['Content-Type'] = 'application/json;charset=utf-8';
    }
    let user = JSON.parse(localStorage.getItem("code_user") || '{}')
    config.headers['token'] = user.token
    return config
}, error => {
    return Promise.reject(error)
});

// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        let res = response.data;
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        let message = '请求失败'

        if (error.response) {
            const status = error.response.status
            const data = error.response.data

            switch(status) {
                case 401:
                    message = '未授权，请重新登录'
                    localStorage.removeItem('code_user')
                    router.push('/login')
                    break
                case 403:
                    message = '拒绝访问'
                    break
                case 404:
                    message = '请求地址不存在'
                    break
                case 500:
                    message = data?.message || '服务器内部错误'
                    break
                default:
                    message = data?.message || `请求失败(${status})`
            }
        } else if (error.request) {
            message = '网络超时，请检查网络连接'
        } else {
            message = error.message || '未知错误'
        }

        ElMessage.error(message)
        console.error('请求错误:', error)
        return Promise.reject(error)
    }
)

export default request
