import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from './stores/auth';
import Shell from './views/shell/Shell.vue';
import Login from './views/login/Login.vue';
import Today from './views/today/Today.vue';
import Ledger from './views/ledger/Ledger.vue';
import Habits from './views/habits/Habits.vue';
import Fitness from './views/fitness/Fitness.vue';
import Schedule from './views/schedule/Schedule.vue';
import Buy from './views/buy/Buy.vue';
import Media from './views/media/Media.vue';
import Settings from './views/settings/Settings.vue';
import More from './views/more/More.vue';
import Archive from './views/archive/Archive.vue';

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: Login, meta: { public: true } },
    {
      path: '/',
      component: Shell,
      children: [
        { path: '', redirect: '/今日' },
        { path: '今日', component: Today },
        { path: '记账', component: Ledger },
        { path: '习惯', component: Habits },
        { path: '健身', component: Fitness },
        { path: '日程', component: Schedule },
        { path: '待买', component: Buy },
        { path: '收藏', component: Media },
        { path: '设置', component: Settings },
        { path: '时光档案', component: Archive },
        { path: '更多', component: More },
      ],
    },
  ],
});

function decodedPath(path: string) {
  try {
    return decodeURIComponent(path);
  } catch {
    return path;
  }
}

router.beforeEach((to) => {
  const path = decodedPath(to.path);
  if (path !== to.path) return { path, query: to.query, hash: to.hash, replace: true };

  const auth = useAuthStore();
  if (to.meta.public) {
    if (auth.token && to.path === '/login') return '/今日';
    return true;
  }
  if (!auth.token) return '/login';
  return true;
});
