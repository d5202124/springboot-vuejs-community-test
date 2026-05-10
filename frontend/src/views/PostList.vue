<template>
  <div>
    <!-- 發文表單 -->
    <el-card class="post-form-card">
      <el-form @submit.prevent="submitPost">
        <el-input
          v-model="newPost.content"
          type="textarea"
          :rows="4"
          placeholder="寫點什麼..."
          resize="none"
        />
        <el-input v-model="newPost.image" placeholder="圖片 URL（選填）" style="margin-top:8px" />
        <div style="text-align:right; margin-top:8px">
          <el-button type="primary" :loading="submittingPost" @click="submitPost">發文</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 載入狀態 -->
    <div v-if="loading" style="text-align:center; padding:40px">
      <el-skeleton :rows="4" animated />
    </div>
    <el-empty v-else-if="posts.length === 0" description="目前還沒有任何貼文，來發第一篇吧！" />

    <!-- 貼文列表 -->
    <el-card v-for="post in posts" :key="post.postId" class="post-card">
      <!-- 貼文 meta -->
      <div class="post-meta">
        <el-avatar :src="post.coverImage || ''" :size="48">
          {{ post.userName?.charAt(0) }}
        </el-avatar>
        <div class="post-meta-info">
          <strong>{{ post.userName }}</strong>
          <span class="post-time">{{ formatDate(post.createdAt) }}</span>
        </div>
        <!-- 只有自己的貼文才顯示編輯/刪除按鈕 -->
        <div v-if="post.userId === currentUserId" class="post-actions">
          <el-button size="small" text @click="editPost(post)">編輯</el-button>
          <el-button size="small" text type="danger" @click="confirmDeletePost(post.postId)">刪除</el-button>
        </div>
      </div>

      <!-- 編輯模式 / 一般顯示 -->
      <div v-if="editId === post.postId" class="edit-form">
        <el-input v-model="editPostData.content" type="textarea" :rows="3" resize="none" />
        <el-input v-model="editPostData.image" placeholder="圖片 URL" style="margin-top:8px" />
        <div style="margin-top:8px; display:flex; gap:8px">
          <el-button type="primary" size="small" @click="updatePost(post.postId)">儲存</el-button>
          <el-button size="small" @click="cancelEdit">取消</el-button>
        </div>
      </div>
      <div v-else>
        <!-- white-space: pre-wrap（CSS 中設定）保留換行與空白，維持原始排版 -->
        <p class="post-content">{{ post.content }}</p>
        <img v-if="post.image" :src="post.image" class="post-img" alt="貼文圖片" />
      </div>

      <!-- 留言區塊 -->
      <div class="comment-box">
        <el-divider />
        <div v-if="comments[post.postId]">
          <div v-for="comment in comments[post.postId]" :key="comment.commentId" class="comment-item">
            <div class="comment-header">
              <span class="comment-author">{{ comment.userName }}</span>
              <span class="comment-time">{{ formatDate(comment.createdAt) }}</span>
              <!-- 只有自己的留言才顯示編輯/刪除按鈕 -->
              <div v-if="comment.userId === currentUserId" class="comment-actions">
                <el-button size="small" text @click="editComment(comment)">編輯</el-button>
                <el-button size="small" text type="danger" @click="confirmDeleteComment(comment)">刪除</el-button>
              </div>
            </div>
            <!-- 留言編輯模式，同貼文邏輯：editCommentId 對上才顯示編輯框 -->
            <div v-if="editCommentId === comment.commentId" style="margin-top:6px">
              <el-input v-model="editCommentContent" type="textarea" :rows="2" resize="none" />
              <div style="margin-top:6px; display:flex; gap:8px">
                <el-button type="primary" size="small" @click="updateComment(comment.commentId, comment.postId)">儲存</el-button>
                <el-button size="small" @click="cancelCommentEdit">取消</el-button>
              </div>
            </div>
            <p v-else class="comment-content">{{ comment.content }}</p>
          </div>
        </div>

        <!-- 留言輸入框 -->
        <div class="comment-input-row">
          <el-input
            v-model="commentMap[post.postId]"
            placeholder="新增留言..."
            size="small"
            style="flex:1"
          />
          <el-button size="small" type="primary" plain @click="submitComment(post.postId)">送出</el-button>
        </div>
      </div>
    </el-card>

    <!-- 錯誤提示 -->
    <el-alert v-if="loadError" :title="loadError" type="error" show-icon :closable="false" style="margin-top:12px" />
  </div>
