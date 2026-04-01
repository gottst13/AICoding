import Vue from 'vue'
import VueRouter from 'vue-router'
import ZoneManagement from '@/views/parking/ZoneManagement.vue'
import SpaceManagement from '@/views/parking/SpaceManagement.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'ZoneManagement',
    component: ZoneManagement
  },
  {
    path: '/spaces',
    name: 'SpaceManagement',
    component: SpaceManagement
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
