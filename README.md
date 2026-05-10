# 社群媒體平台 / Community Platform

全端社群媒體應用程式，以 Spring Boot + Vue.js 實作，涵蓋使用者認證、發文、留言等核心功能，並落實多項資安防護。

A full-stack social media application built with Spring Boot and Vue.js, featuring user authentication, posts, comments, and multiple security measures.

---

## 技術棧 / Tech Stack

| 層級 | 技術 |
|------|------|
| 後端 Backend | Java 17 · Spring Boot 3.1 · Spring Security |
| 資料庫 Database | MySQL 8 · Stored Procedure · JdbcTemplate |
| 前端 Frontend | Vue 3 · Vue Router 4 · Vite 5 · Axios |
| 驗證 Auth | JWT (JJWT 0.11) · PBKDF2 密碼雜湊 |
| 建置 Build | Maven 3.8 |

---

## 系統架構 / Architecture

```
frontend (Vue 3 · port 3000)
    │  HTTP / Axios（自動附加 JWT）
    ▼
backend (Spring Boot · port 8080)
    ├── Controller 層   接收請求、參數驗證 (@Valid)
    ├── Service 層      商業邏輯、授權驗證、@Transactional
    ├── Repository 層   資料存取（JdbcTemplate + Stored Procedure）
    └── Common          JWT 工具、密碼雜湊、XSS 防護
    │
    ▼
MySQL 8（13 支 Stored Procedure）
```

---

## 功能清單 / Features

- **手機號碼註冊 / 登入** — JWT 無狀態驗證，Token 存於前端 localStorage
- **發文 CRUD** — 新增、列表（含發文者名稱與頭像）、編輯、刪除
- **留言** — 針對貼文新增、編輯、刪除留言
- **個人資料** — 查看與更新姓名、頭像、自我介紹
- **擁有者授權** — 只有本人可編輯或刪除自己的貼文 / 留言

---

## 資安設計 / Security

| 項目 | 實作方式 |
|------|----------|
| SQL Injection 防護 | JdbcTemplate PreparedStatement + Stored Procedure 參數化查詢 |
| XSS 防護 | JSoup 清理 URL 欄位 HTML 標籤 |
| 密碼儲存 | PBKDF2WithHmacSHA256 + 隨機 Salt，不儲存明文 |
| 身份驗證 | Spring Security Filter Chain + JWT (HS256) |
| 授權控制 | Service 層比對 JWT userId 與資源擁有者，不信任前端傳入值 |
| 輸入驗證 | Bean Validation (@NotBlank · @Size · @Pattern) 於 DTO 層 |
| CSRF | 停用（JWT 無狀態，無 Session / Cookie 需保護） |

---

## 啟動說明 / Getting Started

### 環境需求 / Prerequisites

- Java 17+
- Node.js 18+
- MySQL 8
- Maven 3.8+

### 資料庫初始化 / Database Setup

```bash
mysql -u root -p < backend/DB/db_setup.sql
```

> `db_setup.sql` 會依序建立資料庫、資料表、並載入全部 Stored Procedure。

### 後端啟動 / Backend

啟動前需設定以下環境變數：

**Windows (PowerShell)**
```powershell
$env:DB_PASSWORD = "your_db_password"
$env:JWT_SECRET  = "your_jwt_secret_at_least_64_chars"
```

**Mac / Linux**
```bash
export DB_PASSWORD=your_db_password
export JWT_SECRET=your_jwt_secret_at_least_64_chars
```

```bash
cd backend
mvn spring-boot:run
# 服務啟動於 http://localhost:8080
```

### 前端啟動 / Frontend

```bash
cd frontend
npm install
npm run dev
# 瀏覽器開啟 http://localhost:3000
```

---

## 專案結構 / Project Structure

