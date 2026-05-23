package sosumenh;

import item.Item;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jdbc.DBConnecter;
import jdbc.daos.PlayerDAO;
import lombok.Getter;
import network.Message;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import player.Inventory;
import player.Player;
import services.InventoryService;
import services.ItemService;
import services.Service;
import utils.Logger;

/**
 * @author BTH Cute Phô Mai Que
 */
public class SoSuMenhService {

    private Item createItem(JSONObject dataObject) {
        int tempId = Integer.parseInt(dataObject.get("temp_id").toString());
        int quantity = Integer.parseInt(dataObject.get("quantity").toString());
        Item item = ItemService.gI().createNewItem((short) tempId);
        item.quantity = quantity;

        JSONArray optionsArray = (JSONArray) dataObject.get("options");
        if (optionsArray != null) {
            for (Object optionObj : optionsArray) {
                JSONObject optionObject = (JSONObject) optionObj;
                int param = Integer.parseInt(optionObject.get("param").toString());
                int optionId = Integer.parseInt(optionObject.get("id").toString());
                item.itemOptions.add(new Item.ItemOption(optionId, param));
            }
        }
        return item;
    }

    // ========================
    // DTOs cho Sổ Sứ Mệnh Panel
    // ========================

    public static class RewardOptionView {
        public short optionId;
        public int param;
        public String text;

        public RewardOptionView(short optionId, int param, String text) {
            this.optionId = optionId;
            this.param = param;
            this.text = text;
        }
    }

    public static class RewardItemView {
        public short tempId;
        public String itemName;
        public short iconId;
        public int quantity;
        public List<RewardOptionView> options;

        public RewardItemView(short tempId, String itemName, short iconId, int quantity) {
            this.tempId = tempId;
            this.itemName = itemName;
            this.iconId = iconId;
            this.quantity = quantity;
            this.options = new ArrayList<>();
        }
    }

    public static class RewardTierView {
        public int level;
        public boolean normalClaimed;
        public boolean normalClaimable;
        public boolean vipClaimed;
        public boolean vipClaimable;
        public List<RewardItemView> normalRewards;
        public List<RewardItemView> vipRewards;

        public RewardTierView(int level) {
            this.level = level;
            this.normalRewards = new ArrayList<>();
            this.vipRewards = new ArrayList<>();
        }
    }

    public static class MissionTaskView {
        public int taskId;
        public String taskName;
        public int currentCount;
        public int targetCount;
        public boolean finished;
        public int pointReward;

        public MissionTaskView(int taskId, String taskName, int currentCount, int targetCount, boolean finished, int pointReward) {
            this.taskId = taskId;
            this.taskName = taskName;
            this.currentCount = currentCount;
            this.targetCount = targetCount;
            this.finished = finished;
            this.pointReward = pointReward;
        }
    }

    public static class SoSuMenhSnapshot {
        public int maxLevel;
        public int currentLevel;
        public int currentPoint;
        public int nextLevelPoint;
        public boolean vipUnlocked;
        public int coutday;
        public int selectedLevel;
        public List<RewardTierView> tiers;
        public List<MissionTaskView> tasks;

        public SoSuMenhSnapshot() {
            this.tiers = new ArrayList<>();
            this.tasks = new ArrayList<>();
        }
    }

    // ========================
    // Reward Loading
    // ========================

