# 03 - Content Systems (Map, Item, NPC, Clan, Task)

## 1) Mục tiêu

Đồng bộ cách mô tả các hệ thống content để AI không sửa sai layer.

## 2) Domain boundaries

### Item

- Package liên quan: `src/item`, `src/services/ItemService.java`, `src/services/func/UseItem.java`.
- Rule: item effect xử lý ở service/func, không trút logic vào transport.

### Map + mob spawn

- Package liên quan: `src/map`, `src/mob`, `src/server/Manager.java`.
- Rule: map runtime và mob state là source of truth trong combat context.

### NPC

- Package liên quan: `src/npc`, `src/services/NpcService.java`.
- Rule: NPC là interaction point, business tiếp tục ở service.

### Clan

- Package liên quan: `src/clan`, `src/services/ClanService.java`.
- Rule: thao tác clan cần cẩn thận side-effect thông báo/tổ chức.

### Task/quest

- Package liên quan: `src/task`, `src/services/TaskService.java`.
- Rule: update progression phải nhất quán với reward và condition.

## 3) Pattern mở rộng tính năng content

1. Định nghĩa rule gameplay ở service.
2. Nếu cần data mới, thêm schema-safe query qua DAO.
3. Cập nhật response message theo flow chuẩn.
4. Verify interaction với hệ thống liên quan (item <-> task, clan <-> event...).

## 4) Cảnh báo coupling thường gặp

- Item effect có thể ảnh hưởng combat và task đồng thời.
- Event/map rule đặc thù có thể override logic chung.
- Clan/party mechanics dễ tạo tác động gián tiếp đến reward.

## 5) Mẫu spec cho deep-dive domain

Khi cần đào sâu một domain, tạo file bổ sung theo mẫu:

- Bối cảnh domain.
- Entry points.
- State + invariants.
- DAO contracts.
- Response contracts.
- Regression risks.
