# Sổ Sứ Mệnh UI Redesign Implementation Plan

Date: 2026-05-23

## Goal

Làm lại Sổ Sứ Mệnh thành panel riêng trong client, giữ vibe NRO hiện tại nhưng thân thiện hơn với player:

- Player thấy ngay cấp sổ, điểm hiện tại, mốc kế tiếp và trạng thái Vip.
- Player xem được reward thường/Vip bằng item slot, không phải đọc bảng text dài.
- Player nhận từng reward hoặc nhận tất cả reward đang có thể nhận.
- Server vẫn là source of truth, không double reward, không mất quà nếu update mailbox lỗi.
- Không thay đổi gameplay/economy ngoài luồng hiển thị và nhận thưởng Sổ Sứ Mệnh.

## Current Context

Server:

- Main feature nằm trong `src/npc/npc_manifest/SoSuMenh.java`.
- State player nằm trong `src/sosumenh/SoSuMenhPlayer.java`.
- Reward/claim logic nằm trong `src/sosumenh/SoSuMenhService.java`.
- Reward config đọc từ bảng `so_su_menh_reward`, với JSON item ở cột `items` và `items2`.
- Task template đọc từ bảng `so_su_menh_task`.
- Claim hiện dùng message `-76` action `0/1` của achievement flow:
  - action `0`: server gửi danh sách `Archivement`.
  - action `1`: server báo client mark index đã nhận.
- Server route claim nằm trong `src/server/Controller.java`, command `-76`.

Client:

- `Assets/Scripts/Controller.cs` đọc message `-76` và mở panel achievement mặc định.
- `Assets/Scripts/Panel.cs` render achievement list trong `paintArchivement`.
- `Assets/Scripts/Archivement.cs` chỉ có `info1`, `info2`, `money`, `isFinish`, `isRecieve`.
- UI hiện tại thiếu reward detail/item icon nên không thể làm battle-pass UX thật nếu chỉ sửa paint cũ.

## Product Decision

Use a dedicated Sổ Sứ Mệnh panel.

Visual direction:

- Legacy NRO polished, not modern web UI.
- Palette: be/vàng/nâu giống panel cũ, có nhấn đỏ/cam cho action claimable, xanh cho completed.
- Use small readable game font, compact rows, item slots, clear pressed/selected states.
- UI must remain friendly for players: clear next action, no long wall of text, no hidden claim condition.

Interaction:

- Player can claim one reward lane at selected level.
- Player can claim all currently claimable rewards.
- Vip lane displays locked state if player has not unlocked Vip.
- Reward is still delivered to mailbox as current behavior.

Protocol direction:

- Reuse command `-76` to avoid introducing a new command id.
- Add new Sổ Sứ Mệnh-specific actions under `-76`.
- Keep existing achievement actions `0` and `1` compatible.

## Protocol Contract

Do not reuse action `0` or `1`; they are already used by achievement list.

Add these server-to-client/client-to-server actions under message `-76`:

### Action `2`: Open/Refresh Sổ Sứ Mệnh Panel

Direction: server -> client.

Payload order:

1. `byte action = 2`
2. `byte maxLevel`
3. `byte currentLevel`
4. `int currentPoint`
5. `int nextLevelPoint`
6. `boolean vipUnlocked`
7. `byte coutday`
8. `byte selectedLevel`
9. `byte tierCount`
10. Repeat `tierCount` times:
    - `byte level`
    - `boolean normalClaimed`
    - `boolean normalClaimable`
    - `boolean vipClaimed`
    - `boolean vipClaimable`
    - `byte normalRewardCount`
    - Repeat `normalRewardCount` times: reward item payload
    - `byte vipRewardCount`
    - Repeat `vipRewardCount` times: reward item payload
11. `byte taskCount`
12. Repeat `taskCount` times:
    - `byte taskId`
    - `UTF taskName`
    - `int currentCount`
    - `int targetCount`
    - `boolean finished`
    - `int pointReward`

Reward item payload:

