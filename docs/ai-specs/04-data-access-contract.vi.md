# 04 - Data Access Contract (JDBC/DAO)

## 1) Nguyên tắc bắt buộc

- SQL mới phải nằm trong `src/jdbc` hoặc `src/jdbc/daos`.
- Service/controller không viết SQL inline.
- DAO method phải đặt tên rõ nghĩa và trả kết quả an toàn null.

## 2) Luồng truy cập dữ liệu chuẩn

1. Service nhận request đã qua validation cơ bản.
2. Service gọi DAO/fetcher method.
3. DAO map result về model/dto runtime.
4. Service xử lý business tiếp và phản hồi client.

## 3) Quy tắc thiết kế method DAO

- 1 method = 1 trách nhiệm query/update.
- Input có validation ở service trước khi xuống DAO.
- Có handling cho:
  - empty result
  - malformed data
  - SQL exception
- Logging có context tối thiểu: action, key id, error summary.

## 4) Data safety checklist

- Không đổi schema assumption nếu task không yêu cầu migration.
- Không update hàng loạt với điều kiện mơ hồ.
- Không chèn hardcode id/range không có nguồn gốc.
- Kiểm tra transaction boundary khi thao tác nhiều bảng.

## 5) Contracts cần ghi trong spec feature

Mỗi feature dùng DB cần mô tả rõ:

- Bảng nào được đọc/ghi.
- Điều kiện where chính.
- Trường nào là source of truth.
- Expected fallback khi DB lỗi.

## 6) Risk hotspots

- Mapping data_point/player stat (dễ gây sai combat runtime).
- Các query liên quan account/session/cơ chế anti-login.
- Các query event/time-window dễ sai timezone/period boundary.
