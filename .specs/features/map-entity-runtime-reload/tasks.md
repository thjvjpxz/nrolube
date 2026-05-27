# Map Entity Runtime Reload - Phase 1 Tasks

**Nguồn spec**: `/home/think/workspace/kimthi/nro/nrolube-web/docs/superpowers/specs/2026-05-27-map-entity-editor-design.md`
**Phạm vi**: Giai Đoạn 1 - Backend Reload Dữ Liệu Khi Server Đang Chạy
**Status**: Draft

---

## Yêu Cầu Truy Vết

- **MER-01**: Backend đọc lại `map_template.npcs`, `map_template.mobs`, `map_template.waypoints` từ DB theo format startup hiện có.
- **MER-02**: Parser backend cho NPC, mob, waypoint được tách nhỏ, tên rõ nghĩa, log được `mapId` và tên cột khi parse lỗi.
- **MER-03**: Command admin `remap` chạy trong `Command.check`.
- **MER-04**: `remap` refresh dữ liệu trong `Manager.MAP_TEMPLATES` và runtime `Manager.MAPS`.
- **MER-05**: NPC reload dùng `NpcFactory.createNPC(mapId, 1, x, y, npcId)`.
- **MER-06**: Waypoint reload thay `map.wayPoints` bằng danh sách mới đã parse.
- **MER-07**: Mob reload dựng lại mob từ template map cho các zone, có cảnh báo vận hành khi server đang có combat/người chơi.
- **MER-08**: Admin nhận thông báo số map, NPC, mob, waypoint đã reload và số lỗi nếu có.

## Ràng Buộc Dự Án Và Clean Code

- SQL mới phải nằm trong DAO/fetcher dưới `src/jdbc/daos`.
- `server/Command` chỉ dispatch command, không parse DB hoặc chứa business logic reload.
- Logic reload runtime đặt trong service/facade nhỏ, tên rõ nghĩa.
- Parser không dùng string replace rải rác trong `Manager`; gói vào helper có method một nhiệm vụ.
- Comment mới viết tiếng Việt, chỉ giải thích lý do/rủi ro, không mô tả điều code đã nói rõ.
- Không đổi packet/message ID, protocol, hoặc flow command cũ.
- Không swallow exception trong code mới; log đủ context và trả kết quả user-safe.
- Mỗi task khi implement phải commit riêng sau khi gate xanh.

## Gate Kiểm Tra

Repo hiện là Maven project và chưa có test suite chuẩn. Gate mặc định cho phase 1:

```bash
mvn -q -DskipTests package
```

Nếu trong lúc implement thêm được unit test nhỏ không phụ thuộc DB/runtime, task đó phải chạy thêm test liên quan trước gate package.

---

## Execution Plan

### Phase A - Parser Và DAO Nền Tảng

```text
T1 -> T2 -> T3
```

### Phase B - Runtime Reload Service

```text
T3 -> T4 -> T5 -> T6
```

### Phase C - Command Và Verify

```text
T6 -> T7 -> T8
```

---

## Task Breakdown

### T1: Tạo DTO cho dữ liệu entity map

**What**: Tạo các DTO nhỏ biểu diễn dữ liệu reload của một map, NPC placement, mob spawn, waypoint.
**Where**: `src/jdbc/daos/MapEntityReloadData.java`
**Depends on**: None
**Reuses**: `models.Template.MapTemplate`, `map.WayPoint`
**Requirement**: MER-01, MER-02

**Tools**:

- MCP: CodeGraph để đối chiếu field hiện có khi implement.
- Skill: `clean-code`

**Done when**:

- [ ] Có DTO chứa `mapId`, danh sách NPC, danh sách mob, danh sách waypoint.
- [ ] Tên class/field rõ nghĩa, không dùng `data`, `info`, `arr` cho dữ liệu có ý nghĩa cụ thể.
- [ ] Không thêm behavior DB/runtime vào DTO.
- [ ] Gate pass: `mvn -q -DskipTests package`.

**Tests**: build
**Gate**: package

---

### T2: Tạo parser map entity backend

**What**: Tách parser cho `npcs`, `mobs`, `waypoints` từ format DB hiện tại thành method nhỏ có validate và log context.
**Where**: `src/jdbc/daos/MapEntityParser.java`
**Depends on**: T1
**Reuses**: Logic parse trong `src/server/Manager.java` phần load `map_template`
**Requirement**: MER-01, MER-02

**Tools**:

- MCP: CodeGraph để đối chiếu logic `Manager.loadDatabase`.
- Skill: `clean-code`

**Done when**:

