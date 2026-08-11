# Deferred Work

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Tạo `clinic-service` mirroring cấu trúc `user-service` (package `com.solehealth.clinic`, port 8082, db `health_base_clinic`)
  evidence: Spec gốc vượt 1600 token khi scaffold cả 5 service + gateway cùng lúc; carve off sau khi pattern user-service + gateway đã verify hoạt động

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Tạo `medication-service` mirroring cấu trúc `user-service` (package `com.solehealth.medication`, port 8083, db `health_base_medication`)
  evidence: Spec gốc vượt 1600 token khi scaffold cả 5 service + gateway cùng lúc; carve off sau khi pattern user-service + gateway đã verify hoạt động

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Tạo `diagnostic-service` mirroring cấu trúc `user-service` (package `com.solehealth.diagnostic`, port 8084, db `health_base_diagnostic`)
  evidence: Spec gốc vượt 1600 token khi scaffold cả 5 service + gateway cùng lúc; carve off sau khi pattern user-service + gateway đã verify hoạt động

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Tạo `notification-service` mirroring cấu trúc `user-service` (package `com.solehealth.notification`, port 8085, db `health_base_notification`)
  evidence: Spec gốc vượt 1600 token khi scaffold cả 5 service + gateway cùng lúc; carve off sau khi pattern user-service + gateway đã verify hoạt động

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Đổi `user-service` và `api-gateway` pom.xml từ các artifact test riêng lẻ (`spring-boot-starter-*-test`) sang umbrella `spring-boot-starter-test` nếu xác nhận đây không phải pattern chuẩn của Spring Boot 4.1.0, sau khi build thực tế lần đầu xác nhận có lỗi resolve dependency hay không
  evidence: Review phát hiện pattern non-standard nhưng đã tồn tại sẵn từ `health-base-backend/pom.xml` gốc (pre-existing, không phải do lần tách này gây ra) — cần build thật (docker compose build) để xác nhận có vỡ hay không, môi trường hiện tại không có Docker để verify

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Thêm Spring profile test dùng H2/testcontainers cho `UserServiceApplicationTests`/`ApiGatewayApplicationTests` thay vì yêu cầu Postgres thật đang chạy ở localhost khi `mvn test`
  evidence: Pattern pre-existing từ monolith gốc (test contextLoads() rỗng, phụ thuộc DB thật), giờ nhân đôi ra 2 service — nên xử lý trước khi thêm business logic + test thật

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Thêm test tích hợp cho route gateway (`Path=/api/users/**` + `StripPrefix=2`) thay vì chỉ verify thủ công qua curl
  evidence: Đây là logic mới do lần tách này tạo ra, hiện chưa có test tự động nào bảo vệ khỏi thay đổi prefix/strip-count sai trong tương lai

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Chuyển `DB_PASSWORD`/`POSTGRES_PASSWORD` trong `docker-compose.yml` từ plaintext sang `.env`/secrets pattern
  evidence: Pre-existing từ monolith gốc, không phải do lần tách này gây ra, nhưng JD nhấn mạnh bảo mật cho "vulnerable populations" nên nên xử lý trước khi thêm nghiệp vụ thật

- source_spec: `_bmad-output/implementation-artifacts/spec-backend-microservices-gateway.md`
  summary: Thêm error-handling contract thống nhất ở `api-gateway` (route-not-found, backend-unreachable) khớp format `ErrorResponse` hiện có ở `user-service`, thay vì dùng default error body của Spring Boot
  evidence: Gateway hiện không có `GlobalExceptionHandler`/`ErrorResponse` riêng như user-service — chưa vi phạm acceptance criteria (Spring Boot default error body vẫn đủ rõ ràng) nhưng sẽ càng lệch chuẩn khi thêm nhiều service phía sau gateway
