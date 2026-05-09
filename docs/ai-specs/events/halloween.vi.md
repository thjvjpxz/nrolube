# Event Spec - Halloween

## 1) Điểm bật/tắt

- Cờ chính: `EventConfig.HALLOWEEN_EVENT`.
- Key config: `event.halloween`.

## 2) Luồng khởi tạo

- `EventManager` gọi `new Halloween().init()` khi cờ ON.
- Manifest tạo boss:
  - `BIMA` (10)
  - `MATROI` (10)
  - `DOI` (10)
- `ServerManager` start `HalloweenEventManager` khi cờ ON.

## 3) Drop theo DB

- `MobRewardService` hỗ trợ key `HALLOWEEN`/`halloween`.
- Nếu cần tuning reward, ưu tiên sửa bảng `mob_reward`.

## 4) Checklist

1. Bật `event.halloween`.
2. Kiểm tra thread `HalloweenEventManager`.
3. Xác nhận spawn đủ 3 loại boss event.
4. Kiểm tra seasonal drop theo key Halloween trong `mob_reward`.
