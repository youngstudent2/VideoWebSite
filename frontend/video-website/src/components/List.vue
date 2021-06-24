<template>
  <div class="list">
      <el-row>
          <el-col :span="22" :offset="2">
              <el-table :data="videos" max-height="600" style="width:100%">
                  <el-table-column
                    prop="name" label="视频名" width="500"
                  ></el-table-column>
                  <el-table-column
                    prop="size" label="大小" width="100"
                  ></el-table-column>
                  <el-table-column
                    prop="lastModified" label="最近更改" width="180"
                  ></el-table-column>
                  <el-table-column
                    label="操作" width="400"
                  >
                    <template slot-scope="scope">
                        <el-button
                            @click.native.prevent="
                                downloadRow(scope.$index, videos, '360p')
                            "
                            type="text"
                            size="small"
                            >360p</el-button
                        >
                        <el-button
                            @click.native.prevent="
                                downloadRow(scope.$index, videos, '720p')
                            "
                            type="text"
                            size="small"
                            >720p</el-button
                        >
                        <el-button
                            @click.native.prevent="
                                downloadRow(scope.$index, videos, '1080p')
                            "
                            type="text"
                            size="small"
                            >1080p</el-button
                        >
                        <el-button
                            @click.native.prevent="
                                downloadRow(scope.$index, videos, 'origin')
                            "
                            type="text"
                            size="small"
                            >原画</el-button
                        >
                        <el-button
                            @click.native.prevent="
                                deleteRow(scope.$index, videos)
                            "
                            type="text"
                            size="small"
                            >删除</el-button
                        >
                    </template>
                  </el-table-column>
              </el-table>
          </el-col>

      </el-row>
  </div>
</template>

<script>
import {getAllVideos,deleteVideo,downloadVideo} from "@/api"
export default {
    data() {
        return {
            videos:[]
        }
    },
    mounted() {
        this.loadVideos()
    },
    methods: {
        deleteRow(row, data) {
            deleteVideo(data[row].name).then(res => {
                this.$message({
                    message:"成功删除",
                    type:"success"
                })
                this.loadVideos()
            })
        },
        downloadRow(row, data, resolution) {
            downloadVideo(data[row].name, {resolution})
        },
        loadVideos() {
            getAllVideos().then(res => {
                console.log(res)
                res["data"].forEach(element => {
                    element["size"] = (element["size"]>>20) + "MB"
                    element["lastModified"] = new Date(element["lastModified"]).toLocaleString()
                });
                this.videos = res["data"]
            })
        }
    }
}
</script>

<style>

</style>