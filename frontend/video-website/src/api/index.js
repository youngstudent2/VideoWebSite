import {Axios} from "./axios"

export function getAllVideos() {
    return Axios({
        url:"/videos",
        method:"get"
    })
}

export function deleteVideo(name) {
    return Axios({
        url:"/video/" + name,
        method: "delete"
    })
}

export function downloadVideo(name, params) {
    window.open(`${Axios.defaults.baseURL}/video/${name}?resolution=${params['resolution']}`)
}