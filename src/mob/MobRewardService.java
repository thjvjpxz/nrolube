package mob;

import event.EventManager;
import item.Item;
import item.Item.ItemOption;
import jdbc.DBConnecter;
import map.ItemMap;
import map.Zone;
import player.Pet;
import player.Player;
import server.ServerNotify;
import services.InventoryService;
import services.ItemService;
import services.MapService;
import utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service quản lý việc rơi vật phẩm từ quái
 * Dữ liệu được load từ bảng `mob_reward` trong database
 */
public class MobRewardService {

    private static MobRewardService instance;

    // Cache danh sách reward
    private List<MobReward> rewards = new CopyOnWriteArrayList<>();

    public static MobRewardService gI() {
        if (instance == null) {
            instance = new MobRewardService();
        }
        return instance;
    }

    /**
     * Load dữ liệu từ database
     * Gọi khi server khởi động hoặc khi cần reload
     */
    public void load() {
        rewards.clear();
        String sql = "SELECT * FROM mob_reward WHERE is_active = 1";

        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            boolean hasDropGroupColumn;
            try {
                rs.findColumn("drop_group");
                hasDropGroupColumn = true;
            } catch (SQLException e) {
                hasDropGroupColumn = false;
                System.err.println("[MobRewardService] Cột drop_group chưa tồn tại trong DB, derive từ dữ liệu cũ");
            }

            while (rs.next()) {
                MobReward reward = new MobReward();
                reward.id = rs.getInt("id");
                reward.mobId = rs.getInt("mob_id");
                reward.mapId = rs.getInt("map_id");
                reward.itemTemplateId = rs.getInt("item_template_id");
                reward.rate = rs.getInt("rate");
                reward.quantityMin = rs.getInt("quantity_min");
                reward.quantityMax = rs.getInt("quantity_max");
                reward.gender = rs.getInt("gender");
                reward.eventKey = rs.getString("event_key");
                reward.mapType = rs.getString("map_type");
                reward.conditionType = rs.getString("condition_type");

                // Đọc drop_group với fallback khi DB chưa migrate
                if (hasDropGroupColumn) {
                    String rawGroup = rs.getString("drop_group");
                    if (rawGroup != null && !rawGroup.trim().isEmpty()) {
                        reward.dropGroup = rawGroup.trim().toUpperCase();
                    } // else giữ default "NORMAL"
                } else {
                    reward.dropGroup = deriveDropGroup(reward.eventKey, reward.conditionType);
                }

                reward.isRandomRange = rs.getBoolean("is_random_range");
                reward.randomRange = rs.getInt("random_range");
                reward.notifyGlobal = rs.getBoolean("notify_global");
                reward.description = rs.getString("description");
                reward.isActive = rs.getBoolean("is_active");

                // Parse options từ JSON
                reward.parseOptions(rs.getString("options_json"));

                rewards.add(reward);
            }

            System.out.println("Loaded MobRewards: " + rewards.size() + " entries");

        } catch (Exception e) {
            System.err.println("Error loading MobRewards: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reload dữ liệu (Hot-reload không cần restart server)
     */
    public void reload() {
        load();
        System.out.println("[MobRewardService] Đã reload " + rewards.size() + " cấu hình drop!");
    }

    /**
     * Derive dropGroup từ dữ liệu cũ khi DB chưa có cột drop_group.
     * Giữ tương thích logic tách normal/event cũ.
     */
    private static String deriveDropGroup(String eventKey, String conditionType) {
        if (eventKey != null && !eventKey.trim().isEmpty()) {
            return "EVENT";
        }
        if (conditionType != null && !conditionType.trim().isEmpty()) {
            return "SPECIAL";
        }
        return "NORMAL";
    }

    // Group priority: SPECIAL > EQUIPMENT > GEM > MATERIAL > GOLD > NORMAL
    private static final String[] GROUP_PRIORITY = { "SPECIAL", "EQUIPMENT", "GEM", "MATERIAL", "GOLD", "NORMAL" };

    /**
     * Reward là event reward khi drop_group = EVENT hoặc có event_key.
     * Safety: nếu web Phase 2 chưa deploy, DB default là NORMAL,
     * reward có event_key vẫn cần vào event pool.
     */
    private static boolean isEventReward(MobReward reward) {
        return "EVENT".equals(reward.dropGroup)
                || (reward.eventKey != null && !reward.eventKey.isEmpty());
    }

    /**
     * Lấy danh sách vật phẩm rơi khi quái chết
     * Mỗi reward tự roll rate, candidate cùng group ưu tiên chọn 1 winner
     * Tối đa 1 reward thường + 1 reward event mỗi lần quái chết
     * 
     * @param player Người chơi giết quái
     * @param mob    Quái bị giết
     * @param x      Tọa độ x rơi item
     * @param yEnd   Tọa độ y rơi item
     * @return Danh sách ItemMap sẽ rơi
     */
    public List<ItemMap> getDrops(Player player, Mob mob, int x, int yEnd) {
        List<ItemMap> drops = new ArrayList<>();
        Zone zone = mob.zone;
        int mapId = zone.map.mapId;
        int mobTempId = mob.tempId;
        int pt4la = mob.pt4la;

        Player dropOwner = getRealPlayer(player);

        // 1. Lọc reward theo mob/map/event/gender/condition, tách theo dropGroup
        List<MobReward> normalRewards = new ArrayList<>();
        List<MobReward> eventRewards = new ArrayList<>();
        for (MobReward reward : rewards) {
            if (reward.mobId != -1 && reward.mobId != mobTempId) {
                continue;
            }
            if (reward.mapType != null && !reward.mapType.isEmpty()) {
                if (!checkMapType(reward.mapType, mapId)) {
                    continue;
                }
            } else {
                if (reward.mapId != -1 && reward.mapId != mapId) {
                    continue;
                }
            }
            if (reward.eventKey != null && !checkEvent(reward.eventKey)) {
                continue;
            }
            if (reward.gender != -1 && reward.gender != dropOwner.gender) {
                continue;
            }
            if (reward.conditionType != null && !checkCondition(reward.conditionType, dropOwner, player, mob)) {
                continue;
            }

            if (isEventReward(reward)) {
                eventRewards.add(reward);
            } else {
                normalRewards.add(reward);
            }
        }

        // 2. Roll từng normal reward độc lập, thu thập candidate
        List<MobReward> normalCandidates = collectCandidates(normalRewards, pt4la);

        // 3. Chọn winner normal theo group priority
        if (!normalCandidates.isEmpty()) {
            MobReward winner = pickWinnerByPriority(normalCandidates);
            processRewardDrop(winner, drops, zone, x, yEnd, dropOwner);
        }

        // 4. Roll từng event reward, chọn winner ngẫu nhiên
        List<MobReward> eventCandidates = collectCandidates(eventRewards, pt4la);
        if (!eventCandidates.isEmpty()) {
            MobReward winner = eventCandidates.get(Util.nextInt(eventCandidates.size()));
            processRewardDrop(winner, drops, zone, x, yEnd, dropOwner);
        }

        return drops;
    }

    /**
     * Roll rate cho từng reward (rate / pt4la), trả về danh sách candidate trúng roll.
     */
    private List<MobReward> collectCandidates(List<MobReward> rewards, int pt4la) {
        List<MobReward> candidates = new ArrayList<>();
        for (MobReward reward : rewards) {
            int adjustedRate = reward.rate / pt4la;
            if (adjustedRate < 1)
                adjustedRate = 1;
            if (Util.isTrue(1, adjustedRate)) {
                candidates.add(reward);
            }
        }
        return candidates;
    }

    /**
     * Chọn 1 winner từ candidate theo group priority.
     * SPECIAL > EQUIPMENT > GEM > MATERIAL > GOLD > NORMAL.
     * Nếu nhiều candidate cùng group priority, chọn random 1.
     */
    private MobReward pickWinnerByPriority(List<MobReward> candidates) {
        for (String group : GROUP_PRIORITY) {
            List<MobReward> grouped = new ArrayList<>();
            for (MobReward r : candidates) {
                if (group.equals(r.dropGroup)) {
                    grouped.add(r);
                }
            }
            if (!grouped.isEmpty()) {
                return grouped.get(Util.nextInt(grouped.size()));
            }
        }
        return candidates.get(0);
    }

    /**
     * Xử lý drop cho 1 reward đã thắng: tạo ItemMap và thêm vào danh sách rơi.
     */
    private void processRewardDrop(MobReward reward, List<ItemMap> drops, Zone zone,
            int x, int yEnd, Player realPlayer) {
        try {
            ItemMap itemMap = createItemMap(reward, zone, x, yEnd, realPlayer.id);
            if (itemMap != null) {
                drops.add(itemMap);

                if (reward.notifyGlobal) {
                    ServerNotify.gI().notify(realPlayer.name + " vừa nhặt được "
                            + itemMap.itemTemplate.name + " tại "
                            + zone.map.mapName + " khu " + zone.zoneId);
                }
            }
        } catch (Exception e) {
            System.err.println("Error processing reward ID " + reward.id + ": " + e.getMessage());
        }
    }

    /**
     * Lấy player gốc (nếu là pet/clone thì lấy master)
     */
    private Player getRealPlayer(Player player) {
        if (player.isPet) {
            return ((Pet) player).master;
        }
        return player;
    }

    /**
     * Kiểm tra loại map
     */
    private boolean checkMapType(String mapType, int mapId) {
        MapService ms = MapService.gI();
        switch (mapType) {
            case "MAP_COLD":
                return ms.isMapCold(mapId);
            case "MAP_SKH":
                return ms.isMapUpSKH(mapId);
            case "MAP_PORATA":
                return ms.isMapUpPorata(mapId);
            case "MAP_TUONG_LAI":
                return ms.isMapTuongLai(mapId);
            case "MAP_NGHIA_DIA":
                return ms.isNghiaDia(mapId);
            case "MAP_HUY_DIET":
                return ms.isHuyDiet(mapId);
            case "MAP_HANH_TINH_THUC_VAT":
                return ms.isMapHanhTinhThucVat(mapId);
            case "MAP_PHO_BAN":
                return ms.isMapPhoBan(mapId);
            case "MAP_HALLOWEEN":
                return ms.isMapEventHalloween(mapId);
            case "MAP_SKY_PEAR":
                return ms.isSkyPear(mapId);
            case "MAP_NAPPA":
                return ms.isMapNappa(mapId);
            default:
                return false;
        }
    }

    /**
     * Kiểm tra sự kiện có đang bật không
     */
    private boolean checkEvent(String eventKey) {
        return switch (eventKey) {
            case "CHRISTMAS", "christmas" -> config.EventConfig.CHRISTMAS_EVENT;
            case "HALLOWEEN", "halloween" -> config.EventConfig.HALLOWEEN_EVENT;
            case "LUNAR_NEW_YEAR" -> config.EventConfig.LUNAR_NEW_YEAR;
            case "HUNG_VUONG", "hung_vuong" -> config.EventConfig.HUNG_VUONG_EVENT;
            case "TRUNG_THU", "trung_thu" -> config.EventConfig.TRUNG_THU_EVENT;
            default -> false;
        };
    }

    /**
     * Kiểm tra điều kiện đặc biệt
     */
    private boolean checkCondition(String conditionType, Player dropOwner, Player conditionPlayer, Mob mob) {
        switch (conditionType) {
            case "FULL_SET_THAN":
                return InventoryService.gI().fullSetThan(conditionPlayer);
            case "IS_BUMA":
                return dropOwner.nPoint.isBuma;
            case "IS_QUAN_DI_BIEN":
                return dropOwner.nPoint.isQuanDiBien;
            case "USE_MAYDO":
                return dropOwner.itemTime.isUseMayDo && mob.tempId > 57 && mob.tempId < 66;
            case "USE_MAYDO2":
                return dropOwner.itemTime.isUseMayDo2 && mob.tempId > 80 && mob.tempId < 81;
            case "HAS_NTK":
                return InventoryService.gI().findItemNTK(dropOwner);
            case "DROP_SET_KICH_HOAT":
                return true;
            case "DROP_SET_KICH_HOAT_VIP":
                return true;
            default:
                return true;
        }
    }

    /**
     * Tạo ItemMap từ MobReward config
     */
    private ItemMap createItemMap(MobReward reward, Zone zone, int x, int yEnd, long playerId) {
        int itemId = reward.itemTemplateId;

        // Xử lý random range
        if (reward.isRandomRange && reward.randomRange > 0) {
            itemId = reward.itemTemplateId + Util.nextInt(0, reward.randomRange);
        }

        // Xử lý Set Kích Hoạt (cần logic đặc biệt)
        if ("DROP_SET_KICH_HOAT".equals(reward.conditionType)) {
            return createSetKichHoat(zone, x, yEnd, playerId, false);
        }
        if ("DROP_SET_KICH_HOAT_VIP".equals(reward.conditionType)) {
            return createSetKichHoat(zone, x, yEnd, playerId, true);
        }

        // Tính số lượng
        int quantity = reward.quantityMin;
        if (reward.quantityMax > reward.quantityMin) {
            quantity = Util.nextInt(reward.quantityMin, reward.quantityMax);
        }

        ItemMap itemMap = new ItemMap(zone, itemId, quantity, x, yEnd, playerId);

        List<MobRewardOption> opts = reward.itemOptions.getOrDefault(itemId, reward.defaultOptions);
        for (MobRewardOption opt : opts) {
            itemMap.options.add(new ItemOption(opt.id, opt.resolveParam()));
        }

        return itemMap;
    }

    /**
     * Tạo Set Kích Hoạt (logic đặc biệt cần giữ lại từ code cũ)
     */
    private ItemMap createSetKichHoat(Zone zone, int x, int yEnd, long playerId, boolean isVip) {
        // Lấy player để biết gender
        Player player = zone.getPlayerInMap(playerId);
        if (player == null)
            return null;

        short itTemp = (short) ItemService.gI().randTempItemKichHoat(player.gender);
        ItemMap it = new ItemMap(zone, itTemp, 1, x, yEnd, playerId);

        List<Item.ItemOption> ops = ItemService.gI().getListOptionItemShop(itTemp);
        if (!ops.isEmpty()) {
            it.options = ops;
        }

        if (isVip) {
            int[] opsrand = ItemService.gI().randOptionItemKichHoatNew(player.gender);
            it.options.add(new Item.ItemOption(opsrand[0], 0));
            it.options.add(new Item.ItemOption(opsrand[1], 0));
            it.options.add(new Item.ItemOption(opsrand[2], 0));
            it.options.add(new Item.ItemOption(opsrand[3], 0));
        } else {
            int[] opsrand = ItemService.gI().randOptionItemKichHoat(player.gender);
            it.options.add(new Item.ItemOption(opsrand[0], 0));
            it.options.add(new Item.ItemOption(opsrand[1], 0));
        }

        it.options.add(new Item.ItemOption(30, 0));
        return it;
    }

    /**
     * Lấy số lượng reward đang active
     */
    public int getRewardCount() {
        return rewards.size();
    }
}