1. `short tempId`
2. `UTF itemName`
3. `short iconId`
4. `int quantity`
5. `byte optionCount`
6. Repeat `optionCount` times:
   - `short optionId`
   - `int param`
   - `UTF optionText`

Notes:

- `maxLevel` is 20 for current data.
- `currentLevel = point / 50` as current logic.
- `nextLevelPoint = (currentLevel + 1) * 50`; clamp or set `currentPoint` when max level reached.
- `selectedLevel` should be near `currentLevel`; use `min(max(currentLevel, 1), maxLevel)`.
- `tierCount` can be all 20 for simpler client logic. If packet size becomes too large, server may send a 7-level window later, but phase 1 should use all 20 to keep scrolling/selection simple.
- `normalClaimable = currentLevel >= level && !normalClaimed`.
- `vipClaimable = vipUnlocked && currentLevel >= level && !vipClaimed`.

### Action `6`: Claim One Sổ Sứ Mệnh Reward

Direction: client -> server.

Payload order:

1. `byte action = 6`
2. `byte level`
3. `byte lane`

Lane values:

- `0`: normal
- `1`: vip

Server response:

- On success or handled no-op, send action `2` full snapshot again.
- Also send user-safe notification with existing `Service.gI().sendThongBao`.
- If invalid, send notification and still send snapshot if state may have changed.

### Action `7`: Claim All Sổ Sứ Mệnh Rewards

Direction: client -> server.

Payload order:

1. `byte action = 7`

Server behavior:

- Iterate level `1..maxLevel`.
- Claim every normal reward where claimable.
- Claim every vip reward where claimable and Vip unlocked.
- If no rewards are claimable, send a clear message.
- Send action `2` full snapshot after processing.

### Action `5`: Error Snapshot

Direction: server -> client.

Payload order:

1. `byte action = 5`
2. `UTF userMessage`

Client behavior:

- Open the Sổ Sứ Mệnh panel in error/empty state.
- Show `userMessage`.
- Keep close/back controls usable.

Use this when reward config cannot be parsed or DB read fails.

Action choice:

- Server-to-client action `2` opens/refreshes the panel.
- Server-to-client action `5` opens an error panel.
- Client-to-server action `6` claims one reward.
- Client-to-server action `7` claims all rewards.
- Do not use client-to-server action `3` or `4`, because existing achievement claim messages send only an index byte and index values `3/4` are valid old achievement indices.

## Phase 1: Server Implementation

### 1. Add Sổ Sứ Mệnh DTOs

Preferred location: `src/sosumenh/SoSuMenhService.java` as private static helper classes first, unless file becomes too large.

DTOs needed:

- `RewardOptionView`
  - `short optionId`
  - `int param`
  - `String text`
- `RewardItemView`
  - `short tempId`
  - `String itemName`
  - `short iconId`
  - `int quantity`
  - `List<RewardOptionView> options`
- `RewardTierView`
  - `int level`
  - `List<RewardItemView> normalRewards`
  - `List<RewardItemView> vipRewards`
  - `boolean normalClaimed`
  - `boolean vipClaimed`
  - `boolean normalClaimable`
  - `boolean vipClaimable`
- `MissionTaskView`
  - `int taskId`
  - `String taskName`
  - `int currentCount`
  - `int targetCount`
  - `boolean finished`
  - `int pointReward`
- `SoSuMenhSnapshot`
  - `int maxLevel`
  - `int currentLevel`
  - `int currentPoint`
  - `int nextLevelPoint`
  - `boolean vipUnlocked`
  - `int coutday`
  - `int selectedLevel`
  - `List<RewardTierView> tiers`
  - `List<MissionTaskView> tasks`

Keep DTOs read-only after construction where practical.

### 2. Centralize reward row loading

Add method:

```java
private List<RewardTierView> loadRewardTiers(Player player) throws SQLException
```

Behavior:

- Query `SELECT * FROM so_su_menh_reward ORDER BY level ASC`.
- For each row:
  - Read `level`.
  - Parse `items` as normal rewards.
  - Parse `items2` as Vip rewards.
  - Compute claimed/claimable from `player.sosumenhplayer.reward[level - 1]` and `rewardVip[level - 1]`.
