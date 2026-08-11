---
title: 'Tách backend thành microservices + API Gateway điều phối (Phase 1: user-service + gateway)'
type: 'refactor'
created: '2026-08-10'
status: 'done'
review_loop_iteration: 0
context: []
baseline_commit: 'NO_VCS'
---

<!-- Target: 900–1300 tokens. Above 1600 = high risk of context rot. -->

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** `health-base-backend` hiện là 1 monolith Spring Boot duy nhất, chưa khớp Bước 2 của kế hoạch rebuild (tách microservices + OpenAPI, JD yêu cầu kinh nghiệm microservices).

**Approach:** Đổi tên/tái sử dụng `health-base-backend` thành `user-service` độc lập, thêm 1 `api-gateway` (Spring Cloud Gateway, servlet/MVC-based) làm cổng vào duy nhất route tĩnh tới user-service qua hostname Docker Compose, mỗi service có database Postgres riêng. Đây là phase 1 để chứng minh pattern hoạt động end-to-end; 4 service còn lại (clinic, medication, diagnostic, notification) đã ghi vào deferred-work để mirror lại pattern này sau.

## Boundaries & Constraints

**Always:**
- `user-service` và `api-gateway` là 2 Spring Boot app độc lập: pom.xml riêng, package riêng (`com.solehealth.user`, `com.solehealth.gateway`), application.yml riêng, Dockerfile riêng — không Maven multi-module cha.
- Gateway dùng Spring Cloud Gateway **Server MVC** (`spring-cloud-starter-gateway-server-webmvc`, servlet-based) để khớp stack WebMVC hiện có — không dùng biến thể reactive/WebFlux.
- Chỉ gateway có CORS config (browser chỉ gọi gateway); user-service không cần CORS.
- `user-service` có database Postgres riêng (`health_base_user`) trên container Postgres hiện có, tạo qua init script viết theo dạng lặp-được để sau này thêm db cho 4 service còn lại chỉ cần thêm tên vào danh sách.
- `health-base-backend` được **đổi tên/tái sử dụng thành `user-service`** (không tạo trùng lặp, không để lại thư mục cũ).
- Cổng: gateway 8080 (public), user-service 8081.
- Frontend (`health-base-frontend/src/shared/api/client.ts`) đã trỏ `http://localhost:8080/api` — không sửa frontend.
- Chỉ scaffold (health check, exception handler, datasource) — không thêm entity/controller/business logic nào.

**Ask First:**
- Nếu `spring-cloud-starter-gateway-server-webmvc` không có release train tương thích sạch với Spring Boot parent 4.1.0 hiện tại → HALT, hỏi trước khi đổi version Spring Boot.
- Nếu port 8080/8081 xung đột với tiến trình khác đang chạy trên máy → hỏi trước khi đổi port.

**Never:**
- Không tạo clinic/medication/diagnostic/notification-service trong spec này (đã deferred).
- Không thêm Kafka, Redis, ElasticSearch, GraphQL, Eureka/service discovery — ngoài phạm vi.
- Không thêm auth/JWT thật — SecurityConfig vẫn permitAll/stateless như hiện tại.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Healthy stack | `docker compose up -d --build` sau khi hoàn tất | postgres + user-service + api-gateway chạy, không crash loop | N/A |
| Gateway route | `GET localhost:8080/api/users/actuator/health` | 200, proxy tới user-service, trả `{"status":"UP"}` | N/A |
| Service down | user-service bị dừng, gọi `GET localhost:8080/api/users/**` | Gateway trả lỗi 502/503 kèm thông báo rõ ràng | Gateway không crash |
| DB isolation | `docker compose exec postgres psql -U health_base -l` | Có database `health_base_user` riêng biệt | N/A |

</frozen-after-approval>

## Code Map

