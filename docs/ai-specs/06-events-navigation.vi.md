# 06 - Điều Hướng Spec Sự Kiện

File này là bản đồ điều hướng. Mỗi sự kiện chính có file spec riêng, không gộp chung.

## 1) Cách đọc khi làm task event

1. Đọc file này để xác định đúng sự kiện.
2. Mở file deep spec của sự kiện đó trong `docs/ai-specs/events/`.
3. Nếu task có đụng drop theo DB, đọc thêm `04-data-access-contract.vi.md`.
4. Nếu task có đụng runtime/thread, đọc thêm `05-operations-runtime.vi.md`.

## 2) Danh sách spec sâu theo sự kiện chính

- Hùng Vương: `events/hung-vuong.vi.md`
- Tết (Lunar New Year): `events/lunar-new-year.vi.md`
- Halloween: `events/halloween.vi.md`
- Christmas: `events/christmas.vi.md`
- Trung Thu: `events/trung-thu.vi.md`

## 3) Luồng chung của mọi sự kiện

1. Cờ bật/tắt nằm ở `EventConfig` (`event.*` trong properties).
2. `EventManager` đồng bộ cờ và gọi event manifest tương ứng.
3. `Event.init()` chạy theo thứ tự `npc()` -> `boss()` -> `itemMap()` -> `itemBoss()`.
4. `ServerManager` khởi động manager thread cho event boss theo cờ config.
5. `MobRewardService` đọc `mob_reward.event_key` để lọc drop theo event.

## 4) Rule khi thêm sự kiện mới

- Không nhét logic đặc thù trực tiếp vào transport/session.
- Spawn boss/NPC trong manifest và boss classes chuyên biệt.
- Drop theo event ưu tiên cấu hình DB `mob_reward`.
- Mỗi event mới bắt buộc có file riêng trong `docs/ai-specs/events/`.
