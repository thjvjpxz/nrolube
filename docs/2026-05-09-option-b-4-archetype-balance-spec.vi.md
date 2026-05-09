# Spec: Option B - 3 Hệ + Build Archetype Theo Hệ (Data-first)

Ngày 2026-05-09, spec này thay thế cách hiểu cũ "4 archetype toàn cục".
Tên file được giữ để trace lịch sử, nhưng thiết kế chốt là:

```text
3 hệ trong game -> skill identity của từng hệ -> build path trong từng hệ -> chuẩn hóa data item/drop
```

Không nên bắt đầu từ 4 archetype chung cho toàn server, vì mỗi hệ có skill scale khác nhau.
Ví dụ Xayda có thể build theo máu, phản sát thương, dame, né đòn; Namek có trục KI/hồi phục; Trái Đất có trục khống chế/burst/charge.
Ngoài stat, phải tính cả tempo combat: hồi chiêu, tầm đánh, channel/downtime, chi phí HP/KI/MP và số lần skill có thể tạo hit trong một khoảng thời gian.

## 1) Mục Tiêu

- Tăng độ sâu build, người chơi có lý do thử nhiều set trong cùng một hệ.
- PvE/PvP đa dạng hơn, tăng vòng lặp tối ưu build.
- Giảm hiện tượng một build thống trị toàn server.
- Ưu tiên chuẩn hóa data trước: `tab_shop` -> `mob_reward` -> runtime guard nếu cần.
- Hạn chế chạm công thức combat global trong pha đầu.

## 2) Kết Luận Thiết Kế

## 2.1 3 hệ là lớp thiết kế chính

Build phải được đọc theo hệ:

- Trái Đất: khống chế, burst theo cửa sổ, dịch chuyển/tạo vị trí, charge AoE.
- Namek: hồi phục, hỗ trợ, KI/MP scaling, triệu hồi, khống chế.
- Xayda: máu lớn, biến hình, áp lực cận chiến, phản sát thương, tự sát, hồi năng lượng.

Archetype chỉ là lớp phụ bên trong từng hệ. Cùng là "tank" nhưng tank Namek là sustain/support, tank Xayda có thể là HP/reflect, còn Trái Đất phòng thủ chủ yếu là shield/positioning.

## 2.2 Không cấm một cặp stat nếu cặp đó là identity của hệ

Rule cũ "dame + HP là all-in-one" bị sai với Xayda, vì skill `Tự phát nổ` scale theo HP max, `Biến khỉ` tăng HP và dame, `Huýt sáo` tăng HP. Tương tự, Namek cần KI/MP cao cho `Makankosappo`.

Rule mới là:

- Được ghép stat nếu nó phục vụ cùng một build path của hệ.
- Không được ghép nhiều build path mạnh trên cùng item hoặc cùng bộ slot.
- Item shared/all-class phải dùng band thấp hơn item khóa hệ.

## 2.3 Tempo là một trục balance riêng

Một skill có damage thấp nhưng hồi chiêu rất nhanh vẫn có power budget lớn, vì nó nhân giá trị của:

- crit,
- hút HP/MP,
- stack cộng dồn theo hit,
- hiệu ứng giảm hồi chiêu,
- tốc đánh,
- uptime khống chế hoặc áp lực liên tục.

Vì vậy không được cân bằng item chỉ bằng `damage%` trên skill. Cần đọc theo:

```text
effective tempo = damage window + cooldown thực tế + chi phí tài nguyên + rủi ro khi cast/channel
```

Ví dụ quan trọng: `Liên hoàn` Namek có damage 160-190% nhưng cooldown 350-330ms. Runtime check còn dùng `coolDown - 50`, nên lv7 có thể tạo áp lực gần 280ms/lần trước khi tính `Tốc Đánh +#%`. Đây là lợi thế tempo rất lớn, không thể cấp thêm full crit/lifesteal/tank như một skill cooldown dài.

## 3) Căn Cứ Từ Skill Runtime

Nguồn tham chiếu chính:

- `sql/tomahoc_db.sql`: bảng `skill_template`, `nclass_id` (`0 = Trái Đất`, `1 = Namek`, `2 = Xayda`).
- `src/skill/Skill.java`: skill id.
- `src/services/SkillService.java`: hành vi khi dùng skill.
- `src/player/NPoint.java`: scale damage, HP, MP, crit, option stack.
- `src/utils/SkillUtil.java`: thời gian, phần trăm buff, range.

## 3.1 Trái Đất

| Skill | Vai trò gameplay | Hệ quả build |
| --- | --- | --- |
| `Dragon` | Cận chiến cơ bản, cooldown 500ms | Build dame/crit có nền DPS ổn định; hưởng mạnh từ tốc đánh và stack theo hit. |
| `Kamejoko`, `Super Kamejoko` | Chưởng/burst tầm xa; Kamejoko 2-5s, Super Kamejoko 80-170s | Cần dame, crit damage, pierce/accuracy vừa phải; Kamejoko là burst chu kỳ vừa, Super Kame là peak cooldown dài. |
| `Thái Dương Hạ San` | AoE stun/blind, cooldown 30-60s, thời gian theo level | Mở cửa sổ burst; item nên hỗ trợ timing/control, không biến thành tank all-in-one. |
| `Kaioken` | Cận chiến 500ms, tiêu hao HP max | DPS tempo rất cao nhưng có self-cost; cần dame và một mức HP/hồi phục đủ sống, không nên có phòng thủ cực cao. |
| `Quả Cầu Kênh Khi` | Charge/throw, AoE, cooldown 300-360s, scale theo tình huống và dame | Build charge AoE được peak cao hơn vì downtime dài và có rủi ro channel. |
| `Khiên năng lượng` | Giảm/chặn damage, cooldown 75-105s | Hỗ trợ duel/positioning, không phải lý do để stack thêm HP/def/reflect cao. |
| `Dịch chuyển tức thời` | Áp sát, stun ngắn, cooldown 14-20s, tạo crit window | Phù hợp burst/control, cần dame và timing; không phải sustained DPS. |
| `Thôi miên` | Sleep/control, cooldown 30-42s | Tăng giá trị build control; dùng để mở cửa sổ đánh, không nên kèm full burst/tank. |
| `Phân thân`, `Biến Hình`, `Tàng hình` | Clone, buff HP/dame nhẹ, mở combat bằng crit/stun | Tăng độ sâu duel và burst window. |

Identity: Trái Đất nên là hệ có cửa sổ khống chế và burst rõ, mạnh khi căn đúng timing, yếu hơn nếu bị kéo dài combat hoặc bị kháng control.

## 3.2 Namek

