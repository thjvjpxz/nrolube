# Phân Tích Chuyên Sâu Cân Bằng Mob/Player

## Phạm vi và phương pháp

- Nguồn dữ liệu kết hợp:
  - Luồng runtime trong `src/mob`, `src/player`, `src/services`, `src/map`, `src/jdbc`, `src/power`.
  - Snapshot DB thực tế qua MCP `user-nrolune-local` (`mob_template`, `map_template`, `player`, `power_limit`).
  - Đồ thị rủi ro thay đổi qua MCP `user-gitnexus` (`context`, `impact`).
- Mục tiêu: tạo baseline cân bằng khả dụng cho `mob <-> player` theo các tầng tiến trình, sau đó đề xuất hướng tuning an toàn.
- Ràng buộc: đây là lượt phân tích, chưa chỉnh gameplay code.

## 1) Bản đồ pipeline combat (DB -> runtime -> damage)

## 1.1 Nạp dữ liệu và gán chỉ số runtime

- `mob_template` được load trong `Manager` (`id`, `type`, `hp`, `percent_dame`, `percent_tiem_nang`, ...).
- Mob runtime được khởi tạo ở `Map.initMob(...)`:
  - HP combat thực tế lấy từ `map_template.mobs[*][2]` (`mobHp`), không lấy trực tiếp từ `mob_template.hp`.
  - `mob.pDame` và `mob.pTiemNang` lấy từ `mob_template.percent_dame` và `percent_tiem_nang`.
- Chỉ số cốt lõi player nạp từ `player.data_point` trong `NDVSqlFetcher`:
  - `[limitPower, power, tiemNang, stamina, maxStamina, hpg, mpg, dameg, defg, critg, ..., hpCurrent, mpCurrent]`.

## 1.2 Luồng damage: player -> mob

- Điểm vào:
  - `SkillService.playerAttackMob(...)`
  - `dameHit = plAtt.nPoint.getDameAttack(true)`
  - `mob.injured(plAtt, dameHit, dieWhenHpFull)`
- Nhóm modifier chính trước `mob.injured`:
  - Tăng/giảm theo skill, item/set/charm trong `NPoint.getDameAttack(true)`.
  - Stack `tlDameAttMob` (buff damage chuyên đánh quái).
  - Crit (`isCrit`) và nhân crit.
  - Nhánh skill đặc biệt (`MAKANKOSAPPO`, `QUA_CAU_KENH_KHI`, ...).
- Trong `Mob.injured(...)` còn các lớp chặn:
  - Anti-oneshot khi full HP (`dieWhenHpFull=false`).
  - Hard cap cho mob tập luyện (`MOC_NHAN`, `BU_NHIN_MA_QUAI`).
  - Rule đặc thù map (ví dụ map khí gas hủy diệt ép skill damage về 1 trong điều kiện nhất định).

## 1.3 Luồng damage: mob -> player

- Điểm vào:
  - `Mob.mobAttackPlayer(...)`
  - Damage nền từ `MobPoint.getDameAttack()`
  - `player.injured(null, dameMob, false, true)` (`isMobAttack=true`).
- Công thức damage nền của mob (`MobPoint.getDameAttack()`):
  - nếu `point.dame != 0`: dùng giá trị này với nhiễu ngẫu nhiên nhỏ
  - ngược lại: `hpFull * random(pDame-1..pDame+1) / 100 + random(-level*10..level*10)`
- Modifier runtime quan trọng:
  - Charm giảm damage quái (`tdDaTrau`, tương tác pet/master).
  - `lvMob > 0` ở map không phải phó bản: damage quái đổi sang % HP mục tiêu.
  - Giảm bởi satellite defend, item-time, các cửa miễn nhiễm theo charm.
- Trong `Player.injured(..., isMobAttack=true)`:
  - Pipeline phòng thủ khác đáng kể so với nhiều nhánh PvP.
  - Một số nhánh giảm/chính xác theo skill PvP bị bỏ qua khi nguồn tấn công là mob.
  - Có thêm guardrail sinh tồn (`bùa bất tử`, Halloween, rule map đặc thù).

## 1.4 Liên kết reward

- `Mob.getTiemNangForPlayer(pl, damage)`:
  - Tính từ `maxTiemNang` của mob và tỉ lệ damage (`damage / mobHp`), sau đó scale theo chênh lệch level.
  - Có thêm điều chỉnh theo map và ngưỡng power của player.

---

## 2) Baseline theo tier (Power, Player snapshot, phân bố Mob/Map)

## 2.1 Mốc tier `power_limit` (14 tier)

- Ngưỡng power T1 -> T14:
  - bắt đầu `17,999,999,999`, kết thúc `280,010,000,000`.
- Baseline point tương ứng từ DB tăng theo:
  - HP `220,000 -> 700,000`
  - Damage `11,000 -> 35,000`
  - Defense `550 -> 10,000`
  - Critical `5 -> 10`.

## 2.2 Snapshot player (`player.data_point`) từ DB thực tế

- Tổng bản ghi hiện tại: 3 player.
- Mẫu parse:
  - `admin`: power `202,711,081,056`, hp hiện tại `23,543,216`, mp hiện tại `29,863,104`, damage point gốc `5,016`.
  - 2 bản ghi power thấp gần mốc tân thủ (`~2k power`).
- Nhận định: phân phối còn thưa, nên cân bằng cần dựa vào cấu trúc công thức + template nhiều hơn là thống kê population.

## 2.3 Baseline `mob_template`

- Tổng template: `127`.
- Mẫu phân bố chính:
  - `118/127` mob dùng `(percent_dame=5, percent_tiem_nang=50)`.
  - Ngoại lệ high-end:
    - 6 mob ở `(6,80)`, 2 mob ở `(6,60)`.
  - 1 outlier `percent_dame=0` (`id=3`).

## 2.4 Baseline spawn map (`map_template.mobs`) từ DB

- Map parse được với slot mob hợp lệ: `134`.
- Tổng slot parse được: `1,112`.
- Phân vị HP theo slot:
  - min `100`
  - p50 `170,000`
  - p75 `1,400,000`
  - p90 `7,520,020`
  - p95 `10,350,000`
  - max `500,000,000`.
- Map có HP trung bình rất cao:
  - `Hành tinh ngục tù 2`, `Juventus`, `Đáy Xã Hội`, `Hành tinh ngục tù 3`.
- Slot level cao tập trung mạnh ở band `20`, `21`, `22`, và band custom `100+`.

---

## 3) Bộ chỉ số cân bằng đã tính (TTK, áp lực DPS, EHP proxy, OneShot Risk)

## 3.1 Mô hình tính

- Giả định nhịp hit nền theo vòng lặp mob (~2 giây/nhịp).
- `TTK_player_to_mob` (giây):
  - `ceil(mobHp / playerDamage) * 2`.
