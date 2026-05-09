# AI Specs Playbook (NROLUBE)

Bộ tài liệu này giúp AI nắm context nhanh, đúng boundaries package, và giảm lỗi khi sửa code trong monolith `nrolube`.

## 1) Cách dùng cho AI agent

Thứ tự đọc bắt buộc:

1. `00-architecture-overview.vi.md`
2. `01-request-lifecycle.vi.md`
3. `04-data-access-contract.vi.md`
4. `02-player-combat.vi.md`
5. `03-content-gameplay-systems.vi.md`
6. `06-events-navigation.vi.md`
7. `05-operations-runtime.vi.md`

Nếu task thuộc một domain cụ thể, đọc thêm phần tương ứng trong file domain rồi mới sửa code.
Với task về sự kiện, mở tiếp các file trong `docs/ai-specs/events/`.

## 2) Mục tiêu của bộ spec

- Chuẩn hóa context cho AI trước khi implement/fix.
- Giữ thay đổi nhỏ, cục bộ, dễ rollback.
- Tránh phá protocol, packet flow, và DB assumptions.
- Giảm "hallucination refactor" khi chạm file lớn legacy.

## 3) Nguyên tắc bắt buộc khi AI chỉnh code

- Chỉ sửa file nằm trong code path liên quan trực tiếp đến task.
- Không đổi packet ID, message format, command text flow nếu không có yêu cầu rõ ràng.
- SQL mới phải đặt trong lớp DAO/fetcher thuộc `src/jdbc` hoặc `src/jdbc/daos`.
- Service/controller không viết SQL inline.
- Validate input bên ngoài trước parse/convert.
- Không swallow exception; phải có logging đủ context.

## 4) Domain map ngắn

- `src/network`, `src/server`: transport/session, routing, server runtime.
- `src/services`, `src/services/func`: business logic chính.
- `src/player`, `src/mob`, `src/map`, `src/skill`, `src/item`: gameplay runtime.
- `src/jdbc`, `src/jdbc/daos`: data access.
- `sql/`: schema và dữ liệu nền.

## 5) Checklist trước khi implement

- Đã đọc đủ các file lõi và file event liên quan (nếu task có đụng event).
- Xác định rõ input -> service -> dao -> response path.
- Xác định scope file sẽ sửa và file chắc chắn không sửa.
- Xác định rủi ro compatibility (protocol, DB, gameplay balance).
- Có bước verify tối thiểu sau khi sửa (build/test hoặc walkthrough logic).

## 6) Checklist review sau implement

- Không có side-effect ngoài scope.
- Không có SQL inline ở service/controller.
- Không có thay đổi hành vi public ngoài yêu cầu.
- Error handling đầy đủ và user-safe.
- Tài liệu/spec liên quan được cập nhật nếu behavior thay đổi.

## 7) Nguồn suy luận kiến trúc

Bộ spec này dựa trên:

- Cấu trúc package thực tế trong `src/`.
- Các execution flows và module clusters từ GitNexus index của repo `nrolube`.
- Tài liệu hiện có trong `docs/` về balance/combat.