</template>

<script>
import api from '../api';
import { ElMessage, ElMessageBox } from 'element-plus';

export default {
  data() {
    return {
      // Number() 轉型：localStorage 讀出的值是字串，需轉成數字才能與 post.userId 比對
      currentUserId: Number(localStorage.getItem('userId')) || null,

      posts: [],
      loading: false,
      loadError: '',
      submittingPost: false,

      newPost: { content: '', image: '' },
      editId: null,
      editPostData: { content: '', image: '' },

      editCommentId: null,
      editCommentContent: '',

      commentMap: {},  // { [postId]: '輸入中的留言文字' }，每篇貼文的輸入框各自獨立
      comments: {},    // { [postId]: [留言陣列] }
    };
  },

  async mounted() {
    await this.loadPosts();
  },

  methods: {
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
      if (!this.newPost.content.trim()) return;
      this.submittingPost = true;
      try {
        // userId 不需手動傳入，後端從 JWT 取得（SecurityUtils.getCurrentUserId）
        await api.createPost({ content: this.newPost.content, image: this.newPost.image });
        this.newPost.content = '';
        this.newPost.image = '';
        await this.loadPosts();  // 重新載入，讓新貼文出現在列表中
        ElMessage.success('發文成功');
      } catch (e) {
        ElMessage.error(e.response?.data || '發文失敗，請稍後再試');
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
        ElMessage.success('貼文已更新');
      } catch (e) {
        ElMessage.error(e.response?.data || '更新失敗');
      }
    },

    // 刪除前先顯示確認對話框，避免誤操作
    async confirmDeletePost(postId) {
      try {
        await ElMessageBox.confirm('確定要刪除這篇貼文嗎？', '刪除確認', {
          confirmButtonText: '刪除',
          cancelButtonText: '取消',
          type: 'warning',
        });
        await api.deletePost(postId);
        await this.loadPosts();
        ElMessage.success('貼文已刪除');
      } catch (e) {
        if (e !== 'cancel') ElMessage.error(e.response?.data || '刪除失敗');
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
      if (!content?.trim()) return;
      try {
        await api.createComment({ postId, content });
        this.commentMap[postId] = '';  // 清空該貼文的留言輸入框
        await this.loadComments(postId);
        ElMessage.success('留言成功');
      } catch (e) {
        ElMessage.error(e.response?.data || '留言失敗');
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
        ElMessage.success('留言已更新');
      } catch (e) {
        ElMessage.error(e.response?.data || '更新失敗');
      }
    },

    async confirmDeleteComment(comment) {
      try {
        await ElMessageBox.confirm('確定要刪除這則留言嗎？', '刪除確認', {
          confirmButtonText: '刪除',
          cancelButtonText: '取消',
          type: 'warning',
        });
        await api.deleteComment(comment.commentId);
        await this.loadComments(comment.postId);
        ElMessage.success('留言已刪除');
      } catch (e) {
        if (e !== 'cancel') ElMessage.error(e.response?.data || '刪除失敗');
      }
    },
  },
};
</script>

<style scoped>
.post-form-card {
  margin-bottom: 24px;
}
.post-card {
  margin-bottom: 20px;
}
.post-meta {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}
.post-meta-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.post-time {
  font-size: 0.9rem;
  color: #909399;
}
.post-actions {
  display: flex;
  gap: 4px;
}
.post-content {
  white-space: pre-wrap;
  margin: 0 0 12px;
  color: #303133;
  line-height: 1.8;
  font-size: 1.1rem;
}
.post-img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
  border-radius: 6px;
  margin-top: 8px;
}
.edit-form {
  margin-bottom: 8px;
}
.comment-box {
  margin-top: 4px;
}
.comment-item {
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}
.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}
.comment-author {
  font-weight: bold;
  font-size: 0.95rem;
  color: #303133;
}
.comment-time {
  font-size: 0.85rem;
  color: #909399;
  flex: 1;
}
.comment-actions {
  display: flex;
  gap: 2px;
}
.comment-content {
  margin: 0;
  font-size: 1rem;
  color: #606266;
  white-space: pre-wrap;
}
.comment-input-row {
  display: flex;
  gap: 8px;
  margin-top: 12px;
}
</style>