- `TTK_mob_to_player` (giây):
  - damage nền mob: `mobHp * percent_dame / 100`
  - `ceil(playerHp / mobHit) * 2`.
- One-shot risk:
  - true nếu `mobHit >= playerHp` trước các lớp giảm/né khác.
- Reward efficiency proxy mỗi hit:
  - xấp xỉ trung tính theo lõi `getTiemNangForPlayer`:
  - `reward_per_hit ~= min(playerDamage * percent_tiem_nang / 100, 29,999,999)`.

## 3.2 Tập kịch bản

- `lvl20_med`: mobHp `6,375,000`, pd `5`, ptn `50`
- `lvl22_med`: mobHp `12,000,000`, pd `5`, ptn `50`
- `lvl100_med`: mobHp `7,520,020`, pd `6`, ptn `80`
- `lvl100_peak`: mobHp `80,000,000`, pd `6`, ptn `80`

## 3.3 Tóm tắt theo band tier

- Early band (T1-T4):
  - `lvl20_med`: player TTK avg `946s`, mob TTK avg `2.5s`, one-shot `3/4`.
  - `lvl22_med`: player TTK avg `1779s`, mob TTK avg `2s`, one-shot `4/4`.
- Mid band (T5-T9):
  - `lvl20_med`: player TTK avg `556.4s`, mob TTK avg `4s`, one-shot `0/5`.
  - `lvl22_med`: player TTK avg `1046.4s`, mob TTK avg `2s`, one-shot `5/5`.
- Late band (T10-T14):
  - `lvl20_med`: player TTK avg `429.6s`, mob TTK avg `4.8s`, one-shot `0/5`.
  - `lvl22_med`: player TTK avg `806.8s`, mob TTK avg `3.2s`, one-shot `2/5`.
- Kịch bản `lvl100_peak`:
  - vẫn cho thấy áp lực rất cao (`mob TTK ~2s` trong baseline thô), trong khi player TTK còn rất dài.

## 3.4 Diễn giải

- Baseline hiện tại bị lệch đáng kể:
  - tăng trưởng HP slot map nhanh hơn nhiều so với tăng trưởng `power_limit.damage`.
  - coupling `mobHp * percent_dame` tạo burst quá lớn ở map HP cao.
- Vấn đề cân bằng không chỉ là chỉnh số damage đơn lẻ, mà là thiết kế coupling giữa HP mob và damage mob gây ra.

---

## 4) Phân tích outlier và rủi ro thay đổi

## 4.1 Outlier dữ liệu

- Outlier template:
  - `mob_template.id=3` có `percent_dame=0`.
  - `id=109/117` (`Máy Đo Sức Mạnh`) và template HP 2B làm méo hành vi công thức tổng quát.
- Outlier spawn:
  - slot HP rất cao tới `500,000,000` vẫn đi cùng logic damage liên kết theo HP.
  - band level `100+` chứa các pocket HP cố định rất cao.
- Outlier progression:
  - `Service.getCurrLevel(...)` có bước nhảy rất lớn trước level 20 (`< 11,100,010,000,000`).

## 4.2 Blast radius theo GitNexus

- `NPoint.getDameAttack`:
  - rủi ro `HIGH`, chạm luồng combat lõi (`SkillService.useSkillAttack`, `Controller.onMessage`) và nhiều module.
- `Mob.injured`:
  - rủi ro `HIGH`, ảnh hưởng rộng qua `SkillService`, map update, hệ boss override.
- `Player.injured`:
  - rủi ro `CRITICAL`, fan-out rất lớn (services + nhiều nhánh boss/event).

## 4.3 Vùng chỉnh sửa an toàn vs rủi ro

- Vùng tương đối an toàn:
  - scale ở tầng bảng (`mob_template`, phân phối `map_template` theo nhóm map) + rule map-level.
- Vùng rủi ro cao:
  - method toàn cục (`Player.injured`, `Mob.injured`, `NPoint.getDameAttack`) nếu sửa không có guard/flag.

---

## 5) Chiến lược tuning theo tiến trình

## 5.1 Hướng A: Template-first (rủi ro triển khai thấp hơn)

- Giữ công thức lõi trước, tuning trên `mob_template` và phân bố `map_template`:
  - tách HP cao khỏi áp lực damage quá mức bằng cách hạ band `percent_dame` ở content HP cao.
  - làm mượt đường cong HP ở các cụm level `20/21/22/100+`.
  - target window:
    - Early: mob thường không one-shot trong baseline.
    - Mid: mob TTK -> player khoảng `6-12s` ở map thường.
    - Late: giữ độ khó nhưng tránh chết `<=2s` ngoài map/event đặc biệt.
- Dùng ma trận điều khiển:
  - `(map_group, hp_range, percent_dame, percent_tiem_nang, expected_ttk_window)`.

## 5.2 Hướng B: Formula-first (rủi ro cao hơn, đòn bẩy lớn hơn)

- Refactor coupling nơi damage mob scale trực tiếp từ full HP.
- Thêm cơ chế chặn biên:
  - ví dụ `mob_base_damage = clamp(hpScaledDamage, minFloor, maxByTier)` + override theo map.
- Thêm nhánh theo mode:
  - giữ độ gắt cho map/event đặc biệt nhưng ổn định map progression thường.
- Bắt buộc rollout theo giai đoạn và có feature flag vì blast radius `HIGH/CRITICAL`.

## 5.3 Thứ tự rollout khuyến nghị

1. Làm `Template-first` trước trên các cụm map biến thiên cao (`20+`, `100+`, prison/special).
2. Quan sát telemetry thực chiến (tỉ lệ chết, thời lượng giao tranh, tiêu hao hồi phục).
3. Sau đó mới chỉnh công thức giới hạn ở nhánh có guard rõ ràng.

---

## 6) Lộ trình phân tích đầy đủ nguồn tăng chỉ số (mở rộng)

Bạn phản hồi đúng: bản trước mới ưu tiên combat lõi, chưa tách hết lớp tăng chỉ số từ skill/đồ/cải trang/hợp thể và hệ phụ. Lộ trình dưới đây dùng để hoàn thiện phần đó theo pha.

## 6.1 Pha A - Lập inventory nguồn tăng chỉ số (stat source inventory)

- Mục tiêu: liệt kê 100% nguồn tác động lên `hp/mp/dame/def/crit` và các tỷ lệ phụ (`tlDame`, `tlDameCrit`, `tlNeDon`, `tlGiap`, ...).
- Cụm file bắt buộc:
  - `src/player/NPoint.java` (`setPointWhenWearClothes`, `resetPoint`, `setCaiTrang`, `setDeoLung`, `setPet`, `setLinhThu`, `setThuCuoi`, `getDameAttack`).
  - `src/services/SkillService.java` (buff/debuff theo skill active, nhánh skill đặc biệt).
  - `src/services/ItemTimeService.java` + cờ `itemTime.*`.
  - `src/player/Player.java` + `src/player/PointFusion.java` + `src/player/Fusion.java` (hợp thể/fusion).
  - `src/jdbc/daos/NDVSqlFetcher.java` + `src/jdbc/daos/PlayerDAO.java` (DB fields lưu điểm gốc, điểm hợp thể).