- `health-base-backend/pom.xml` -- dependency set gốc (webmvc, data-jpa, validation, actuator, postgresql, lombok), copy sang user-service + trim cho gateway
- `health-base-backend/src/main/java/com/solehealth/base/common/exception/*.java` -- pattern ErrorResponse/GlobalExceptionHandler/ResourceNotFoundException, giữ nguyên logic trong user-service (đổi package)
- `health-base-backend/src/main/java/com/solehealth/base/common/config/SecurityConfig.java` -- pattern stateless permitAll, giữ trong user-service
- `health-base-backend/src/main/java/com/solehealth/base/common/config/CorsConfig.java` -- chuyển nguyên logic này sang `api-gateway`, xoá khỏi user-service
- `health-base-backend/src/main/resources/application.yml` -- pattern datasource/port/actuator, chỉnh port 8081 + db `health_base_user`
- `docker-compose.yml` -- container Postgres hiện có, mở rộng thêm init script + 2 service mới
- `health-base-frontend/src/shared/api/client.ts` -- xác nhận baseURL đã là gateway, không cần sửa

## Tasks & Acceptance

**Execution:**
- [x] `user-service/` -- Đổi tên thư mục `health-base-backend` → `user-service`; đổi package `com.solehealth.base` → `com.solehealth.user`, artifactId → `user-service`; xoá `CorsConfig.java`; sửa `application.yml` (port 8081, db `health_base_user`); thêm `Dockerfile` (multi-stage maven build → JRE runtime) -- tái sử dụng scaffold sẵn có
- [x] `api-gateway/` -- Tạo mới Spring Boot app dùng `spring-cloud-starter-gateway-server-webmvc`; route tĩnh `/api/users/**` tới `user-service` qua hostname Docker Compose; chứa `CorsConfig` (di dời từ user-service cũ); port 8080; `Dockerfile` -- cổng vào duy nhất cho frontend
- [x] `docker/postgres/init-multiple-dbs.sh` -- script tạo database `health_base_user`, viết dạng danh sách lặp-được để dễ mở rộng thêm db sau này -- database-per-service
- [x] `docker-compose.yml` -- mount init script vào postgres; thêm 2 service `user-service` + `api-gateway` (build context + Dockerfile, env `DB_HOST/DB_PORT/DB_NAME/DB_USER/DB_PASSWORD`, `depends_on: postgres` healthy) -- chạy stack cục bộ bằng 1 lệnh

**Acceptance Criteria:**
- Given stack đã `docker compose up -d --build`, when gọi `GET localhost:8080/actuator/health`, then trả `{"status":"UP"}` từ gateway.
- Given stack đang chạy, when gọi `GET localhost:8080/api/users/actuator/health`, then trả 200 UP proxy đúng từ user-service.
- Given `health-base-backend` đã được đổi tên, when scan thư mục gốc, then không còn tồn tại `health-base-backend`.
- Given Postgres container mới khởi tạo, when liệt kê database, then có database `health_base_user` riêng biệt.

## Spec Change Log

- 2026-08-10: Token count vượt 1600 (spec gốc ~2600–3000 token cho 5 service + gateway). Human chọn [S] Split. Carve off clinic/medication/diagnostic/notification-service sang `deferred-work.md`. Spec thu hẹp còn user-service + api-gateway (phase 1, chứng minh pattern). KEEP: toàn bộ Boundaries, Code Map, Tasks liên quan user-service/api-gateway/docker-compose không đổi nội dung, chỉ bỏ 4 service kia.

## Design Notes

Route thực tế trong `api-gateway/src/main/resources/application.yml` (Spring Cloud Gateway Server MVC, đã verify chạy được):

```yaml
spring:
  cloud:
    gateway:
      server:
        webmvc:
          routes:
            - id: user-service
              uri: http://user-service:8081
              predicates:
                - Path=/api/users/**
              filters:
                - StripPrefix=2
```

Lưu ý so với draft ban đầu: prefix cấu hình đúng là `spring.cloud.gateway.server.webmvc.routes` (không phải `spring.cloud.gateway.mvc.routes` — prefix đó không tồn tại ở bản đã dùng và khiến route không đăng ký). Cũng cần filter `StripPrefix=2` vì user-service expose endpoint (vd `/actuator/health`) ở root, không có tiền tố `/api/users`.

