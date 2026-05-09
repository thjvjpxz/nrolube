# E2E Event - Tết (Lunar New Year)

## 1) Người chơi thấy gì khi event mở

- Có nội dung boss event Tết.
- Có NPC và hoạt động seasonal như nấu bánh.
- Thường là event đa hoạt động: vừa combat vừa thu thập/chế tạo.

## 2) Thu thập nguyên liệu gì

- Nhánh nấu bánh chưng (`NoiBanh`):
  - `Thỏi vàng` (id `457`) x10
  - `Cơm nếp` (id `1214`) x5
  - `Lá dong` (id `1217`) x5
  - `Sợi cói` (id `1218`) x5
- Nhánh mâm ngũ quả (`BumaTH`):
  - `Thỏi vàng` (id `457`) x5
  - `Mãng cầu` (id `1177`) x10
  - `Dừa` (id `1178`) x10
  - `Đu đủ` (id `1179`) x10
  - `Xoài` (id `1180`) x10
  - `Sung` (id `1181`) x10

## 3) Nguyên liệu lấy từ đâu

- `Mãng cầu/Dừa/Đu đủ/Xoài/Sung`:
  - Drop trực tiếp từ boss `Lân con` khi hạ boss.
- `Bao lì xì rồng` (id `1183`):
  - Drop từ `Lân con` với tỉ lệ cao.
- `Cơm nếp/Lá dong/Sợi cói`:
  - Drop seasonal qua cấu hình `mob_reward` với `event_key = LUNAR_NEW_YEAR`.
  - Cấu hình hiện tại là dạng global (không khóa theo một map duy nhất).
- `Thỏi vàng`:
  - Dùng nguồn thỏi vàng người chơi đang có (farm/đổi theo các cơ chế hiện có của server).

## 4) Đổi nguyên liệu lấy gì

- `NoiBanh`:
  - Đủ nguyên liệu -> tạo `Tệp bánh chưng` (id `1219`).
- `BumaTH`:
  - Đủ nguyên liệu -> đổi `Mâm ngũ quả` (id `1182`).
- Dùng `Mâm ngũ quả` (`UseItem`):
  - Random vàng / ngọc xanh / dưa hấu / item hiếm.
- Dùng `Bao lì xì rồng` (`UseItem`):
  - Random vàng / hồng ngọc / ngọc xanh.

## 5) Boss tên gì, xuất hiện ở đâu, đánh chết được gì

- Boss chính: `Lân con`.
- Map xuất hiện (theo `BossesData.LAN_CON.mapJoin`):
  - `Làng Aru` (`0`)
  - `Đảo Kamê` (`5`)
  - `Làng Mori` (`7`)
  - `Làng Kakarot` (`14`)
  - `Vách núi Aru` (`42`)
  - `Vách núi Moori` (`43`)
  - `Vách núi Kakarot` (`44`)
- Hạ `Lân con` nhận:
  - `Bao lì xì rồng` (id `1183`) có tỉ lệ.
  - Bộ trái cây event (`1177` -> `1181`) số lượng ngẫu nhiên.

## 6) Hành trình E2E khuyến nghị

1. Săn `Lân con` để lấy trái cây + bao lì xì.
2. Farm thêm `Cơm nếp/Lá dong/Sợi cói` từ quái theo drop seasonal.
3. Đổi tại NPC:
   - `NoiBanh` để ra `Tệp bánh chưng`.
   - `BumaTH` để ra `Mâm ngũ quả`.
4. Mở `Mâm ngũ quả`/`Bao lì xì` để nhận tài nguyên ngẫu nhiên.

## 7) Điểm cần UX rõ cho người chơi

- Công thức nguyên liệu cần hiển thị rõ.
- Thông báo thiếu nguyên liệu cần cụ thể.
- Cần nhắc trạng thái hành trang trước khi nhận đồ.