- Deliverable:
  - Bảng `StatSourceMatrix` gồm:
    - `source_type` (equip, skill, effect, fusion, event, clan, itemTime, map),
    - `trigger_condition`,
    - `stack_rule` (cộng/nhân/override/cap),
    - `stat_targets`,
    - `file_method`.

## 6.2 Pha B - Chuẩn hóa thứ tự áp dụng modifier (order-of-operations)

- Mục tiêu: chốt chính xác thứ tự cộng/trừ/nhân/cap để tránh sai khi mô phỏng.
- Trục cần khóa:
  - Điểm gốc (`data_point`) -> cộng điểm từ đồ/card/set -> buff tạm thời (`itemTime`, skill effect) -> trạng thái đặc biệt (cải trang/linh thú/thú cưỡi) -> crit/variance -> map-rule.
- Deliverable:
  - `ModifierExecutionOrder` cho 2 hướng:
    - `player -> mob`,
    - `mob -> player`.

## 6.3 Pha C - Tách riêng cụm tăng chỉ số bạn nêu

- **Skill**:
  - skill chủ động, skill duy trì, skill charge/prepare, skill biến hình, skill đặc biệt (QCKK, Makankosappo, Kaioken, ...).
- **Đồ / set / card / option**:
  - option từ body items, set bonus, card option, blackball reward, train armor, phụ kiện.
- **Cải trang / đeo lưng / pet skin / linh thú / thú cưỡi**:
  - các cờ bool trong `NPoint` và hiệu ứng gián tiếp lên damage/crit/phòng thủ.
- **Hợp thể**:
  - `pointfusion` (`hp_point_fusion`, `mp_point_fusion`, `dame_point_fusion`) + vòng đời `Fusion.typeFusion`.
- Deliverable:
  - 4 bảng con theo nhóm trên, mỗi bảng có cột `stackable`, `exclusive_group`, `max_cap`.

## 6.4 Pha D - Mô phỏng tổ hợp build (build simulation)

- Mục tiêu: đo chênh lệch khi stack nhiều lớp buff cùng lúc.
- Thiết kế test matrix:
  - Theo tier (`power_limit`),
  - Theo archetype build (glass-cannon, balanced, tanky, crit-heavy),
  - Theo state (no-buff, buff chuẩn map, buff full event).
- Metric mở rộng:
  - `BurstWindowDamage(3s/5s)`,
  - `SustainDPS(30s)`,
  - `EffectiveHP_vs_Mob`,
  - `KillTimeVariance` theo crit/proc.

## 6.5 Pha E - Cân bằng theo vùng gameplay

- Tách target riêng theo cụm:
  - map thường progression,
  - map farm tài nguyên,
  - map event,
  - phó bản/boss arena.
- Mỗi cụm có `target envelope` khác nhau cho:
  - `TTK_player_to_mob`,
  - `TTK_mob_to_player`,
  - `OneShotRisk`.

## 6.6 Pha F - Chốt rule chỉnh sửa an toàn

- Ưu tiên:
  1. chỉnh dữ liệu (`mob_template`, `map_template`) và rule map trước,
  2. sau đó mới chỉnh công thức global.
- Bất kỳ chỉnh nào chạm:
  - `Player.injured`,
  - `Mob.injured`,
  - `NPoint.getDameAttack`
  cần kèm:
  - feature flag,
  - canary map set,
  - regression pack boss/event.

## 6.7 Kết quả phân tích sâu vòng 1 (StatSourceMatrix v1)

Phần này là kết quả bóc tách thực tế từ code hiện tại, tập trung vào các nguồn bạn nhắc: skill, đồ, cải trang, hợp thể và các lớp tăng chỉ số liên quan.

### A) Điểm gốc và dữ liệu persistence

- **Nguồn gốc base point**:
  - `player.data_point` nạp vào `NPoint` trong `NDVSqlFetcher`:
    - `limitPower`, `power`, `hpg`, `mpg`, `dameg`, `defg`, `critg`.
- **Điểm hợp thể lưu DB**:
  - `hp_point_fusion`, `mp_point_fusion`, `dame_point_fusion` nạp vào `PointFusion` trong `NDVSqlFetcher`.
  - Persist ngược trong `PlayerDAO` qua `player.pointfusion.getHpFusion/getMpFusion/getDameFusion`.
- **Mốc mở trần sức mạnh**:
  - `itemTime.isOpenPower` tự tăng `limitPower` theo timer rồi gọi `initPowerLimit()`.

### B) Lõi tính điểm tổng (điểm hội tụ stat)

- **Điểm hội tụ chính**: `NPoint.calPoint()` -> `setPointWhenWearClothes()`.
- **Bước reset bắt buộc**: `resetPoint()` xóa toàn bộ bonus tạm (`tlDame`, `tlDameCrit`, `tlDameAttMob`, `tlNeDon`, `tlGiap`, buff cờ đặc biệt...).
- **Ý nghĩa**: hệ hiện tại hoạt động theo mô hình `rebuild-from-scratch`, nên sai thứ tự apply là nguồn lệch cân bằng lớn nhất.

### C) Nhóm tăng từ đồ / option / set / card

- **Item option + card option** được cộng mạnh trong `setPointWhenWearClothes()`:
  - cộng thẳng (`hpAdd`, `mpAdd`, `dameAdd`, `defAdd`, `critAdd`),
  - cộng tỷ lệ (`tlDame`, `tlDameAttMob`, `tlDameCrit`, `tlHp`, `tlMp`, `tlDef`...),
  - cờ đặc biệt (teleport, hút HP/MP, phản sát thương, chính xác, giảm giáp...).
- **Set đồ** được gom ở `SetClothes.setup()`:
  - đếm stack set (`songoku`, `kakarot`, `nail`, `cadicM`, `thanVuTruKaio`, ...),
  - từ đó được tiêu thụ lại trong các hàm tính `NPoint` và `getDameAttack(...)`.

### D) Nhóm cải trang / đeo lưng / pet skin / linh thú / thú cưỡi

- Nằm ở 5 cụm:
  - `setCaiTrang()`
  - `setDeoLung()`
  - `setPet()`
  - `setLinhThu()`
  - `setThuCuoi()`
- Mỗi cụm bật các cờ boolean (`isToppo`, `isNaruto`, `isBanthan`, `isFireSoul`, `isTenLuaCaMap`, ...),
  sau đó các cờ này được dùng rải rác ở các bước tính HP/MP/dame/crit.
- **Rủi ro cân bằng**: nhiều cờ là tăng dạng “điều kiện kép” (theo skin + theo skill/map), dễ tạo stack bùng nổ nếu không giới hạn cap.

### E) Nhóm skill/effect (active state)