| Skill | Vai trò gameplay | Hệ quả build |
| --- | --- | --- |
| `Demon` | Cận chiến cơ bản, cooldown 400ms | Nền đánh thường nhanh hơn Dragon/Galick; hưởng mạnh từ tốc đánh, hút HP và stack theo hit. |
| `Masenko` | Chưởng nhanh, cooldown 700-800ms | Đây là lợi thế tempo của Namek. Set Nail 4/5 món còn giảm hồi chiêu 20-50%, nên cần tách thành đường spam chưởng riêng. |
| `Trị thương` | Heal/revive, cooldown 30-44s, hồi HP/MP theo phần trăm skill | Tạo build healer/support với HP, KI/MP, regen, damage thấp. |
| `Makankosappo` | Charge/laze, cooldown 300-360s, dùng hết MP, damage dựa trên `mpMax` | Namek có build KI/MP burst riêng; peak được cao vì downtime dài và rủi ro channel. |
| `Đẻ trứng` | Gọi `MobMe`, cooldown 360-540s, scale theo stat người chơi | Mở build summon/tempo dài hạn, cần HP/KI và utility ổn định. |
| `Liên hoàn` | Cận chiến cực nhanh, cooldown 330-350ms | Sustained DPS/áp lực PvP rất mạnh; không được cấp full lifesteal/crit/tank vì số hit cao nhân giá trị mọi on-hit stat. |
| `Biến Sôcôla` | Khống chế dài, cooldown 24-30s | Tăng vai trò control/support trong PvP. |
| `Khiên năng lượng` | Sống sót trong combat | Phù hợp support/laze channel, nhưng không được cộng thêm burst cao vô tội vạ. |
| `Ma phong ba` | Special control/burst | Nên là đỉnh của build control, không phải stat all-in-one. |
| `Phân thân`, `Biến Hình`, `Tàng hình` | Clone, buff HP/dame nhẹ, stealth opener | Tăng biến thể support/duel. |

Identity: Namek nên là hệ có sustain, hỗ trợ đội hình, KI scaling và control. Namek có thể dame cao theo `Makankosappo` hoặc `Liên hoàn`, nhưng phải trả giá bằng khả năng sống sót/hồi phục.

## 3.3 Xayda

| Skill | Vai trò gameplay | Hệ quả build |
| --- | --- | --- |
| `Galick` | Cận chiến cơ bản, cooldown 500ms, damage cao hơn Dragon/Demon | Nền dame Xayda mạnh hơn nhưng không nhanh bằng Demon/Liên hoàn. |
| `Antomic` | Chưởng 1-2.2s, damage 110-290% | Là sustained caster chậm hơn Masenko nhưng hit nặng hơn; không đồng thời có reflect/tank cao. |
| `Tái tạo năng lượng` | Charge hồi HP/MP, cooldown 25-55s, hồi theo % mỗi tick | Mở build sustain/tempo, đặc biệt khi có HP lớn; mạnh trong combat kéo dài. |
| `Biến khỉ` | Cooldown 300-360s, tăng HP 40-100%, tăng dame 4-10%, crit trong code được set rất cao | Tạo build bruiser/transform theo uptime buff; cần kiểm soát crit item vì skill đã cho cửa sổ crit mạnh. |
| `Tự phát nổ` | Cooldown 120s, sát thương dựa trên HP max, caster chết | Tạo build HP bomber riêng. HP là damage stat của build này, nhưng có chi phí chết nên được peak cao theo điều kiện. |
| `Huýt sáo` | Cooldown 180-210s, tăng HP cho đồng minh không phải Namek, thêm crit; với Namek cùng phe có logic trừ HP | Tạo build support/offensive aura riêng cho Xayda, power nằm ở team tempo hơn là DPS cá nhân. |
| `Trói` | Hold/control, cooldown 15-45s | Mở build control sustain; cooldown tăng theo level nên phải cân bằng cùng thời gian trói dài hơn. |
| `Khiên năng lượng` | Bảo vệ cửa sổ ngắn | Hỗ trợ tank/bomber/control, không nên cho phép stack vô hạn. |
| `Cađíc liên hoàn chưởng` | Special burst | Đỉnh của build dame/burst Xayda. |
| `Phân thân`, `Biến Hình`, `Tàng hình` | Clone, transform nhẹ, stealth opener | Tăng lựa chọn duel và mở combat. |

Identity: Xayda nên có nhiều hướng rõ: máu, phản sát thương, dame, né đòn, control/sustain. Đây là hệ được phép có HP cao, nhưng HP cao không được mặc định đi kèm crit/dame/reflect/né đòn đều cao.

## 3.4 Bảng tempo tóm tắt

Các con số dưới đây lấy từ `skill_template` và runtime hiện tại. Đây không phải công thức combat mới, chỉ là cách đọc power budget khi chuẩn hóa data.

| Hệ | Skill | Damage/scale chính | Cooldown gốc | Tempo đọc balance |
| --- | --- | --- | --- | --- |
| Trái Đất | `Dragon` | 100-160% dame | 500ms | Sustained melee trung bình, hưởng mạnh từ tốc đánh/on-hit. |
| Trái Đất | `Kamejoko` | 150-450% dame | 2-5s | Burst tầm xa chu kỳ vừa, không phải spam skill. |
| Trái Đất | `Kaioken` | 160-220% dame + self HP cost | 500ms | DPS cao, phải trả bằng HP/MP; item phòng thủ phải bị giới hạn. |
| Trái Đất | `TDHS` | Stun/blind AoE | 30-60s | Control window, power nằm ở mở burst chứ không ở damage. |
| Trái Đất | `QCKK` | AoE/charge, scale theo cụm mục tiêu + dame | 300-360s | Peak rất dài chu kỳ, được phép mạnh nhưng cần rủi ro channel. |
| Namek | `Demon` | 95-155% dame | 400ms | Sustained melee nhanh hơn đấm cơ bản của hệ khác. |
| Namek | `Masenko` | 100-160% dame | 700-800ms | Spam chưởng nhanh; Set Nail có thể kéo cooldown xuống rất thấp. |
| Namek | `Liên hoàn` | 160-190% dame | 330-350ms | Tempo cực cao, on-hit stat và lifesteal phải bị siết. |
| Namek | `Makankosappo` | 70-130% của `mpMax`, dùng hết MP | 300-360s | Long-cooldown MP burst, peak cao nhưng downtime rõ. |
| Namek | `Trị thương` | 50-80% heal | 30-44s | Sustain/support, không nên kèm burst cao. |
| Xayda | `Galick` | 110-170% dame | 500ms | Melee nền mạnh, tempo trung bình. |
| Xayda | `Antomic` | 110-290% dame | 1-2.2s | Hit nặng hơn Masenko nhưng chậm hơn, caster damage. |
| Xayda | `Tái tạo năng lượng` | Hồi 4-10% HP/MP mỗi tick | 25-55s | Sustain tempo, mạnh theo HP/MP lớn và combat dài. |
| Xayda | `Biến khỉ` | +40-100% HP, +4-10% dame, crit window | 300-360s | Uptime transform dài chu kỳ, siết crit item. |
| Xayda | `Tự phát nổ` | HP max, caster chết | 120s | HP burst có chi phí cực lớn, không được kèm tank/reflect/dodge cao. |
| Xayda | `Trói` | Control theo level | 15-45s | Control sustain, phải đọc cùng duration trói. |

## 3.5 Runtime tempo modifiers cần tính vào data

