# Event Spec - Hùng Vương

## 1) Mục tiêu và phạm vi

Sự kiện Hùng Vương trong code hiện tại tập trung vào nhánh boss chiến đấu và phần thưởng khi hạ boss.

Phạm vi chính:

- Bật/tắt event qua cấu hình.
- Spawn boss event.
- Combat behavior của boss.
- Reward/drop runtime và tích hợp drop theo DB.

## 2) Điểm bật/tắt và khởi tạo

- Bật bằng `event.hung_vuong=true`.
- Luồng kích hoạt:
  - `Manager.loadProperties` nạp `event.hung_vuong` vào `EventConfig.HUNG_VUONG_EVENT`.
  - `EventManager.syncFromEventConfig` đồng bộ cờ.
  - `EventManager.init` gọi `new HungVuong().init()` khi cờ ON.
  - `ServerManager` start `HungVuongEventManager` thread khi cờ ON.

## 3) Spawn boss và quan hệ boss

- Manifest `HungVuong` gọi `createBoss(BossID.THUY_TINH, 10)`.
- `THUY_TINH` trong `BossesData` có `bossesAppearTogether = {SON_TINH}`.
- Kết quả runtime: cụm chiến đấu có cả `Thủy Tinh` và `Sơn Tinh` theo quan hệ boss đi cùng.

## 4) Combat behavior chính

Cả `Sơn Tinh` và `Thủy Tinh` đang có các điểm chung:

- Có né đòn theo tỉ lệ.
- Có check shield.
- Có hard cap damage mỗi hit không xuyên giáp (xấp xỉ 1,000,000).
- Có auto leave map sau timeout, và reset timeout khi còn player trong map.
- Có logic chọn mục tiêu theo cờ đối địch.

## 5) Reward và vật phẩm

- Item cải trang:
  - `Sơn Tinh` drop item `421`.
  - `Thủy Tinh` drop item `422`.
- Option item được add runtime với tham số ngẫu nhiên.
- Có option hiếm theo tỉ lệ.
- Có điều kiện phụ theo trạng thái người kết liễu (`isMiNuong`/`isHacMiNuong`) để drop thêm item `1839`.

## 6) Tích hợp với drop DB

- `MobRewardService` hỗ trợ key event `HUNG_VUONG`/`hung_vuong`.
- Nếu cấu hình `mob_reward.event_key = HUNG_VUONG`, drop DB chỉ active khi event đang ON.
- Đây là điểm mở rộng reward an toàn mà không cần sửa code combat boss.

## 7) Checklist vận hành

1. Bật `event.hung_vuong`.
2. Restart server và kiểm tra trạng thái event in ra.
3. Xác nhận thread `HungVuongEventManager` hoạt động.
4. Xác nhận spawn boss cụm Sơn Tinh/Thủy Tinh.
5. Kiểm tra drop item 421/422 và các option.
6. Kiểm tra rule DB có `event_key = HUNG_VUONG` nếu có tuning reward.

## 8) Rủi ro khi chỉnh sửa

- Sửa `BossesData` có thể ảnh hưởng map join/skill/rest cycle.
- Sửa `reward()` hoặc `afk()` dễ gây lỗi duplicate drop hoặc mất drop.
- Sửa check event trong `MobRewardService` có thể ảnh hưởng toàn bộ seasonal drop.