`pom.xml` gateway dùng BOM `spring-cloud-dependencies` version `2025.1.2` ("Oakwood", verified tương thích Spring Boot 4.1.0 qua Spring blog + Maven Central metadata) import trong `<dependencyManagement>`, cùng `spring-cloud-starter-gateway-server-webmvc` 5.0.2.

Viết `init-multiple-dbs.sh` dạng vòng lặp qua danh sách tên db (hiện chỉ `health_base_user`) để 4 service deferred sau này chỉ cần thêm tên vào danh sách, không viết lại script.

## Verification

**Commands:**
- `docker compose up -d --build` -- expected: 3 container (postgres + user-service + api-gateway) running/healthy, không restart loop
- `curl -s localhost:8080/actuator/health` -- expected: `{"status":"UP"}`
- `curl -s localhost:8080/api/users/actuator/health` -- expected: 200, `{"status":"UP"}` proxy từ user-service
- `docker compose exec postgres psql -U health_base -l` -- expected: liệt kê database `health_base_user`

**Manual checks (if no CLI):**
- Mở frontend (`npm run dev`), xác nhận không lỗi CORS trong console khi gọi API qua gateway.

## Suggested Review Order

**Service topology (docker-compose)**

- Toàn bộ stack: postgres + user-service + api-gateway; chỉ gateway lộ port ra host, user-service chỉ nội bộ.
  [`docker-compose.yml:37`](../../docker-compose.yml#L37)

- Gateway chờ user-service healthy (không chỉ started) trước khi nhận traffic, tránh proxy sớm khi context chưa boot xong.
  [`docker-compose.yml:47`](../../docker-compose.yml#L47)

**Gateway routing**

- Route tĩnh duy nhất `/api/users/**` → user-service qua hostname Docker, strip 2 segment trước khi forward.
  [`application.yml:9`](../../api-gateway/src/main/resources/application.yml#L9)

- Dùng Spring Cloud Gateway Server MVC (servlet-based) thay vì reactive để khớp stack WebMVC hiện có toàn hệ thống.
  [`pom.xml:60`](../../api-gateway/pom.xml#L60)

- CORS chỉ còn ở gateway — user-service không cần CORS nữa vì browser chỉ gọi qua gateway.
  [`CorsConfig.java:9`](../../api-gateway/src/main/java/com/solehealth/gateway/config/CorsConfig.java#L9)

**user-service scaffold (đổi tên từ health-base-backend)**

- Port 8081 + database riêng `health_base_user` — nền cho nguyên tắc database-per-service.
  [`application.yml:6`](../../user-service/src/main/resources/application.yml#L6)

- Package đổi `com.solehealth.base` → `com.solehealth.user`; exception/security handler giữ nguyên logic, chỉ đổi namespace.
  [`UserServiceApplication.java:1`](../../user-service/src/main/java/com/solehealth/user/UserServiceApplication.java#L1)

**Database per service**

- Script tạo `health_base_user`, viết dạng danh sách lặp-được để 4 service deferred sau chỉ cần thêm tên vào mảng.
  [`init-multiple-dbs.sh:9`](../../docker/postgres/init-multiple-dbs.sh#L9)

**Container hardening (patch sau review adversarial)**

- Non-root user + HEALTHCHECK thêm sau khi review chỉ ra container chạy root và thiếu healthcheck gây race condition.
  [`Dockerfile:14`](../../user-service/Dockerfile#L14)

**Peripherals**

- Test scaffold hiện chỉ `contextLoads()` rỗng, cần Postgres thật đang chạy — đã ghi vào deferred-work để bổ sung test profile sau.
  [`UserServiceApplicationTests.java:1`](../../user-service/src/test/java/com/solehealth/user/UserServiceApplicationTests.java#L1)
