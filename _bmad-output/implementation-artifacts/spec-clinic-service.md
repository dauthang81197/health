---
title: 'Mirror clinic-service theo pattern user-service + route qua api-gateway'
type: 'feature'
created: '2026-08-11'
status: 'done'
review_loop_iteration: 0
context: ['{project-root}/_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md']
baseline_commit: '5df9c1a187a50e31810f12c338ec8b35335c2d3e'
---

<frozen-after-approval reason="human-owned intent — do not modify unless human renegotiates">

## Intent

**Problem:** Deferred-work liệt kê `clinic-service` là service tiếp theo cần mirror pattern `user-service` (đã done), nhưng chưa được tạo — hệ thống hiện chỉ có 1/4 service mirror hoàn tất.

**Approach:** Tạo mới `clinic-service` (Spring Boot, package `com.solehealth.clinic`, port 8082, db `health_base_clinic`) sao chép chính xác cấu trúc `user-service` hiện có (pom.xml, Dockerfile, SecurityConfig, exception handling, application.yml — chỉ scaffold, chưa business logic), thêm route mới trong `api-gateway`, đăng ký db mới trong init script, và thêm service vào `docker-compose.yml`.

## Boundaries & Constraints

**Always:**
- `clinic-service` là Spring Boot app độc lập (pom.xml, package `com.solehealth.clinic`, application.yml, Dockerfile riêng) — không Maven multi-module, không CORS config riêng.
- Copy nguyên logic `user-service` (SecurityConfig stateless/permitAll, GlobalExceptionHandler/ErrorResponse/ResourceNotFoundException) — chỉ đổi namespace.
- Port 8082, db `health_base_clinic`.
- Gateway thêm route `id: clinic-service`, `uri: http://clinic-service:8082`, `Path=/api/clinics/**`, `StripPrefix=2` -- không sửa route `user-service` hiện có.
- `docker-compose.yml`: thêm service `clinic-service` (build `./clinic-service`, env `DB_*`, `SERVER_PORT=8082`, `depends_on: postgres` healthy, không publish port host); `api-gateway.depends_on` thêm `clinic-service` healthy.
- `init-multiple-dbs.sh`: chỉ append `"health_base_clinic"` vào mảng `DATABASES` -- không viết lại script.
- Chỉ scaffold (health check, exception handler, datasource, security) — không entity/controller/business logic.

**Ask First:**
- Nếu port 8082 xung đột với tiến trình khác đang chạy trên máy → hỏi trước khi đổi port.

**Never:**
- Không tạo `medication-service`/`diagnostic-service`/`notification-service` trong spec này (đã deferred).
- Không thêm entity/controller/business logic, Kafka/Redis/ElasticSearch/GraphQL/Eureka, hoặc auth/JWT thật.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Healthy stack | `docker compose up -d --build` | postgres + user-service + clinic-service + api-gateway chạy, không crash loop | N/A |
| Gateway route clinic | `GET localhost:8080/api/clinics/actuator/health` | 200, proxy tới clinic-service, `{"status":"UP"}` | N/A |
| Route cũ không vỡ | `GET localhost:8080/api/users/actuator/health` | vẫn 200 UP như trước | N/A |
| clinic-service down | clinic-service dừng, gọi `GET localhost:8080/api/clinics/**` | Gateway trả 502/503 rõ ràng | Gateway không crash |
| DB isolation | `docker compose exec postgres psql -U health_base -l` (fresh volume) | Có cả `health_base_user` và `health_base_clinic` riêng biệt | N/A |

</frozen-after-approval>

## Code Map

- `user-service/**` -- template gốc, copy 1:1 sang clinic-service, chỉ đổi package/tên/port/db
- `api-gateway/src/main/resources/application.yml`, `docker-compose.yml`, `docker/postgres/init-multiple-dbs.sh` -- 3 file dùng chung cần cập nhật để onboard service mới

## Tasks & Acceptance