- Trạng thái hiệu ứng tập trung ở `EffectSkill`:
  - `isMonkey`, `isSuper`, `isBienHinh`, `isDameBuff`, `isShielding`, `isTanHinh`, ...
- `EffectSkillService` quản lý lifecycle:
  - bật/tắt trạng thái + thời lượng.
  - ví dụ `setDameBuff(..., tileDameBuff)` và `removeDameBuff(...)`.
- Khi tính damage, `NPoint.getDameAttack(...)` và các hàm set stat đọc trực tiếp các cờ này để cộng thêm phần trăm.

### F) Nhóm buff thời gian (itemTime)

- `ItemTime` có rất nhiều cờ tác động stat/combat:
  - `isUseBoHuyet`, `isUseBoKhi`, `isUseGiapXen`, `isUseCuongNo`,
  - `isEatMeal`, `isEatMeal2`, `isEatMeal3`,
  - `isUseLoX2/5/7/10/15`, `isUseKhauTrang`, `isUseCMS`, ...
- Hết hạn timer sẽ gọi `Service.point(player)` để rebuild stat.
- **Điểm cần kiểm soát**: nhiều buff cùng tác động trên cùng một trục (damage/crit/HP) nhưng đến từ nhiều cờ độc lập.

### G) Nhóm hợp thể (fusion)

- State hợp thể ở `Fusion.typeFusion`, vòng đời bởi `Fusion.update()` và các service liên quan.
- Trong `NPoint`, khi hợp thể đang bật có nhánh cộng thêm từ pet/master ở nhiều đoạn.
- `pointfusion` dùng như mốc lưu đỉnh chỉ số hợp thể (đặc biệt `DameFusion`) để phục vụ logic/tính năng liên quan.
- **Rủi ro cân bằng**: hợp thể + set + itemTime + effect có thể tạo stack nhiều tầng nếu không tách rõ thứ tự nhân/cộng.

### H) Nhóm bang hội / sự kiện / map rule

- Bang hội:
  - `hpbang`, `mpbang`, `damebang`, `critbang`, `csbang` được cộng vào HP/MP/Dame/Crit với cap theo `clan.level`.
- Sự kiện:
  - ví dụ `EventDAO.getRemainingTimeToIncreaseDame()` tạo thêm % damage trong khung giờ.
- Rule map:
  - map cold, blackball war, map đặc thù ảnh hưởng trực tiếp vào damage/HP behavior.

### I) Thứ tự áp dụng hiện tại (rút gọn)

1. Nạp điểm gốc (`data_point`) + trạng thái runtime.
2. `calPoint()` gọi `setPointWhenWearClothes()` -> reset toàn bộ bonus.
3. Cộng bonus từ item/card/set/cải trang/đeo lưng/pet/linh thú/thú cưỡi.
4. Cộng buff item-time + effect-skill + state đặc biệt (monkey/super/biến hình).
5. Cộng bang hội/sự kiện/map rule.
6. Khi đánh: `getDameAttack(...)` áp skill multiplier + crit + variance + nhánh đặc biệt.

### J) Lỗ hổng phân tích đã xác nhận và sẽ làm tiếp

- Chưa có bảng `exclusive_group`/`priority` chuẩn hóa cho tất cả cờ boolean.
- Chưa lock đầy đủ trục “cộng trước hay nhân trước” cho từng nhóm modifier.
- Chưa có profile build mẫu (no-buff / map-buff / full-buff) để đo sai khác thực chiến.

Kế tiếp sẽ làm Pha B + Pha C theo ma trận vừa bóc, rồi dựng mô phỏng build ở Pha D.

## 6.8 ModifierExecutionOrder (Pha B - chi tiết thứ tự áp dụng)

Đây là thứ tự thực thi rút ra trực tiếp từ code hiện tại, dùng làm chuẩn khi phân tích/tuning để tránh nhầm tầng.

### 6.8.1 Thứ tự rebuild stat nền (`NPoint.calPoint -> setPointWhenWearClothes`)

1. `resetPoint()`:
   - xóa toàn bộ biến cộng thêm/tỷ lệ/cờ trạng thái phụ.
2. Cộng reward/card/option:
   - blackball reward, card option, item option qua `addOption(...)`, badge option.
3. Cộng đếm set/item body:
   - worldcup count, teleport flags, armor train bonus.
4. `setBasePoint()` theo thứ tự cứng:
   - `checkLevel()`
   - `setHpMax() -> setHp()`
   - `setMpMax() -> setMp()`
   - `setDame() -> setDef() -> setCrit()`
   - `setHpHoi() -> setMpHoi()`
   - cờ đặc thù (`setLtdb`, `setThoBulma`, `setDietQuy`, ...)
   - set lại cờ skin (`setCaiTrang`, `setDeoLung`, `setPet`, `setThuCuoi`, `setLinhThu`).
5. Sau base:
   - `setOutfitFusion*()`
   - gọi lại một số cờ skin
   - `setSpeed()`.

Ý nghĩa: hệ thống dùng mô hình “recompute full state”, nên mọi tuning cần xác định đang tác động ở bước 2, 4 hay 5.

### 6.8.2 Thứ tự damage `player -> mob`

1. `SkillService.playerAttackMob(...)`
2. Lấy damage từ `plAtt.nPoint.getDameAttack(true)`.
3. Áp thêm các nhánh ngoài `getDameAttack` trong `SkillService`:
   - bùa bất tử/Halloween chặn đánh,
   - charm `tdManhMe`,
   - pet/master charm `tdDeTu`,
   - miss handling.
4. `mob.injured(plAtt, dameHit, dieWhenHpFull)`:
   - anti-oneshot full HP,
   - cap đặc thù mob tập,
   - map rule đặc biệt.

Trong `getDameAttack(true)` thứ tự chính:

1. Refresh flag skin/pet/linh thú/đeo lưng + xác định crit state.
2. Lấy `dameAttack = this.dame` (đã qua `setDame()`).
3. Áp `% theo skill` + `% intrinsic` + `% dameAfter`.
4. Nếu có `isDameBuff` thì cộng `% buff`.
5. Nếu đánh mob (`isAttackMob=true`) thì cộng `tlDameAttMob`.
6. Áp crit (`*2` và `tlSDCM`).
7. Áp `percentXDame`.
8. Áp random variance cuối.
9. Áp x-chưởng nếu có.

### 6.8.3 Thứ tự damage `mob -> player`

1. `Mob.mobAttackPlayer(...)`
2. Base mob hit từ `MobPoint.getDameAttack()`:
   - `point.dame` hoặc `hpFull * pDame% + jitter theo level`.
3. Áp modifier mob-side:
   - charm giảm damage, `lvMob` rule (% HP mục tiêu), satellite defend, CMS, charm miễn damage.
4. Gọi `player.injured(null, dameMob, false, true)`.

Trong `Player.injured(..., isMobAttack=true)` thứ tự phòng thủ:

