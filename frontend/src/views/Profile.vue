<template>
  <div class="profile-wrap">
    <el-card class="profile-card">
      <h2 class="title">個人資料</h2>

      <!-- 載入中、載入成功或失敗 -->
      <div v-if="loading" style="text-align:center; padding:40px">
        <el-skeleton :rows="4" animated />
      </div>
      <el-alert v-else-if="loadError" :title="loadError" type="error" show-icon :closable="false" />

      <el-form v-else label-width="120px">
        <el-form-item label="姓名">
          <el-input v-model="form.userName" />
        </el-form-item>

        <el-form-item label="手機號碼">
          <!-- :value 非 v-model，唯讀不雙向綁定；disabled 禁止互動 -->
          <el-input :value="phone" disabled />
        </el-form-item>

        <el-form-item label="封面圖片 URL">
          <el-input v-model="form.coverImage" placeholder="選填" />
          <!-- 輸入URL v-if 確保空白時不顯示破圖 -->
          <el-avatar v-if="form.coverImage" :src="form.coverImage" :size="64" style="margin-top:8px" />
        </el-form-item>

        <el-form-item label="個人簡介">
          <el-input v-model="form.biography" type="textarea" :rows="3" placeholder="選填" resize="none" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="save">
            {{ saving ? '儲存中...' : '儲存' }}
          </el-button>
          <el-button @click="$router.push('/posts')">返回</el-button>
        </el-form-item>

        <el-alert v-if="message" :title="message" :type="success ? 'success' : 'error'"
          show-icon :closable="false" style="margin-top:8px" />
      </el-form>
    </el-card>
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
.profile-wrap {
  display: flex;
  justify-content: center;
  padding-top: 20px;
}
.profile-card {
  width: 680px;
}
.title {
  text-align: center;
  margin: 0 0 24px;
  color: #303133;
}
</style>