**Execution:**
- [x] `clinic-service/` -- Tạo toàn bộ cấu trúc (pom.xml, Dockerfile, application.yml, `com.solehealth.clinic.ClinicServiceApplication`, `common/config/SecurityConfig`, `common/exception/{ErrorResponse,GlobalExceptionHandler,ResourceNotFoundException}`, test class, .gitignore, .gitattributes, .mvn/, mvnw*) -- copy 1:1 pattern user-service, đổi namespace/port 8082/db health_base_clinic
- [x] `api-gateway/src/main/resources/application.yml` -- Thêm route `clinic-service` (`uri: http://clinic-service:8082`, `Path=/api/clinics/**`, `StripPrefix=2`) -- mở route proxy
- [x] `docker-compose.yml` -- Thêm service `clinic-service` (build `./clinic-service`, env `DB_*`, `SERVER_PORT=8082`, `depends_on: postgres` healthy); thêm `clinic-service: condition: service_healthy` vào `depends_on` của `api-gateway` -- đưa vào stack cục bộ
- [x] `docker/postgres/init-multiple-dbs.sh` -- Append `"health_base_clinic"` vào mảng `DATABASES` -- tạo db riêng khi postgres init lần đầu

**Acceptance Criteria:**
- Given `clinic-service/pom.xml` mới, when `docker compose build clinic-service`, then build image thành công, không lỗi resolve dependency.
- Given toàn bộ thay đổi đã áp dụng, when diff `user-service/**` và route `user-service` trong gateway, then không có sửa đổi nào ngoài việc thêm mới clinic-service.

## Spec Change Log

## Design Notes

Init script Postgres chỉ chạy khi volume `health-base-pgdata` chưa tồn tại — verify cần `docker compose down -v` trước `up --build`, nếu không db mới không xuất hiện dù script đã đúng.

## Verification

**Commands:**
- `docker compose down -v && docker compose up -d --build` -- expected: 4 container (postgres, user-service, clinic-service, api-gateway) running/healthy, không restart loop
- `curl -s localhost:8080/api/clinics/actuator/health` -- expected: `{"status":"UP"}`
- `curl -s localhost:8080/api/users/actuator/health` -- expected: `{"status":"UP"}` (regression check route cũ)
- `docker compose exec postgres psql -U health_base -l` -- expected: liệt kê cả `health_base_user` và `health_base_clinic`

## Suggested Review Order

**Service topology (docker-compose)**

- Service mới `clinic-service` mirror khối `user-service`: build context riêng, không publish port host, chờ postgres healthy.
  [`docker-compose.yml:37`](../../docker-compose.yml#L37)

- `api-gateway` giờ chờ cả `user-service` lẫn `clinic-service` healthy trước khi nhận traffic — điểm cần theo dõi khi thêm service tiếp theo.
  [`docker-compose.yml:63`](../../docker-compose.yml#L63)

**Gateway routing**

- Route thứ 2 `/api/clinics/**` → clinic-service, strip 2 segment, đặt ngay dưới route `user-service` không sửa route cũ.
  [`application.yml:19`](../../api-gateway/src/main/resources/application.yml#L19)

**clinic-service scaffold (mirror user-service)**

- Port 8082 + db riêng `health_base_clinic` — instance thứ 2 của pattern database-per-service.
  [`application.yml:6`](../../clinic-service/src/main/resources/application.yml#L6)

- Package `com.solehealth.clinic`; entrypoint mirror 1:1 `UserServiceApplication`.
  [`ClinicServiceApplication.java:1`](../../clinic-service/src/main/java/com/solehealth/clinic/ClinicServiceApplication.java#L1)

- SecurityConfig stateless/permitAll copy nguyên logic từ user-service — chưa auth thật, đúng scope spec.
  [`SecurityConfig.java:10`](../../clinic-service/src/main/java/com/solehealth/clinic/common/config/SecurityConfig.java#L10)

- GlobalExceptionHandler copy nguyên logic — review đã ghi nhận info-leak/4xx-mapping cần hardening chung, đã đưa vào deferred-work.
  [`GlobalExceptionHandler.java:31`](../../clinic-service/src/main/java/com/solehealth/clinic/common/exception/GlobalExceptionHandler.java#L31)

**Database per service**

- Append `health_base_clinic` vào mảng lặp-được — chỉ chạy trên volume Postgres mới, cần `down -v` khi verify.
  [`init-multiple-dbs.sh:11`](../../docker/postgres/init-multiple-dbs.sh#L11)

**Peripherals**

- Dockerfile đổi port EXPOSE/HEALTHCHECK sang 8082, giữ nguyên non-root user + healthcheck pattern.
  [`Dockerfile:18`](../../clinic-service/Dockerfile#L18)

- Test scaffold `contextLoads()` rỗng, vẫn phụ thuộc Postgres thật — cùng vấn đề đã ghi nhận cho user-service, giờ nhân đôi.
  [`ClinicServiceApplicationTests.java:7`](../../clinic-service/src/test/java/com/solehealth/clinic/ClinicServiceApplicationTests.java#L7)