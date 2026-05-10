import axios from 'axios';

// baseURL: '/api' 配合 vite.config.js proxy，自動轉發到 localhost:8080
const api = axios.create({
  baseURL: '/api',
});

// 請求攔截器：每次請求前自動從 localStorage 取出 JWT 附加到 Header
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwt');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default {
  // 認證
  register(data) { return api.post('/auth/register', data); },
  login(data) { return api.post('/auth/login', data); },

  // 發文
  getPosts() { return api.get('/posts'); },
  createPost(data) { return api.post('/posts', data); },
  updatePost(postId, data) { return api.put(`/posts/${postId}`, data); },
  deletePost(postId) { return api.delete(`/posts/${postId}`); },

  // 留言
  createComment(data) { return api.post('/comments', data); },
  getComments(postId) { return api.get(`/comments/post/${postId}`); },
  updateComment(commentId, data) { return api.put(`/comments/${commentId}`, data); },
  deleteComment(commentId) { return api.delete(`/comments/${commentId}`); },

  // 個人資料
  getProfile() { return api.get('/users/me'); },
  updateProfile(data) { return api.put('/users/me', data); },
};
