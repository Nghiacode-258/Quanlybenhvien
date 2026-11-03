# XônPanh HMS — Hospital Management System (Spring Boot + Thymeleaf Edition)

Hệ thống Quản lý Bệnh viện XônPanh HMS được chuyển đổi sang nền tảng **Java Spring Boot 3.x + Thymeleaf** (Frontend-only với Mock In-Memory Services), giữ nguyên phong cách thiết kế y tế hiện đại, chuyên nghiệp với Tailwind CSS và Font Awesome.

---

## 0. Kết nối Supabase (đã chuyển toàn bộ dữ liệu sang CSDL thật)

Toàn bộ service giờ đọc/ghi trực tiếp Supabase (PostgreSQL) qua `JdbcTemplate`; **không còn dữ liệu mock trong bộ nhớ**.

1. Supabase > SQL Editor, chạy lần lượt:
   1. `db/schema.sql` (có `DROP TABLE`, xóa dữ liệu cũ — chỉ chạy khi tạo mới)
   2. `db/migration_app.sql` (bổ sung cột/ràng buộc cho ứng dụng; chạy lại được nhiều lần)
   3. `db/seed.sql` (dữ liệu mẫu, gồm NV001..NV006 cho 5 vai trò; chạy lại không trùng)
   4. `db/rls.sql` (bật RLS)
2. Copy `.env.example` thành `.env` và điền mật khẩu DB (đã nằm trong `.gitignore`).
3. `mvnw spring-boot:run` rồi mở http://localhost:8081

Cấu trúc mới: `Repository/*` chứa SQL (mỗi nhóm bảng một lớp), `Service/*` chứa nghiệp vụ + transaction.
Nút "Đặt lại phiên" chỉ xóa vai trò/nhân viên đang chọn trong session, **không** xóa dữ liệu CSDL.
`src/test/.../HmsApplicationTests.java.mock.bak` là test cũ gắn với MockDataStore, giữ lại để tham khảo.

---

## 1. Công nghệ & Kiến trúc

- **Ngôn ngữ:** Java 21+
- **Framework:** Spring Boot 3.3.5 (Spring MVC, Thymeleaf, Spring Boot DevTools)
- **Giao diện:** Thymeleaf Templates, Tailwind CSS (CDN), Font Awesome 6.4
- **Dữ liệu:** In-Memory Mock Services (Thread-safe Concurrent collections), không cần cơ sở dữ liệu thật (không dùng JPA/Hibernate hay PostgreSQL)
- **Cơ chế chuyển vai trò:** Demo workspace selector qua HTTP Session & query parameter `?role=...`

---

## 2. Cấu trúc thư mục dự án

```text
HMS-WEB/
├── pom.xml                               # Cấu hình Maven Spring Boot 3.3.5, Java 21
├── mvnw / mvnw.cmd                       # Maven Wrapper tiện dụng
├── src/main/java/com/hms/
│   ├── HmsApplication.java              # Main Application Class
│   ├── controller/                      # Spring MVC Controllers
│   │   ├── GlobalControllerAdvice.java  # Tự động inject activeRole, roles, today
│   │   ├── RoleSelectionController.java # Điều hướng /, /select-role/{role}, /reset-demo
│   │   ├── DashboardController.java     # /dashboard (Quản lý bệnh viện)
│   │   ├── RegistrationController.java  # /registration (Y tá & Thu ngân: tiếp đón, phiếu khám)
│   │   ├── ExaminationController.java   # /examination, /examination/detail/{id} (Bác sĩ)
│   │   ├── LaboratoryController.java    # /laboratory (Xét nghiệm cận lâm sàng)
│   │   ├── PharmacyController.java      # /pharmacy (Dược sĩ: kho thuốc, cấp phát đơn)
│   │   ├── BillingController.java       # /billing, /billing/print/{id} (Thu ngân)
│   │   ├── MedicalRecordController.java # /medical-records (Hồ sơ bệnh án)
│   │   ├── StaffController.java         # /staff (Danh bạ nhân viên, phòng khám)
│   │   └── ReportController.java        # /reports, /reports/export-csv (Báo cáo thống kê)
│   ├── model/                           # Entity & Domain Models (Patient, Examination, Invoice, ...)
│   ├── dto/                             # Form DTOs (PatientFormDto, ExamFormDto, DoctorExamDto, ...)
│   └── mock/                            # Mock Data Store & Mock Services
│       ├── MockDataStore.java           # Dữ liệu mẫu khởi tạo đồng bộ
│       ├── MockPatientService.java      # Quản lý bệnh nhân, người nhà, BHYT
│       ├── MockExaminationService.java  # Phiếu khám, chỉ định XN, kê đơn thuốc
│       ├── MockPharmacyService.java     # Kho dược, quản lý lô, cấp phát FEFO
│       ├── MockBillingService.java      # Hóa đơn, thanh toán, doanh thu
│       └── MockStaffService.java        # Nhân viên, phòng khám, chuyên khoa
└── src/main/resources/
    ├── application.properties           # Cấu hình server port 8080, Thymeleaf cache
    ├── static/
    │   ├── css/style.css                # CSS bổ trợ, in hóa đơn, responsive
    │   └── js/app-interactions.js       # JS tương tác tối thiểu (thêm dòng thuốc, modal)
    └── templates/                       # Thymeleaf HTML Templates
        ├── fragments/                   # Layout, Sidebar theo vai trò, Header, Modals
        ├── auth/role-select.html        # Trang chọn vai trò / không gian làm việc
        ├── dashboard/index.html         # Tổng quan KPI & biểu đồ (Quản lý)
        ├── registration/index.html      # Tiếp đón & đăng ký phiếu khám
        ├── examination/                 # Khám bệnh & form kê đơn, chỉ định xét nghiệm
        ├── laboratory/index.html        # Xét nghiệm cận lâm sàng
        ├── pharmacy/index.html          # Dược & cấp phát thuốc
        ├── billing/                     # Viện phí, thanh toán & in hóa đơn
        ├── medical-records/index.html   # Tra cứu hồ sơ bệnh án
        ├── staff/index.html             # Quản lý nhân sự & phòng ban
        └── reports/index.html           # Báo cáo thống kê & xuất CSV
```