- `SkillService.canUseSkillWithCooldown` check `coolDown - 50`, nên các skill cooldown ngắn được lợi tương đối lớn hơn. Ví dụ `Liên hoàn` 330ms thành khoảng 280ms trước khi tính giảm hồi chiêu khác.
- `NPoint.speedat` từ option `190` và một số set làm giảm cooldown cho nhóm skill đánh/chưởng nhanh (`Dragon`, `Demon`, `Galick`, `Kaioken`, `Liên hoàn`, `Kamejoko`, `Antomic`, `Masenko`).
- Set Nail (`237/238/239/240`) giảm hồi chiêu Masenko: 4 món khoảng 20%, 5 món khoảng 50% theo runtime.
- Option `157` cộng dồn damage khi chỉ dùng `Liên hoàn`; vì cooldown rất ngắn, stack này lên nhanh hơn các đường khác.
- Option `156` cộng dồn damage khi chỉ dùng đấm, `158` cộng dồn khi dùng `Kaioken`; các option này phải đọc cùng cooldown skill.
- Option `183` giảm hồi chiêu Khiên, option `123` giảm hồi chiêu Trói; nếu đi cùng HP/def/dodge cao sẽ tạo sustain/control uptime quá lớn.
- Option `163` tăng sát thương Laze, set Namek mới có thể cộng thêm `laze` và `speedat`; đây là cầu nối giữa `Makankosappo` burst và tempo chưởng/cận chiến, cần giới hạn để không gom hai path.

## 3.6 GitNexus blast-radius findings phải giữ trong spec

GitNexus xác nhận các điểm dưới đây là surface vận hành rủi ro cao. Phase data-first không được ngầm sửa công thức global ở các điểm này nếu chưa có flag, canary và rollback rõ.

| Surface | GitNexus impact | Ý nghĩa với spec |
| --- | --- | --- |
| `Service.point` | `CRITICAL`, 228 symbols, 21 process, 14 module | Đây là điểm rebuild stat trung tâm, bị gọi từ skill, item, effect, pet, bot, boss và player update. Mọi thay đổi phải có feature flag, kill switch, canary theo cụm user và rollback script. |
| `NPoint.setPointWhenWearClothes` | `CRITICAL`, 74 symbols, 20 process, 11 module | Không chỉnh công thức mặc đồ trong phase data nếu chỉ cần sửa option/item. Nếu bắt buộc chỉnh, phải coi là migration runtime riêng. |
| `SkillService.canUseSkillWithCooldown` | `CRITICAL`, 163 symbols, lan sang pet/boss flow | Tempo không chỉ là player. Test phải có pet, boss, bot, clone/summon để tránh buff/nerf PvE ngoài ý muốn. |
| `ItemTime.update`, `EffectSkin.update` | `CRITICAL`, cùng gọi về `Service.point` theo timer | Cần đo tần suất rebuild stat/phút/người chơi và guard chống rebuild storm khi nhiều effect hết hạn cùng lúc. |
| `Mob.getItemMobReward` | Có side-effect vào `TaskService`, `BadgesTaskService`, `PlayerEvent` | Cân bằng drop phải kiểm tra tốc độ nhiệm vụ, điểm sự kiện và badge progression, không chỉ kiểm tra option item rơi ra. |
| `NDVSqlFetcher.loadPlayer` | `CRITICAL`, ảnh hưởng login/load, top, minigame, SuperRank | Tuning data không được làm sai parse item, snapshot stat, leaderboard/minigame/super-rank flow. |
| `ItemService` | `CRITICAL`, 581 symbols, 32 process, 14 module | Nguồn tạo item tập trung nhưng có nhiều factory hardcode: set kích hoạt, hủy diệt, thiên sứ, rương, reward, giftcode, combine. Phải audit nguồn sinh item, không chỉ audit bảng shop/drop. |
| `InventoryService` | `CRITICAL`, 496 symbols, 28 process, 13 module | Mặc/tháo đồ chạm slot, gender, `power_require`, pet body và gọi `Service.point`. Data tuning phải test equip gate, pet gear và item movement. |
| `ShopService` | `CRITICAL`, 72 symbols, 16 process, 6 module | Shop không chỉ là giá bán; còn gold/gem/ruby/coupon, shop đặc biệt dùng `item_spec`, VND/cash, điểm event và danh hiệu. |
| `CombineService` | `CRITICAL`, 211 symbols, 5 process, 3 module | Craft/upgrade/tẩy/chế tạo có thể tạo hoặc biến đổi option mạnh ngoài shop/drop. Phải được đưa vào item-source audit. |
| `TaskService` | `CRITICAL`, 365 symbols | Task, side task, clan task, boss/NPC và reward đan vào nhau. Tuning reward không được làm sai tốc độ nhiệm vụ hoặc điều kiện mở nội dung. |
| `PlayerService` | `CRITICAL`, 386 symbols, 12 process, 10 module | Player state dùng trong combat, PVP, pet, boss, map và TNSM. Không sửa helper player global trong phase data. |
| `Player.injured` | `CRITICAL`, 156 symbols | Damage taken, dodge/reflect/immortal charm và boss/mob attack đều đi qua đây. Chỉ audit, không sửa công thức trong phase đầu. |
| `Mob.injured` | `HIGH`, 125 symbols, 2 process, 4 module | PvE clear speed, phản sát thương mob và boss/bigboss flow bị ảnh hưởng trực tiếp nếu đổi damage-to-mob. |
| `AchievementService` | `CRITICAL`, 335 symbols, 4 process | Achievement đi qua combat, mob, shop, task, clan và PVP. Cân bằng build không được làm lệch mốc thành tựu hoặc reward thành tựu. |
| `BadgesTaskService` | `CRITICAL`, 415 symbols | Badge progression được gọi từ task, player, mob, shop danh hiệu, UseItem, summon dragon, combine và boss. Drop/reward tuning phải đo badge riêng. |
| `ClanService` | `CRITICAL`, 68 symbols, 7 process, 4 module | Clan task, dungeon clan, member state và load player có nhánh riêng. Reward/progression clan không được bị kéo theo data item mới. |
| `ChangeMapService` | `CRITICAL`, 661 symbols, 25 process, 20 module | Map gate, boss join/leave, pet, PVP, dungeon và event đều phụ thuộc đổi map. Tuning PvE phải báo cáo theo map/bracket, không chỉ theo mob/item. |
| `TransactionService` / `Trade` | `TransactionService CRITICAL`, 44 symbols; `Trade LOW`, 2 symbols | Giao dịch người chơi là đường lan truyền item và currency. Economy rollback phải tính cả trade/transaction/ký gửi. |
| `LuckyNumberService` / `LuckyRound` | `LuckyNumberService CRITICAL`, 404 symbols; `LuckyRound LOW`, 6 symbols | Minigame có thể tạo item/currency ngoài shop/drop. Source catalog phải phân loại minigame reward. |
| `SuperRankService` / `TopService` | `SuperRankService HIGH`, 15 symbols; `TopService MEDIUM`, 19 symbols | Rank/top dùng stat snapshot và reward riêng. Data tuning phải giữ đúng top, SuperRank và reward rank. |
| `GiftCodeService` | `LOW`, 16 symbols nhưng là source data nhạy cảm | Blast radius runtime thấp nhưng giftcode là đường cấp item trực tiếp; phải parse `giftcode.detail` và gắn source. |