1. Lấy `tlGiap`, `tlNeDon` hiện tại.
2. (Một số nhánh PvP skill bypass vì `isMobAttack=true`).
3. Clamp:
   - `tlNeDon <= 90`,
   - `tlGiap <= 86`.
4. Roll né (`tlNeDon`), nếu né thì 0 damage.
5. Trừ theo giáp phần trăm (`tlGiap`).
6. Trừ theo `def` tuyệt đối qua `subDameInjureWithDeff` (`damage -= def`, sàn 1).
7. Áp giáp xen item-time (`isUseGiapXen` / `isUseGiapXen2`) cho nguồn mob hoặc một số skill.
8. Áp guard survival đặc thù (bùa bất tử/Halloween/map constraints...).
9. Trừ HP thực tế.

### 6.8.4 Bảng ưu tiên áp modifier theo trục chỉ số

- `HP/MP Max`:
  - Base (`hpg/mpg + add`) -> % stack (`tlHp/tlMp`) -> set/skin/fusion/itemTime/event/map -> clan bonus -> cap/guard map.
- `Damage nền (this.dame)`:
  - Base (`dameg + dameAdd`) -> `tlDame` -> set/skin/pet/fusion/itemTime/event/map -> clan bonus -> state đặc biệt (super/biến hình...).
- `Damage ra skill (getDameAttack)`:
  - `this.dame` đã rebuild -> skill%/intrinsic% -> crit/xDame -> random -> hậu xử lý đặc thù.
- `Damage nhận`:
  - né/giáp% -> def tuyệt đối -> item-time phòng thủ -> shield/guard state -> map special rules.

### 6.8.5 Điểm cần khóa khi tuning để không “đè lớp”

- Không trộn tuning giữa `setDame()` và `getDameAttack()` trong cùng đợt nếu chưa có baseline tách lớp.
- Với mob damage, ưu tiên giảm ở tầng `pDame/map rule` trước khi sửa `Player.injured`.
- Nếu chỉnh crit/crit damage, phải test lại cả 2 đường:
  - `playerAttackMob`,
  - `playerAttackPlayer` (vì cùng dùng `getDameAttack`, nhưng hậu xử lý khác nhau).

## 6.9 Pha C - Ma trận `exclusive_group` và `stack_rule`

Mục này chốt nhóm loại trừ/cộng dồn theo đúng hành vi code hiện tại.

### 6.9.1 Nhóm trạng thái loại trừ cứng (mutually exclusive)

- **FusionMode (`player.fusion.typeFusion`)**:
  - `NON_FUSION`, `LUONG_LONG_NHAT_THE`, `HOP_THE_PORATA`, `HOP_THE_PORATA2` là các mode độc quyền theo enum.
  - Tại một thời điểm chỉ có 1 mode hoạt động.
- **LoXDebuffTier (`itemTime.isUseLoX2/5/7/10/15`)**:
  - Trong `setHpMax`, `setMpMax`, `setDame` dùng chuỗi `if ... else if`, nên chỉ 1 mức LoX được áp.
- **TypeChibiBranch (`effectSkill.isChibi`)**:
  - Chibi có nhánh phụ thuộc `typeChibi`, trong đó loại `3` tạo hiệu ứng mạnh lên HP/point.
  - Nên xem như một branch độc quyền nội bộ của trạng thái Chibi.

### 6.9.2 Nhóm “ưu tiên phiên bản 2” (override by stronger tier)

- **BoHuyetTier**:
  - logic: `isUseBoHuyet` chỉ áp khi `!isUseBoHuyet2`, còn `isUseBoHuyet2` có nhánh riêng mạnh hơn.
  - đánh dấu: `stack_rule = prefer_v2`.
- **BoKhiTier**:
  - tương tự BoHuyet, `isUseBoKhi2` ưu tiên hơn `isUseBoKhi`.
- **CuongNoTier**:
  - `isUseCuongNo2` là bản nâng cấp của `isUseCuongNo` theo cùng pattern.
- **GiapXenTier**:
  - ở `Player.injured`, nếu `isUseGiapXen2` bật thì dùng nhánh giảm riêng mạnh hơn nhánh `isUseGiapXen`.

### 6.9.3 Nhóm đa kênh có thể cộng dồn (stackable channels)

- **MealChannel**:
  - `isEatMeal`, `isEatMeal2`, `isEatMeal3` là 3 kênh độc lập; mỗi kênh có `icon` riêng quyết định bonus.
  - Vì là cờ khác nhau, có thể cùng tồn tại và cộng dồn đa tầng.
- **EffectSkillChannel**:
  - `isMonkey`, `isSuper`, `isBienHinh`, `isDameBuff`, `isShielding`, ... là nhiều cờ song song.
  - Code hiện tại chưa có khóa tổng quát “chỉ 1 form biến hình”, nên có nguy cơ stack chéo.
- **CharmChannel**:
  - `Charms` lưu timer riêng cho từng loại (`tdManhMe`, `tdDaTrau`, `tdOaiHung`, `tdBatTu`, ...), nên có thể đồng thời active.

### 6.9.4 Nhóm slot item (exclusive theo slot, stack theo slot khác)

- **SkinSlot5**: `setCaiTrang()` bật đúng 1 tập cờ theo item cải trang slot 5.
- **BackSlot8**: `setDeoLung()` bật theo item đeo lưng slot 8.
- **PetSkinSlot7**: `setPet()` bật theo pet skin slot 7.
- **LinhThuSlot11**: `setLinhThu()` bật theo slot 11.
- **MountSlot9**: `setThuCuoi()` bật theo thú cưỡi slot 9.
- Kết luận:
  - trong *mỗi slot* là exclusive,
  - *giữa các slot* là stackable, vì cờ từ slot khác vẫn giữ.

### 6.9.5 Nhóm bị cap trong phòng thủ (stack nhưng có trần)

- Trong `Player.injured`:
  - `tlNeDon` bị cap `<= 90`.
  - `tlGiap` bị cap `<= 86`.
- Nhóm này có thể cộng dồn từ nhiều nguồn, nhưng output cuối bị trần cứng ở pipeline nhận damage.

### 6.9.6 Nhóm có “double-dip” cần theo dõi đặc biệt

- **DamageScalePath**:
  - tăng ở `setDame()` (dame nền) + tiếp tục nhân trong `getDameAttack()` (skill/intrinsic/crit/xDame).
  - nếu cùng source đi vào cả 2 tầng sẽ tạo double-dip.
- **HP/MPScalePath**:
  - tăng qua `setHpMax/setMpMax` rồi lại có nhánh trong effect/map/fusion.
  - cần tách rõ source nào được phép vào “nền” và source nào chỉ vào “combat-time”.

### 6.9.7 Ma trận phân loại nhanh cho tuning

