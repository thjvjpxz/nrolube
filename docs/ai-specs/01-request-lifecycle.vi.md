# 01 - Request Lifecycle Và Message Flow

## 1) Luồng tổng quát

1. Client gửi packet/message vào session.
2. Transport layer đọc và đóng gói message object.
3. Controller/handler parse command + payload.
4. Service thực thi business rule.
5. Service tạo response message.
6. Player/session sender đẩy message về client.

## 2) Flow đã xác nhận từ GitNexus

Các process trace tiêu biểu:

- `useItem` -> `sendThongBao` -> `player.sendMessage` -> `session.sendMessage` -> `sender.sendMessage` -> `isConnected`
- `createClan` -> `sendThongBao` -> `player.sendMessage` -> `session.sendMessage` -> `sender.sendMessage` -> `isConnected`
- Transaction-like action có pattern tương tự.

Ý nghĩa: output path thông báo/phản hồi đang hội tụ về cùng một transport chain, cần tránh sửa tản mạn ở nhiều điểm.

## 3) Trách nhiệm theo tầng

### Controller/handler

- Parse input.
- Validate format cơ bản (null, length, type).
- Route đúng service.

### Service

- Validate business.
- Gọi DAO nếu cần đọc/ghi DB.
- Quyết định response type/message.

### Session/sender

- Chỉ làm nhiệm vụ truyền message.
- Không đặt business logic trong sender.

## 4) Điều cần tránh

- Parse phức tạp và business chèn vào controller.
- Gọi SQL trực tiếp ở handler/service routing.
- Nhân đôi logic response ở nhiều service khác nhau.

## 5) Pattern thêm tính năng an toàn

1. Xác định command hoặc điểm vào.
2. Tìm service hiện tại xử lý command đó.
3. Thêm validation nhỏ gọn ở service.
4. Nếu cần DB, thêm method DAO gọn.
5. Tạo response thống nhất với flow sendMessage hiện tại.

## 6) Pattern debug

- Bắt đầu từ symptom trên client.
- Liên kết về message type/command.
- Lần theo chain: handler -> service -> player/session sender.
- Nếu có bất thường ở response, check `isConnected` và session state.
