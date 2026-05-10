# Event Spec - Christmas

## 1) Điểm bật/tắt

- Cờ chính: `EventConfig.CHRISTMAS_EVENT`.
- Key config: `event.christmas`.

## 2) Luồng khởi tạo

- `EventManager` gọi `new Christmas().init()` khi cờ ON.
- Manifest tạo `ONG_GIA_NOEL` số lượng 30.
- `ServerManager` start `ChristmasEventManager` khi cờ ON.

## 3) Drop theo DB

- `MobRewardService` hỗ trợ key `CHRISTMAS`/`christmas`.
- Reward seasonal nên tách qua `mob_reward` để giảm sửa code runtime.

## 4) Checklist

1. Bật `event.christmas`.
2. Kiểm tra thread event manager đã chạy.
3. Xác nhận spawn `ONG_GIA_NOEL` đúng số lượng/chu kỳ.
4. Kiểm tra rule drop theo key Christmas.