- **Nhóm `exclusive_group` đề xuất**:
  - `fusion_mode`
  - `lox_debuff_tier`
  - `bohuyet_tier`
  - `bokhi_tier`
  - `cuongno_tier`
  - `giapxen_tier`
  - `skin_slot_5`
  - `back_slot_8`
  - `pet_slot_7`
  - `linhthu_slot_11`
  - `mount_slot_9`
- **Nhóm `stack_rule` đề xuất**:
  - `additive_percent`
  - `additive_flat`
  - `prefer_v2`
  - `capped_output`
  - `slot_exclusive_cross_slot_stackable`
  - `combat_time_multiplier`

### 6.9.8 Quy tắc áp dụng khi cân bằng (khuyến nghị kỹ thuật)

- Mọi source mới phải khai báo rõ:
  - `exclusive_group`,
  - `stack_rule`,
  - `cap_rule`,
  - `apply_layer` (`base_point` hoặc `combat_time`).
- Không cho source mới tác động đồng thời cả `setDame()` và `getDameAttack()` nếu không có chủ đích rõ ràng.
- Với nhóm `prefer_v2`, bắt buộc giữ điều kiện phủ định bản cũ để tránh cộng chồng ngoài ý muốn.

## 6.10 Pha D - Profile build và mô phỏng thực chiến (vòng 1)

Mục tiêu pha này: tạo baseline mô phỏng theo profile build để nhìn nhanh độ lệch giữa các tầng buff, trước khi đụng tuning code/data.

### 6.10.1 Định nghĩa profile mô phỏng

- **`no-buff`**:
  - Chỉ dùng stat nền từ tier (`power_limit`) + build thường, không bật chuỗi buff theo thời gian.
- **`map-buff`**:
  - Build phổ biến khi farm/map thường, có một phần buff thời vụ.
  - Hệ số mô phỏng:
    - `outgoingDamage x1.45`
    - `effectiveHp x1.35`
    - `incomingDamage x0.80`
- **`full-buff`**:
  - Build dồn buff tối đa trong cửa sổ ngắn.
  - Hệ số mô phỏng:
    - `outgoingDamage x2.60`
    - `effectiveHp x2.00`
    - `incomingDamage x0.45`

Lưu ý: đây là hệ số mô phỏng tổng hợp từ stack-rule đã bóc, dùng để so sánh tương quan. Không thay thế log telemetry thực tế.

### 6.10.2 Công thức mô phỏng sử dụng

- `TTK_player_to_mob = ceil(mobHp / outgoingDamage) * 2s`
- `TTK_mob_to_player = ceil(effectiveHp / incomingMobHit) * 2s`
- `OneShotRisk = (incomingMobHit >= effectiveHp)`

Trong đó:

- `incomingMobHit ~= mobHp * percent_dame / 100 * incomingDamageMultiplier`
- `outgoingDamage ~= tierDamage * outgoingDamageMultiplier`
- `effectiveHp ~= tierHp * effectiveHpMultiplier`

### 6.10.3 Kết quả tóm tắt theo band tier

#### `no-buff` (baseline hiện tại)

- **Early (T1-T4)**:
  - `lvl20_med`: p2m `946.0s`, m2p `2.5s`, oneshot `3/4`
  - `lvl22_med`: p2m `1779.0s`, m2p `2.0s`, oneshot `4/4`
- **Mid (T5-T9)**:
  - `lvl20_med`: p2m `556.4s`, m2p `4.0s`, oneshot `0/5`
  - `lvl22_med`: p2m `1046.4s`, m2p `2.0s`, oneshot `5/5`
- **Late (T10-T14)**:
  - `lvl20_med`: p2m `429.6s`, m2p `4.8s`, oneshot `0/5`
  - `lvl22_med`: p2m `806.8s`, m2p `3.2s`, oneshot `2/5`

#### `map-buff`

- **Early**:
  - `lvl20_med`: p2m `653.0s`, m2p `4.0s`, oneshot `0/4`
  - `lvl22_med`: p2m `1227.5s`, m2p `2.0s`, oneshot `4/4`
- **Mid**:
  - `lvl20_med`: p2m `384.0s`, m2p `6.0s`, oneshot `0/5`
  - `lvl22_med`: p2m `722.4s`, m2p `4.0s`, oneshot `0/5`
- **Late**:
  - `lvl20_med`: p2m `296.4s`, m2p `8.0s`, oneshot `0/5`
  - `lvl22_med`: p2m `556.8s`, m2p `4.0s`, oneshot `0/5`

#### `full-buff`

- **Early**:
  - `lvl20_med`: p2m `364.5s`, m2p `9.0s`, oneshot `0/4`
  - `lvl22_med`: p2m `685.0s`, m2p `5.0s`, oneshot `0/4`
- **Mid**:
  - `lvl20_med`: p2m `215.2s`, m2p `14.4s`, oneshot `0/5`
  - `lvl22_med`: p2m `403.2s`, m2p `8.0s`, oneshot `0/5`
- **Late**:
  - `lvl20_med`: p2m `166.0s`, m2p `18.8s`, oneshot `0/5`
  - `lvl22_med`: p2m `310.8s`, m2p `10.4s`, oneshot `0/5`

### 6.10.4 Phát hiện quan trọng từ mô phỏng

- Kể cả profile `full-buff`, kịch bản `lvl100_peak` vẫn cho `m2p ~2s` ở hầu hết band:
  - cho thấy vấn đề cấu trúc nằm ở cụm HP cao + công thức damage quái.
- `map-buff` giúp kéo tốt cụm `lvl20/lvl22` ở mid-late, nhưng chưa xử lý triệt để cụm peak.
- Hướng tuning hợp lý:
  - giữ stack profile player như hiện tại trước,
  - ưu tiên chỉnh cụm mob high-HP/high-peak ở template/map-rule.

### 6.10.5 Đầu vào cho Pha E (cân bằng theo vùng gameplay)

- Dùng 3 profile trên làm trục chuẩn khi chia map:
  - map thường progression nên cân theo `map-buff`,
  - map event/boss cho phép gần `full-buff`,
  - mọi map không-event phải tránh `oneshot` cao ở `no-buff`.

## 6.11 Pha E - Chia cụm map và target envelope theo gameplay

Pha này chuyển từ “phân tích kỹ thuật” sang “mục tiêu cân bằng vận hành”: mỗi cụm map có ngưỡng TTK khác nhau, không dùng 1 chuẩn chung cho toàn server.

### 6.11.1 Nhóm map theo hành vi gameplay (dựa trên `MapService`)

- **Progression thường (core farm/quest)**:
  - map thường `type=0`, không thuộc phó bản/event đặc biệt.
- **Offline/training zone**:
  - map `type=1` (`home`, `thần điện`, `tháp`, `kaio`, `time room`...) chủ yếu train/utility.
- **Phó bản chuẩn** (`isMapPhoBan`):
  - Doanh trại (`53-62`),
  - Bản đồ kho báu (`135-138`),
  - Con đường rắn độc (`141-144`),
  - Khí gas hủy diệt (`147-152`, trừ `150`).
