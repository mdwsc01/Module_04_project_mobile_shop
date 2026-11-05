# Mobile SHOP — Website quản lý cửa hàng điện thoại (bản cập nhật chức năng)

Dự án Java Spring Boot + MySQL + HTML/CSS/JavaScript theo file Figma người dùng cung cấp. **Giữ giao diện quản trị, bổ sung các chức năng backend, phân trang và upload ảnh từ máy tính.**

## 1. Công nghệ / Dependencies

- Java **17**, Spring Boot 3.5.16, Maven
- Spring Web, Spring Data JPA, Spring Validation, Spring Security, Thymeleaf
- MySQL Connector/J, Spring Boot Starter Test, Spring Security Test
- Frontend: HTML + CSS + JavaScript thuần (không cần Node/npm)

## 2. Chạy trên IntelliJ + Laragon

1. Giải nén ZIP; IntelliJ IDEA → **Open** → thư mục `MobileShop` chứa `pom.xml`.
2. Chọn JDK 17 trong **File → Project Structure**, sau đó **Reload Maven**.
3. Bật **Laragon → Start All** để chạy MySQL (thông thường username `root`, password rỗng).
4. Đọc `src/main/resources/application.properties`. URL MySQL đã có `createDatabaseIfNotExist=true` để **tạo `mobile_shop` tự động** khi tài khoản có quyền tạo database.
5. Chạy `MobileShopApplication.java` → mở **http://localhost:8080**.
6. **Tài khoản demo khi database `admin_accounts` trống:** `admin` / `Admin@123`. Tài khoản được thêm vào MySQL và mật khẩu lưu bằng BCrypt, không lưu dạng plain text. Thay bằng `ADMIN_USERNAME`, `ADMIN_PASSWORD` trong môi trường thật.
7. Đăng ký thêm tài khoản ở **http://localhost:8080/register**. Password **>= 6 ký tự** và gồm chữ hoa, chữ thường, số, ký tự đặc biệt; username không được trùng.
8. Đăng xuất bằng nút **Sign out**.

> **Quan trọng về bảo mật:** Ví dụ này dùng trang đăng ký công khai để làm theo yêu cầu bài tập. Khi triển khai lên Internet, đặt `ALLOW_PUBLIC_ADMIN_REGISTRATION=false` để tránh bất kỳ ai tự cấp tài khoản Admin. Sau đó cần quy trình tạo admin có kiểm soát. Đổi cả mật khẩu admin demo. Không commit mật khẩu thật.

## 3. Upload ảnh điện thoại từ máy tính

Vào **Products → Add Product → Ảnh sản phẩm — chọn từ máy tính → Choose File**. Hộp thoại chọn file trên Windows hiện lên; chọn **.jpg / .jpeg / .png**, tối đa **5 MB**, rồi xem ảnh preview ngay trong form.

Khi bấm **Lưu**:

1. JS gửi file ảnh bằng `multipart/form-data` tới `POST /api/uploads/images` (field **`file`**).
2. Spring Boot kiểm tra MIME, nội dung ảnh thực, kích thước file và kích thước pixel.
3. File được lưu vào thư mục `uploads/` ngoài JAR bằng tên UUID ngẫu nhiên (cấu hình `FILE_UPLOAD_DIR`).
4. API trả `{ "imageUrl": "/uploads/<uuid>.jpg" }`. JS gửi `imageUrl` này qua API tạo/sửa sản phẩm để lưu vào MySQL.
5. Trở về danh sách điện thoại, ảnh hiển thị ở cột Name. **Khi sửa, nếu không chọn file mới thì giữ ảnh cũ.**

Không dùng trường Image URL phải gõ tay; đường dẫn gửi trong JSON bị kiểm tra để chỉ cho phép đường dẫn ảnh từ thư mục uploads. Nếu database cũ có `imageUrl` dạng link HTTP từ bản starter, hãy chọn ảnh từ máy và lưu sản phẩm lại để chuyển sang chuẩn `/uploads/...`. Những file upload bị thay thế/xóa sản phẩm không được tự xóa vật lý (nên dọn ảnh mồ côi định kỳ khi mở rộng dự án).

