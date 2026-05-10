# E2E Event - Trung Thu

## 1) Người chơi thấy gì khi event mở

- Boss Trung Thu xuất hiện theo cụm riêng của event.
- Trọng tâm trải nghiệm là combat + tích lũy reward seasonal.

## 2) Thu thập nguyên liệu gì

- Nhánh drop trực tiếp:
  - `Linh Hồn Khỉ` (id `1045`) từ `Khỉ đột`.
- Nhánh trang bị/cải trang:
  - Item `2123` từ `Nguyệt thần`.
  - Item `2124` từ `Nhật thần` (spawn đi kèm).

## 3) Nguyên liệu lấy từ đâu

- Hạ `Khỉ đột` -> rơi `Linh Hồn Khỉ` (`1045`).
- Hạ cặp `Nguyệt thần`/`Nhật thần` -> rơi item `2123`/`2124` có option.

## 4) Đổi nguyên liệu lấy gì

- Hiện tại chưa thấy flow NPC đổi nguyên liệu Trung Thu riêng trong code event.
- `Linh Hồn Khỉ` (`1045`) dùng trực tiếp để kích hoạt trạng thái biến khỉ tạm thời.

## 5) Boss tên gì, xuất hiện ở đâu, đánh chết được gì

- `Khỉ đột`:
  - Map xuất hiện: dải `0-20`.
  - Tên map chính trong dải này: `Làng Aru`, `Đồi hoa cúc`, `Thung lũng tre`, `Rừng nấm`, `Rừng xương`, `Đảo Kamê`, `Đông Karin`, `Làng Mori`, `Thị trấn Moori`, `Thung lũng Namếc`, `Thung lũng Maima`, `Vực maima`, `Đảo Guru`, `Làng Kakarot`, `Đồi hoang`, `Làng Plant`, `Rừng nguyên sinh`, `Rừng thông Xayda`, `Thành phố Vegeta`, `Vách núi đen`.
  - Hạ boss: rơi `Linh Hồn Khỉ` (`1045`).
- `Nguyệt thần`:
  - Map xuất hiện: dải `0-20`.
  - Hạ boss: trigger thưởng qua trạng thái AFK, drop item `2123` có nhiều option.
  - Có quan hệ đi kèm `Nhật thần`.
- `Nhật thần`:
  - Spawn theo cụm với `Nguyệt thần`.
  - Hạ boss: hỗ trợ chuỗi thưởng, drop item `2124` có option.

## 6) Hành trình E2E khuyến nghị

1. Farm nhanh `Khỉ đột` để gom `Linh Hồn Khỉ`.
2. Đi nhóm săn cặp `Nguyệt thần`/`Nhật thần` để lấy đồ option.
3. Chọn khung giờ ít cạnh tranh để tối ưu số lượt hạ boss.

## 7) Điều nên truyền thông cho người chơi

- Event mở trong khung thời gian nào.
- Nên chuẩn bị gì trước khi vào event (hành trang, build cơ bản).
- Phần thưởng mục tiêu của event là gì để người chơi đặt kỳ vọng đúng.
