<template>
  <div>
      <el-menu router 
            mode="horizontal"
            :default-active="activeIndex"
            @select="select"
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b">
          <el-menu-item index="/index">首页</el-menu-item>
          <el-menu-item index="/upload">上传</el-menu-item>
      </el-menu>
  </div>
</template>

<script>
export default {
    data() {
        return {
            activeIndex: "index",
        }
    },
    methods: {
        select(key, keyPath) {
            this.activeIndex = key;
        },
    }
}
</script>

<style>

</style>