GitNexus cũng cho thấy `ItemService.createNewItem` có caller trực tiếp từ ít nhất 66 file. Các nhóm nguồn nổi bật: `UseItem`, `RewardService`, `Combine/manifest`, `ShopService`, `ConsignShopService`, `GiftCodeService`, `FarmService`, `TaskService`, achievement/badge, NPC đổi quà, `SummonDragon`, minigame, boss reward và event service. Vì vậy mọi kết luận "item đã sạch" chỉ hợp lệ khi đã audit toàn bộ nguồn sinh item/currency/progression này.

## 4) Build Path Theo Hệ

## 4.1 Trái Đất

| Build path | Skill neo | Trục chính | Trục phụ cho phép | Không được ghép |
| --- | --- | --- | --- | --- |
| Control Burst | `Thái Dương Hạ San`, `Dịch chuyển`, `Thôi miên`, `Kamejoko` | `49/50/147`, `14`, `5`, accuracy/pierce vừa | HP/KI thấp-vừa | HP/def/dodge/reflect cao cùng lúc. |
| QCKK Area Burst | `Quả Cầu Kênh Khi`, `Khiên` | dame, KI/MP vừa, an toàn channel | HP vừa hoặc shield utility | Crit cao + HP cao + def cao trên cùng set. |
| Kaioken Striker | `Kaioken`, `Dragon` | dame, crit, crit damage | HP/regen vừa để bù chi phí HP | Reflect/dodge cao hoặc tank stat cao. |
| Shield/Stealth Duelist | `Khiên`, `Tàng hình`, `Dịch chuyển` | utility, crit opener, dodge vừa | dame vừa | Biến thành tank bất tử với damage cao. |

Hướng chuẩn data: Trái Đất có thể burst rất mạnh, nhưng item Trái Đất damage cao phải mỏng hơn Namek healer và Xayda tank.

## 4.2 Namek

| Build path | Skill neo | Trục chính | Trục phụ cho phép | Không được ghép |
| --- | --- | --- | --- | --- |
| Healer Support | `Trị thương`, `Khiên` | HP, KI/MP, regen, damage reduction | stun resist/utility | Crit burst hoặc dame cao. |
| Masenko Tempo Caster | `Masenko`, Set Nail | speed/cooldown, dame chưởng vừa, KI/MP ổn định | crit thấp-vừa, pierce vừa | Lifesteal cao, tank stat cao, hoặc Makankosappo peak cao cùng lúc. |
| Makankosappo Lazer | `Makankosappo` | KI/MP max, dame vừa, pierce/accuracy vừa | shield/channel safety | HP/def/reflect cao như tank. |
| Summon Control | `Đẻ trứng`, `Biến Sôcôla`, `Ma phong ba` | HP/KI, utility/control | damage vừa-thấp | Burst crit cao cùng sustain cao. |
| Liên Hoàn Skirmisher | `Liên hoàn`, `Demon` | tempo cận chiến, dame vừa, stack `157` | lifesteal rất nhẹ, HP vừa-thấp | Crit cao + lifesteal cao + tank stat; heal/support stat đầy đủ. |

Hướng chuẩn data: Namek không chỉ là tank/heal. Phải có ít nhất hai đường DPS riêng: `Liên hoàn` sustained melee và `Masenko` tempo caster. Hai đường này không được lấy thêm full healer sustain, và không được gom chung với `Makankosappo` peak MP burst trên cùng set.

## 4.3 Xayda

| Build path | Skill neo | Trục chính | Trục phụ cho phép | Không được ghép |
| --- | --- | --- | --- | --- |
| HP Bomber | `Tự phát nổ`, `Huýt sáo`, `Khiên` | `6/22/77` HP max | regen/utility thấp-vừa | Crit/dame cao, reflect cao, dodge cao cùng lúc. |
| Reflect Tank | `Tái tạo năng lượng`, `Khiên`, cận chiến | HP, `47/94`, `97`, `108` có cap | damage thấp | `14/5/49/50/147` cao. |
| Monkey Bruiser | `Biến khỉ`, `Galick`, `Antomic` | HP + dame, crit window từ skill | regen/accuracy vừa | Reflect/dodge cao; crit item quá cao vì `Biến khỉ` đã tạo crit window. |
| Control Sustain | `Trói`, `Tái tạo năng lượng`, `Huýt sáo` | HP, regen, utility/control | damage vừa-thấp | Burst cao hoặc reflect cao. |
| Saiyan Damage | `Galick`, `Antomic`, `Cađíc LHC` | `49/50/147`, `5`, pierce/accuracy | HP thấp-vừa | HP bomber stat hoặc reflect tank stat. |

Hướng chuẩn data: Xayda được phép có nhiều path như user nêu: máu, phản sát thương, dame, né đòn. Điểm cần chặn là một set vừa là HP bomber, vừa reflect tank, vừa damage burst.

## 5) Nhóm Option Dùng Để Chuẩn Hóa

| Nhóm | Option ID | Ghi chú |
| --- | --- | --- |
| Damage | `49/50/147` | Dame %, sát thương tổng/nguồn dame. |
| Crit | `14`, `5` | Tỉ lệ crit, crit damage. |
| HP | `6/22/77` | HP flat/percent/theo option hiện có. |
| KI/MP | `103` | Cần đọc riêng cho Namek `Makankosappo`. |
| Def/reduction | `47/94` | Giảm sát thương/giáp, có cap runtime. |
| Regen | `80/81` | Hồi HP/KI theo chu kỳ. |
| Dodge | `108` | `Player.injured` có cap logic hiện tại tới 90. |
| Lifesteal | `95/96` | Hút HP/MP sau khi gây damage. |
| Reflect | `97` | Phản sát thương qua `phanSatThuong`. |
| Pierce/utility | `98/99/100/101/116` | Xuyên giáp, utility/reward, kháng stun. |
| Tempo/cooldown | `123/183/190` | Giảm hồi Trói/Khiên, tốc đánh. Phải bị siết trên skill cooldown ngắn. |
| Skill stack | `156/157/158/163` | Cộng dồn đấm/Liên Hoàn/Kaioken, sát thương Laze. Phải đọc cùng tần suất ra đòn. |
| Set tempo | `237-240`, `245-248`, `250-255` | Set Nail, Thần Vũ Trụ Kaio, Kaioken/Liên Hoàn/Giảm sát thương. |
| Equip/progression gate | `21/72/160` | `21` có thể override yêu cầu sức mạnh khi mặc, `72` dùng level đồ trong `getDoneLevel`, `160` ảnh hưởng TNSM pet. Không được chỉnh lẫn với combat stat nếu chưa test progression. |
| Runtime boost | charm/itemTime/intrinsic/map/server rate | Bùa, item time, intrinsic, cờ, map, server exp rate và pet-master bonus đều nằm ngoài option item nhưng ảnh hưởng damage/TNSM/tempo. |

## 6) Rule Chuẩn Hóa Data

## 6.1 Rule chung