- Skip or error on out-of-range level:
  - Recommended: log and skip levels outside `1..20`.
  - Do not crash player session for bad admin data if other rows are valid.

Reward parse method:

```java
private List<RewardItemView> parseRewardItems(String rawJson)
```

Rules:

- If JSON is null/empty/not array, return empty list and log warning.
- For each item object:
  - Parse `temp_id`.
  - Parse `quantity`.
  - `options` missing/null means empty list.
  - Use `ItemService.gI().getTemplate(tempId)` for name/icon.
  - Use `ItemService.gI().getItemOptionTemplate(optionId).name.replace("#", String.valueOf(param))`.
- If one reward item is invalid, log enough context and skip that item, not entire panel.

### 3. Build snapshot

Add method:

```java
private SoSuMenhSnapshot buildSnapshot(Player player) throws SQLException
```

Behavior:

- Ensure `player.sosumenhplayer` and reward arrays exist.
- `maxLevel = 20`.
- `currentPoint = player.sosumenhplayer.getPoint()`.
- `currentLevel = Math.min(maxLevel, Math.max(0, player.sosumenhplayer.getLevel()))`.
- `nextLevelPoint = currentLevel >= maxLevel ? currentPoint : (currentLevel + 1) * 50`.
- `vipUnlocked = player.sosumenhplayer.isVip()`.
- `coutday = player.sosumenhplayer.getCoutday()`.
- `selectedLevel = Math.min(maxLevel, Math.max(1, currentLevel == 0 ? 1 : currentLevel))`.
- `tiers = loadRewardTiers(player)`.
- `tasks = buildMissionTasks(player)`.

`buildMissionTasks(player)`:

- Iterate `player.sosumenhplayer.ssmTaskMain`.
- Resolve template by `SoSuMenhManager.getInstance().findById(task.idTask)`.
- `taskName = template.getTask()`.
- `pointReward = template.getPoint()`.
- `targetCount` currently hardcoded in `SoSuMenhPlayer.addCountTask`.
- Add helper method to map id to target count:
  - id 1 -> 200
  - id 2 -> 100
  - id 3 -> 100
  - id 4 -> 1000
  - id 5 -> 30
- If template missing, skip task and log.

Optional cleanup:

- Extract the target count mapping to `SoSuMenhPlayer` or `SoSuMenhService` private method.
- Do not redesign task model in this phase.

### 4. Send snapshot packet

Add public method:

```java
public void openPanel(Player player)
```

Behavior:

- Build snapshot.
- Send message `-76`, action `2`, payload above.
- On exception:
  - Log exception with class logger.
  - Send `-76`, action `5`, message `Không tải được dữ liệu Sổ Sứ Mệnh`.
  - Also optionally send `Service.gI().sendThongBao(player, "...")`.

Add private writer helpers:

- `writeSnapshot(Message msg, SoSuMenhSnapshot snapshot)`
- `writeTier(Message msg, RewardTierView tier)`
- `writeRewardItem(Message msg, RewardItemView item)`
- `writeTask(Message msg, MissionTaskView task)`

Important:

- Check byte counts before casting.
- Use `writeByte(Math.min(value, 127))` where relevant if old message writer uses signed byte semantics.
- Keep payload order exactly as documented.

### 5. Replace NPC "Xem phần thưởng" text wall

In `src/npc/npc_manifest/SoSuMenh.java`:

- `case 0` should call:

```java
SoSuMenhService.getInstance().openPanel(player);
```

- Remove or stop using current `StringBuilder` reward text wall.
- Keep menu option label `Xem phần thưởng`, or rename to `Mở sổ`.
- Do not remove existing options:
  - Nhận thưởng
  - Mở khóa sổ
  - Mua lever
  - Xem xếp hạng
  - Xem thông tin hiện tại
  - Hòm Thư

After phase 2 client is implemented, consider removing old `Nhận thưởng` submenu or redirect it to the panel. For compatibility during phase 1, keep it.

### 6. Add claim-one server method

Add:

```java
public void claimOne(Player player, int level, int lane)
```

Validation order:

1. `player` and session not null.
2. `level >= 1 && level <= 20`.
3. `lane == 0 || lane == 1`.
4. Player level enough: `player.sosumenhplayer.getLevel() >= level`.
5. If lane Vip, require `player.sosumenhplayer.isVip()`.
6. If already claimed, send `Bạn đã nhận phần thưởng này rồi` and refresh snapshot.
7. Load reward for exact level and lane.
8. If reward empty/missing, send `Không có phần thưởng ở mốc này` and refresh snapshot.
9. Add items to mailbox.
10. Call `NDVSqlFetcher.updateMailBox(player)`.
11. Only after mailbox update succeeds, mark reward array claimed.
12. Send success message and snapshot.

Implementation detail:

- Reuse `createItem(JSONObject dataObject)` from current `giveReward`, but make it robust for empty options.
- `giveReward` currently loops all rows. Replace with a focused method:

```java
private boolean giveRewardForLevel(Player player, int level, String rewardColumn)
```

Return true only when mailbox update and claimed mark should happen.

Better structure:

```java
private List<Item> createRewardItemsForLevel(int level, String rewardColumn) throws SQLException
private boolean appendItemsToMailbox(Player player, List<Item> items)
private boolean isClaimed(Player player, int level, int lane)
private void markClaimed(Player player, int level, int lane)
```

### 7. Add claim-all server method

Add:

```java
public void claimAll(Player player)
```

Behavior:

- Build list of claimable `(level, lane)` pairs.
- For each pair:
  - Load reward items.
  - Add to a temporary `List<Item> rewardsToAdd`.
- If empty:
  - Send `Không có phần thưởng nào có thể nhận`.
  - Refresh snapshot.
  - Return.
- Append all items to mailbox once.
- Call `NDVSqlFetcher.updateMailBox(player)` once.
- If update succeeds:
  - Mark all included pairs claimed.
  - Send `Đã nhận X mốc thưởng, vui lòng kiểm tra hòm thư`.
- If update fails:
  - Do not mark any claimed.
  - Send `Không thể gửi quà vào hòm thư, vui lòng thử lại`.
- Refresh snapshot.

Important:

- This preserves idempotency and avoids partial claimed state.
- If mailbox capacity matters but mailbox list allows adding beyond capacity, keep existing behavior. Do not introduce capacity restriction unless current mailbox system requires it.

### 8. Route new `-76` actions in server controller

In `src/server/Controller.java`, command `-76`:

Current behavior reads one byte as index and routes by `player.typeRecvieArchiment`. Change carefully so old achievement index claims still work.

Controller logic:

```java
byte first = _msg.reader().readByte();
if (first == 6) {
    int level = _msg.reader().readByte();
    int lane = _msg.reader().readByte();
    SoSuMenhService.getInstance().claimOne(player, level, lane);
    break;
}
if (first == 7) {
    SoSuMenhService.getInstance().claimAll(player);
    break;
}
byte index = first;
// existing typeRecvieArchiment claim routing
```

This avoids breaking old achievement indices `0..4`.

### 9. Keep old achievement methods during transition

Do not delete immediately:

- `receive`
- `receiveVip`
- `loadAchievements`
- `show`

They may still be used by old menu path `Nhận thưởng`. After client panel is stable, a later cleanup can redirect/remove old path.

### 10. Server verification

Run targeted checks:

- Compile server:
  - Use the repo's existing build command if present.
  - If no build tool is configured, run project-specific compile command or report limitation.
- Manual server checks:
  - Open NPC and choose `Xem phần thưởng`.
  - Confirm packet action `2` reaches client without disconnect.
  - Claim normal level with enough level.
  - Claim Vip level without Vip unlocked: rejected.
  - Unlock Vip and claim Vip reward.
  - Claim all after some claimed states: only unclaimed rewards added.
  - Mailbox update fail simulation if possible: no claimed mark.

## Phase 2: Client Implementation

### 1. Add client data models

Preferred new files under `Assets/Scripts`:

