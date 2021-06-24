import axios from 'axios'
import qs from 'qs'

let baseURL
if (process.env.NODE_ENV === 'development') {
    // baseURL = '/api'
    baseURL = 'http://localhost:8090/api'
} else {
    baseURL = ''
}

axios.defaults.timeout = 6000
axios.defaults.baseURL = baseURL
axios.defaults.responseType = "json"
axios.defaults.withCredentials = false

export const Axios = axios.create({
    baseURL:baseURL,
    timeout:6000,
    responseType:"json",
    withCredentials:false
});

Axios .interceptors.request.use((config) => {
    if (config.method == "post" || config.method == "put") {
        //config.data = qs.stringify(config.data)
    } 
    return config
},(error) => {
    return Promise.reject(error)
})

Axios .interceptors.response.use((res) => {
    if (res.status == 200) {
        return Promise.resolve(res)
    }
    return res
}, (error) => {
    return Promise.reject(error)
})
