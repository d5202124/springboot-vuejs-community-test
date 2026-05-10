<template>
  <div class="app-container">
    <header>
      <!-- 點擊標題回首頁，已登入導向貼文列表否則導向登入頁 -->
      <h1 @click="goHome" class="logo">玉山社群</h1>
      <!-- 只有登入後才顯示使用者資訊列 -->
      <div v-if="userName" class="user-info">
        <span>{{ userName }}</span>
        <button @click="$router.push('/profile')">個人資料</button>
        <button @click="logout">登出</button>
      </div>
    </header>

    <!-- @loggedIn / @profileUpdated：子頁面更新後通知 App.vue 同步 header 名稱 -->
    <router-view @loggedIn="onLoggedIn" @profileUpdated="onProfileUpdated" />
  </div>
</template>

<script>
export default {
  data() {
    return {
      // 優先讀userName，其次讀userPhone，沒有則null
      userName: localStorage.getItem('userName') || localStorage.getItem('userPhone') || null,
    };
  },
  methods: {
    // 接收 Login.vue 的 loggedIn 事件，更新 header 顯示名稱
    onLoggedIn({ userName }) {
      this.userName = userName;
    },
    // 接收 Profile.vue 的 profileUpdated 事件，同步更新名稱
    onProfileUpdated({ userName }) {
      this.userName = userName;
    },
    goHome() {
      if (localStorage.getItem('jwt')) this.$router.push('/posts');
      else this.$router.push('/login');
    },
    // 登出：清除所有本地儲存的使用者資訊，導向登入頁
    logout() {
      localStorage.removeItem('jwt');
      localStorage.removeItem('userPhone');
      localStorage.removeItem('userId');
      localStorage.removeItem('userName');
      this.userName = null;
      this.$router.push('/login');
    },
  },
};
</script>

<style>
body {
  font-family: Arial, sans-serif;
}
.app-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}
header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  border-bottom: 1px solid #eee;
  padding-bottom: 16px;
}
.logo {
  cursor: pointer;
  margin: 0;
}
.user-info span {
  margin-right: 12px;
  font-weight: bold;
}
button {
  margin: 0 4px;
  cursor: pointer;
}
</style>
