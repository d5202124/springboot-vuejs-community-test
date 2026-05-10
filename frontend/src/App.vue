<template>
  <el-container class="layout">
    <el-header class="header">
      <span class="logo" @click="goHome">社群平台</span>
      <div v-if="userName" class="user-info">
        <span class="username">{{ userName }}</span>
        <el-button size="small" @click="$router.push('/profile')">個人資料</el-button>
        <el-button size="small" type="danger" plain @click="logout">登出</el-button>
      </div>
    </el-header>

    <el-main class="main">
      <!-- @loggedIn / @profileUpdated：子頁面更新後通知 App.vue 同步 header 名稱 -->
      <router-view @loggedIn="onLoggedIn" @profileUpdated="onProfileUpdated" />
    </el-main>
  </el-container>
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
* { box-sizing: border-box; }
body { margin: 0; font-family: Arial, sans-serif; background: #f4f6f8; font-size: 16px; }

.layout { min-height: 100vh; }

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 60px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.logo {
  font-size: 1.2rem;
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.username {
  font-size: 0.9rem;
  color: #606266;
  margin-right: 4px;
}

.main {
  width: 50%;
  margin: 0 auto;
  padding: 36px 0;
}
</style>