- [ ] Parser đọc được `npcs` dạng `[[npcId,x,y]]`.
- [ ] Parser đọc được `mobs` dạng `[[mobId,level,hp,x,y]]` và dạng legacy `["[mobId,level,hp,x,y]"]`.
- [ ] Parser đọc được `waypoints` dạng legacy string-array giống DB hiện tại.
- [ ] Parser trả lỗi có `mapId` và tên cột khi format sai.
- [ ] Parser không silently drop entry lỗi.
- [ ] Method parser nhỏ, mỗi method xử lý một loại entity.
- [ ] Gate pass: `mvn -q -DskipTests package`.

**Tests**: build; ưu tiên thêm unit test nếu cấu trúc project cho phép không kéo DB/runtime.
**Gate**: package

---

### T3: Tạo DAO đọc entity map từ DB

**What**: Tạo DAO đọc `id`, `npcs`, `mobs`, `waypoints` từ `map_template` và trả về danh sách DTO đã parse.
**Where**: `src/jdbc/daos/MapEntityReloadDAO.java`
**Depends on**: T1, T2
**Reuses**: `jdbc.DBConnecter`, pattern DAO từ `ShopDAO`
**Requirement**: MER-01, MER-02

**Tools**:

- MCP: CodeGraph hoặc native file read để tham chiếu `ShopDAO`.
- Skill: `clean-code`

**Done when**:

- [ ] DAO chỉ chứa SQL/read DB, không update runtime `Manager.MAPS`.
- [ ] Query chỉ lấy cột cần cho reload entity.
- [ ] Resource DB dùng try-with-resources.
- [ ] Lỗi parse/SQL được log đủ context.
- [ ] DAO trả kết quả gồm danh sách map hợp lệ và số lỗi parse nếu có.
- [ ] Gate pass: `mvn -q -DskipTests package`.

**Tests**: build
**Gate**: package

---

### T4: Thêm method runtime reload an toàn trong `Map`

**What**: Thêm method nhỏ trên `map.Map` để thay NPC, waypoint, và rebuild mob cho các zone từ arrays đã parse.
**Where**: `src/map/Map.java`
**Depends on**: T1
**Reuses**: `initNpc(byte[], short[], short[])`, `initMob(byte[], byte[], int[], short[], short[])`, `zones`, `Zone.mobs`
**Requirement**: MER-04, MER-05, MER-06, MER-07

**Tools**:

- MCP: CodeGraph để kiểm tra caller/callee của `initMob`, `initNpc`, `Zone.mobs`.
- Skill: `clean-code`

**Done when**:

- [ ] Có method runtime reload không làm đổi constructor hoặc startup flow.
- [ ] NPC reload dùng cùng logic `NpcFactory.createNPC(mapId, 1, x, y, npcId)`.
- [ ] Waypoint được replace bằng danh sách mới.
- [ ] Mob trong từng zone được clear/rebuild từ template mới theo logic tương đương `initMob`.
- [ ] Có guard/log cảnh báo khi map đang có player hoặc zone có activity, để admin biết rủi ro vận hành.
- [ ] Không đổi packet/protocol hoặc behavior ngoài reload.
- [ ] Gate pass: `mvn -q -DskipTests package`.

**Tests**: build
**Gate**: package

---

### T5: Tạo service/facade reload map entity

**What**: Tạo service gọi DAO, đồng bộ `Manager.MAP_TEMPLATES`, tìm runtime map, gọi method reload trên `Map`, và trả summary.
**Where**: `src/services/MapEntityReloadService.java`
**Depends on**: T3, T4
**Reuses**: `server.Manager.MAP_TEMPLATES`, `server.Manager.MAPS`, `services.MapService.getMapById`
**Requirement**: MER-04, MER-05, MER-06, MER-07, MER-08

**Tools**:

- MCP: CodeGraph để xác định access pattern tới `Manager.MAPS` và `MAP_TEMPLATES`.
- Skill: `clean-code`

**Done when**:

- [ ] Service có singleton hoặc pattern phù hợp với các service hiện có.
- [ ] Service không chứa SQL trực tiếp.
- [ ] Service update đúng `MapTemplate` tương ứng trong `Manager.MAP_TEMPLATES`.
- [ ] Service reload runtime map nếu map tồn tại trong `Manager.MAPS`.
- [ ] Summary có số map đọc được, map reload được, NPC, mob, waypoint, lỗi.
- [ ] Lỗi từng map không làm mất toàn bộ kết quả nếu có thể tiếp tục an toàn.
- [ ] Gate pass: `mvn -q -DskipTests package`.

**Tests**: build
**Gate**: package

---

### T6: Tinh chỉnh summary và message vận hành

