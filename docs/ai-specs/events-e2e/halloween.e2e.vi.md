# E2E Event - Halloween

## 1) Người chơi thấy gì khi event mở

- Nhiều boss theo chủ đề Halloween xuất hiện cùng lúc.
- Trọng tâm là đi săn boss theo nhịp thời gian event.

## 2) Thu thập nguyên liệu gì

- Vật phẩm chính rơi từ boss Halloween: `Bí ngô` (id `585`).
- Đây là item event dạng VPSK.

## 3) Nguyên liệu lấy từ đâu

- Nguồn chính: hạ các boss Halloween:
  - `Bí ma`
  - `Ma trơi`
  - `Dơi`
- Cả 3 boss đều đang drop `Bí ngô` (id `585`) theo logic `reward()`.

## 4) Đổi nguyên liệu lấy gì

- Trong code hiện tại, chưa thấy flow đổi `Bí ngô` tại NPC event riêng.
- Có thể có luồng dùng/đổi từ hệ thống khác hoặc dữ liệu ngoài phạm vi hiện tại; cần xác nhận nếu muốn công bố cho người chơi.

## 5) Boss tên gì, xuất hiện ở đâu, đánh chết được gì

- `Ma trơi`:
  - Map xuất hiện chính: `Đảo Kamê` (`5`).
  - Hạ boss: rơi `Bí ngô` (`585`).
- `Dơi`:
  - Map xuất hiện chính: `Đảo Kamê` (`5`).
  - Hạ boss: rơi `Bí ngô` (`585`).
- `Bí ma`:
  - Dải map rộng (theo `BossesData.BI_MA.mapJoin`), gồm:
    - Cụm map tân thủ/trung: `0-20`, `24-37`
    - Cụm Fide và phụ cận: `63-77`, `79-84`
    - Cụm thành phố và băng tuyết: `92-94`, `96-110`
  - Ví dụ map dễ nhận diện: `Làng Aru`, `Đảo Kamê`, `Làng Kakarot`, `Thành phố phía đông`, `Thành phố phía nam`, `Cao nguyên`, `Thành phố phía bắc`, `Ngọn núi phía bắc`, `Rừng băng`, `Hang băng`.
  - Hạ boss: rơi `Bí ngô` (`585`).

## 6) Hành trình E2E khuyến nghị

1. Chọn map theo mật độ người phù hợp (ít tranh chấp).
2. Săn boss theo vòng lặp ngắn để gom `Bí ngô`.
3. Theo dõi thông báo event để biết thời điểm farm hiệu quả.

## 7) Trải nghiệm người chơi quan trọng

- Cảm giác "nhịp nhanh", ít downtime.
- Cạnh tranh boss cao ở khung giờ đông người.
- Cần cân bằng giữa farm hiệu quả và độ khó giao tranh.
