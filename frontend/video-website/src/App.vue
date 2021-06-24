<template>
  <el-container id="app">
    <el-header>
      <Header/>
    </el-header>
    <el-main>
    <transition name="fade">
      <router-view/>
    </transition>
    </el-main>
  </el-container>
</template>

<script>
import Header from "@/components/Header";
export default {
  name: 'App',
  components:{
    Header,
  }
}
</script>

<style>

</style>
