<template>
  <div class="auth-box">
    <h2>登入</h2>
    <form @submit.prevent="login">
      <div>
        <label>手機號碼</label>
        <!-- 固定顯示「09」前綴，使用者只輸入後 8 碼 -->
        <div class="phone-row">
          <span class="phone-prefix">09</span>
          <input v-model="phoneSuffix" type="tel" maxlength="8" placeholder="12345678" required />
        </div>
      </div>

      <div>
        <label>密碼</label>
        <!-- showPassword 控制顯示或遮罩，type="button" 避免觸發表單提交 -->
        <div class="pw-row">
          <input v-model="form.password" :type="showPassword ? 'text' : 'password'" required />
          <button type="button" class="eye-btn" @click="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁' }}
          </button>
        </div>
      </div>

      <button type="submit">登入</button>
      <p v-if="message" class="error">{{ message }}</p>
    </form>
    <p>還沒有帳號？<router-link to="/register">立即註冊</router-link></p>
  </div>
</template>

<script>
import api from '../api';

export default {
  data() {
    return {
      phoneSuffix: '',
      form: { password: '' },
      showPassword: false,
      message: '',
    };
  },
  methods: {
    async login() {
      try {
        const phone = '09' + this.phoneSuffix;
        const response = await api.login({ phone, password: this.form.password });
        const token = response.data.token;
        const userName = response.data.userName;

        // JWT 格式：header.payload.signature，atob 解碼 Base64 取出 payload
        const payload = JSON.parse(atob(token.split('.')[1]));

        localStorage.setItem('jwt', token);
        localStorage.setItem('userPhone', phone);
        localStorage.setItem('userId', payload.userId);
        localStorage.setItem('userName', userName);

        // 通知父元件（App.vue）更新 header 顯示的名稱
        this.$emit('loggedIn', { phone, userId: payload.userId, userName });
        this.$router.push('/posts');
      } catch (error) {
        this.message = error.response?.data || '登入失敗';
      }
    },
  },
};
</script>

<style scoped>
.auth-box {
  max-width: 400px;
  margin: 40px auto;
  padding: 24px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
form > div { margin-bottom: 12px; }
label { display: block; margin-bottom: 4px; }
input { width: 100%; padding: 8px; box-sizing: border-box; }
button[type="submit"] { width: 100%; padding: 10px; margin-top: 8px; }
.phone-row { display: flex; align-items: center; }
.phone-prefix {
  background: #f0f0f0;
  border: 1px solid #ccc;
  border-right: none;
  padding: 8px 10px;
  border-radius: 4px 0 0 4px;
  font-size: 0.95rem;
  white-space: nowrap;
}
.phone-row input { border-radius: 0 4px 4px 0; }
.pw-row { display: flex; align-items: center; }
.pw-row input { flex: 1; border-radius: 4px 0 0 4px; }
.eye-btn {
  background: #f0f0f0;
  border: 1px solid #ccc;
  border-left: none;
  padding: 8px 10px;
  border-radius: 0 4px 4px 0;
  cursor: pointer;
  font-size: 1rem;
  line-height: 1;
}
.error { color: red; }
</style>