- **PvP/event combat vùng riêng**:
  - Black Ball War (`85-91`),
  - Ma Bu (`114-120`),
  - Mabu 2H (`127-128`),
  - các map sự kiện theo mùa (Halloween, SkyPear, Hủy Diệt...).
- **Peak/special high-pressure**:
  - cụm map có mob HP rất cao (`lvl100+`, prison/special, peak encounters).

### 6.11.2 Target envelope đề xuất theo cụm

- **Progression thường** (chuẩn vận hành chính):
  - profile tham chiếu: `map-buff`.
  - mục tiêu:
    - `TTK_player_to_mob`: `10s -> 45s` (mob thường),
    - `TTK_mob_to_player`: `8s -> 20s`,
    - `OneShotRisk`: gần `0%` với non-boss/non-event.
- **Offline/training**:
  - profile tham chiếu: `no-buff`.
  - mục tiêu:
    - ưu tiên ổn định và học skill, không tạo áp lực chết nhanh,
    - cho phép `TTK_player_to_mob` dài hơn progression.
- **Phó bản chuẩn**:
  - profile tham chiếu: giữa `map-buff` và `full-buff`.
  - mục tiêu:
    - `TTK_player_to_mob`: `20s -> 90s` tùy tầng,
    - `TTK_mob_to_player`: `6s -> 15s`,
    - `OneShotRisk`: thấp ở wave thường, tăng nhẹ ở mini-boss.
- **PvP/event combat**:
  - profile tham chiếu: `full-buff`.
  - mục tiêu:
    - cho phép burst cao hơn map thường,
    - nhưng vẫn tránh kill tức thì liên tục do 1 nguồn stack duy nhất.
- **Peak/special high-pressure**:
  - chấp nhận độ khó cao hơn cụm khác,
  - vẫn cần ngưỡng an toàn:
    - tránh trạng thái “mọi profile đều `m2p ~2s`” kéo dài ở non-final encounters.

### 6.11.3 Ưu tiên tuning theo cụm (không đụng global sớm)

1. **Ưu tiên 1**: chỉnh cụm `peak/special` và map HP cao trước (giảm áp lực `m2p` cực đoan).
2. **Ưu tiên 2**: chỉnh cụm progression thường để đưa `OneShotRisk` về gần 0.
3. **Ưu tiên 3**: tinh chỉnh phó bản để giữ độ khó nhưng tránh trừng phạt quá dốc.
4. **Sau cùng** mới cân nhắc sửa công thức global (`Player.injured`, `Mob.injured`, `NPoint.getDameAttack`).

### 6.11.4 Guardrail khi triển khai theo vùng

- Mỗi lần tuning chỉ chọn 1 cụm map chính để canary.
- Với mỗi cụm phải log tối thiểu:
  - death rate theo phút,
  - median combat duration,
  - potion/regen consumption,
  - tỷ lệ chết trong 3s đầu combat.
- Nếu 2 chỉ số vượt ngưỡng cùng lúc (ví dụ death rate tăng và combat duration giảm sâu) thì rollback cụm ngay.

### 6.11.5 Kết luận Pha E

- Cân bằng theo vùng gameplay là bắt buộc cho codebase này vì hệ stack modifier nhiều lớp.
- Mục tiêu ngắn hạn:
  - kéo cụm progression về envelope an toàn,
  - giữ event/phó bản có độ thử thách,
  - cô lập peak-map để chỉnh chuyên biệt thay vì làm méo toàn bộ game loop.

## 6.12 Pha F - Actionable playbook (batch vận hành)

Mục này biến toàn bộ phân tích thành lộ trình chạy thực tế theo đợt, ưu tiên thay đổi dữ liệu trước, giảm rủi ro chạm công thức global.

### 6.12.1 Nguyên tắc triển khai

- Mỗi batch chỉ can thiệp **1 lớp chính**:
  - Batch 1: map/template data,
  - Batch 2: modifier stack theo cụm,
  - Batch 3: công thức global (nếu bắt buộc).
- Mỗi batch có 3 trạng thái:
  - `dry-run` (mô phỏng số liệu),
  - `canary` (1 cụm map),
  - `rollout` (mở rộng).
- Chỉ chuyển batch tiếp theo khi batch trước đạt pass criteria.

### 6.12.2 Batch 1 - Data-first (template/map distribution)

- **Mục tiêu**:
  - giảm áp lực `m2p` ở cụm high-HP mà chưa đụng `Player.injured`/`NPoint.getDameAttack`.
- **Phạm vi**:
  - `mob_template.percent_dame`, `mob_template.percent_tiem_nang`,
  - `map_template.mobs[*][hp]` theo cụm map ưu tiên.
- **Canary đề xuất**:
  - cụm `peak/special` trước (prison/high-HP),
  - sau đó cụm progression có one-shot cao.
- **SQL mẫu (tham khảo, phải kiểm tra trước khi chạy thật)**:
  - hạ `percent_dame` cho nhóm HP quá cao:
    - `UPDATE mob_template SET percent_dame = percent_dame - 1 WHERE hp >= 30000000 AND percent_dame >= 6;`
  - tách riêng nhóm boss đặc thù không thuộc chuẩn chung bằng whitelist id.
- **Pass criteria**:
  - giảm `OneShotRisk` ở canary >= 50%,
  - `TTK_player_to_mob` không tăng quá 20% ngoài target envelope cụm.
- **Rollback**:
  - backup bảng trước batch (`mob_template`, `map_template`),
  - rollback theo snapshot nếu death spike kéo dài > 30 phút.

### 6.12.3 Batch 2 - Stack normalization theo profile

- **Mục tiêu**:
  - giảm hiện tượng “double-dip” và stack bùng nổ từ nhiều kênh buff cùng trục.
- **Phạm vi**:
  - chuẩn hóa `exclusive_group` + `stack_rule` đã nêu ở mục 6.9.
  - ưu tiên nhóm `prefer_v2` và nhóm multi-channel dễ chồng (`Meal`, `EffectSkill`, `Charm`).
- **Chiến lược**:
  - thêm guard để source mới không tác động cả `setDame()` và `getDameAttack()` cùng lúc.
  - khóa soft-cap theo profile:
    - `map-buff` cho progression,
    - `full-buff` chỉ mở ở event/phó bản.
- **Pass criteria**:
  - variance damage giữa `map-buff` và `full-buff` giảm về vùng kiểm soát trên map thường,
  - không xuất hiện nguồn đơn lẻ tạo burst vượt ngưỡng thiết kế.
- **Rollback**:
  - toggle feature flag cho từng nhóm stack,
  - tắt theo nhóm thay vì rollback toàn bộ patch.

### 6.12.4 Batch 3 - Formula-first (chỉ khi Batch 1-2 chưa đủ)

- **Mục tiêu**:
  - xử lý tận gốc coupling bất lợi giữa `mobHp` và `mob outgoing damage`.