> **Không** mở file HTML riêng qua `file://`: chạy qua Spring Boot để upload và load ảnh. Ảnh được lưu trên **ổ đĩa máy chạy server**, không phải trong MySQL; MySQL chỉ lưu đường dẫn tương đối.

## 4. Chức năng đã bổ sung theo bảng yêu cầu

| Nhóm | Chức năng |
|---|---|
| Sản phẩm | Thêm/sửa bằng modal, validate ở giao diện và server, xóa có xác nhận, lấy danh sách từ MySQL |
| Sản phẩm | Phân trang server-side; lọc gần đúng Brand; lọc giá Min/Max; tìm sản phẩm có tồn kho **>=** ngưỡng nhập; sắp xếp theo mã, tên, giá hoặc tồn |
| Sản phẩm | Chọn file ảnh JPG/PNG từ máy; upload lên server; xem trước ảnh |
| Khách hàng | Thêm/sửa/xóa có modal và xác nhận; email duy nhất; phân trang, tìm kiếm gần đúng |
| Hóa đơn | Chọn khách hàng, thêm sản phẩm và số lượng; trừ tồn kho với khóa dòng và transaction; không cho vượt tồn |
| Hóa đơn | Chuyển trạng thái `PENDING → CONFIRMING → SHIPPING → COMPLETED`; cho phép hủy hóa đơn; hoàn tồn **một lần** |
| Hóa đơn | Danh sách có phân trang; tìm gần đúng tên khách hàng và tìm theo ngày lập; xem chi tiết |
| Dashboard | Đếm khách hàng, điện thoại, hóa đơn; tổng doanh thu chỉ gồm đơn COMPLETED; thống kê doanh thu theo **ngày / tháng / năm** (toàn bộ các kỳ có dữ liệu) |
| Admin | Đăng nhập bằng tài khoản MySQL, mật khẩu BCrypt; đăng ký trùng username không được; lỗi login; logout |

**Quy tắc bảo toàn lịch sử:** Không cho xóa khách hàng hoặc điện thoại đã có trong hóa đơn. Thay vào đó, sử dụng nút Khóa/Ngừng bán. Cho phép hủy kể cả hóa đơn đã hoàn tất để xử lý trường hợp hoàn/huỷ đơn, nhưng chỉ nên làm theo quy trình nghiệp vụ của cửa hàng. Doanh thu được tính theo thời điểm hoàn thành (`completedAt`), hóa đơn cũ dùng `createdAt` nếu chưa có thời điểm hoàn thành.

## 5. API REST (phải đăng nhập)

| Phương thức | Endpoint | Ý nghĩa |
|---|---|---|
| GET | `/api/products?page=0&size=10&brand=Sam&minPrice=1000000&maxPrice=30000000&minStock=5&sortBy=price&direction=asc` | Trang sản phẩm lọc/sắp xếp |
| GET | `/api/products/options` | Tất cả điện thoại cho dropdown hóa đơn |
| POST | `/api/uploads/images` | **Body → form-data → `file` kiểu File** |
| POST / PUT | `/api/products` / `/api/products/{id}` | Thêm/cập nhật điện thoại |
| DELETE | `/api/products/{id}` | Xóa (nếu chưa nằm trong hóa đơn) |
| GET | `/api/customers?page=0&size=10&keyword=An` | Danh sách khách hàng |
| GET | `/api/customers/options` | Tất cả khách hàng cho dropdown |
| POST / PUT / DELETE | `/api/customers` / `/api/customers/{id}` | CRUD khách hàng |
| GET | `/api/invoices?page=0&size=10&customer=Nguyen&date=2026-11-05` | Tìm hóa đơn theo tên và/hoặc ngày |
| GET | `/api/invoices/{id}` | Chi tiết hóa đơn |
| POST | `/api/invoices` | Tạo hóa đơn, trừ hàng tồn |
| PATCH | `/api/invoices/{id}/status` | Cập nhật trạng thái và hoàn tồn khi hủy |
| GET | `/api/dashboard` | Các chỉ số tổng quan |
| GET | `/api/dashboard/revenue?groupBy=day` | Doanh thu theo ngày (`month`, `year` cũng được) |
| GET / POST | `/register` | Form / submit đăng ký Admin công khai khi bật cờ cấu hình |
| GET / POST | `/login` | Đăng nhập bằng Spring Security session |
| POST | `/logout` | Đăng xuất (có CSRF) |

