<template>
  <div class="upload">
      <el-upload :file-list="fileList" :action="upload_url">
          <el-button size="small" type="primary">点击上传</el-button>
      </el-upload>
  </div>
</template>

<script>
export default {
    data() {
        return {
            fileList: [],
            upload_url: "http://localhost:8090/api/upload"
        }
    },
}
</script>

<style>

</style>