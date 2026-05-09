# E2E Event - Hùng Vương

## 1) Người chơi thấy gì khi event mở

- Boss event xuất hiện theo cụm Sơn Tinh - Thủy Tinh.
- Đây là event thiên về chiến đấu boss, không phải event làm nhiệm vụ NPC dài.

## 2) Thu thập nguyên liệu gì

- Event này không theo hướng thu thập nguyên liệu chế tạo kiểu Tết.
- Trọng tâm là săn boss để lấy cải trang và item thưởng trực tiếp.

## 3) Nguyên liệu lấy từ đâu

- Nguồn chính: hạ boss `Sơn Tinh`/`Thủy Tinh`.
- Nhánh phụ: drop event-key `HUNG_VUONG` nếu được cấu hình trong `mob_reward`.

## 4) Đổi nguyên liệu lấy gì

- Hiện chưa có flow NPC đổi nguyên liệu riêng cho Hùng Vương trong code event.
- Reward nhận trực tiếp từ boss là chủ đạo.

## 5) Boss tên gì, xuất hiện ở đâu, đánh chết được gì

- Boss chính:
  - `Thủy Tinh`
  - `Sơn Tinh` (spawn kèm theo quan hệ đi cùng)
- Map xuất hiện (theo `BossesData`):
  - Dải map `0-14` (Làng Aru -> Làng Kakarot và các map liền kề đầu game).
  - Bao gồm: `Làng Aru`, `Đồi hoa cúc`, `Thung lũng tre`, `Rừng nấm`, `Rừng xương`, `Đảo Kamê`, `Đông Karin`, `Làng Mori`, `Đồi nấm tím`, `Thị trấn Moori`, `Thung lũng Namếc`, `Thung lũng Maima`, `Vực maima`, `Đảo Guru`, `Làng Kakarot`.
- Hạ boss nhận:
  - `Sơn Tinh` -> item `421` (Cải trang Sơn Tinh) + option random.
  - `Thủy Tinh` -> item `422` (Cải trang Thủy Tinh) + option random.
  - Có thể có thêm item phụ `1839` khi thỏa điều kiện trạng thái nhân vật.

## 6) Hành trình người chơi (E2E)

1. Vào map có cụm boss Hùng Vương.
2. Giao tranh và cố gắng chốt kết liễu.
3. Nhặt item cải trang + kiểm tra option.
4. Lặp lại chu kỳ săn boss để tối ưu đồ.

## 7) Phần thưởng người chơi quan tâm

- Item thời trang event:
  - Cải trang Sơn Tinh.
  - Cải trang Thủy Tinh.
- Option trên item có yếu tố ngẫu nhiên.
- Một số điều kiện trạng thái đặc biệt có thể mở thêm phần thưởng phụ.

## 8) Chiến thuật trải nghiệm khuyên dùng

- Đi theo nhóm để tranh boss ổn định hơn.
- Ưu tiên sống sót và giữ nhịp gây sát thương đều.
- Dành slot hành trang trước khi đi farm event.

## 9) Những điểm dễ gây khó chịu cho người chơi

- Tranh chấp last-hit hoặc tranh quyền nhặt đồ.
- Rơi đồ ngẫu nhiên khiến cảm giác "hên xui" cao.
- Nếu map đông, trải nghiệm combat có thể nhiễu bởi nhiều mục tiêu.
