# 02 - Player Combat Và Stat Runtime

## 1) Phạm vi

Spec này mô tả luật runtime liên quan đến:

- player stat và scaling.
- player <-> mob damage pipeline.
- skill modifier và guardrail.

## 2) Thành phần cốt lõi

- `src/player/NPoint.java`: tính toán stat, dame, hp/mp, crit, option stack.
- `src/services/SkillService.java`: điểm vào tấn công và sử dụng skill.
- `src/mob/*`: mob injured/attack logic.
- `src/utils/SkillUtil.java`: utility theo skill/cooldown/percent.

## 3) Combat pipeline (rút gọn)

### Player đánh mob

1. Action vào `SkillService`.
2. Tính dame theo `NPoint` + modifier.
3. Gọi `mob.injured(...)`.
4. Áp guardrails theo map/mob mode.
5. Tính reward (tiềm năng...) theo ratio damage.

### Mob đánh player

1. Mob point sinh damage nền.
2. Áp modifier map/charm/state.
3. Gọi `player.injured(..., isMobAttack=true)`.
4. Áp guardrail sinh tồn và immunity logic.

## 4) Rule khi sửa balance/combat

- Không sửa nhiều nhóm modifier cùng lúc nếu chưa có baseline.
- Tách thay đổi thành từng lớp:
  - formula layer
  - skill layer
  - map/special-case layer
- Mỗi thay đổi cần ghi rõ:
  - đối tượng ảnh hưởng (PvE/PvP/map nào)
  - expected delta (damage, TTK, survivability)
  - cách rollback.

## 5) Risk hotspots

- File lớn và coupling cao: `NPoint.java`, `SkillService.java`.
- Skill có tempo cao (cooldown ngắn) dễ gây mất cân bằng nếu thêm stat all-in-one.
- Guardrail map đặc thù dễ bị bỏ quên khi sửa logic tổng quát.

## 6) Checklist cho task combat

- Có baseline trước khi sửa (DB/runtime snapshot).
- Xác định rõ skill/map/tier bị tác động.
- Test lại 2 chiều: player->mob và mob->player.
- Xác nhận reward pipeline không bị vỡ.
