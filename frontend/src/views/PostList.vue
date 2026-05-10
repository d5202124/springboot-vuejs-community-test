<template>
  <div>
    <h2>最新發文</h2>

    <div v-if="toast.message" :class="['toast', toast.type]">{{ toast.message }}</div>

    <!-- 發文表單 -->
    <section class="post-form">
      <h3>新增發文</h3>
      <form @submit.prevent="submitPost">
        <textarea v-model="newPost.content" placeholder="寫點什麼..." required></textarea>
        <input v-model="newPost.image" placeholder="圖片 URL（選填）" />
        <button type="submit" :disabled="submittingPost">{{ submittingPost ? '發送中...' : '發文' }}</button>
      </form>
    </section>

    <div v-if="loading" class="status-msg">載入中...</div>
    <div v-else-if="loadError" class="status-msg error-msg">{{ loadError }}</div>
    <div v-else-if="posts.length === 0" class="status-msg">目前還沒有任何貼文，來發第一篇吧！</div>

    <!-- 貼文列表：v-for 迭代陣列，:key 幫助 Vue 追蹤每個元素的身份（必填） -->
    <section v-else class="posts">
      <article v-for="post in posts" :key="post.postId" class="post-card">

        <!-- 貼文 meta 列：頭像、作者名稱、時間、編輯/刪除按鈕 -->
        <div class="post-meta">
          <!-- v-if：只有帳號有設定封面圖才顯示頭像 -->
          <img v-if="post.coverImage" :src="post.coverImage" class="avatar" alt="頭像" />
          <strong>{{ post.userName }}</strong>
          <span class="post-time">{{ formatDate(post.createdAt) }}</span>
          <!-- 只有自己的貼文才顯示編輯/刪除按鈕 -->
          <div v-if="post.userId === currentUserId" class="post-actions">
            <button class="icon-btn" @click="editPost(post)" title="編輯">✏</button>
            <button class="icon-btn" @click="confirmDeletePost(post.postId)" title="刪除">🗑</button>
          </div>
        </div>

        <div v-if="editId === post.postId" class="edit-form">
          <form @submit.prevent="updatePost(post.postId)">
            <textarea v-model="editPostData.content" required></textarea>
            <input v-model="editPostData.image" placeholder="圖片 URL" />
            <button type="submit">儲存</button>
            <button type="button" @click="cancelEdit">取消</button>
          </form>
        </div>
        <div v-else>
          <!-- white-space: pre-wrap（CSS 中設定）保留換行與空白，維持原始排版 -->
          <p class="post-content">{{ post.content }}</p>
          <img v-if="post.image" :src="post.image" alt="貼文圖片" class="post-img" />
        </div>

        <!-- 留言區塊 -->
        <div class="comment-box">
          <h4>留言</h4>
          <form @submit.prevent="submitComment(post.postId)">
            <textarea v-model="commentMap[post.postId]" placeholder="新增留言..." required></textarea>
            <button type="submit">送出</button>
          </form>

          <!-- comments[post.postId] 是該貼文的留言陣列，透過 v-if 確認有資料才渲染 -->
          <div v-if="comments[post.postId]">
            <div v-for="comment in comments[post.postId]" :key="comment.commentId" class="comment-item">
              <div class="comment-header">
                <small><strong>{{ comment.userName }}</strong> · {{ formatDate(comment.createdAt) }}</small>
                <!-- 只有自己的留言才顯示編輯/刪除按鈕 -->
                <div v-if="comment.userId === currentUserId" class="comment-actions">
                  <button class="icon-btn-sm" @click="editComment(comment)" title="編輯">✏</button>
                  <button class="icon-btn-sm" @click="confirmDeleteComment(comment)" title="刪除">🗑</button>
                </div>
              </div>
              <!-- 留言編輯模式，同貼文邏輯：editCommentId 對上才顯示編輯框 -->
              <div v-if="editCommentId === comment.commentId">
                <textarea v-model="editCommentContent"></textarea>
                <button @click="updateComment(comment.commentId, comment.postId)">儲存</button>
                <button @click="cancelCommentEdit">取消</button>
              </div>
              <p v-else class="comment-content">{{ comment.content }}</p>
            </div>
          </div>
        </div>

      </article>
    </section>
  </div>
