# Kế hoạch luyện tập tay — Sole Health Base Platform

> Mục tiêu: tự code tay để hiểu bản chất, chuẩn bị phỏng vấn Senior Fullstack Engineer (ReactJS/Java/GraphQL). Xem lộ trình gốc ở [`sole_health_plan_extracted.txt`](./sole_health_plan_extracted.txt) — tài liệu này chỉ chi tiết hoá phần **luyện tập hàng ngày**, bám theo cùng lộ trình đó.

## Nguyên tắc làm việc

- **Bạn viết code, tôi không viết hộ.** Vai trò của tôi: giải thích khái niệm, review code bạn viết, đặt câu hỏi vấn đáp, chỉ hướng nghiên cứu. Chỉ khi bạn nói rõ "cho tôi xem code mẫu/giải pháp tham khảo" tôi mới viết code.
- Mỗi session dưới đây kết thúc bằng cách bạn **paste code cho tôi review** + **tự trả lời (hoặc nhờ tôi hỏi vấn đáp)** phần "Câu hỏi phỏng vấn liên quan".
- Đánh dấu `[x]` vào checkbox khi hoàn thành để tự theo dõi tiến độ — file này sống lâu dài, cứ commit lại khi cập nhật.
- Không cần làm tuần tự tuyệt đối 4 track — nhưng Track A (business logic) nên đi trước Track B (JWT) vì JWT cần có endpoint thật để bảo vệ.

## Nhịp độ đề xuất

| Ngày trong tuần | Việc chính |
|---|---|
| Thứ 2–6 (buổi tối, ~1–1.5h) | 1 session Track A hoặc B (xen kẽ) + 1–2 bài Track C (DSA) |
| Thứ 7 | 1 session Track A/B dài hơn (2–3h) hoặc review lại code tuần |
| Chủ nhật | Track D — tự vấn đáp system design, không code |

---

## Track A — Business logic thật (Spring Boot / JPA)

Bám đúng "Bước 1: Core business logic" trong lộ trình gốc — phần này bị scaffold trước (chỉ có health-check rỗng), giờ quay lại làm tay cho thật.

- [ ] **A1. CRUD `User` trong `microservice/user-service`**
  Entity `User` (id, email, fullName, role, createdAt, updatedAt — tự thêm field thấy hợp lý) → Repository (Spring Data JPA) → Service (rule: email unique, validate input) → Controller REST (`POST/GET/PUT/DELETE /users`).
  *Câu hỏi phỏng vấn liên quan:* JPA lazy vs eager fetch là gì, khi nào chọn cái nào? `@Transactional` propagation mặc định là gì, khác `REQUIRES_NEW` chỗ nào? Vì sao không nên expose Entity trực tiếp ra API (DTO pattern)?

- [ ] **A2. Validation & Exception Handling — tự thiết kế lại**
  Đọc lại 2 finding trong [`deferred-work.md`](../_bmad-output/implementation-artifacts/deferred-work.md) về `GlobalExceptionHandler` (leak `ex.getMessage()`, thiếu map exception → 4xx, bỏ sót `getGlobalErrors()`), tự sửa cho cả `user-service`.
  *Câu hỏi:* Vì sao không nên trả raw exception message cho client? Đây là loại lỗ hổng gì trong OWASP Top 10? `@ControllerAdvice` hoạt động thế nào dưới cơ chế Spring AOP?

- [ ] **A3. `Clinic` + `Appointment` (quan hệ) trong `microservice/clinic-service`**
  Entity có quan hệ (vd `Doctor` 1-n `Appointment`, hoặc `Patient` n-n `Clinic`). Viết tay migration (Flyway/Liquibase) thay vì để `ddl-auto: update` như hiện tại.
  *Câu hỏi:* Cascade type nào an toàn cho quan hệ appointment? N+1 query là gì, cách phát hiện (bật `show-sql` + đếm query) và fix (`@EntityGraph`, `JOIN FETCH`)? Vì sao `ddl-auto: update` không nên dùng production?

- [ ] **A4. Test thật thay `contextLoads()` rỗng**
  Xử lý finding "test phụ thuộc Postgres thật" trong deferred-work — tự thêm Testcontainers hoặc H2 profile, viết unit test cho Service + integration test cho Controller.
  *Câu hỏi:* Testcontainers khác H2 chỗ nào khi test tính năng đặc thù Postgres (vd JSONB, full-text search)? Test pyramid là gì?

- [ ] **A5. OpenAPI spec cho từng service**
  Thêm `springdoc-openapi`, tự viết annotation mô tả rõ request/response — JD yêu cầu rõ "microservices and OpenAPI standards".
  *Câu hỏi:* OpenAPI/Swagger giúp gì cho contract-first development? Khác biệt REST resource design tốt vs xấu (idempotency, status code đúng nghĩa)?

---

## Track B — Auth/JWT thật (thay `permitAll` hiện tại)

- [ ] **B1. Vẽ luồng trước khi code**
  Vẽ (trên giấy/markdown) luồng register → login → access token → refresh token → logout. Không code vội.
  *Câu hỏi:* Vì sao JWT phù hợp cho stateless microservices hơn session-cookie truyền thống? Trade-off gì (revoke khó hơn)?