- Mỗi item chỉ nên có 1 trục chính và tối đa 1 trục phụ nhẹ.
- Mỗi set full slot chỉ nên có 1 build path chính và 1 build path phụ, không gom 3 path mạnh.
- Item all-class/shared phải dùng band stat thấp hơn item khóa hệ.
- Các item event/boss có thể có peak cao hơn, nhưng vẫn phải có build path rõ.
- Không dùng option ID riêng lẻ để kết luận item mạnh/yếu; phải đọc theo `item_template`, gender/class, slot, source shop/drop và bộ option.
- Skill cooldown càng ngắn thì budget cho crit, lifesteal, dodge, reflect, stack damage và giảm hồi chiêu càng thấp.
- Skill cooldown dài hoặc có channel/self-cost được phép peak cao hơn, nhưng không được bỏ mất rủi ro/downtime bằng item phòng thủ quá mạnh.

## 6.2 Rule anti all-in-one theo hệ

- Trái Đất:
  - Nếu item có `49/50/147 + 14/5` cao, hạ `77/94/108/97`.
  - Build control được có utility, không được kèm full tank stat.
- Namek:
  - Healer support có HP/KI/regen cao thì dame/crit phải thấp.
  - Makankosappo được có KI/MP cao + damage vừa, nhưng không được full tank.
  - Masenko Tempo Caster được giảm hồi chiêu/damage chưởng, nhưng không được kèm lifesteal/tank cao.
  - Liên Hoàn DPS không được kèm full healer sustain, crit cao, lifesteal cao hoặc nhiều giảm hồi chiêu.
- Xayda:
  - HP Bomber được HP cao, nhưng không được kèm reflect/dodge/dame/crit cao.
  - Reflect Tank được `97/94/108` nhưng damage/crit phải thấp.
  - Monkey Bruiser được HP+dame, nhưng item crit phải cẩn thận vì skill đã tạo cửa sổ crit rất mạnh.

## 6.3 Rule tempo budget

- Nhóm skill tempo nhanh:
  - `Liên hoàn`, `Demon`, `Dragon`, `Galick`, `Kaioken`, `Masenko`.
  - Không ghép nhiều hơn 2 nhóm sau trên cùng set: crit cao, lifesteal, giảm hồi chiêu/tốc đánh, stack damage, tank stat.
- Nhóm skill burst chu kỳ vừa:
  - `Kamejoko`, `Antomic`, `Dịch chuyển`, `Trói`, `Sôcôla`, `TDHS`.
  - Cho phép damage/control cao hơn skill spam, nhưng phải có downtime và counterplay.
- Nhóm skill long-cooldown/channel:
  - `Makankosappo`, `QCKK`, `Super Kame`, `Ma phong ba`, `Cađíc LHC`, `Biến khỉ`, `Huýt sáo`, `Phân thân`.
  - Cho phép peak/utility cao hơn, nhưng không được kèm full uptime defense.
- Nhóm skill self-cost:
  - `Kaioken`, `Tự phát nổ`.
  - HP có thể là tài nguyên vận hành build, nhưng không được biến thành vừa damage vừa bất tử.

## 6.4 Rule theo nguồn/slot

- `setCaiTrang`: ưu tiên identity/utility, không biến thành nguồn stat tổng hợp vượt trần.
- `setPet`, `setDeoLung`, `setLinhThu`, `setThuCuoi`: mỗi nguồn chỉ nên đẩy một trục chính, tránh lặp lại cùng một bộ stat mạnh trên mọi slot.
- `tab_shop.items`: phải là nguồn meta sạch nhất, vì người chơi tiếp cận trực tiếp.
- `mob_reward.options_json`: phải đồng bộ với shop, không để drop phá rule đã công bố.
- `skill_template.skills`: là nguồn data cho damage, cooldown, mana, power require, `max_fight`, thời gian tàng hình/choáng và tỉ lệ choáng; phải được parse và đối chiếu với code runtime.
- `item_template`: phải đúng `type`, `gender`, `power_require`, `is_up_to_up`, `part`, vì đây là gate mặc đồ và slot.
- Legacy player item: pha đầu chỉ audit/report, chưa mass edit nếu chưa có chính sách bồi hoàn.

## 6.5 Rule an toàn stat rebuild

- Mọi thay đổi chạm tới `Service.point`, `NPoint.calPoint`, `NPoint.setPointWhenWearClothes`, `NPoint.addOption`, hoặc đường effect/item expiry gọi `Service.point` phải có:
  - feature flag mặc định tắt,
  - kill switch tắt ngay không cần deploy,
  - canary theo cụm user/bracket,
  - snapshot data trước rollout,
  - rollback script khôi phục data/config.
- Phase data-first ưu tiên chỉnh `tab_shop.items` và `mob_reward.options_json`; không sửa công thức rebuild stat global nếu chưa chứng minh data không đủ.
- Runtime guard nếu thêm ở phase này phải bắt đầu bằng audit/dry-run. Chỉ chuyển sang enforce sau khi canary không tạo lệch build, lag tick hoặc rebuild storm.
- Phải đo baseline số lần `Service.point()`/phút/người chơi trước khi bật guard. Canary không được làm tăng bất thường tần suất rebuild trong combat, login, đổi đồ, hết hiệu ứng hoặc timer buff.

## 6.6 Rule non-player tempo parity

- Tempo balance phải có test matrix riêng cho:
  - player thật,
  - pet,
  - boss,
  - bot,
  - clone/summon như phân thân, đệ, lính đánh thuê nếu dùng chung skill path.
- Khi đụng `SkillService.canUseSkillWithCooldown`, `speedat`, cooldown set hoặc option giảm hồi chiêu, phải so sánh attack interval, DPS và skill uptime của player với non-player.
- Không được để item player mới vô tình buff boss/pet/bot vì dùng chung cooldown logic hoặc stat rebuild.

## 6.7 Rule drop side-effect

- Mọi chỉnh sửa `mob_reward.options_json` phải kiểm tra cả item rơi ra và side-effect nhiệm vụ/sự kiện:
  - `TaskService`,
  - `BadgesTaskService`,
  - `PlayerEvent`,
  - event item đặc biệt theo map/sự kiện.
- Drop tuning không được làm lệch tốc độ hoàn thành nhiệm vụ, badge hoặc điểm event nếu mục tiêu chỉ là đa dạng build.
- Nếu cần tăng reward theo path, ưu tiên tăng độ rõ của path item thay vì tăng tổng giá trị drop trên cùng mob.

## 6.8 Rule player data regression

- Mọi thay đổi format hoặc ý nghĩa option phải chạy qua login/load regression vì `NDVSqlFetcher.loadPlayer` đang là blast-radius cao.
- Không được làm sai:
  - parse `items_body`, `items_bag`, `items_box`,
  - snapshot stat sau khi load,
  - leaderboard/top task,
  - minigame,
  - SuperRank.
- Nếu cần migration legacy item, phải tách khỏi rollout balance và có report trước/sau cho từng nhóm item.

## 6.9 Rule full item-source coverage