</template>

<script>
import api from '../api';

export default {
  data() {
    return {
      // Number() 轉型：localStorage 讀出的值是字串，需轉成數字才能與 post.userId 比對
      currentUserId: Number(localStorage.getItem('userId')) || null,

      posts: [],           // 所有貼文的陣列
      loading: false,      // 是否正在載入貼文
      loadError: '',       // 載入失敗時的錯誤訊息
      submittingPost: false,  // 是否正在發文（防止重複送出）

      toast: { message: '', type: 'ok' },  // type: 'ok' 綠色成功 / 'error' 紅色失敗

      newPost: { content: '', image: '' },       // 新發文的輸入欄位
      editId: null,                               // 目前正在編輯的貼文 postId
      editPostData: { content: '', image: '' },   // 編輯中的貼文資料

      editCommentId: null,       // 目前正在編輯的留言 commentId
      editCommentContent: '',    // 編輯中的留言內容

      commentMap: {},  // { [postId]: '輸入中的留言文字' }，每篇貼文的輸入框各自獨立
      comments: {},    // { [postId]: [留言陣列] }
    };
  },

  async mounted() {
    await this.loadPosts();
  },

  methods: {
    showToast(message, type = 'ok') {
      this.toast = { message, type };
      setTimeout(() => { this.toast.message = ''; }, 3000);
    },

    // 載入所有貼文，並同時載入每篇貼文的留言
    async loadPosts() {
      this.loading = true;
      this.loadError = '';
      try {
        const response = await api.getPosts();
        this.posts = response.data;
        // 並行載入所有貼文的留言
        await Promise.all(this.posts.map((post) => this.loadComments(post.postId)));
      } catch {
        this.loadError = '載入貼文失敗，請重新整理頁面';
      } finally {
        // finally 無論成功或失敗都會執行，確保 loading 狀態被重置
        this.loading = false;
      }
    },

    formatDate(value) {
      return new Date(value).toLocaleString('zh-TW', { timeZone: 'Asia/Taipei' });
    },

    // 發文
    async submitPost() {
      this.submittingPost = true;
      try {
        // userId 不需手動傳入，後端從 JWT 取得（SecurityUtils.getCurrentUserId）
        await api.createPost({ content: this.newPost.content, image: this.newPost.image });
        this.newPost.content = '';
        this.newPost.image = '';
        await this.loadPosts();  // 重新載入，讓新貼文出現在列表中
        this.showToast('發文成功');
      } catch (e) {
        this.showToast(e.response?.data || '發文失敗，請稍後再試', 'error');
      } finally {
        this.submittingPost = false;
      }
    },

    // 進入貼文編輯模式：把貼文目前的內容填入編輯框
    editPost(post) {
      this.editId = post.postId;
      this.editPostData = { content: post.content, image: post.image || '' };
    },

    // 取消編輯：清除 editId，編輯框消失，恢復一般顯示
    cancelEdit() {
      this.editId = null;
      this.editPostData = { content: '', image: '' };
    },

    // 送出貼文編輯
    async updatePost(postId) {
      try {
        await api.updatePost(postId, { content: this.editPostData.content, image: this.editPostData.image });
        this.cancelEdit();
        await this.loadPosts();
        this.showToast('貼文已更新');
      } catch (e) {
        this.showToast(e.response?.data || '更新失敗', 'error');
      }
    },

    // 刪除前先顯示確認對話框，避免誤操作
    confirmDeletePost(postId) {
      if (!window.confirm('確定要刪除這篇貼文嗎？')) return;
      this.deletePost(postId);
    },

    async deletePost(postId) {
      try {
        await api.deletePost(postId);
        await this.loadPosts();
        this.showToast('貼文已刪除');
      } catch (e) {
        this.showToast(e.response?.data || '刪除失敗', 'error');
      }
    },

    // 載入指定貼文的留言，並用展開運算子更新 comments 物件
    async loadComments(postId) {
      try {
        const response = await api.getComments(postId);
        // 用展開運算子替換整個物件，確保 Vue 響應式系統偵測到變化
        this.comments = { ...this.comments, [postId]: response.data };
      } catch {
        this.comments = { ...this.comments, [postId]: [] };
      }
    },

    // 送出留言
    async submitComment(postId) {
      const content = this.commentMap[postId];
      if (!content) return;  // 空白留言不送出
      try {
        await api.createComment({ postId, content });
        this.commentMap[postId] = '';  // 清空該貼文的留言輸入框
        await this.loadComments(postId);
        this.showToast('留言成功');
      } catch (e) {
        this.showToast(e.response?.data || '留言失敗', 'error');
      }
    },

    // 進入留言編輯模式
    editComment(comment) {
      this.editCommentId = comment.commentId;
      this.editCommentContent = comment.content;
    },

    cancelCommentEdit() {
      this.editCommentId = null;
      this.editCommentContent = '';
    },

    // 送出留言編輯
    async updateComment(commentId, postId) {
      try {
        await api.updateComment(commentId, { content: this.editCommentContent });
        this.cancelCommentEdit();
        await this.loadComments(postId);
        this.showToast('留言已更新');
      } catch (e) {
        this.showToast(e.response?.data || '更新失敗', 'error');
      }
    },

    confirmDeleteComment(comment) {
      if (!window.confirm('確定要刪除這則留言嗎？')) return;
      this.deleteComment(comment);
    },

    async deleteComment(comment) {
      try {
        await api.deleteComment(comment.commentId);
        await this.loadComments(comment.postId);
        this.showToast('留言已刪除');
      } catch (e) {
        this.showToast(e.response?.data || '刪除失敗', 'error');
      }
    },
  },
};
</script>

