# E2E Event - Christmas

## 1) Người chơi thấy gì khi event mở

- Boss theo chủ đề Giáng Sinh xuất hiện với mật độ cao.
- Event hướng tới farm nhanh và tích lũy phần thưởng seasonal.

## 2) Thu thập nguyên liệu gì

- Vật phẩm cốt lõi: `Hộp quà giáng sinh` (id `648`).

## 3) Nguyên liệu lấy từ đâu

- Nguồn rơi chính không phải từ hạ boss:
  - `Ông già Noel` định kỳ thả `Hộp quà giáng sinh` khi đang hoạt động.
  - Logic hiện tại: mỗi ~30s có các lượt thả với tỉ lệ 1/3, 1/5, 1/7.
- Lưu ý: `Ông già Noel` không thiết kế theo kiểu "đánh chết lấy reward" trong bản code hiện tại.

## 4) Đổi nguyên liệu lấy gì

- Dùng `Hộp quà giáng sinh` (`UseItem` -> `ItemService.OpenItem648`) để mở thưởng.
- Pool phần thưởng gồm nhiều nhóm:
  - Đồ thường
  - Item kích hoạt
  - Vật phẩm sự kiện
  - Ván bay/cải trang/phụ kiện Giáng Sinh

## 5) Boss tên gì, xuất hiện ở đâu, đánh chết được gì

- Boss chính: `Ông già Noel`.
- Map xuất hiện rất rộng (theo `BossesData.ONG_GIA_NOEL.mapJoin`), trải trên các cụm:
  - `0-20`, `24-37`
  - `63-77`, `79-84`
  - `92-94`, `96-110`
- Ví dụ map thường gặp: `Đảo Kamê`, `Làng Kakarot`, `Thành phố phía đông`, `Thành phố phía nam`, `Cao nguyên`, `Thành phố phía bắc`, `Rừng băng`, `Hang băng`.
- Phần thưởng thực chiến:
  - Không theo hướng hạ boss trực tiếp.
  - Chủ yếu là nhặt `Hộp quà giáng sinh` do boss phát ra theo chu kỳ.

## 6) Hành trình E2E khuyến nghị

1. Theo boss trong map đông vừa phải để nhặt box ổn định.
2. Gom `Hộp quà giáng sinh`.
3. Mở box để quay phần thưởng event.

## 7) Điểm người chơi thường quan tâm

- Tỉ lệ gặp boss và nhịp spawn.
- Chất lượng phần thưởng nhận được so với thời gian bỏ ra.
- Mức cạnh tranh ở khung giờ cao điểm.