---

## 3. Hướng dẫn cài đặt & Khởi chạy

### Yêu cầu tiên quyết
- **JDK 21** trở lên đã cài đặt trên máy.
- Biến môi trường `JAVA_HOME` trỏ tới JDK.

### Khởi chạy ứng dụng
Mở terminal tại thư mục gốc của dự án (`HMS-WEB`):

```bash
# Sử dụng Maven Wrapper có sẵn trong dự án:
./mvnw spring-boot:run

# Hoặc nếu máy đã cài Maven:
mvn spring-boot:run
```

Sau khi ứng dụng khởi động thành công:
- Truy cập trình duyệt tại: **`http://localhost:8080/`**

---

## 4. 5 Vai trò làm việc (Workspaces)

Hệ thống có trang mở đầu tại **`http://localhost:8080/`** cho phép chọn 1 trong 5 vai trò demo:

| STT | Vai trò | URL làm việc mặc định | Quyền hạn & Phân hệ được truy cập |
|---|---|---|---|
| 1 | **Quản lý bệnh viện** | `/dashboard` | Toàn quyền: Dashboard KPI, Tiếp đón, Khám bệnh, Xét nghiệm, Dược, Viện phí, Hồ sơ bệnh án, Nhân sự, Báo cáo & Xuất CSV |
| 2 | **Bác sĩ** | `/examination` | Danh sách khám, Màn hình chi tiết khám, Triệu chứng, Chọn ICD-10, Chỉ định xét nghiệm, **Form kê đơn thuốc**, Hồ sơ bệnh án |
| 3 | **Y tá / Điều dưỡng** | `/registration` | Danh sách bệnh nhân (thêm/sửa, người nhà, BHYT), Đăng ký phiếu khám, Xét nghiệm cận lâm sàng (`/laboratory`), Hồ sơ bệnh án |
| 4 | **Dược sĩ** | `/pharmacy` | Đơn thuốc chờ cấp phát, Chi tiết đơn, Xác nhận cấp phát trừ kho theo FEFO, Danh mục thuốc, Danh sách lô thuốc |
| 5 | **Thu ngân** | `/billing` | Danh sách phiếu thu viện phí, Lọc trạng thái, Xác nhận thanh toán, In hóa đơn (`/billing/print/{id}`), Đăng ký khám (`/registration`) |

> **Lưu ý:** Nút **"Đổi vai trò"** và menu chuyển vai trò nhanh trên thanh Header hỗ trợ chuyển đổi giữa các vai trò bất kỳ lúc nào để duyệt giao diện mà không cần đăng nhập lại. Nút **"Đặt lại dữ liệu"** giúp khôi phục dữ liệu mẫu ban đầu.

---

## 5. Các tính năng nổi bật đã hoàn thiện

1. **Form Kê đơn thuốc Bác sĩ:**
   - Được tích hợp trực tiếp trong màn hình chi tiết phiếu khám (`/examination/detail/{id}`).
   - Cho phép chọn thuốc từ danh mục, nhập số lượng, đơn vị tính, cách dùng.
   - Hỗ trợ thêm/bớt nhiều dòng thuốc linh hoạt trước khi lưu.
   - Khi lưu, tự động sinh mã đơn thuốc `DT...` và tạo hóa đơn viện phí `HD...` chờ thanh toán.

2. **Chuyển phần Tiếp đón / Đăng ký khám cho Thu ngân:**
   - Thu ngân có toàn quyền truy cập phân hệ Tiếp đón & Đăng ký khám (`/registration`) cũng như thanh toán viện phí (`/billing`).

3. **Cấp phát thuốc Dược sĩ (FEFO):**
   - Dược sĩ xem danh sách đơn chờ cấp phát, bấm xác nhận trừ số lượng tồn kho của các lô gần hạn dùng nhất trước.

4. **In Hóa đơn Viện phí:**
   - Giao diện in tiêu chuẩn chuẩn y tế tại `/billing/print/{id}` tự động kích hoạt `window.print()`.
