# Khung chức năng — Mobile SHOP

## Quản lý điện thoại

- **Thêm mới:** từ Products → Add Product mở modal. Bắt buộc Name, Brand, Price >= 0, Stock nguyên >= 0; ảnh chỉ được chọn từ file JPG/PNG trên máy (tối đa 5 MB). Validate trước khi lưu. Lưu sản phẩm và đường dẫn ảnh vào CSDL; quay lại danh sách.
- **Cập nhật:** bấm Sửa để mở modal với dữ liệu cũ; **không có trường sửa ID**. Chọn file mới nếu muốn thay ảnh; không chọn thì giữ ảnh cũ.
- **Xóa:** mở modal xác nhận Hủy/OK; không xóa sản phẩm đã có trong đơn hàng.
- **Danh sách:** lấy từ CSDL, phân trang (10/trang mặc định), sắp xếp theo ID/Tên/Giá/Tồn kho tăng/giảm.
- **Tìm kiếm:** Brand dùng `LIKE` không phân biệt chữ hoa/thường; Price `min <= price <= max`; tồn kho theo `stock >= minStock`. Cho phép phối hợp nhiều điều kiện.

## Quản lý khách hàng

- **Thêm/sửa:** modal, kiểm tra thông tin bắt buộc, định dạng email và số điện thoại, email duy nhất. Không sửa ID.
- **Xóa:** xác nhận, dừng nếu bấm Hủy. Khách đã có hóa đơn chỉ cho phép Khóa/Mở để giữ lịch sử.
- **Danh sách:** lấy từ CSDL và có phân trang; hỗ trợ tìm gần đúng tên, email, số điện thoại, địa chỉ.

## Quản lý thông tin mua bán

- **Thêm đơn:** chọn khách, sản phẩm, số lượng. Không vượt tồn kho. Lưu invoice và invoice_items bằng transaction; đồng thời trừ tồn kho tương ứng. Đơn hàng có trạng thái đầu PENDING.
- **Trạng thái:** PENDING → CONFIRMING → SHIPPING → COMPLETED; có thể Hủy, hoàn kho đúng một lần và không cho mở lại hóa đơn đã Hủy.
- **Danh sách:** hóa đơn lấy từ CSDL, có phân trang, xem chi tiết, tìm theo tên khách hàng `LIKE` hoặc ngày lập (ngày cụ thể).
- **Thống kê:** Dashboard lấy tổng doanh thu từ hóa đơn COMPLETED; nhóm theo ngày, tháng, năm, bao gồm toàn bộ các kỳ có dữ liệu (biểu đồ tối đa 12 kỳ gần nhất).

## Xác thực admin

- **Đăng nhập:** bắt buộc trước khi vào trang quản trị, tài khoản MySQL, BCrypt password, báo lỗi nếu sai.
- **Đăng ký:** form riêng, username duy nhất; mật khẩu >=6 ký tự, có chữ thường, chữ hoa, chữ số, ký tự đặc biệt; lưu tài khoản trong MySQL rồi chuyển về login.
- **Đăng xuất:** vô hiệu phiên và chuyển về login.
- **An toàn triển khai:** tắt đăng ký Admin công khai trước khi đưa lên Internet (`ALLOW_PUBLIC_ADMIN_REGISTRATION=false`).