- [ ] **B2. `JwtService` tay** (dùng `jjwt` hoặc `nimbus-jose-jwt`)
  Sign + verify token, claims tối thiểu (`sub`, `role`, `exp`, `iat`).
  *Câu hỏi:* HMAC (khoá đối xứng) vs RSA/EC (khoá bất đối xứng) cho JWT — khi nào chọn cái nào trong kiến trúc microservices (ai giữ private key)?

- [ ] **B3. `JwtAuthenticationFilter` + tích hợp `SecurityFilterChain`**
  Thay `.anyRequest().permitAll()` hiện tại trong `SecurityConfig` của từng service.
  *Câu hỏi:* Spring Security filter chain hoạt động thế nào (`OncePerRequestFilter`, `SecurityContextHolder`)? Vì sao `sessionCreationPolicy(STATELESS)` cần đi kèm JWT filter?

- [ ] **B4. Refresh token + logout (cân nhắc Redis — đúng Bước 4 lộ trình gốc)**
  Refresh token store ở đâu? Blacklist access token khi logout thế nào nếu stateless?
  *Câu hỏi:* Đánh đổi giữa short-lived access token + refresh token vs long-lived single token?

- [ ] **B5. RBAC (role-based access control)**
  Map với ghi chú "audit log + RBAC chặt chẽ" cho "vulnerable populations" trong lộ trình gốc.
  *Câu hỏi:* `@PreAuthorize` hoạt động dựa trên cơ chế gì? Method security vs URL-pattern security, khi nào dùng loại nào?

---

## Track C — DSA hàng ngày

Tách hẳn khỏi context Spring Boot — luyện thuật toán/cấu trúc dữ liệu thuần cho vòng phỏng vấn coding.

**Xoay vòng theo tuần** (lặp lại chu kỳ 4 tuần, mỗi vòng sau làm nhanh hơn):

| Tuần | Chủ đề |
|---|---|
| 1 | Array / String / HashMap |
| 2 | Two pointers / Sliding window / Binary search |
| 3 | Tree / Graph (BFS, DFS) |
| 4 | Dynamic programming |

- [ ] Chọn nguồn bài tự luyện (LeetCode/NeetCode 150 là lựa chọn phổ biến) — tôi có thể gợi ý bài cụ thể theo chủ đề nếu bạn muốn, không cần tôi giải hộ.
- Mỗi bài xong tự trả lời: độ phức tạp thời gian/không gian, có tối ưu hơn được không, viết test case biên (mảng rỗng, 1 phần tử, trùng lặp, âm...).

---

## Track D — System design / kiến trúc (không code, chỉ vấn đáp)

Câu hỏi lấy trực tiếp từ các quyết định **đã có thật trong repo này** — luyện trả lời rành mạch, vì đây đúng loại câu phỏng vấn senior hay hỏi ("tại sao bạn chọn X, đánh đổi gì").

- [ ] Vì sao `api-gateway` dùng Spring Cloud Gateway **Server MVC** (servlet-based) thay vì bản reactive/WebFlux? ([`pom.xml`](../microservice/api-gateway/pom.xml))
- [ ] Vì sao tách **database-per-service** (`health_base_user`, `health_base_clinic`) thay vì 1 database chung? Vấn đề gì nếu share?
- [ ] `StripPrefix=2` trong route gateway hoạt động thế nào? Điều gì xảy ra nếu số path segment sai? (liên quan finding: bare path `/api/clinics` không có trailing segment bị 404)
- [ ] `user-service` và `clinic-service` hiện dùng **chung 1 Postgres role** — vì sao đây là vấn đề bảo mật dù đã tách database riêng? Cách khắc phục (`CREATE ROLE` + `GRANT` riêng cho từng service)?
- [ ] `api-gateway` hard-depend `service_healthy` trên từng service phía sau — đánh đổi giữa fail-fast lúc khởi động và khả năng phục hồi (resilience)? Circuit breaker (Resilience4j) giải quyết vấn đề gì mà healthcheck dependency không giải quyết được?
- [ ] `docker/postgres/init-multiple-dbs.sh` chỉ chạy trên volume Postgres **mới** — vì sao migration tool (Flyway/Liquibase) là giải pháp đúng hơn về lâu dài?
- [ ] Nếu phải thêm GraphQL gateway tổng hợp dữ liệu từ các REST service phía sau (Bước 3 lộ trình gốc) — thiết kế schema thế nào để tránh N+1 ở tầng gateway (DataLoader pattern)?

> Nguồn đầy đủ các finding khác: [`deferred-work.md`](../_bmad-output/implementation-artifacts/deferred-work.md) — mỗi mục đều có thể biến thành 1 câu hỏi "tại sao đây là vấn đề, sửa thế nào".

---

## Checklist tự đánh giá mỗi session

- [ ] Code compile/chạy được, tự test qua (`curl`, Postman, hoặc unit test)
- [ ] Tự giải thích được **tại sao** chọn cách làm này, không chỉ **làm gì**
- [ ] Đã trả lời (viết ra hoặc nói thành lời) phần "Câu hỏi phỏng vấn liên quan"
- [ ] Nhờ tôi review code + hỏi vấn đáp thêm nếu muốn đào sâu
