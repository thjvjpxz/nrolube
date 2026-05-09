# Event Spec - Tết (Lunar New Year)

## 1) Điểm bật/tắt

- Cờ chính: `EventConfig.LUNAR_NEW_YEAR`.
- Key config: `event.lunar_new_year`.

## 2) Luồng khởi tạo

- `EventManager` gọi `new LunarNewYear().init()` khi cờ ON.
- Manifest hiện tại tạo boss `LAN_CON` số lượng 10.
- `ServerManager` start `LunarNewYearEventManager` khi cờ ON.

## 3) NPC và tương tác đặc thù

- `NpcFactory` có gate theo cờ Tết:
  - `NOI_BANH` chỉ load khi event Tết bật.
  - Một số NPC map 42/43/44 cũng có điều kiện theo cờ Tết.
- `NoiBanh` cho phép nấu bánh chưng:
  - Trừ nguyên liệu theo số lượng yêu cầu.
  - Tạo item bánh và add vào túi.

## 4) Drop theo DB

- `MobRewardService` hỗ trợ key event cho nhánh Tết:
  - `LUNNAR_NEW_YEAR`, `LUNAR_NEW_YEAR`, `TET`, `tet`.
- Reward seasonal nên ưu tiên cấu hình qua `mob_reward`.

## 5) Checklist

1. Bật `event.lunar_new_year`.
2. Kiểm tra spawn `LAN_CON`.
3. Kiểm tra NPC `NOI_BANH` xuất hiện đúng map.
4. Test flow nấu bánh và trừ vật phẩm.
5. Kiểm tra seasonal drop key Tết trong `mob_reward`.