- `SoSuMenhUiState.cs`
- `SoSuMenhTier.cs`
- `SoSuMenhRewardItem.cs`
- `SoSuMenhTaskView.cs`
- `SoSuMenhRewardOption.cs`

If the project style avoids many files, keep small classes in one `SoSuMenhUiState.cs`.

Fields:

```csharp
public class SoSuMenhUiState
{
    public int maxLevel;
    public int currentLevel;
    public int currentPoint;
    public int nextLevelPoint;
    public bool vipUnlocked;
    public int coutday;
    public int selectedLevel;
    public SoSuMenhTier[] tiers;
    public SoSuMenhTaskView[] tasks;
    public string errorMessage;
}
```

```csharp
public class SoSuMenhTier
{
    public int level;
    public bool normalClaimed;
    public bool normalClaimable;
    public bool vipClaimed;
    public bool vipClaimable;
    public SoSuMenhRewardItem[] normalRewards;
    public SoSuMenhRewardItem[] vipRewards;
}
```

```csharp
public class SoSuMenhRewardItem
{
    public short tempId;
    public string itemName;
    public short iconId;
    public int quantity;
    public SoSuMenhRewardOption[] options;
}
```

```csharp
public class SoSuMenhRewardOption
{
    public short optionId;
    public int param;
    public string text;
}
```

```csharp
public class SoSuMenhTaskView
{
    public int taskId;
    public string taskName;
    public int currentCount;
    public int targetCount;
    public bool finished;
    public int pointReward;
}
```

### 2. Store state globally

Preferred location:

- Add `public SoSuMenhUiState soSuMenhState;` to `Char.cs`, or
- Add static holder `SoSuMenhUi.gI().state`.

Use `Char.myCharz().soSuMenhState` if consistent with existing player-bound UI data like `arrArchive`.

### 3. Parse new `-76` actions in `Controller.cs`

Current `case -76` switch reads action byte:

- action `0`: achievement list
- action `1`: mark achievement received

Add:

- action `2`: parse SSM snapshot.
- action `5`: parse SSM error.

Parsing snapshot:

```csharp
case 2:
{
    SoSuMenhUiState state = new SoSuMenhUiState();
    state.maxLevel = msg.reader().readByte();
    state.currentLevel = msg.reader().readByte();
    state.currentPoint = msg.reader().readInt();
    state.nextLevelPoint = msg.reader().readInt();
    state.vipUnlocked = msg.reader().readBoolean();
    state.coutday = msg.reader().readByte();
    state.selectedLevel = msg.reader().readByte();
    int tierCount = msg.reader().readByte();
    state.tiers = new SoSuMenhTier[tierCount];
    ...
    int taskCount = msg.reader().readByte();
    state.tasks = new SoSuMenhTaskView[taskCount];
    ...
    Char.myCharz().soSuMenhState = state;
    GameCanvas.panel.setTypeSoSuMenh();
    GameCanvas.panel.show();
    break;
}
```

Add private parse helpers if local style allows:

- `readSoSuMenhTier(Message msg)`
- `readSoSuMenhRewardItem(Message msg)`
- `readSoSuMenhTask(Message msg)`

If `Controller.cs` convention avoids helper methods, still keep code grouped and readable.

Parsing error:

```csharp
case 5:
{
    SoSuMenhUiState state = new SoSuMenhUiState();
    state.errorMessage = msg.reader().readUTF();
    Char.myCharz().soSuMenhState = state;
    GameCanvas.panel.setTypeSoSuMenh();
    GameCanvas.panel.show();
    break;
}
```

### 4. Add client send methods

Preferred location: `Service.cs`.

Add:

```csharp
public void ssmClaimOne(int level, int lane)
{
    Message message = null;
    try
    {
        message = new Message(-76);
        message.writer().writeByte(6);
        message.writer().writeByte(level);
        message.writer().writeByte(lane);
        session.sendMessage(message);
    }
    finally
    {
        message?.cleanup();
    }
}
```

```csharp
public void ssmClaimAll()
{
    Message message = null;
    try
    {
        message = new Message(-76);
        message.writer().writeByte(7);
        session.sendMessage(message);
    }
    finally
    {
        message?.cleanup();
    }
}
```

