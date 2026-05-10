<template>
  <div class="auth-box">
    <h2>註冊帳號</h2>
    <!-- @submit.prevent 攔截原生表單提交，交由 register() 處理 -->
    <form @submit.prevent="register">
      <div>
        <label>姓名</label>
        <input v-model="form.userName" required />
      </div>

      <div>
        <label>手機號碼</label>
        <!-- 固定顯示「09」前綴，使用者只輸入後 8 碼數字 -->
        <div class="phone-row">
          <span class="phone-prefix">09</span>
          <input
            v-model="phoneSuffix"
            type="tel"
            maxlength="8"
            placeholder="12345678"
            required
          />
        </div>
      </div>

      <div>
        <label>密碼</label>
        <div class="pw-row">
          <!-- :type 動態綁定：showPassword 為true時顯示 -->
          <input v-model="form.password" :type="showPassword ? 'text' : 'password'" required />
          <button type="button" class="eye-btn" @click="showPassword = !showPassword">
            {{ showPassword ? '🙈' : '👁' }}
          </button>
        </div>
        <small class="hint">至少8碼，須含大寫、小寫英文與數字，不含特殊符號</small>
      </div>

      <div>
        <label>確認密碼</label>
        <div class="pw-row">
          <input v-model="confirmPassword" :type="showConfirm ? 'text' : 'password'" required />
          <button type="button" class="eye-btn" @click="showConfirm = !showConfirm">
            {{ showConfirm ? '🙈' : '👁' }}
          </button>
        </div>
      </div>

      <div><label>封面圖片 URL（選填）</label><input v-model="form.coverImage" /></div>
      <div><label>個人簡介（選填）</label><textarea v-model="form.biography"></textarea></div>

      <button type="submit">註冊</button>
      <p v-if="message" :class="success ? 'ok' : 'error'">{{ message }}</p>
    </form>
    <p>已有帳號？<router-link to="/login">登入</router-link></p>
  </div>
</template>

<script>
import api from '../api';

// 至少8碼，須含大小寫英文與數字，不含特殊符號
const PASSWORD_REGEX = /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)[A-Za-z\d]{8,}$/;

export default {
  data() {
    return {
      form: { userName: '', password: '', coverImage: '', biography: '' },
      phoneSuffix: '',       // 使用者輸入的後 8 碼，組合時再加上 '09'
      confirmPassword: '',   // 確認密碼欄位，只用於前端比對，不傳送到後端
      showPassword: false,
      showConfirm: false,
      message: '',           // 操作結果訊息
      success: false,        // 是否成功，控制訊息顯示顏色
    };
  },
  methods: {
    async register() {
      // 前端先做基本驗證，避免無效請求送到後端

      // 確認手機後 8 碼為純數字
      if (!/^\d{8}$/.test(this.phoneSuffix)) {
        this.success = false;
        this.message = '手機號碼請輸入09後的8碼數字';
        return;
      }

      // 確認密碼符合格式規則
      if (!PASSWORD_REGEX.test(this.form.password)) {
        this.success = false;
        this.message = '密碼須至少8碼，包含大小寫英文與數字，不含特殊符號';
        return;
      }

      // 確認兩次密碼一致
      if (this.form.password !== this.confirmPassword) {
        this.success = false;
        this.message = '兩次密碼輸入不一致';
        return;
      }

      try {
        await api.register({ ...this.form, phone: '09' + this.phoneSuffix });

        this.success = true;
        this.message = '註冊成功，跳轉到登入頁...';

        // 1.2 秒後自動跳轉，讓使用者有時間看到成功訊息
        setTimeout(() => this.$router.push('/login'), 1200);
      } catch (error) {
        this.success = false;
        // 手機已被註冊等等錯誤訊息從後端回傳，若沒有則顯示預設訊息
        this.message = error.response?.data || '註冊失敗';
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
form > div {
  margin-bottom: 12px;
}
label {
  display: block;
  margin-bottom: 4px;
}
input, textarea {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  resize: none;
}
button[type="submit"] {
  width: 100%;
  padding: 10px;
  margin-top: 8px;
}
.phone-row {
  display: flex;
  align-items: center;
  gap: 0;
}
.phone-prefix {
  background: #f0f0f0;
  border: 1px solid #ccc;
  border-right: none;
  padding: 8px 10px;
  border-radius: 4px 0 0 4px;
  font-size: 0.95rem;
  white-space: nowrap;
}
.phone-row input {
  border-radius: 0 4px 4px 0;
}
.pw-row {
  display: flex;
  align-items: center;
}
.pw-row input {
  flex: 1;
  border-radius: 4px 0 0 4px;
}
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
.hint {
  color: #888;
  font-size: 0.78rem;
  display: block;
  margin-top: 4px;
}
.error { color: red; }
.ok { color: green; }
</style>