    private List<RewardItemView> parseRewardItems(String rawJson) {
        List<RewardItemView> result = new ArrayList<>();
        if (rawJson == null || rawJson.isEmpty()) {
            Logger.warning("[SSM] parseRewardItems: rawJson is null or empty");
            return result;
        }
        JSONArray dataArray;
        try {
            dataArray = (JSONArray) JSONValue.parse(rawJson);
            if (dataArray == null || dataArray.isEmpty()) {
                Logger.warning("[SSM] parseRewardItems: JSON is not an array or empty");
                return result;
            }
        } catch (Exception e) {
            Logger.warning("[SSM] parseRewardItems: failed to parse JSON: " + e.getMessage());
            return result;
        }
        for (Object obj : dataArray) {
            try {
                JSONObject dataObject = (JSONObject) JSONValue.parse(obj.toString());
                if (dataObject == null) {
                    Logger.warning("[SSM] parseRewardItems: item JSON is null, skip");
                    continue;
                }
                int tempId = Integer.parseInt(dataObject.get("temp_id").toString());
                int quantity = Integer.parseInt(dataObject.get("quantity").toString());
                models.Template.ItemTemplate itemTemplate = ItemService.gI().getTemplate(tempId);
                String itemName;
                short iconId;
                if (itemTemplate != null) {
                    itemName = itemTemplate.name;
                    iconId = itemTemplate.iconID;
                } else {
                    itemName = "Unknown(" + tempId + ")";
                    iconId = -1;
                    Logger.warning("[SSM] parseRewardItems: item template not found for tempId=" + tempId);
                }
                RewardItemView itemView = new RewardItemView((short) tempId, itemName, iconId, quantity);

                JSONArray optionsArray = (JSONArray) dataObject.get("options");
                if (optionsArray != null) {
                    for (Object optionObj : optionsArray) {
                        JSONObject optionObject = (JSONObject) optionObj;
                        int optionId = Integer.parseInt(optionObject.get("id").toString());
                        int param = Integer.parseInt(optionObject.get("param").toString());
                        models.Template.ItemOptionTemplate optionTemplate = ItemService.gI().getItemOptionTemplate(optionId);
                        String optionText;
                        if (optionTemplate != null) {
                            optionText = optionTemplate.name.replace("#", String.valueOf(param));
                        } else {
                            optionText = "Option#" + optionId + "(" + param + ")";
                        }
                        itemView.options.add(new RewardOptionView((short) optionId, param, optionText));
                    }
                }
                result.add(itemView);
            } catch (Exception e) {
                Logger.warning("[SSM] parseRewardItems: skip invalid item: " + e.getMessage());
            }
        }
        return result;
    }