API GET dạng `Page` trả `content`, `totalElements`, `totalPages`, `number`, `size`. `page` bắt đầu từ **0**; `size` từ **1 đến 100**. Bộ lọc số lượng tồn kho mang nghĩa *tối thiểu còn bao nhiêu chiếc*, không phải khớp chuỗi gần đúng; Brand và tên khách hàng mới là bộ lọc **LIKE**.

### Body mẫu

Tạo sản phẩm khi đã upload ảnh:

```json
{"name":"iPhone 16 Plus 128GB","brand":"Apple","price":23990000,"stock":10,"imageUrl":"/uploads/<uuid-tra-ve-tu-api>.jpg","status":"ACTIVE"}
```

Nếu chưa có ảnh, gửi `"imageUrl":""`.

Tạo khách hàng:

```json
{"name":"Nguyễn Văn Nam","email":"nam@example.com","phone":"0981122334","address":"Hà Nội","active":true}
```

Tạo đơn (ID phải tồn tại):

```json
{"customerId":1,"items":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}
```

Chuyển trạng thái:

```json
{"status":"CONFIRMING"}
```

Trạng thái hợp lệ: `PENDING`, `CONFIRMING`, `SHIPPING`, `COMPLETED`, `CANCELED`. Một hóa đơn đã hủy không thể chuyển sang trạng thái khác; cập nhật cùng trạng thái không cộng/trừ tồn lần hai.

## 6. Kiểm thử

- Cách dễ nhất: đăng nhập trong trình duyệt và thao tác theo thứ tự trong `POSTMAN_TESTS.md`.
- Nếu dùng Postman, phải đăng nhập và duy trì cookie **JSESSIONID**, đồng thời gửi header **X-CSRF-TOKEN** khi POST/PUT/PATCH/DELETE. Đọc ví dụ trong `POSTMAN_TESTS.md`.
- Kiểm thử đơn vị có trong `src/test/java`: đăng ký, upload, lọc sản phẩm, tạo/hủy đơn. Chạy `mvn test` hoặc Maven → test trong IntelliJ.

## 7. Cấu trúc quan trọng

```text
src/main/java/com/example/mobileshop/
├── config/        SecurityConfig, AdminSeedConfig, DemoDataConfig, UploadResourceConfig
├── controller/    ProductController, CustomerController, InvoiceController, DashboardController,
│                 RegistrationController, ImageUploadController, ViewController, SessionController
├── dto/           ProductRequest, CustomerRequest, InvoiceRequest, RegisterRequest...
├── entity/        Product, Customer, Invoice, InvoiceItem, AdminAccount, trạng thái
├── repository/    ProductRepository, CustomerRepository, InvoiceRepository, AdminAccountRepository...
├── service/       ProductService, CustomerService, InvoiceService,
│                 ImageStorageService, RegistrationService
└── exception/     GlobalExceptionHandler
src/main/resources/
├── application.properties
├── templates/     index.html, login.html, register.html
└── static/        css/app.css, js/app.js
```

### Giới hạn và lưu ý

Đây là dự án dùng để học tập, không phải hệ thống đã kiểm thử tải lớn hoặc kiểm toán bảo mật. Chưa có phân quyền theo nhân viên, xử lý đổi/trả/phí ship/giảm giá, đóng băng sửa dữ liệu lịch sử ở mức DBA, dọn ảnh mồ côi hoặc triển khai cloud. `ddl-auto=update` tiện cho bài tập; production nên dùng Flyway/Liquibase. Cổng mặc định 8080. Vì môi trường tạo file không có Maven và MySQL đang chạy, **chưa thể xác nhận build Spring Boot + chạy database end-to-end**; JavaScript đã qua kiểm tra cú pháp, mã có bài test để bạn chạy sau khi import.