- **Phạm vi rủi ro cao**:
  - `MobPoint.getDameAttack`,
  - `Mob.injured`,
  - `Player.injured`,
  - `NPoint.getDameAttack`.
- **Yêu cầu bắt buộc trước khi chạy**:
  - feature flag theo cụm map,
  - canary tối thiểu 1 ngày vận hành,
  - regression pack boss/event đầy đủ.
- **Pass criteria**:
  - đạt target envelope của cụm mà không tăng regression bug combat.
- **Rollback**:
  - kill switch công thức mới về legacy branch,
  - giữ nguyên data tuning từ batch trước nếu vẫn ổn.

### 6.12.5 Checklist vận hành theo batch (runbook ngắn)

- Trước batch:
  - chụp snapshot DB,
  - freeze config thay đổi ngoài phạm vi batch,
  - xác nhận canary map list.
- Trong canary:
  - theo dõi mỗi 10 phút:
    - death rate,
    - median combat duration,
    - one-shot count,
    - potion consumption.
- Sau canary:
  - so sánh với baseline 24h trước,
  - quyết định `rollout / hold / rollback`.

### 6.12.6 Mẫu bảng theo dõi quyết định

- `batch_id`
- `map_cluster`
- `profile_reference` (`no-buff`, `map-buff`, `full-buff`)
- `before_metrics` / `after_metrics`
- `pass_fail`
- `rollback_needed`
- `notes`

### 6.12.7 Kết thúc Pha F

- Với playbook này, đội vận hành có thể chạy cân bằng theo vòng lặp nhỏ, giảm tối đa rủi ro “đập công thức global một lần”.
- Chu kỳ đề xuất:
  - 1 tuần / vòng tuning,
  - mỗi vòng tối đa 1 batch lớn + 1 hotfix nhỏ.

---

## 6.13) Edge stat sources và simulation flags (bổ sung vòng 2)

Mục này chốt các nguồn cộng chỉ số chưa nằm ở nhóm "core obvious" nhưng vẫn đủ mạnh để làm lệch kết quả cân bằng nếu bỏ qua trong pipeline phân tích.

### 6.13.1 Nhóm nguồn cộng thêm cần tách riêng

- **Badge/Huy hiệu (exclusive)**:
  - Option của badge được bơm qua `BagesTemplate.sendListItemOption(player)` rồi đi qua `addOption(...)` trong `NPoint.calPoint`.
  - Đặc tính vận hành hiện tại: mỗi thời điểm chỉ 1 badge `isUse = true`, nên đây là kênh `exclusive_group` độc lập, không phải stack tự do.
- **Clan bonus có trần theo cấp clan**:
  - Các kênh `hpbang`, `mpbang`, `damebang`, `critbang` được cộng vào stat sau khi clamp theo `clan.level`.
  - Nếu mô phỏng chỉ lấy snapshot stat cuối mà không giữ logic clamp theo level, kết quả dự báo sẽ lệch.
- **Inheritance pet/master + VIP path**:
  - Một phần buff của pet phụ thuộc trạng thái của master (buff đồ ăn, charm, VIP, bonus riêng pet/master).
  - Cần coi đây là kênh "cross-entity source" thay vì nguồn local của actor đang tính damage.
- **Satellite state (short window)**:
  - `isDefend`, `isIntelligent` là state ngắn hạn theo thời gian, không phải buff tĩnh.
  - Tác động chính: giảm sát thương nhận trong combat window và tăng hiệu quả gain trong loop farm.
- **Danh hiệu/Chân mệnh (visual + option-carrying item)**:
  - Phần effect title chủ yếu là visual, nhưng item mang type tương ứng vẫn có thể chứa option chiến đấu.
  - Cần tách "visual-only effect" khỏi "option-bearing equipment" để tránh đếm trùng.
- **Companion side-channel (lính thuê/linh danh thuê)**:
  - Không cộng trực tiếp vào `nPoint` player chính theo cùng trục chuẩn.
  - Tuy nhiên làm thay đổi DPS/EHP thực chiến của party => cần đưa vào kịch bản mô phỏng cấp hệ thống.

### 6.13.2 Cập nhật StatSourceMatrix (delta)

- `BadgeOptionChannel`:
  - type: `exclusive_channel`
  - apply_stage: `calPoint -> addOption`
  - note: "chỉ 1 badge active, không stack ngang badge"
- `ClanCapChannel`:
  - type: `capped_additive`
  - cap_formula: phụ thuộc `clan.level`
  - note: "phải mô phỏng cùng clan-level distribution"
- `PetMasterInheritanceChannel`:
  - type: `cross_entity_conditional`
  - dependency: state master/pet theo role
  - note: "không thể giả lập độc lập 1 actor"
- `SatelliteTemporalChannel`:
  - type: `temporal_state`
  - duration: short burst window
  - note: "cần mô phỏng duty-cycle thay vì always-on"
- `CompanionSystemChannel`:
  - type: `systemic_party_modifier`
  - scope: encounter-level
  - note: "không gộp vào chỉ số base player"

### 6.13.3 Cờ mô phỏng bắt buộc thêm vào profile

Để metric `TTK`, `OneShotRisk`, `RewardEfficiency` không bị bias, bổ sung các flag sau vào profile `no-buff/map-buff/full-buff`:

- `badge_on` + `badge_id_effect`
- `clan_level` + `clan_bonus_vector`
- `pet_master_inheritance_on`
- `satellite_duty_cycle` (tỉ lệ uptime của `isDefend/isIntelligent`)
- `companion_mode` (`none`, `mercenary`, `linh_danh_thue`)

### 6.13.4 Rủi ro nếu không đưa nhóm edge vào tuning

- đánh giá thiếu burst/mitigation ở các map đông người và cụm event,
- dự báo sai hiệu quả canary do chênh lệch phân bố clan-level giữa giờ thấp điểm/cao điểm,
- false-positive khi kết luận "formula lỗi" trong khi nguyên nhân nằm ở stacking channel ngoài core.

### 6.13.5 Kết luận bổ sung vòng 2

- Bộ nguồn tăng chỉ số cho combat đã gần đầy đủ ở mức vận hành.
- Các nhóm edge ở mục 6.13 không phải lúc nào cũng chi phối, nhưng là nguồn lệch đáng kể trong các ca outlier và phải được đưa vào runbook mô phỏng.

---

## 7) Checklist regression trước khi commit cân bằng

- Test tách riêng map thường, map event, map phó bản.
- Re-test đầy đủ các điểm chạm:
  - `SkillService.playerAttackMob`
  - `Mob.injured`
  - `Mob.mobAttackPlayer`
  - `Player.injured`.
- Test sanity cho boss có override `injured`.
- Xác nhận progression reward (`SM/TN`) vẫn tăng hợp lý và không mở đường farm lỗi.
- Xác nhận không xuất hiện spike one-shot ngoài map event.
