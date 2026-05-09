# 00 - Kiến Trúc Tổng Quan

## 1) Bối cảnh

`nrolube` là Java monolith game server. Cấu trúc hiện tại theo domain package, không theo microservice.

## 2) Các lớp kiến trúc chính

### Transport + session

- `src/network/io`: sender/collector, low-level message IO.
- `src/network/session`: phiên kết nối, kiểm soát gửi/nhận, trạng thái kết nối.
- `src/network/server`: server socket và accept handler.

### Routing + server control

- `src/server`: controller runtime, maintenance, bootstrap manager.
- `src/network/handler`: hợp đồng message handler.

### Business logic

- `src/services`: service tổng quát theo feature.
- `src/services/func`: chức năng gameplay chi tiết (item usage, transaction, minigame action...).

### Gameplay domain

- `src/player`, `src/skill`, `src/item`, `src/map`, `src/mob`, `src/npc`, `src/boss`.
- Các package này chứa state runtime và luật game.

### Persistence

- `src/jdbc`, `src/jdbc/daos`: DB access, result wrappers, fetchers.
- `sql/`: schema, dữ liệu seed/snapshot.

## 3) Dependency direction khuyến nghị

1. Transport/session -> controller/handler
2. Controller/handler -> service
3. Service -> gameplay model + DAO
4. DAO -> DB
5. Response ngược lại qua service -> player/session -> sender

Không đảo chiều dependency từ DAO lên service hoặc từ model gameplay lên tầng transport.

## 4) Module ưu tiên theo GitNexus cluster

Theo index hiện tại, các cụm lớn cần cẩn thận khi sửa:

- `Services` (lớn nhất, coupling cao).
- `Player`
- `Server`
- `Jdbc`
- `Session`

Hàm ý: thay đổi trong `services` dễ có blast radius lớn, cần giới hạn scope.

## 5) Quy tắc boundaries

- `Controller`/routing chỉ parse/dispatch, không nhét business dài.
- Business đặt ở service hoặc service func chuyên trách.
- SQL chỉ ở DAO/fetcher.
- Các utility dùng lại từ `src/utils` nếu đã có.

## 6) Mẫu mô tả task cho AI

Khi giao việc cho AI, luôn ghi rõ:

- Entry point message/command.
- Service chính chịu trách nhiệm.
- DAO nào được phép đụng.
- Expected response (client nhận gì).
- Scope file được sửa.
