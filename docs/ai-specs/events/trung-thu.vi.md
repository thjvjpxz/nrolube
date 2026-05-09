# Event Spec - Trung Thu

## 1) Điểm bật/tắt

- Cờ chính: `EventConfig.TRUNG_THU_EVENT`.
- Key config: `event.trung_thu`.

## 2) Luồng khởi tạo

- `EventManager` gọi `new TrungThu().init()` khi cờ ON.
- Manifest tạo boss:
  - `KHIDOT` (10)
  - `NGUYETTHAN` (10)

## 3) Runtime note

- `ServerManager` hiện đang start `TrungThuEventManager` theo thread riêng.
- Khi thay đổi logic bật/tắt Trung Thu, cần rà lại điểm start thread để tránh lệch trạng thái với cờ config.

## 4) Drop theo DB

- `MobRewardService` hỗ trợ key `TRUNG_THU`/`trung_thu`.
- Ưu tiên cấu hình reward qua `mob_reward`.

## 5) Checklist

1. Bật `event.trung_thu`.
2. Kiểm tra spawn `KHIDOT`, `NGUYETTHAN`.
3. Kiểm tra thread event manager chạy đúng theo kỳ vọng.
4. Kiểm tra seasonal drop theo key Trung Thu.
