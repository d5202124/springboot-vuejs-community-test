<template>
  <div class="profile-box">
    <h2>個人資料</h2>

    <!-- 載入中、載入成功或失敗 -->
    <div v-if="loading" class="status-msg">載入中...</div>
    <div v-else-if="loadError" class="status-msg error-msg">{{ loadError }}</div>

    <form v-else @submit.prevent="save">
      <div>
        <label>姓名</label>
        <input v-model="form.userName" required />
      </div>
      <div>
        <label>手機號碼</label>
        <!-- 手機號碼禁止互動 -->
        <input :value="phone" disabled class="readonly" />
      </div>
      <div>
        <label>封面圖片 URL（選填）</label>
        <input v-model="form.coverImage" />
        <!-- 輸入URL v-if 確保空白時不顯示破圖 -->
        <img v-if="form.coverImage" :src="form.coverImage" class="preview" alt="預覽" />
      </div>
      <div>
        <label>個人簡介（選填）</label>
        <textarea v-model="form.biography"></textarea>
      </div>

      <div class="actions">
        <!-- 儲存中禁用按鈕，文字也同步更新 -->
        <button type="submit" :disabled="saving">{{ saving ? '儲存中...' : '儲存' }}</button>
        <button type="button" @click="$router.push('/posts')">返回</button>
      </div>

      <!-- 切換顏色 -->
      <p v-if="message" :class="success ? 'ok' : 'error'">{{ message }}</p>
    </form>
  </div>
</template>

<script>
import api from '../api';

export default {
  data() {
    return {
      loading: true,      // 預設 true，等 mounted() 載入完成後設為 false
      loadError: '',      // 載入失敗訊息
      saving: false,      // 是否正在儲存（防止重複提交）
      success: false,     // 儲存結果，控制訊息顏色
      message: '',        // 操作結果訊息
      phone: '',          // 顯示用的手機號碼（唯讀，不放在 form 中）
      form: { userName: '', coverImage: '', biography: '' },
    };
  },

  async mounted() {
    try {
      const res = await api.getProfile();
      this.phone = res.data.phone;
      this.form.userName = res.data.userName;
      this.form.coverImage = res.data.coverImage || '';   // API 可能回傳 null
      this.form.biography = res.data.biography || '';
    } catch {
      this.loadError = '載入個人資料失敗';
    } finally {
      this.loading = false;
    }
  },

  methods: {
    async save() {
      this.saving = true;
      this.message = '';
      try {
        // PUT /api/users/me 更新個人資料，後端從 JWT 得知是哪位使用者
        const res = await api.updateProfile(this.form);

        // 更新 localStorage，讓 App.vue header 顯示的名稱也同步更新
        localStorage.setItem('userName', res.data.userName);
        // 通知父元件（App.vue）更新 header 顯示的名稱
        this.$emit('profileUpdated', { userName: res.data.userName });

        this.success = true;
        this.message = '個人資料已更新';
      } catch (e) {
        this.success = false;
        this.message = e.response?.data || '更新失敗';
      } finally {
        this.saving = false;
      }
    },
  },
};
</script>

<style scoped>
.profile-box {
  max-width: 480px;
  margin: 40px auto;
  padding: 24px;
  border: 1px solid #ddd;
  border-radius: 8px;
}
form > div {
  margin-bottom: 14px;
}
label {
  display: block;
  margin-bottom: 4px;
  font-weight: bold;
}
input, textarea {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  resize: none;
}
/* 手機號碼欄位的唯讀樣式 */
.readonly {
  background: #f5f5f5;
  color: #888;
  cursor: not-allowed;
}
/* 封面圖片預覽：圓形，object-fit: cover 讓圖片填滿圓形不留空白 */
.preview {
  margin-top: 8px;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  object-fit: cover;
  display: block;
}
.actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}
.actions button {
  flex: 1;
  padding: 10px;
}
.status-msg {
  text-align: center;
  padding: 32px;
  color: #888;
}
.error-msg { color: #e53935; }
.ok { color: green; margin-top: 10px; }
.error { color: red; margin-top: 10px; }
</style>
