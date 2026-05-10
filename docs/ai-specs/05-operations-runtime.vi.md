# 05 - Operations, Runtime Và Vận Hành

## 1) Phạm vi

Spec này tập trung vào startup, maintenance, và runtime control:

- `src/server/ServerManager.java`
- `src/server/Manager.java`
- `src/server/Maintenance.java`
- session/network runtime liên quan

## 2) Vòng đời runtime (tổng quát)

1. Bootstrap config + data.
2. Init map/player/system managers.
3. Mở server socket và bắt đầu nhận kết nối.
4. Chạy event loop, session handling, business dispatch.
5. Maintenance/safe shutdown khi cần.

## 3) Rule an toàn vận hành

- Thay đổi maintenance flow phải đảm bảo save/flush dữ liệu.
- Không chèn business logic vào startup code.
- Runtime control và gameplay logic phải tách biệt.
- Chỉnh sửa session/rate checks cần test tải cao.

## 4) Failure handling

- Nếu lỗi startup:
  - dừng khởi động sớm (fail fast),
  - log rõ subsystem bị lỗi.
- Nếu lỗi runtime:
  - ưu tiên bảo toàn kết nối và dữ liệu,
  - tránh crash lan rộng.

## 5) Checklist cho task ops/runtime

- Có ảnh hưởng đến init order không.
- Có ảnh hưởng đến reconnect/session state không.
- Có ảnh hưởng đến maintenance command không.
- Có ảnh hưởng đến graceful shutdown không.

## 6) Mục tiêu khi AI sửa runtime

- Giữ hành vi hệ thống ổn định.
- Giảm regression trên kết nối và message flow.
- Mọi thay đổi đều có rollback path rõ ràng.
