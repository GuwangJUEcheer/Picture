<template>
  <div id="basicLayout">
    <a-layout style="min-height:100vh;">
      <a-layout-header class="header">
        <GlobalHeader/>
      </a-layout-header>
      <a-layout-content class="content">
        <RouterView/>
      </a-layout-content>
      <a-layout-footer class="footer">
      <a href="https://www.google.co.jp">有问题 就Google</a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import {RouterView } from 'vue-router';
import GlobalHeader from '../components/headers/GlobalHeader.vue';

</script>

<style scoped>
#basicLayout .header{
  background-color: white;
  padding-inline: 20px;
  color:unset;
  margin-bottom: 16px;
}

#basicLayout .content{
  margin: 10px 2% 2% 4px;
  padding-bottom: 50px;
}

#basicLayout .footer{
 background-color: #efefef;
 padding:16px;
 /*
   让footer固定到底部
 */
 position: fixed;
  margin-top: 4px;
 bottom: 0;
 left: 0;
 right: 0;
 text-align: center;
}
</style>