- Không được kết luận meta item đã sạch nếu mới audit `tab_shop` và `mob_reward`.
- Item-source audit bắt buộc bao gồm:
  - `tab_shop.items`,
  - `mob_reward.options_json`,
  - `giftcode.detail`,
  - `shop_ky_gui.itemOption`,
  - `radar.options`,
  - `item_nhabep`,
  - hardcoded factory trong `ItemService`,
  - `UseItem` mở rương/hộp/sách/linh thú,
  - `RewardService`,
  - `Combine/manifest`,
  - boss reward,
  - NPC đổi quà,
  - `FarmService`,
  - `TaskService`,
  - `AchievementService`,
  - `BadgesTaskService`,
  - `ClanService` và clan task/dungeon reward,
  - `SummonDragon`, `SummonDragonNamek`,
  - `LuckyRound`,
  - `LuckyNumberService`,
  - `DailyGiftService`,
  - `SuperRankService`, `TopService`,
  - `TransactionService`, `Trade`, ký gửi,
  - event service như Tet/Shenron/DragonNamecWar.
- Mọi nguồn tạo item build gear hoặc currency quan trọng phải được gắn `source`, `path`, `gender`, `slot`, `option group`, `power gate`, `tradeability` và `economy channel`.
- Nếu nguồn nào chưa phân loại được, tạm coi là unsafe source và không dùng làm bằng chứng balance.

## 6.10 Rule equip/economy/progression contract

- Equip contract:
  - `InventoryService` kiểm tra `item_template.type`, `gender`, `power_require` và option `21` trước khi mặc.
  - Pet chỉ mặc đồ khi đạt ngưỡng sức mạnh riêng; item pet/body phải test riêng với player item.
  - Không đổi `type/gender/power_require` nếu chưa test bag/body/box/pet body và `Service.point`.
- Economy contract:
  - Shop có nhiều currency: gold, gem, ruby, coupon, VND/cash, event point và shop đặc biệt dùng `item_spec`.
  - Cân bằng item không được vô tình đổi sink/source tiền tệ nếu mục tiêu chỉ là build diversity.
  - Cần KPI riêng cho giá mua, tốc độ farm nguyên liệu, số lượt mua, số lượt giao dịch/ký gửi và lạm phát item mạnh.
- Progression contract:
  - TNSM/power chịu ảnh hưởng bởi `NPoint.calSucManhTiemNang`, bùa trí tuệ, item time, cờ, map, server rate, intrinsic và pet-master bonus.
  - Option tăng TNSM hoặc yêu cầu sức mạnh không được nằm chung trong batch combat nếu chưa đo power/hour, task power gate và pet progression.
  - `skill_template.power_require` và `item_template.power_require` phải giữ đúng đường tiến cấp của 3 hệ.

## 6.11 Rule skill/intrinsic/template source-of-truth

- `Manager.loadDatabase` load nhiều bảng template ảnh hưởng gameplay: `skill_template`, `intrinsic`, `item_option_template`, `item_template`, `mob_template`, `map_template`, `radar`, task template, badge task, clan task, farm/crop, giftcode và consign.
- Khi phân tích skill 3 hệ, data từ `skill_template.skills` phải được export thành report riêng:
  - damage,
  - cooldown,
  - mana use,
  - power require,
  - max fight,
  - control duration/rate nếu có.
- Intrinsic là một lớp identity theo hệ, ảnh hưởng damage/crit/TNSM. Không sửa item để bù intrinsic nếu chưa có matrix theo intrinsic phổ biến của từng hệ.
- `mob_template.percent_tiem_nang`, map mob HP/level và map type ảnh hưởng PvE progression; drop balance phải đọc cùng mob/map, không đọc item rơi độc lập.

## 7) Kế Hoạch Triển Khai Data-first

## 7.1 Batch 0 - Data hygiene

- Parse toàn bộ `tab_shop.items`.
- Sửa các row JSON lỗi trước khi balance. Audit hiện tại phát hiện `tab_shop` có row không parse được: `13`, `19`, `21`, `23`, `41`, `70`.
- Parse toàn bộ `mob_reward.options_json`.
- Parse các nguồn data có option/item JSON khác:
  - `skill_template.skills`,
  - `giftcode.detail`,
  - `shop_ky_gui.itemOption`,
  - `radar.options`,
  - `item_nhabep`,
  - legacy `player.items_body`, `items_bag`, `items_box`.
- Export `item_template` với `id/type/gender/power_require/is_up_to_up/part`.
- Export `skill_template.skills` theo 3 hệ để khóa damage/cooldown/mana/power require trước khi sửa item.
- Tạo item-source catalog từ GitNexus:
  - tất cả caller của `ItemService.createNewItem`,
  - hardcoded factory trong `ItemService`,
  - boss reward,
  - `RewardService`,
  - `UseItem`,
  - `Combine/manifest`,
  - `AchievementService`,
  - `BadgesTaskService`,
  - `ClanService`,
  - `SuperRankService`, `TopService`,
  - `LuckyRound`, `LuckyNumberService`,
  - `TransactionService`, `Trade`, ký gửi,
  - NPC/event/farm/giftcode/daily gift/summon dragon.
- Tạo report option distribution theo:
  - source: shop/drop/boss/event/box/combine/giftcode/NPC/farm/consign/trade/minigame/clan/top/legacy,
  - item type/slot,
  - gender/class scope,
  - power gate,
  - option group,
  - cooldown/tempo group,
  - nghi vấn all-in-one.

Exit criteria:

- Các nguồn JSON item/skill parse được 100%.
- Có danh mục nguồn sinh item đầy đủ; nguồn chưa phân loại phải được đánh dấu unsafe.
- Có danh sách item theo hệ/build path trước khi sửa param.

## 7.2 Batch 1 - Gắn build path cho data shop

- Gắn mỗi item shop vào một trong các scope:
  - Trái Đất build path,
  - Namek build path,
  - Xayda build path,
  - shared/all-class low-band.
- Chỉnh option/param theo rule của build path.
- Đổi tên tab/mô tả nếu cần để người chơi hiểu đây là set theo hệ nào.

Exit criteria:

- Không còn item shop có 3 trục mạnh: damage/crit + HP/def + dodge/reflect.
- Không còn set tempo nhanh có đủ crit cao + lifesteal cao + tank stat.
- Mỗi hệ có ít nhất 3 path khả dụng, Namek có rõ `Liên hoàn`/`Masenko`/`Makankosappo`, Xayda có rõ HP/reflect/dame/né đòn.

## 7.3 Batch 2 - Đồng bộ mob reward

- Chuẩn hóa `mob_reward.options_json` theo cùng taxonomy với shop.
- Drop map thường ưu tiên band ổn định, không vượt shop/event quá nhiều.
- Drop boss/event được peak cao hơn nhưng phải gắn path rõ.
- Không chỉ đồng bộ mob drop: phải kiểm tra các nguồn tạo item cùng tier từ `ItemService`, `RewardService`, `UseItem`, `Combine/manifest`, boss reward, giftcode, NPC đổi quà, achievement, badge, clan, minigame, top/SuperRank, transaction/trade và ký gửi.
- Nếu source hardcode sinh set kích hoạt/hủy diệt/thiên sứ/radar/linh thú, phải gắn build path hoặc đưa vào blacklist audit.

Exit criteria:

