<script setup>
import { TeacherRoutes } from '@/router/teacherRoute'
import DefaultLayout from './base/DefaultLayout.vue'
</script>

<template>
  <DefaultLayout>
    <template v-for="o in TeacherRoutes[0].children" :key="o.name">
      <a-menu-item v-if="o.meta?.selectedKey" :key="o.meta.selectedKey">
        <router-link :to="{ name: o.name }">
          <component :is="o.meta.icon" />
          <span>{{ o.meta.name }}</span>
        </router-link>
      </a-menu-item>
    </template>
  </DefaultLayout>
</template>
