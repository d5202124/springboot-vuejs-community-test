<template>
  <div class="auth-wrap">
    <el-card class="auth-card">
      <h2 class="title">註冊帳號</h2>
      <el-form @submit.prevent="register">
        <el-form-item label="姓名">
          <el-input v-model="form.userName" required />
        </el-form-item>

        <el-form-item label="手機號碼">
          <!-- 固定顯示「09」前綴，使用者只輸入後 8 碼數字 -->
          <el-input v-model="phoneSuffix" type="tel" maxlength="8" placeholder="12345678">
            <template #prepend>09</template>
          </el-input>
        </el-form-item>

        <el-form-item label="密碼">
          <!-- :type 動態綁定：showPassword 為true時顯示 -->
          <el-input v-model="form.password" :type="showPassword ? 'text' : 'password'"
            :suffix-icon="showPassword ? 'Hide' : 'View'"
            @click-suffix="showPassword = !showPassword" />
          <div class="hint">至少8碼，須含大寫、小寫英文與數字，不含特殊符號</div>
        </el-form-item>

        <el-form-item label="確認密碼">
          <el-input v-model="confirmPassword" :type="showConfirm ? 'text' : 'password'"
            :suffix-icon="showConfirm ? 'Hide' : 'View'"
            @click-suffix="showConfirm = !showConfirm" />
        </el-form-item>

        <el-form-item label="封面圖片 URL（選填）">
          <el-input v-model="form.coverImage" />
        </el-form-item>

        <el-form-item label="個人簡介（選填）">
          <el-input v-model="form.biography" type="textarea" :rows="3" />
        </el-form-item>

        <el-button type="primary" style="width:100%" @click="register">註冊</el-button>
        <el-alert v-if="message" :title="message" :type="success ? 'success' : 'error'"
          show-icon :closable="false" style="margin-top:12px" />
      </el-form>
      <p class="link">已有帳號？<router-link to="/login">登入</router-link></p>
    </el-card>
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
.auth-wrap {
  display: flex;
  justify-content: center;
  padding-top: 40px;
}
.auth-card {
  width: 580px;
}
.title {
  text-align: center;
  margin: 0 0 24px;
  color: #303133;
}
.hint {
  font-size: 0.78rem;
  color: #909399;
  margin-top: 4px;
}
.link {
  text-align: center;
  margin-top: 16px;
  color: #606266;
  font-size: 0.9rem;
}
</style>