    private List<RewardTierView> loadRewardTiers(Player player) throws SQLException {
        List<RewardTierView> tiers = new ArrayList<>();
        try (Connection conn = DBConnecter.getConnectionServer();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM so_su_menh_reward ORDER BY level ASC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int level = rs.getInt("level");
                if (level < 1 || level > 20) {
                    Logger.warning("[SSM] loadRewardTiers: level " + level + " out of range 1..20, skip");
                    continue;
                }
                RewardTierView tier = new RewardTierView(level);
                tier.normalRewards = parseRewardItems(rs.getString("items"));
                tier.vipRewards = parseRewardItems(rs.getString("items2"));
                tier.normalClaimed = player.sosumenhplayer.reward[level - 1];
                tier.vipClaimed = player.sosumenhplayer.rewardVip[level - 1];
                tier.normalClaimable = player.sosumenhplayer.getLevel() >= level && !tier.normalClaimed;
                tier.vipClaimable = player.sosumenhplayer.isVip() && player.sosumenhplayer.getLevel() >= level && !tier.vipClaimed;
                tiers.add(tier);
            }
        }
        return tiers;
    }

    // ========================
    // Mission Tasks
    // ========================

    private int getTaskTargetCount(int taskId) {
        switch (taskId) {
            case 1:
                return 200;
            case 2:
                return 100;
            case 3:
                return 100;
            case 4:
                return 1000;
            case 5:
                return 30;
            default:
                Logger.warning("[SSM] getTaskTargetCount: unknown taskId=" + taskId);
                return 0;
        }
    }

    private List<MissionTaskView> buildMissionTasks(Player player) {
        List<MissionTaskView> tasks = new ArrayList<>();
        if (player.sosumenhplayer.ssmTaskMain == null) {
            return tasks;
        }
        for (SoSuMenhTaskMain ssm : player.sosumenhplayer.ssmTaskMain) {
            SoSuMenhTaskTemplate template = SoSuMenhManager.getInstance().findById(ssm.idTask);
            if (template == null) {
                Logger.warning("[SSM] buildMissionTasks: template not found for taskId=" + ssm.idTask + ", skip");
                continue;
            }
            String taskName = template.getTask();
            int pointReward = template.getPoint();
            int targetCount = getTaskTargetCount(ssm.idTask);
            int currentCount = ssm.countTask;
            boolean finished = ssm.finish;
            tasks.add(new MissionTaskView(ssm.idTask, taskName, currentCount, targetCount, finished, pointReward));
        }
        return tasks;
    }

    // ========================
    // Snapshot Building
    // ========================

    private SoSuMenhSnapshot buildSnapshot(Player player) throws SQLException {
        SoSuMenhSnapshot snapshot = new SoSuMenhSnapshot();
        if (player.sosumenhplayer == null) {
            player.sosumenhplayer = new sosumenh.SoSuMenhPlayer(player);
        }
        snapshot.maxLevel = 20;
        snapshot.currentPoint = player.sosumenhplayer.getPoint();
        snapshot.currentLevel = Math.min(snapshot.maxLevel, Math.max(0, player.sosumenhplayer.getLevel()));
        snapshot.nextLevelPoint = snapshot.currentLevel >= snapshot.maxLevel
                ? snapshot.currentPoint
                : (snapshot.currentLevel + 1) * 50;
        snapshot.vipUnlocked = player.sosumenhplayer.isVip();
        snapshot.coutday = player.sosumenhplayer.getCoutday();
        snapshot.selectedLevel = Math.min(snapshot.maxLevel, Math.max(1, snapshot.currentLevel == 0 ? 1 : snapshot.currentLevel));
        snapshot.tiers = loadRewardTiers(player);
        snapshot.tasks = buildMissionTasks(player);
        return snapshot;
    }

    // ========================
    // Packet Writers
    // ========================

    private void writeSnapshot(Message msg, SoSuMenhSnapshot snapshot) throws IOException {
        msg.writer().writeByte(snapshot.maxLevel);
        msg.writer().writeByte(snapshot.currentLevel);
        msg.writer().writeInt(snapshot.currentPoint);
        msg.writer().writeInt(snapshot.nextLevelPoint);
        msg.writer().writeBoolean(snapshot.vipUnlocked);
        msg.writer().writeByte(snapshot.coutday);
        msg.writer().writeByte(snapshot.selectedLevel);
        msg.writer().writeByte(snapshot.tiers.size());
        for (RewardTierView tier : snapshot.tiers) {
            writeTier(msg, tier);
        }
        msg.writer().writeByte(snapshot.tasks.size());
        for (MissionTaskView task : snapshot.tasks) {
            writeTask(msg, task);
        }
    }

    private void writeTier(Message msg, RewardTierView tier) throws IOException {
        msg.writer().writeByte(tier.level);
        msg.writer().writeBoolean(tier.normalClaimed);
        msg.writer().writeBoolean(tier.normalClaimable);
        msg.writer().writeBoolean(tier.vipClaimed);
        msg.writer().writeBoolean(tier.vipClaimable);
        msg.writer().writeByte(tier.normalRewards.size());
        for (RewardItemView item : tier.normalRewards) {
            writeRewardItem(msg, item);
        }
        msg.writer().writeByte(tier.vipRewards.size());
        for (RewardItemView item : tier.vipRewards) {
            writeRewardItem(msg, item);
        }
    }

    private void writeRewardItem(Message msg, RewardItemView item) throws IOException {
        msg.writer().writeShort(item.tempId);
        msg.writer().writeUTF(item.itemName);
        msg.writer().writeShort(item.iconId);
        msg.writer().writeInt(item.quantity);
        msg.writer().writeByte(item.options.size());
        for (RewardOptionView option : item.options) {
            msg.writer().writeShort(option.optionId);
            msg.writer().writeInt(option.param);
            msg.writer().writeUTF(option.text);
        }
    }

    private void writeTask(Message msg, MissionTaskView task) throws IOException {
        msg.writer().writeByte(task.taskId);
        msg.writer().writeUTF(task.taskName);
        msg.writer().writeInt(task.currentCount);
        msg.writer().writeInt(task.targetCount);
        msg.writer().writeBoolean(task.finished);
        msg.writer().writeInt(task.pointReward);
    }

    // ========================
    // Public API
    // ========================

    public void openPanel(Player player) {
        if (player.getSession() == null) {
            return;
        }
        Message msg = null;
        try {
            SoSuMenhSnapshot snapshot = buildSnapshot(player);
            msg = new Message(-76);
            msg.writer().writeByte(2);
            writeSnapshot(msg, snapshot);
            player.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(SoSuMenhService.class, e);
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
            sendErrorPanel(player, "Không tải được dữ liệu Sổ Sứ Mệnh");
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    private void sendErrorPanel(Player player, String userMessage) {
        Message msg = null;
        try {
            msg = new Message(-76);
            msg.writer().writeByte(5);
            msg.writer().writeUTF(userMessage);
            player.sendMessage(msg);
        } catch (IOException e) {
            Logger.logException(SoSuMenhService.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    // ========================
    // Claim Helpers
    // ========================

    private List<Item> createRewardItemsForLevel(int level, String rewardColumn) {
        List<Item> items = new ArrayList<>();
        try (Connection conn = DBConnecter.getConnectionServer();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM so_su_menh_reward WHERE level = ?")) {
            ps.setInt(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String rawJson = rs.getString(rewardColumn);
                    if (rawJson == null || rawJson.isEmpty()) {
                        return items;
                    }
                    JSONArray dataArray = (JSONArray) JSONValue.parse(rawJson);
                    if (dataArray == null) {
                        return items;
                    }
                    for (Object obj : dataArray) {
                        try {
                            JSONObject dataObject = (JSONObject) JSONValue.parse(obj.toString());
                            Item item = createItem(dataObject);
                            items.add(item);
                        } catch (Exception e) {
                            Logger.warning("[SSM] createRewardItemsForLevel: skip invalid item at level=" + level + " column=" + rewardColumn + ": " + e.getMessage());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Logger.logException(SoSuMenhService.class, e);
        }
        return items;
    }

    private boolean isClaimed(Player player, int level, int lane) {
        if (lane == 0) {
            return player.sosumenhplayer.reward[level - 1];
        }
        return player.sosumenhplayer.rewardVip[level - 1];
    }

    private void markClaimed(Player player, int level, int lane) {
        if (lane == 0) {
            player.sosumenhplayer.reward[level - 1] = true;
        } else {
            player.sosumenhplayer.rewardVip[level - 1] = true;
        }
    }

    /**
     * Item có side-effect ngoài bag khi addItemBag: bùa (type 13),
     * item tạo flag player (mabu egg, tennis ship, heal), item mở rộng ô.
     * Nếu SSM config chứa item này thì reject claim để tránh lệch state.
     */
    private static boolean isUnsafeSsmReward(Item item) {
        if (item == null || item.template == null) {
            return false;
        }
        switch (item.template.type) {
            case 13: // charms (bùa) — mutate player.charms
                return true;
        }
        switch (item.template.id) {
            case 568:  // quả trứng mabu
            case 453:  // tàu tennis
            case 74:   // đùi gà hồi HP/MP
            case 1627: // mở rộng hành trang
            case 517:  // mở rộng hành trang
            case 518:  // mở rộng rương đồ
                return true;
        }
        return false;
    }

    /**
     * Kiểm tra toàn bộ item có thể add vào bag không, thuần túy không side-effect.
     * Rule: gold/gem/ruby kiểm tra limit dạng số; item khác mỗi cái cần 1 ô trống
     * (cận trên conservative, bỏ qua stacking). Nếu đủ ô trống thì pass.
     * Trường hợp reject do conservative, player có thể claim từng mốc riêng lẻ.
     *
     * @return true nếu toàn bộ item add được
     */
    private boolean canAddAllToBag(Player player, List<Item> items) {
        if (items.isEmpty()) {
            return true;
        }
        long goldSim = player.inventory.gold;
        long gemSim = player.inventory.gem;
        long rubySim = player.inventory.ruby;
        int neededSlots = 0;
        int emptySlots = 0;

        for (Item bagItem : player.inventory.itemsBag) {
            if (!bagItem.isNotNullItem()) {
                emptySlots++;
            }
        }

        for (Item item : items) {
            if (item.template == null) {
                continue;
            }
            switch (item.template.type) {
                case 9:
                    goldSim += item.quantity;
                    if (goldSim > Inventory.LIMIT_GOLD) {
                        return false;
                    }
                    break;
                case 10:
                    gemSim += item.quantity;
                    if (gemSim > Integer.MAX_VALUE) {
                        return false;
                    }
                    break;
                case 34:
                    rubySim += item.quantity;
                    if (rubySim > Integer.MAX_VALUE) {
                        return false;
                    }
                    break;
                default:
                    neededSlots++;
                    break;
            }
        }

        return neededSlots <= emptySlots;
    }

    // ========================
    // Claim Methods
    // ========================

    /**
     * Snapshot bag + currency + reward flags để rollback toàn bộ nếu add/persist fail.
     */
    private static class ClaimSnapshot {
        final List<Item> bag;
        final long gold;
        final int gem;
        final int ruby;
        final boolean[] reward;
        final boolean[] rewardVip;

        ClaimSnapshot(Player player) {
            this.gold = player.inventory.gold;
            this.gem = player.inventory.gem;
            this.ruby = player.inventory.ruby;
            this.bag = new ArrayList<>();
            for (Item bagItem : player.inventory.itemsBag) {
                if (bagItem.isNotNullItem()) {
                    this.bag.add(ItemService.gI().copyItem(bagItem));
                } else {
                    this.bag.add(ItemService.gI().createItemNull());
                }
            }
            this.reward = player.sosumenhplayer.reward.clone();
            this.rewardVip = player.sosumenhplayer.rewardVip.clone();
        }

        void restore(Player player) {
            player.inventory.gold = this.gold;
            player.inventory.gem = this.gem;
            player.inventory.ruby = this.ruby;
            player.inventory.itemsBag.clear();
            player.inventory.itemsBag.addAll(this.bag);
            System.arraycopy(this.reward, 0, player.sosumenhplayer.reward, 0, Math.min(this.reward.length, player.sosumenhplayer.reward.length));
            System.arraycopy(this.rewardVip, 0, player.sosumenhplayer.rewardVip, 0, Math.min(this.rewardVip.length, player.sosumenhplayer.rewardVip.length));
        }
    }

    public void claimOne(Player player, int level, int lane) {
        if (player == null || player.getSession() == null) {
            return;
        }
        if (level < 1 || level > 20) {
            Service.gI().sendThongBao(player, "Cấp không hợp lệ");
            openPanel(player);
            return;
        }
        if (lane != 0 && lane != 1) {
            Service.gI().sendThongBao(player, "Loại phần thưởng không hợp lệ");
            openPanel(player);
            return;
        }
        if (player.sosumenhplayer.getLevel() < level) {
            Service.gI().sendThongBao(player, "Bạn chưa đạt cấp yêu cầu");
            openPanel(player);
            return;
        }
        if (lane == 1 && !player.sosumenhplayer.isVip()) {
            Service.gI().sendThongBao(player, "Bạn chưa mở khóa Sổ Vip");
            openPanel(player);
            return;
        }
        if (isClaimed(player, level, lane)) {
            Service.gI().sendThongBaoOK(player, "Bạn đã nhận phần thưởng này rồi");
            openPanel(player);
            return;
        }

        // Check persist-ready trước khi mutate bất kỳ state nào
        if (player.iDMark == null || !player.iDMark.isLoadedAllDataPlayer()) {
            Logger.warning("[SSM] claimOne: cannot persist, player state not ready. level=" + level + " lane=" + lane + " player=" + player.name);
            Service.gI().sendThongBao(player, "Có lỗi dữ liệu, vui lòng thử lại");
            openPanel(player);
            return;
        }

        String rewardColumn = lane == 0 ? "items" : "items2";
        List<Item> items = createRewardItemsForLevel(level, rewardColumn);
        if (items.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có phần thưởng ở mốc này");
            openPanel(player);
            return;
        }

        for (Item item : items) {
            if (isUnsafeSsmReward(item)) {
                Logger.warning("[SSM] claimOne: unsafe reward item, reject. level=" + level + " lane=" + lane + " item=" + item.template.id);
                Service.gI().sendThongBao(player, "Phần thưởng đang lỗi cấu hình, vui lòng báo admin");
                openPanel(player);
                return;
            }
        }

        if (!canAddAllToBag(player, items)) {
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ");
            openPanel(player);
            return;
        }

        ClaimSnapshot snap = new ClaimSnapshot(player);
        boolean allAdded = true;
        for (Item item : items) {
            Item copy = ItemService.gI().copyItem(item);
            if (!InventoryService.gI().addItemBag(player, copy)) {
                allAdded = false;
                break;
            }
        }
        if (!allAdded) {
            snap.restore(player);
            Logger.warning("[SSM] claimOne: addItemBag failed after preflight, rolled back. level=" + level + " lane=" + lane);
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ");
            openPanel(player);
            return;
        }

        markClaimed(player, level, lane);

        if (!PlayerDAO.updatePlayerAndReturn(player)) {
            snap.restore(player);
            Logger.warning("[SSM] claimOne: persist failed, rolled back. level=" + level + " lane=" + lane + " player=" + player.name);
            Service.gI().sendThongBao(player, "Có lỗi lưu dữ liệu, vui lòng thử lại");
            openPanel(player);
            return;
        }
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendThongBao(player, "Nhận thành công, vui lòng kiểm tra hành trang");
        openPanel(player);
    }

    public void claimAll(Player player) {
        if (player == null || player.getSession() == null) {
            return;
        }
        int maxLevel = 20;
        List<int[]> claimablePairs = new ArrayList<>();
        for (int level = 1; level <= maxLevel; level++) {
            if (player.sosumenhplayer.getLevel() >= level && !player.sosumenhplayer.reward[level - 1]) {
                claimablePairs.add(new int[]{level, 0});
            }
            if (player.sosumenhplayer.isVip() && player.sosumenhplayer.getLevel() >= level
                    && !player.sosumenhplayer.rewardVip[level - 1]) {
                claimablePairs.add(new int[]{level, 1});
            }
        }
        if (claimablePairs.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có phần thưởng nào có thể nhận");
            openPanel(player);
            return;
        }

        // Check persist-ready trước khi mutate bất kỳ state nào
        if (player.iDMark == null || !player.iDMark.isLoadedAllDataPlayer()) {
            Logger.warning("[SSM] claimAll: cannot persist, player state not ready. player=" + player.name);
            Service.gI().sendThongBao(player, "Có lỗi dữ liệu, vui lòng thử lại");
            openPanel(player);
            return;
        }

        // Build map pair -> items một lần duy nhất
        java.util.Map<String, List<Item>> pairToItems = new java.util.LinkedHashMap<>();
        for (int[] pair : claimablePairs) {
            int level = pair[0];
            int lane = pair[1];
            String key = level + "_" + lane;
            String rewardColumn = lane == 0 ? "items" : "items2";
            List<Item> items = createRewardItemsForLevel(level, rewardColumn);
            pairToItems.put(key, items);
        }

        // Validate toàn bộ pair trước khi mutate bất kỳ state nào
        for (java.util.Map.Entry<String, List<Item>> entry : pairToItems.entrySet()) {
            List<Item> items = entry.getValue();
            if (items == null || items.isEmpty()) {
                Logger.warning("[SSM] claimAll: empty reward config, reject. key=" + entry.getKey());
                Service.gI().sendThongBao(player, "Phần thưởng đang lỗi cấu hình, vui lòng báo admin");
                openPanel(player);
                return;
            }
            for (Item item : items) {
                if (isUnsafeSsmReward(item)) {
                    Logger.warning("[SSM] claimAll: unsafe reward item, reject. key=" + entry.getKey() + " item=" + item.template.id);
                    Service.gI().sendThongBao(player, "Phần thưởng đang lỗi cấu hình, vui lòng báo admin");
                    openPanel(player);
                    return;
                }
            }
        }

        // Gom toàn bộ item để preflight
        List<Item> allItems = new ArrayList<>();
        for (List<Item> items : pairToItems.values()) {
            allItems.addAll(items);
        }
        if (allItems.isEmpty()) {
            Service.gI().sendThongBao(player, "Không có phần thưởng nào có thể nhận");
            openPanel(player);
            return;
        }

        if (!canAddAllToBag(player, allItems)) {
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ");
            openPanel(player);
            return;
        }

        ClaimSnapshot snap = new ClaimSnapshot(player);
        List<int[]> successPairs = new ArrayList<>();
        for (int[] pair : claimablePairs) {
            int level = pair[0];
            int lane = pair[1];
            String key = level + "_" + lane;
            List<Item> items = pairToItems.get(key);
            if (items == null || items.isEmpty()) {
                continue;
            }

            boolean levelAllAdded = true;
            for (Item item : items) {
                Item copy = ItemService.gI().copyItem(item);
                if (!InventoryService.gI().addItemBag(player, copy)) {
                    levelAllAdded = false;
                    break;
                }
            }
            if (levelAllAdded) {
                successPairs.add(pair);
            } else {
                break;
            }
        }

        if (successPairs.size() != claimablePairs.size()) {
            snap.restore(player);
            Logger.warning("[SSM] claimAll: addItemBag failed after preflight, rolled back entire batch. claimed="
                    + successPairs.size() + " expected=" + claimablePairs.size());
            Service.gI().sendThongBao(player, "Hành trang không đủ chỗ");
            openPanel(player);
            return;
        }

        for (int[] pair : successPairs) {
            markClaimed(player, pair[0], pair[1]);
        }

        if (!PlayerDAO.updatePlayerAndReturn(player)) {
            snap.restore(player);
            Logger.warning("[SSM] claimAll: persist failed, rolled back. player=" + player.name + " pairs=" + successPairs.size());
            Service.gI().sendThongBao(player, "Có lỗi lưu dữ liệu, vui lòng thử lại");
            openPanel(player);
            return;
        }
        InventoryService.gI().sendItemBag(player);
        Service.gI().sendThongBao(player, "Đã nhận " + successPairs.size() + " mốc thưởng, vui lòng kiểm tra hành trang");
        openPanel(player);
    }

    @Getter
    private static final SoSuMenhService instance = new SoSuMenhService();
}
