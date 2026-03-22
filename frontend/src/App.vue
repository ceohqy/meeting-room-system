<template>
  <router-view v-slot="{ Component, route }">
    <transition name="page" mode="out-in">
      <component :is="Component" :key="route.path" />
    </transition>
  </router-view>
</template>

<style>
.page-enter-active { animation: pageEnter 0.35s cubic-bezier(0.4,0,0.2,1); }
.page-leave-active { animation: pageLeave 0.2s cubic-bezier(0.4,0,0.2,1); }
@keyframes pageEnter {
  from { opacity: 0; transform: translateY(10px); }
  to   { opacity: 1; transform: translateY(0); }
}
@keyframes pageLeave {
  from { opacity: 1; }
  to   { opacity: 0; }
}
</style>