```
├── backend/
│   ├── DB/                         # schema.sql · stored_procedures.sql · db_setup.sql
│   └── src/main/java/com/example/esun/
│       ├── controller/             # REST API 端點
│       ├── service/                # 商業邏輯 + @Transactional
│       ├── repository/             # JdbcTemplate 資料存取
│       ├── model/                  # User · Post · Comment
│       ├── dto/                    # Request / Response 資料物件
│       ├── security/               # JWT Filter · Token Provider · Security Config
│       └── common/                 # 密碼雜湊 · XSS 清理 · SecurityUtils · GlobalExceptionHandler
└── frontend/
    └── src/
        ├── views/                  # Login · Register · PostList · Profile
        ├── router/                 # Vue Router（含路由守衛）
        ├── api.js                  # Axios 實例 + JWT 攔截器
        └── App.vue                 # 根元件 + Header 導覽列
```

---

## Stored Procedure 設計 / Stored Procedure Design

全部資料庫操作皆透過 Stored Procedure 執行，共 14 支，定義於 `backend/DB/stored_procedures.sql`。

All database operations are executed through Stored Procedures (14 total), defined in `backend/DB/stored_procedures.sql`.

| Stored Procedure | 用途 |
|-----------------|------|
| `sp_create_user` | 新增使用者 |
| `sp_find_user_by_phone` | 以手機號碼查詢使用者（登入用） |
| `sp_get_user_by_id` | 以 ID 查詢使用者 |
| `sp_update_user` | 更新使用者個人資料 |
| `sp_create_post` | 新增貼文 |
| `sp_get_posts` | 取得所有貼文（JOIN User 取得姓名與頭像，依時間倒序） |
| `sp_get_post_by_id` | 以 ID 查詢單筆貼文（授權驗證用） |
| `sp_update_post` | 更新貼文內容 |
| `sp_delete_post` | 刪除貼文 |
| `sp_create_comment` | 新增留言 |
| `sp_get_comments_by_post` | 取得指定貼文的留言（JOIN User 取得留言者姓名，依時間正序） |
| `sp_get_comment_by_id` | 以 ID 查詢單筆留言（授權驗證用） |
| `sp_update_comment` | 更新留言內容 |
| `sp_delete_comment` | 刪除留言 |

**Java 呼叫方式 / How SP is called from Java（JdbcTemplate）：**

```java
// Repository 層範例：呼叫 sp_get_posts
public List<Post> findAllPosts() {
    return jdbcTemplate.query("CALL sp_get_posts()", postMapper);
}

// 帶參數範例：呼叫 sp_create_post
public void createPost(Post post) {
    jdbcTemplate.update("CALL sp_create_post(?, ?, ?)",
        post.getUserId(), post.getContent(), post.getImage());
}
```

所有參數皆以 `?` 佔位符傳入，由 JdbcTemplate 底層的 PreparedStatement 處理，從語法層面防止 SQL Injection。

All parameters are passed as `?` placeholders handled by JdbcTemplate's PreparedStatement, preventing SQL Injection at the syntax level.

---

## API 端點 / API Endpoints

| 方法 | 路徑 | 說明 | 需驗證 |
|------|------|------|--------|
| POST | `/api/auth/register` | 註冊 | |
| POST | `/api/auth/login` | 登入，回傳 JWT | |
| GET | `/api/posts` | 取得所有貼文 | ✓ |
| POST | `/api/posts` | 新增貼文 | ✓ |
| PUT | `/api/posts/{id}` | 編輯貼文（限本人） | ✓ |
| DELETE | `/api/posts/{id}` | 刪除貼文（限本人） | ✓ |
| GET | `/api/comments/post/{postId}` | 取得留言 | ✓ |
| POST | `/api/comments` | 新增留言 | ✓ |
| PUT | `/api/comments/{id}` | 編輯留言（限本人） | ✓ |
| DELETE | `/api/comments/{id}` | 刪除留言（限本人） | ✓ |
| GET | `/api/users/me` | 取得個人資料 | ✓ |
| PUT | `/api/users/me` | 更新個人資料 | ✓ |
