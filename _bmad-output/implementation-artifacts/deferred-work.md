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

- source_spec: `_bmad-output/implementation-artifacts/spec-clinic-service.md`
  summary: Route gateway `Path=/api/{service}/**` không match request đúng bằng prefix không có trailing segment (vd `GET /api/clinics` trả 404 thay vì proxy tới clinic-service)
  evidence: Edge Case Hunter phát hiện — pattern pre-existing từ route `user-service` gốc (đã có từ spec trước), lần này chỉ nhân đôi qua route `clinic-service` mới; sửa sẽ cần đổi predicate `Path=/api/{service},/api/{service}/**` cho cả 2 route

- source_spec: `_bmad-output/implementation-artifacts/spec-clinic-service.md`
  summary: `docker/postgres/init-multiple-dbs.sh` chỉ chạy khi volume `health-base-pgdata` chưa tồn tại (first init) — cần migration path (Flyway/Liquibase hoặc script riêng) để tạo db mới trên volume đã tồn tại từ trước, thay vì chỉ dựa vào `docker-entrypoint-initdb.d` + hướng dẫn thủ công `down -v`
  evidence: Blind Hunter + Edge Case Hunter cùng phát hiện — dev có volume `health-base-pgdata` sẵn từ khi verify user-service, pull spec clinic-service, chạy `docker compose up -d --build` không kèm `down -v` sẽ khiến clinic-service crash-loop vì thiếu db; spec hiện chỉ note caveat này ở Design Notes/Verification, chưa có safeguard code

- source_spec: `_bmad-output/implementation-artifacts/spec-clinic-service.md`
  summary: Hardening `GlobalExceptionHandler` dùng chung cho user-service + clinic-service (nay đã nhân đôi): catch-all 500 trả `ex.getMessage()` verbatim (leak thông tin), không map `DataIntegrityViolationException`/`HttpMessageNotReadableException`/`MethodArgumentTypeMismatchException` sang 4xx phù hợp, không null-guard khi `ex.getMessage()` là null, và `MethodArgumentNotValidException` bỏ sót `getGlobalErrors()` (lỗi validate cross-field) trong `details`
  evidence: Edge Case Hunter + Blind Hunter cùng phát hiện — logic được yêu cầu copy nguyên bản từ user-service theo frozen spec (không được sửa logic trong scope story này), nên đây là pattern pre-existing giờ nhân đôi, cần xử lý tập trung (vd shared library) trước khi thêm nhiều service nữa

- source_spec: `_bmad-output/implementation-artifacts/spec-clinic-service.md`
  summary: `user-service` và `clinic-service` dùng chung 1 Postgres role (`health_base`) cho cả 2 database riêng — không có privilege isolation thật giữa các service dù đã tách database-per-service
  evidence: Blind Hunter phát hiện — credential/role bị lộ ở 1 service sẽ cho phép truy cập cả database của service khác, làm giảm giá trị cô lập của pattern database-per-service; cần role/user Postgres riêng cho mỗi service

- source_spec: `_bmad-output/implementation-artifacts/spec-clinic-service.md`
  summary: `ClinicServiceApplicationTests.contextLoads()` không có datasource test riêng (H2/Testcontainers) — phụ thuộc Postgres thật đang chạy ở `health_base_clinic`, `mvn test` sẽ fail ngoài docker-compose (vd CI thuần)
  evidence: Edge Case Hunter + Blind Hunter cùng phát hiện — cùng loại vấn đề đã ghi nhận trước đó cho user-service/api-gateway, nay nhân bản thêm 1 instance qua clinic-service

- source_spec: `_bmad-output/implementation-artifacts/spec-clinic-service.md`
  summary: `api-gateway` giờ hard-depend `service_healthy` trên cả `user-service` lẫn `clinic-service` — 1 service fail healthcheck vĩnh viễn sẽ chặn toàn bộ gateway (và cả stack user-facing) khởi động, rủi ro này lớn dần theo mỗi service mirror thêm
  evidence: Blind Hunter phát hiện — pattern nằm trong frozen spec (được yêu cầu tường minh), không phải deviation, nhưng đáng theo dõi khi số service tăng lên; cân nhắc readiness không chặn cứng hoặc circuit-breaker khi mirror 4 service còn lại

- source_spec: `_bmad-output/implementation-artifacts/spec-clinic-service.md`
  summary: Chưa có tài liệu/registry theo dõi mapping service ↔ port ↔ route prefix ↔ db name (hiện: 8081/users/health_base_user, 8082/clinics/health_base_clinic) khi pattern tiếp tục nhân bản cho 4 service còn lại
  evidence: Blind Hunter phát hiện — không có nguồn sự thật duy nhất nào ngoài đọc trực tiếp `docker-compose.yml`/`application.yml`, rủi ro trùng port hoặc lệch convention tăng dần theo mỗi service mirror thêm