- Drop không sinh item phá rule shop.
- Không có nguồn `createNewItem`/factory hardcode sinh item phá rule shop.
- Reward có lý do farm theo build path, không chỉ farm một option mạnh nhất.

## 7.4 Batch 3 - Legacy audit

- Audit `player.items_body`, `items_bag`, `items_box` để đo mức ảnh hưởng legacy.
- Chưa mass rewrite nếu chưa có chính sách vận hành.
- Nếu legacy quá lệch, ưu tiên:
  - ngừng spawn thêm,
  - tạo item replacement theo path mới,
  - canary migrate theo nhóm nhỏ.

## 7.5 Batch 4 - Runtime guard nếu cần

Chỉ thêm guard runtime nếu Batch 0-3 không đủ để chặn stack cực đoan.

Guard nên ở mức:

- feature flag mặc định tắt,
- dry-run/audit trước khi enforce,
- cảnh báo/audit khi tổng set vượt budget,
- cap một vài stack ngoài ý muốn,
- không sửa cảm giác skill core của từng hệ,
- có kill switch và rollback script trước khi bật canary.

Không nên sửa sớm các hàm blast-radius cao:

- `Service.point`,
- `NPoint.setPointWhenWearClothes`,
- `Player.injured`,
- `Mob.injured`,
- `SkillService.canUseSkillWithCooldown`,
- `NPoint.getDameAttack`,
- `NPoint.addOption`,
- `ItemService`,
- `InventoryService`,
- `ShopService`,
- `CombineService`,
- `TaskService`,
- `PlayerService`,
- `AchievementService`,
- `BadgesTaskService`,
- `ClanService`,
- `ChangeMapService`,
- `TransactionService`,
- `LuckyNumberService`,
- `SuperRankService`,
- `TopService`.

Nếu bắt buộc phải chạm các hàm trên, tách thành RFC/runtime phase riêng, kèm GitNexus impact report, test matrix player/pet/boss/bot/map/economy/progression và chỉ bật theo canary nhỏ.

## 8) KPI Cân Bằng

- Build diversity: build path top 1 không vượt 45% active player trong bracket chính.
- Class diversity: không hệ nào vượt ngưỡng thống trị nếu gear access tương đương.
- PvE clear speed: không để một path vừa clear nhanh nhất vừa chết ít nhất.
- Sustained DPS: skill hồi nhanh không được vượt burst dài chu kỳ nếu cùng mức an toàn.
- Uptime control/defense: giảm hồi chiêu Khiên/Trói không được tạo trạng thái gần như liên tục.
- PvP:
  - giảm one-shot rate ngoài cửa sổ burst hợp lệ,
  - giảm combat bất tử do dodge/reflect/regen stack,
  - giữ đủ "peak moment" cho burst/control đúng timing.
- Runtime safety:
  - `Service.point()`/phút/người chơi không tăng bất thường so với baseline,
  - không có burst rebuild stat khi nhiều effect/item time hết hạn cùng tick,
  - tick latency và lỗi combat không tăng trong canary.
- Non-player parity:
  - pet/boss/bot không đổi attack interval hoặc skill uptime ngoài mục tiêu đã công bố,
  - clear speed PvE không lệch vì boss/pet/bot bị buff/nerf gián tiếp.
- Progression safety:
  - tốc độ nhiệm vụ, badge và điểm event không lệch khi chỉ tuning drop item,
  - TNSM/power/hour không lệch nếu mục tiêu không phải tăng/giảm progression,
  - task power gate, pet power gate và item `power_require` không bị sai,
  - login/load error, leaderboard, minigame và SuperRank không có regression parse/stat snapshot.
- Source coverage:
  - 100% nguồn sinh item build gear có `source catalog`,
  - 100% nguồn sinh currency quan trọng có `economy channel`,
  - không còn source unsafe sinh item all-in-one,
  - không có factory hardcode vượt rule shop/drop.
- Economy safety:
  - tốc độ tiêu/nhận gold, gem, ruby, coupon, VND/cash và điểm event không lệch ngoài mục tiêu,
  - giá ký gửi/giao dịch item mạnh không tăng sốc do nguồn item mới quá hiếm hoặc quá mạnh,
  - số lượt mua shop/event item không sập vì đổi cost/type_sell sai.
- Retention proxy:
  - số lần đổi set/build,
  - số phiên farm theo reward path,
  - tỉ lệ quay lại sau khi đổi build.

## 9) Checklist Trước Rollout

- `tab_shop.items` parse JSON 100%.
- `mob_reward.options_json` parse JSON 100%.
- `skill_template.skills`, `giftcode.detail`, `shop_ky_gui.itemOption`, `radar.options` parse JSON 100%.
- `item_template` đã được audit `type/gender/power_require/is_up_to_up/part`.
- Có item-source catalog cho toàn bộ nguồn sinh item build gear.
- Có report phân bố option ID theo hệ/path.
- Có danh sách item vi phạm all-in-one theo rule mới.
- Có GitNexus impact report nếu PR chạm:
  - `Service.point`,
  - `NPoint.setPointWhenWearClothes`,
  - `SkillService.canUseSkillWithCooldown`,
  - `Mob.getItemMobReward`,
  - `NDVSqlFetcher.loadPlayer`,
  - `ItemService`,
  - `InventoryService`,
  - `ShopService`,
  - `CombineService`,
  - `TaskService`,
  - `PlayerService`,
  - `Player.injured`,
  - `Mob.injured`,
  - `AchievementService`,
  - `BadgesTaskService`,
  - `ClanService`,
  - `ChangeMapService`,
  - `TransactionService`,
  - `LuckyNumberService`,
  - `SuperRankService`,
  - `TopService`.
- Test nguồn sinh item:
  - shop thường/special/VND/event,
  - mob drop,
  - boss reward,
  - `UseItem` mở rương/hộp/sách/linh thú,
  - combine/craft/upgrade/tẩy,
  - giftcode,
  - NPC đổi quà,
  - farm,
  - lucky round,
  - lucky number/minigame,
  - summon dragon,
  - achievement/task reward,
  - badge/clan reward,
  - SuperRank/top reward,
  - daily gift,
  - ký gửi/trade/transaction nếu item có thể luân chuyển.
- Test các đường stack:
  - `NPoint.addOption`,
  - `setCaiTrang`,
  - `setDeoLung`,
  - `setPet`,
  - `setLinhThu`,
  - `setThuCuoi`.
- Test skill identity:
  - Trái Đất burst/control không thành tank.
  - Namek healer không thành burst top.
  - Namek Liên Hoàn không vừa DPS top vừa hút HP/tank cao.
  - Namek Masenko Tempo Caster không vượt mọi đường sustained DPS khi có Set Nail.
  - Namek Makankosappo scale KI/MP đúng kỳ vọng.
  - Xayda HP Bomber không kèm reflect/dodge/dame cao.
  - Xayda Reflect Tank sống lâu nhưng damage thấp.
- Test non-player tempo:
  - pet dùng skill nhanh không đổi nhịp ngoài ý muốn,
  - boss không bị đổi cooldown/uptime khi đổi stat player,
  - bot/clone/summon không hưởng sai option hoặc guard runtime.