**What**: Tạo object/result formatter để command admin nhận được message ngắn, rõ số lượng và cảnh báo rủi ro mob reload.
**Where**: `src/services/MapEntityReloadResult.java` hoặc cùng package service nếu nhỏ
**Depends on**: T5
**Reuses**: `Service.gI().sendThongBao` style message hiện có
**Requirement**: MER-07, MER-08

**Tools**:

- MCP: none bắt buộc.
- Skill: `clean-code`

**Done when**:

- [ ] Result object không lẫn logic DB/runtime.
- [ ] Message admin có số map, NPC, mob, waypoint đã reload.
- [ ] Message có số lỗi nếu có lỗi parse/reload.
- [ ] Message có cảnh báo ngắn nếu reload mob có rủi ro khi map đang có người.
- [ ] Gate pass: `mvn -q -DskipTests package`.

**Tests**: build
**Gate**: package

---

### T7: Gắn chat command `remap`

**What**: Thêm nhánh admin command `remap` trong `Command.check`, gọi service reload và gửi thông báo admin.
**Where**: `src/server/Command.java`
**Depends on**: T5, T6
**Reuses**: Pattern `reshop`, `redrop`
**Requirement**: MER-03, MER-08

**Tools**:

- MCP: CodeGraph để xem impact của `Command.check` nếu cần.
- Skill: `clean-code`

**Done when**:

- [ ] Chỉ admin mới chạy được `remap`.
- [ ] `Command.check` chỉ dispatch, không parse DB hoặc rebuild runtime trực tiếp.
- [ ] Command gửi message thành công/thất bại an toàn cho admin.
- [ ] Existing commands `reshop`, `redrop`, `giftcode` không đổi behavior.
- [ ] Gate pass: `mvn -q -DskipTests package`.

**Tests**: build
**Gate**: package

---

### T8: Verify end-to-end phase 1

**What**: Chạy kiểm tra build và kiểm tra thủ công có hướng dẫn rõ cho map `0`, `5`, `84`.
**Where**: Không bắt buộc đổi code; nếu cần, cập nhật note trong commit hoặc tài liệu nhỏ.
**Depends on**: T7
**Reuses**: Spec manual checks
**Requirement**: MER-01 đến MER-08

**Tools**:

- MCP: `nrolube-local` nếu cần đọc DB read-only trước/sau.
- Skill: `clean-code`

**Done when**:

- [ ] Gate pass: `mvn -q -DskipTests package`.
- [ ] Manual checklist ghi rõ: sửa DB test nhỏ, chạy `remap`, xác nhận summary count.
- [ ] Không có thay đổi ngoài phạm vi phase 1.
- [ ] Nếu không thể chạy game server trong session, ghi rõ giới hạn verify còn lại.

**Tests**: build + manual checklist
**Gate**: package

---

## Validation Tables

### Granularity Check

| Task | Atomic? | Lý do |
|---|---:|---|
| T1 | OK | Chỉ tạo DTO |
| T2 | OK | Chỉ parser |
| T3 | OK | Chỉ DAO đọc DB |
| T4 | OK | Chỉ runtime reload method trong `Map` |
| T5 | OK | Chỉ service orchestration |
| T6 | OK | Chỉ result/message |
| T7 | OK | Chỉ gắn command |
| T8 | OK | Chỉ verify phase |

### Dependency Cross-Check

| Task | Depends on | Có trong execution plan? |
|---|---|---|
| T1 | None | OK |
| T2 | T1 | OK |
| T3 | T1, T2 | OK |
| T4 | T1 | OK |
| T5 | T3, T4 | OK |
| T6 | T5 | OK |
| T7 | T5, T6 | OK |
| T8 | T7 | OK |

### Test Co-location Validation

| Task | Test type | Gate | Ghi chú |
|---|---|---|---|
| T1 | build | package | DTO không cần DB |
| T2 | build | package | Có thể thêm unit test parser nếu project test setup được thêm an toàn |
| T3 | build | package | DAO phụ thuộc DB runtime, verify chính bằng compile/manual |
| T4 | build | package | Runtime behavior cần manual game server |
| T5 | build | package | Service orchestration cần manual game server |
| T6 | build | package | Message/result compile gate đủ cho plan phase 1 |
| T7 | build | package | Command path manual verify qua game |
| T8 | build + manual | package | Tổng hợp verify |

## Trước Khi Execute

Khi bắt đầu implement từng task, phải nêu rõ:

- Assumptions.
- Files to touch.
- Success criteria.
- Gate command.
- Commit message dự kiến.

Tools mặc định theo yêu cầu hiện tại:

- MCP: CodeGraph cho impact/symbol context; `nrolube-local` chỉ đọc schema/dữ liệu khi cần.
- Skills: `clean-code`.
