import { defineStore } from 'pinia';
import { fetchProfile, getToken, login as requestLogin, register as requestRegister, setToken, type Profile } from '../http/api';

export type { Profile };

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: getToken(),
    user: null as Profile | null,
  }),
  actions: {
    async login(email: string, password: string) {
      this.apply(await requestLogin(email, password));
    },
    async register(email: string, password: string, name: string) {
      this.apply(await requestRegister(email, password, name));
    },
    async fetchMe() {
      if (!this.token) return;
      this.user = await fetchProfile();
    },
    apply(result: { token: string; user: Profile }) {
      this.token = result.token;
      this.user = result.user;
      setToken(result.token);
    },
    logout() {
      this.token = '';
      this.user = null;
      setToken(null);
    },
  },
});
