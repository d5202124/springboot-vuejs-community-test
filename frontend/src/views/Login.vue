<template>
  <div class="auth-wrap">
    <el-card class="auth-card">
      <h2 class="title">登入</h2>
      <el-form @submit.prevent="login">
        <el-form-item label="手機號碼">
          <!-- 固定顯示「09」前綴，使用者只輸入後 8 碼 -->
          <el-input v-model="phoneSuffix" maxlength="8" placeholder="12345678" type="tel">
            <template #prepend>09</template>
          </el-input>
        </el-form-item>

        <el-form-item label="密碼">
          <!-- showPassword 控制顯示明文或遮罩，type="button" 避免觸發表單提交 -->
          <el-input
            v-model="form.password"
            :type="showPassword ? 'text' : 'password'"
            :suffix-icon="showPassword ? 'Hide' : 'View'"
            @click-suffix="showPassword = !showPassword"
          />
        </el-form-item>

        <el-button type="primary" style="width:100%" native-type="submit" @click="login">登入</el-button>
        <el-alert v-if="message" :title="message" type="error" show-icon :closable="false" style="margin-top:12px" />
      </el-form>
      <p class="link">還沒有帳號？<router-link to="/register">立即註冊</router-link></p>
    </el-card>
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
.auth-wrap {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}
.auth-card {
  width: 520px;
}
.title {
  text-align: center;
  margin: 0 0 24px;
  color: #303133;
}
.link {
  text-align: center;
  margin-top: 16px;
  color: #606266;
  font-size: 0.9rem;
}
</style>
