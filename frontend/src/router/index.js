import { createRouter, createWebHistory } from 'vue-router';
import Login from '../views/Login.vue';
import Register from '../views/Register.vue';
import PostList from '../views/PostList.vue';
import Profile from '../views/Profile.vue';

// 需要登入才能進入的路由共用守衛
const authGuard = (to, from, next) => {
  if (!localStorage.getItem('jwt')) next('/login'); // 沒有 JWT 就導向登入
  else next();
};

const routes = [
  {
    path: '/',
    // 根路徑依據是否已登入，分別導向貼文列表或登入頁
    redirect: () => (localStorage.getItem('jwt') ? '/posts' : '/login'),
  },
  { path: '/login', component: Login },
  { path: '/register', component: Register },
  { path: '/posts', component: PostList, beforeEnter: authGuard },
  { path: '/profile', component: Profile, beforeEnter: authGuard },
  // 其他未定義路徑一律導回首頁
  { path: '/:pathMatch(.*)*', redirect: '/' },
];

export default createRouter({
  // createWebHistory：使用真實 URL（/posts）而非 Hash 模式（/#/posts）
  history: createWebHistory(),
  routes,
});