Use existing cleanup/send conventions from nearby `Service.cs` methods.

### 5. Add panel type for Sổ Sứ Mệnh

In `Panel.cs`:

- Add a new type constant if this file uses constants.
- Add method:

```csharp
public void setTypeSoSuMenh()
```

Expected setup:

- `type = <new type id>`.
- `currentListLength = state?.tiers?.Length ?? 0`.
- `selected = selected tier index matching `state.selectedLevel`.
- `cmy = 0`, reset scroll.
- Set `ITEM_HEIGHT` suitable for level rows if needed.

Choose a type id not used by existing panel types. Search existing type assignments before implementing.

### 6. Render dedicated panel header

In `paintTabHeader` or equivalent title method in `Panel.cs`, add case for SSM type:

Title:

- `SỔ SỨ MỆNH`

Header area content:

- Current level: `Cấp X`
- Point progress: `currentPoint/nextLevelPoint`
- Vip status:
  - Vip unlocked: `Sổ Vip: Đã mở`
  - Vip locked: `Sổ Vip: Chưa mở`
- Daily task remaining: `Lượt nhiệm vụ ngày: coutday`

Draw progress bar:

- Background: darker brown/gold.
- Fill: green or gold.
- Clamp width to `0..barW`.
- If max level, show full bar and text `Tối đa`.

### 7. Render track-first body

Add method:

```csharp
private void paintSoSuMenh(mGraphics g)
```

Structure:

1. Clip to scroll area.
2. If `state == null`: draw loading/empty message.
3. If `state.errorMessage` not empty: draw centered error.
4. Draw horizontal level track around selected/current level:
   - Show 5 levels on small width, 7 if panel width allows.
   - Levels before/equal current are unlocked.
   - Selected level has strong border.
   - Claimable level can blink subtly or use gold border.
5. Draw selected tier reward lanes:
   - Lane 0: `Sổ thường`.
   - Lane 1: `Sổ Vip`.
   - Each lane:
     - Status text: `Đã nhận`, `Có thể nhận`, `Chưa đạt`, `Chưa mở Vip`.
     - Item slots with icon and quantity.
     - Button `Nhận` if claimable.
6. Draw mission strip:
   - Show up to 5 task rows.
   - Each row: checkmark if finished, taskName, count/target, `+point`.
   - Keep row height stable.
7. Footer:
   - Button `Nhận tất cả` if any claimable normal/Vip reward.
   - Otherwise disabled-looking text/button `Không có quà có thể nhận`.

Friendly UX requirements:

- Player should know why a reward is locked:
  - `Chưa đạt cấp`
  - `Chưa mở Vip`
  - `Đã nhận`
- Claimable state must be more visible than locked/claimed.
- Avoid long option text in item slots; show item name and quantity first.
- If options need display, show first 1-2 lines in selected item info area or tooltip-like detail area.

### 8. Item slot rendering

Implement helper:

```csharp
private void paintSoSuMenhRewardItem(mGraphics g, SoSuMenhRewardItem item, int x, int y, bool locked)
```

Behavior:

- Draw square slot using existing inventory/shop style if possible.
- Draw item icon by `SmallImage.drawSmallImage` or existing item icon helper used by inventory.
- Draw quantity at bottom-right if `quantity > 1`.
- If locked, overlay translucent dark/gray rectangle or draw muted border.
- If item icon missing, draw placeholder slot and item name abbreviation.

Do not use emoji or new external assets.

### 9. Navigation/input

Keyboard/touch behavior:

- Left/right changes selected level on track.
- Up/down moves between reward lanes/task/action where possible.
- Fire/OK:
  - On claimable lane button: send claim one.
  - On claim all button: send claim all.
- Back/close closes panel as existing panel behavior.

Mouse/touch:

- Tapping level selects it.
- Tapping `Nhận` claims corresponding lane.
- Tapping `Nhận tất cả` sends claim all.

State:

- After sending claim request, avoid spam:
  - Add simple `ssmRequesting` boolean in panel or state.
  - Set true when sending.
  - Reset when snapshot action `2` or error action `5` arrives.
- If no request flag fits existing code style, at least disable repeated claim while `GameCanvas.isLoading` or similar existing state is active.

### 10. Command action handling in `Panel.cs`

Find `perform(int idAction, object p)` or equivalent command handler.

Add command ids:

- `SSM_CLAIM_NORMAL`
- `SSM_CLAIM_VIP`
- `SSM_CLAIM_ALL`
- Optional `SSM_SELECT_LEVEL`

Use numeric ids outside existing ranges.

Actions:

- Claim normal: `Service.gI().ssmClaimOne(selectedLevel, 0)`
- Claim Vip: `Service.gI().ssmClaimOne(selectedLevel, 1)`
- Claim all: `Service.gI().ssmClaimAll()`
- Select level: update `state.selectedLevel` and repaint.

### 11. Client visual polish checklist

Panel must feel like current game:

- No modern web gradients.
- No oversized hero text.
- No decorative noise/orbs.
- Use existing `mFont` instances.
- Use existing panel colors or close palette:
  - Panel background: be/vàng.
  - Border: nâu.
  - Selected: vàng sáng/cam.
  - Claimable: cam/đỏ button with readable text.
  - Completed: xanh.
  - Locked: gray/brown muted.
- Text must fit inside panel width.
- Reward lanes must not overlap on small screen.
- Buttons must be at least current game button hit area size.
- Progress bar text must remain readable.

### 12. Client verification

Manual client checks:

- Open NPC -> `Xem phần thưởng` opens SSM panel, not text popup.
- Panel renders without crash with all 20 levels.
- Select previous/next level.
- Normal reward item icons render.
- Vip locked state clear before unlocking.
- Claim one normal reward updates state after server snapshot.
- Claim all updates all claimable states and shows mailbox message.
- Already claimed reward cannot be claimed again.
- Panel with bad/empty reward data shows error/empty state and can close.
- Small screen/mobile layout does not overlap.

If Unity command-line compile is unavailable, report that limitation and at least run text search/syntax-risk review on touched C# files.

## Implementation Order

1. Server DTO + snapshot builder.
2. Server packet writer action `2`/`5`.
3. NPC `Xem phần thưởng` opens snapshot panel.
4. Server claim one/all methods.
5. Server controller handles client request actions `6`/`7`.
6. Client state classes.
7. Client `Controller.cs` parses action `2`/`5`.
8. Client `Service.cs` sends `6`/`7`.
9. Client `Panel.cs` adds SSM type and rendering.
10. Client input/commands for level select and claim buttons.
11. Manual server-client test.

## Acceptance Criteria

- Player can open a dedicated Sổ Sứ Mệnh panel from NPC.
- Panel uses NRO legacy visual language and is easier to understand than the current text table.
- Player can see level progress and next reward quickly.
- Player can see normal and Vip rewards for each level with item slots.
- Player can claim one reward or all available rewards.
- Vip rewards are visibly locked until Vip is unlocked.
- Rewards are still delivered to mailbox.
- Server prevents double rewards even if client sends repeated claim requests.
- Existing achievement packet action `0/1` still works.
- Existing old SSM achievement list path is not broken during transition.

## Non-goals

- No DB schema migration.
- No economy/reward content changes.
- No global panel refactor.
- No removal of old achievement feature.
- No new external asset dependency.
- No web admin changes in `nrolube-web`.

## Known Risks

- `-76` is already overloaded. Use client request actions `6/7` to avoid collision with old achievement index values.
- `Panel.cs` is large and legacy. Keep SSM rendering helpers local and avoid unrelated refactors.
- Reward JSON may contain bad item ids/options. Parser must skip bad entries and log instead of crashing player.
- Packet can be large if all item options are sent for all 20 levels. If runtime packet size becomes a problem, switch action `2` to send only 7 visible tiers plus selected tier details in a later optimization.
- Mailbox update currently determines persistence. Do not mark reward claimed until mailbox update succeeds.