- Test runtime rebuild:
  - đổi đồ,
  - hết item time,
  - hết effect skin,
  - login/load,
  - combat có nhiều buff/debuff hết hạn cùng lúc.
- Test equip/economy/progression:
  - mặc/tháo item theo `type/gender/power_require/option 21`,
  - pet body gate,
  - shop gold/gem/ruby/coupon/VND/event point,
  - `item_spec` shop đặc biệt,
  - TNSM/power/hour,
  - task power gate.
- Test drop side-effect:
  - task progression,
  - badge progression,
  - event point/item progression.
- Test regression ngoài combat:
  - leaderboard/top task,
  - minigame,
  - SuperRank,
  - parse/snapshot stat sau `loadPlayer`.

## 10) Canary Và Rollback

- Trước canary phải có feature flag, kill switch và snapshot data DB.
- Canary theo cụm user/map/bracket, không full server ngay.
- Theo dõi:
  - death rate/phút,
  - median combat duration,
  - tỉ lệ chết trong 3 giây đầu combat,
  - clear speed PvE,
  - phân bố build path theo hệ,
  - tỉ lệ item mới theo source catalog,
  - số item bị gắn `unknown_source`,
  - gold/gem/ruby/coupon/VND/event-point sink/source,
  - TNSM/power/hour theo map/bracket/hệ,
  - `Service.point()`/phút/người chơi,
  - rebuild storm khi effect/item time hết hạn,
  - pet/boss/bot attack interval,
  - task/badge/achievement/clan/event progression,
  - lucky round/lucky number reward rate,
  - trade/consign volume và giá item mạnh,
  - login/load, leaderboard, minigame, SuperRank error.
- Rollback bằng snapshot data DB nếu:
  - hai chỉ số xấu vượt ngưỡng cùng lúc,
  - một hệ bị mất khả năng chơi chính,
  - item mới tạo được all-in-one rõ ràng,
  - item từ source ngoài shop/drop lọt rule build path,
  - economy sink/source lệch mạnh so với baseline,
  - TNSM/power/hour vượt ngưỡng progression,
  - trade/consign hoặc minigame tạo đường farm item mạnh ngoài kiểm soát,
  - stat rebuild tạo spike runtime,
  - drop tuning làm lệch progression ngoài mục tiêu.
- Thứ tự rollback:
  - tắt feature flag/kill switch trước,
  - restore `tab_shop`, `mob_reward`, `giftcode`, `shop_ky_gui`, `radar`, `item_template`, `skill_template` từ snapshot nếu các bảng này có thay đổi,
  - restore source-catalog override/blacklist,
  - restore config runtime guard nếu có,
  - chạy lại smoke test login/load và combat cơ bản.

## 11) Rủi Ro Và Giảm Thiểu

- Rủi ro: Xayda HP build bị nerf sai vì bị xem như tank all-in-one.
  - Giảm thiểu: tách HP Bomber, Reflect Tank, Monkey Bruiser, Damage thành path riêng.
- Rủi ro: Namek bị đóng khung healer.
  - Giảm thiểu: giữ `Makankosappo` KI/MP burst và `Liên Hoàn` skirmisher.
- Rủi ro: Trái Đất burst quá khó chịu trong PvP.
  - Giảm thiểu: damage cao phải đi kèm phòng thủ thấp và phụ thuộc control window.
- Rủi ro: legacy item làm sai KPI.
  - Giảm thiểu: audit legacy riêng, không đánh giá meta mới chỉ bằng player đã có item cũ.
- Rủi ro: stat rebuild storm khi item time/effect hết hạn đồng loạt.
  - Giảm thiểu: đo baseline `Service.point()`/phút/người chơi, dry-run guard trước, canary nhỏ và có kill switch.
- Rủi ro: chỉnh tempo player làm pet/boss/bot đổi sức mạnh ngoài ý muốn.
  - Giảm thiểu: test non-player tempo parity trước rollout, đặc biệt với `canUseSkillWithCooldown` và `speedat`.
- Rủi ro: chỉnh drop làm lệch nhiệm vụ, badge hoặc điểm event.
  - Giảm thiểu: acceptance criteria cho `TaskService`, `BadgesTaskService`, `PlayerEvent`, không chỉ kiểm tra option item.
- Rủi ro: thay đổi data làm lỗi login/load hoặc leaderboard/minigame/SuperRank.
  - Giảm thiểu: regression `NDVSqlFetcher.loadPlayer`, parse item và snapshot stat trước/sau.
- Rủi ro: source ngoài shop/drop tái tạo item all-in-one.
  - Giảm thiểu: source catalog bắt buộc cho `ItemService`, `UseItem`, `RewardService`, `Combine/manifest`, boss reward, giftcode, NPC, event, farm, lucky round, lucky number, achievement, badge, clan, top, summon dragon, transaction và consign.
- Rủi ro: economy inflation/deflation do đổi shop, event point, giftcode hoặc consign.
  - Giảm thiểu: đo sink/source từng loại tiền và item đặc biệt trước/sau, không chỉ đo combat KPI.
- Rủi ro: giao dịch/ký gửi làm lan truyền item mạnh nhanh hơn dự kiến.
  - Giảm thiểu: đo transaction volume, giá ký gửi và tỉ lệ luân chuyển item mạnh sau canary.
- Rủi ro: minigame/top/SuperRank thành nguồn farm item hoặc currency ngoài đường build path.
  - Giảm thiểu: gắn source catalog và reward-rate KPI riêng cho lucky round, lucky number, top và SuperRank.
- Rủi ro: map gate/dungeon/clan progression lệch do phụ thuộc `ChangeMapService` và `ClanService`.
  - Giảm thiểu: test theo map/bracket, dungeon clan, map event và điều kiện vào map trước rollout rộng.
- Rủi ro: equip gate regression làm item đúng stat nhưng không mặc được, hoặc mặc sai hệ.
  - Giảm thiểu: test `type/gender/power_require/option 21`, pet body gate và item power requirement theo từng bracket.
- Rủi ro: sai nguồn sự thật giữa `skill_template`, intrinsic và item option.
  - Giảm thiểu: xuất report skill/intrinsic trước khi tune item; không dùng item để bù cho lỗi template skill.
- Rủi ro: tăng tốc progression ngoài ý muốn.
  - Giảm thiểu: theo dõi TNSM/power/hour, task power gate, map EXP multiplier, charm/itemTime/intrinsic/pet-master bonus.

## 12) Quyết Định Chốt Cho Phase Tiếp Theo

- Chốt Option B theo mô hình `3 hệ + build path theo hệ`.
- Không tiếp tục dùng "4 archetype toàn cục" làm thiết kế gốc.
- Thứ tự thực hiện: `Data hygiene` -> `source catalog` -> `skill/template report` -> `tab_shop` -> `mob_reward + toàn bộ source tạo item` -> `legacy audit` -> `runtime guard nếu cần`.
- Pha đầu không sửa công thức combat/stat rebuild global.
- Nếu phải sửa runtime, tách thành phase riêng có GitNexus impact, feature flag, kill switch, canary và rollback script.