<style scoped>
/* Toast 固定在右上角，z-index 確保浮在所有內容之上 */
.toast {
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 12px 20px;
  border-radius: 6px;
  color: #fff;
  font-size: 0.9rem;
  z-index: 9999;
  background: #4caf50;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.toast.error {
  background: #e53935;
}
.status-msg {
  text-align: center;
  padding: 32px;
  color: #888;
}
.error-msg {
  color: #e53935;
}
.post-form,
.post-card {
  border: 1px solid #ddd;
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 8px;
}
.post-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.post-time {
  color: #888;
  font-size: 0.85rem;
}
.post-actions {
  margin-left: auto;  /* 把編輯/刪除按鈕推到最右側 */
}
/* white-space: pre-wrap：保留換行符號和空白，維持原始文字排版 */
.post-content {
  white-space: pre-wrap;
}
/* object-fit: contain：縮放圖片使其完整顯示在框內，不裁切 */
.post-img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  display: block;
  border-radius: 4px;
  margin-top: 8px;
}
/* 頭像：圓形裁切，使用 object-fit: cover 讓圖片填滿圓形 */
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.edit-form textarea,
.edit-form input {
  width: 100%;
  margin-bottom: 8px;
  box-sizing: border-box;
}
.icon-btn {
  background: none;
  border: none;
  font-size: 1.1rem;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
}
.icon-btn:hover {
  background: #f0f0f0;
}
.icon-btn-sm {
  background: none;
  border: none;
  font-size: 0.9rem;
  cursor: pointer;
  padding: 1px 4px;
  border-radius: 4px;
}
.icon-btn-sm:hover {
  background: #f0f0f0;
}
.comment-box {
  margin-top: 16px;
  border-top: 1px solid #eee;
  padding-top: 12px;
}
.comment-item {
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}
.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 4px;
}
.comment-actions {
  display: flex;
  gap: 4px;
}
.comment-content {
  margin: 4px 0 0 0;
  white-space: pre-wrap;  /* 同貼文：保留留言原始排版 */
}
textarea {
  width: 100%;
  min-height: 72px;
  padding: 8px;
  box-sizing: border-box;
  margin-bottom: 8px;
  resize: none;
}
input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  margin-bottom: 8px;
}
</style>
