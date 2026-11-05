# Bộ kiểm thử Mobile SHOP — Thứ tự thao tác

## Cấu hình xác thực khi dùng Postman

1. Tạo request **GET http://localhost:8080/login**. Copy giá trị `_csrf` từ input ẩn trong HTML trang đăng nhập; bảo đảm Postman lưu cookie `JSESSIONID`.
2. **POST http://localhost:8080/login** → **Body → x-www-form-urlencoded**: `username=admin`, `password=Admin@123`, `_csrf=<giá trị lấy ở bước 1>`; dùng cùng cookie. Tránh tự động theo redirect nếu khó quan sát mã 302.
3. **GET http://localhost:8080/api/session/csrf** → lấy JSON `token`. Dùng header **X-CSRF-TOKEN: <token>** cho mọi request có dữ liệu (POST/PUT/PATCH/DELETE) bên dưới. Giữ cookie phiên đăng nhập.
4. Khi kiểm thử upload ảnh, không tự đặt `Content-Type: application/json`: Postman cần tự đặt `multipart/form-data` kèm boundary.

> Các request tạo/cập nhật phải thay `{id}` bằng ID thực lấy từ bước trước. Sau khi logout, các API yêu cầu đăng nhập phải bị chặn/chuyển đến login.

| STT | Mục tiêu | Method & URL | Body / kết quả kỳ vọng |
|---:|---|---|---|
| 1 | Trang sản phẩm đầu | GET `/api/products?page=0&size=10` | 200, JSON có `content` và `totalElements` |
| 2 | Tìm Brand gần đúng | GET `/api/products?brand=Sam&page=0&size=10` | 200, chỉ những hãng chứa 'sam' (không phân biệt hoa thường) |
| 3 | Lọc giá | GET `/api/products?minPrice=10000000&maxPrice=30000000` | 200, sản phẩm có giá trong khoảng |
| 4 | Lọc tồn kho | GET `/api/products?minStock=10` | 200, tồn kho từ 10 trở lên |
| 5 | Phân trang và sắp xếp | GET `/api/products?page=0&size=2&sortBy=price&direction=asc` | 200, 2 sản phẩm/trang, giá tăng |
| 6 | Kiểm tra giá sai | GET `/api/products?minPrice=300&maxPrice=100` | 400 |
| 7 | Upload ảnh hợp lệ | POST `/api/uploads/images` | Body form-data `file` (chọn **File** .png/.jpg trên máy); 200 + `imageUrl` |
| 8 | Upload file không hợp lệ | POST `/api/uploads/images` | file .txt/.svg hoặc giả ảnh; 400, có thông báo |
| 9 | Thêm sản phẩm | POST `/api/products` | JSON với imageUrl từ bước 7; 201 và lưu MySQL |
| 10 | Sửa sản phẩm | PUT `/api/products/{id}` | JSON giá/tồn kho khác, giữ ID; 200 |
| 11 | Không cho giá âm | POST `/api/products` | `price:-100` → 400 |
| 12 | Danh sách khách hàng | GET `/api/customers?page=0&size=10` | 200 dạng phân trang |
| 13 | Thêm khách hàng | POST `/api/customers` | 201, trả ID |
| 14 | Email trùng | POST `/api/customers` | Email đã có → 409 |
| 15 | Sửa khách hàng | PUT `/api/customers/{id}` | 200, ID không đổi |
| 16 | Tạo hóa đơn | POST `/api/invoices` | `{"customerId":1,"items":[{"productId":1,"quantity":2}]}` → 201, tồn giảm 2 |
| 17 | Kiểm tra tồn sau mua | GET `/api/products/1` | Stock giảm đúng số lượng |
| 18 | Tạo đơn vượt tồn | POST `/api/invoices` | quantity > stock → 409, tồn không đổi |
| 19 | Tìm hóa đơn gần đúng | GET `/api/invoices?customer=Nguyen` | 200 danh sách theo tên KH |
| 20 | Lọc hóa đơn theo ngày | GET `/api/invoices?date=2026-11-05` | 200 các hóa đơn lập trong ngày |
| 21 | Chi tiết hóa đơn | GET `/api/invoices/{id}` | 200, thông tin từng sản phẩm/giá tại lúc mua |
| 22 | Xác nhận hóa đơn | PATCH `/api/invoices/{id}/status` | `{"status":"CONFIRMING"}` → 200 |
| 23 | Đang giao | PATCH `/api/invoices/{id}/status` | `{"status":"SHIPPING"}` → 200 |
| 24 | Hoàn thành | PATCH `/api/invoices/{id}/status` | `{"status":"COMPLETED"}` → 200, doanh thu tăng |
| 25 | Báo cáo theo ngày | GET `/api/dashboard/revenue?groupBy=day` | 200, series `{period,revenue}` |
| 26 | Báo cáo theo tháng/năm | GET `/api/dashboard/revenue?groupBy=month` rồi `groupBy=year` | 200, tất cả các kỳ có dữ liệu |
| 27 | Hủy đơn | PATCH `/api/invoices/{id}/status` | `{"status":"CANCELED"}` → 200, tồn hoàn lại |
| 28 | Hủy trùng lần hai | PATCH `/api/invoices/{id}/status` | Cùng status → 200, **không hoàn kho 2 lần** |
| 29 | Xóa sản phẩm có lịch sử | DELETE `/api/products/{id}` | 409, không phá khóa ngoại |
| 30 | Xóa KH có lịch sử | DELETE `/api/customers/{id}` | 409 |
| 31 | Xóa dữ liệu chưa dùng | DELETE `/api/products/{id}` | 204 nếu chưa có hóa đơn |
| 32 | Đăng ký admin | Trang web GET `/register` → POST `/register` | Username duy nhất, password chữ hoa + thường + số + đặc biệt |
| 33 | Đăng nhập sai | POST `/login` | Redirect `/login?error` |
| 34 | Đăng xuất | POST `/logout` với CSRF | Redirect `/login?logout`, phiên hết hiệu lực |

### JSON mẫu

**Sản phẩm:**
```json
{"name":"Samsung Galaxy A55 128GB","brand":"Samsung","price":9800000,"stock":20,"imageUrl":"","status":"ACTIVE"}
```

**Khách hàng:**
```json
{"name":"Nguyễn Văn A","email":"nguyenvana.test@example.com","phone":"0987654321","address":"Hà Nội","active":true}
```

**Hóa đơn:**
```json
{"customerId":1,"items":[{"productId":1,"quantity":2}]}
```

**Lưu ý:** Khi thử trạng thái `CANCELED`, thử trên hóa đơn vừa tạo riêng (hoặc trên đơn hoàn tất nếu muốn mô phỏng hoàn hàng). Dữ liệu demo seed chỉ thêm nếu bảng tương ứng đang trống, không xóa dữ liệu cũ.
