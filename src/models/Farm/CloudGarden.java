package models.Farm;

import consts.ConstFarm;
import player.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Class quản lý Khu vườn trên mây của mỗi player
 * Mỗi player có một CloudGarden riêng với các ô ruộng
 * 
 * @author TOMAHOC
 */
public class CloudGarden {

    private Player player; // Player sở hữu khu vườn
    private List<FarmPlot> plots; // Danh sách các ô ruộng
    private int unlockedPlots; // Số ô đã mở khóa
    private long lastUpdateTime; // Thời điểm cập nhật gần nhất
    private boolean loaded; // Đã load từ database chưa

    // Vị trí các ô ruộng theo từng map - 10 ô nằm trên cùng một đường thẳng
    // Key: MapID, Value: int[][] positions {X, Y}
    // Map 39 (Trái Đất), 40 (Namếc), 41 (Xayda) - tạm thời config giống nhau
    private static final int[][][] MAP_PLOT_POSITIONS = {
            // Map 39 (TD) - 10 ô: 5 mở + 5 khóa, nằm trên 1 đường thẳng
            // Điều chỉnh: đặt thấp hơn (Y=450) và giãn cách rộng hơn (spacing 70px)
            {
                    { 120, 415 }, { 190, 415 }, { 260, 415 }, { 330, 415 }, { 400, 415 }, // 5 ô mở
                    { 85, 435 }, { 155, 435 }, { 225, 435 }, { 295, 435 }, { 365, 435 } // 5 ô khóa
            },
            // Map 40 (NM) - config giống Map 39
            {
                    { 120, 415 }, { 190, 415 }, { 260, 415 }, { 330, 415 }, { 400, 415 }, // 5 ô mở
                    { 85, 435 }, { 155, 435 }, { 225, 435 }, { 295, 435 }, { 365, 435 } // 5 ô khóa
            },
            // Map 41 (XD) - config giống Map 39
            {
                    { 120, 415 }, { 190, 415 }, { 260, 415 }, { 330, 415 }, { 400, 415 }, // 5 ô mở
                    { 85, 435 }, { 155, 435 }, { 225, 435 }, { 295, 435 }, { 365, 435 } // 5 ô khóa
            }
    };

    private int currentMapId = consts.ConstFarm.MAP_CLOUD_GARDEN_TD; // Map hiện tại

    /**
     * Lấy vị trí ô ruộng theo mapId
     */
    public static int[][] getPlotPositions(int mapId) {
        int index = mapId - consts.ConstFarm.MAP_CLOUD_GARDEN_TD; // 39 -> 0, 40 -> 1, 41 -> 2
        if (index >= 0 && index < MAP_PLOT_POSITIONS.length) {
            return MAP_PLOT_POSITIONS[index];
        }
        return MAP_PLOT_POSITIONS[0]; // Default to Map 39
    }

    public int getCurrentMapId() {
        return currentMapId;
    }

    public void setCurrentMapId(int mapId) {
        this.currentMapId = mapId;
    }

    // Getters and Setters
    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public List<FarmPlot> getPlots() {
        return plots;
    }

    public void setPlots(List<FarmPlot> plots) {
        this.plots = plots;
    }

    public int getUnlockedPlots() {
        return unlockedPlots;
    }

    public void setUnlockedPlots(int unlockedPlots) {
        this.unlockedPlots = unlockedPlots;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public CloudGarden() {
        this.plots = new ArrayList<>();
        this.unlockedPlots = ConstFarm.INITIAL_PLOTS;
        this.loaded = false;
    }

    public CloudGarden(Player player) {
        this();
        this.player = player;
        initializePlots();
    }

    /**
     * Khởi tạo các ô ruộng ban đầu
     */
    private void initializePlots() {
        plots.clear();
        int[][] positions = getPlotPositions(currentMapId);
        for (int i = 0; i < ConstFarm.MAX_PLOTS; i++) {
            FarmPlot plot = new FarmPlot(i, i < unlockedPlots);
            if (i < positions.length) {
                plot.setPosX(positions[i][0]);
                plot.setPosY(positions[i][1]);
            }
            plots.add(plot);
        }
    }

    /**
     * Cập nhật tất cả các ô ruộng
     * Được gọi định kỳ hoặc khi player vào map
     */
    public boolean update() {
        long currentTime = System.currentTimeMillis();
        boolean anyChanged = false;
        for (FarmPlot plot : plots) {
            if (plot.isUnlocked()) {
                if (plot.update()) {
                    anyChanged = true;
                }
            }
        }
        lastUpdateTime = currentTime;
        return anyChanged;
    }

    /**
     * Lấy ô ruộng theo ID
     */
    public FarmPlot getPlot(int plotId) {
        if (plotId >= 0 && plotId < plots.size()) {
            return plots.get(plotId);
        }
        return null;
    }

    /**
     * Mở khóa ô ruộng tiếp theo
     * 
     * @param useGem true nếu dùng ngọc, false nếu dùng vàng
     * @return true nếu thành công
     */
    public boolean unlockNextPlot(boolean useGem) {
        if (unlockedPlots >= ConstFarm.MAX_PLOTS) {
            return false;
        }

        // Kiểm tra và trừ tiền
        if (useGem) {
            if (player.inventory.getGemAndRuby() < ConstFarm.PLOT_UNLOCK_COST_GEM) {
                return false;
            }
            player.inventory.subGemAndRuby(ConstFarm.PLOT_UNLOCK_COST_GEM);
        } else {
            if (player.inventory.gold < ConstFarm.PLOT_UNLOCK_COST_GOLD) {
                return false;
            }
            player.inventory.gold -= ConstFarm.PLOT_UNLOCK_COST_GOLD;
        }

        // Mở khóa ô tiếp theo
        FarmPlot plot = plots.get(unlockedPlots);
        plot.setUnlocked(true);
        unlockedPlots++;

        return true;
    }

    /**
     * Lấy chi phí mở khóa ô tiếp theo (có thể tăng theo số ô)
     */
    public long getNextUnlockCostGold() {
        // Chi phí có thể tăng theo số ô đã mở khóa
        return (long) (ConstFarm.PLOT_UNLOCK_COST_GOLD * Math.pow(1.5, unlockedPlots - ConstFarm.INITIAL_PLOTS));
    }

    public int getNextUnlockCostGem() {
        // Chi phí ngọc có thể tăng theo số ô đã mở khóa
        return (int) (ConstFarm.PLOT_UNLOCK_COST_GEM * (1 + (unlockedPlots - ConstFarm.INITIAL_PLOTS) * 0.5));
    }

    /**
     * Kiểm tra có thể mở khóa thêm ô không
     */
    public boolean canUnlockMore() {
        return unlockedPlots < ConstFarm.MAX_PLOTS;
    }

    /**
     * Đếm số ô đang trống
     */
    public int countEmptyPlots() {
        int count = 0;
        for (FarmPlot plot : plots) {
            if (plot.isUnlocked() && plot.isEmpty()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Đếm số ô đang trồng cây
     */
    public int countGrowingPlots() {
        int count = 0;
        for (FarmPlot plot : plots) {
            if (plot.isUnlocked() && plot.isGrowing()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Đếm số ô sẵn sàng thu hoạch (bao gồm cả cây héo)
     */
    public int countReadyToHarvestPlots() {
        int count = 0;
        for (FarmPlot plot : plots) {
            if (plot.isReadyToHarvest() || plot.isWithered()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Lấy danh sách các ô sẵn sàng thu hoạch (bao gồm cả cây héo)
     */
    public List<FarmPlot> getReadyToHarvestPlots() {
        List<FarmPlot> readyPlots = new ArrayList<>();
        for (FarmPlot plot : plots) {
            if (plot.isReadyToHarvest() || plot.isWithered()) {
                readyPlots.add(plot);
            }
        }
        return readyPlots;
    }

    /**
     * Lấy danh sách các ô ruộng có cây bị héo (WITHERED)
     */
    public List<FarmPlot> getWitheredPlots() {
        List<FarmPlot> witheredPlots = new ArrayList<>();
        for (FarmPlot plot : plots) {
            if (plot.isUnlocked() && plot.isWithered()) {
                witheredPlots.add(plot);
            }
        }
        return witheredPlots;
    }

    /**
     * Thu hoạch tất cả các ô sẵn sàng
     * 
     * @return Danh sách [cropType, quantity] cho mỗi ô thu hoạch
     */
    public List<int[]> harvestAll() {
        List<int[]> harvested = new ArrayList<>();
        for (FarmPlot plot : plots) {
            if (plot.isReadyToHarvest()) {
                byte cropType = plot.getCropType();
                int quantity = plot.harvest();
                if (quantity > 0) {
                    harvested.add(new int[] { cropType, quantity });
                }
            }
        }
        return harvested;
    }

    /**
     * Lấy thông tin tổng quan về khu vườn
     */
    public String getGardenInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Khu vườn của ").append(player.name).append("\n");
        sb.append("Số ô: ").append(unlockedPlots).append("/").append(ConstFarm.MAX_PLOTS).append("\n");
        sb.append("Đang trống: ").append(countEmptyPlots()).append("\n");
        sb.append("Đang trồng: ").append(countGrowingPlots()).append("\n");
        sb.append("Sẵn sàng thu hoạch: ").append(countReadyToHarvestPlots());
        return sb.toString();
    }

    /**
     * Dọn dẹp khi người chơi thoát
     */
    public void dispose() {
        this.player = null;
        if (this.plots != null) {
            this.plots.clear();
        }
    }

    /**
     * Load dữ liệu từ database
     * Format:
     * plotId|unlocked|stage|cropType|plantedTime|lastStageTime|watered|matureReadyTime;...
     */
    public void loadFromString(String data) {
        if (data == null || data.isEmpty()) {
            initializePlots();
            return;
        }

        try {
            String[] plotDatas = data.split(";");
            plots.clear();
            int[][] positions = getPlotPositions(currentMapId);

            for (int i = 0; i < ConstFarm.MAX_PLOTS; i++) {
                FarmPlot plot = new FarmPlot(i, false);
                if (i < positions.length) {
                    plot.setPosX(positions[i][0]);
                    plot.setPosY(positions[i][1]);
                }

                if (i < plotDatas.length && !plotDatas[i].isEmpty()) {
                    String[] parts = plotDatas[i].split("\\|");
                    if (parts.length >= 7) {
                        plot.setPlotId(Integer.parseInt(parts[0]));
                        plot.setUnlocked(parts[1].equals("1"));
                        plot.setCurrentStage(Byte.parseByte(parts[2]));
                        plot.setCropType(Byte.parseByte(parts[3]));
                        plot.setPlantedTime(Long.parseLong(parts[4]));
                        plot.setLastStageChangeTime(Long.parseLong(parts[5]));
                        plot.setWatered(parts[6].equals("1"));

                        // Load matureReadyTime (tương thích ngược với data cũ)
                        if (parts.length >= 8) {
                            plot.setMatureReadyTime(Long.parseLong(parts[7]));
                        }

                        if (plot.isUnlocked() && i >= unlockedPlots) {
                            unlockedPlots = i + 1;
                        }
                    }
                }

                plots.add(plot);
            }

            // Đảm bảo ít nhất có INITIAL_PLOTS ô được mở khóa
            if (unlockedPlots < ConstFarm.INITIAL_PLOTS) {
                unlockedPlots = ConstFarm.INITIAL_PLOTS;
                for (int i = 0; i < unlockedPlots; i++) {
                    plots.get(i).setUnlocked(true);
                }
            }

            loaded = true;
        } catch (Exception e) {
            // Nếu có lỗi, khởi tạo mới
            initializePlots();
        }
    }

    /**
     * Lưu dữ liệu sang string để lưu database
     */
    public String saveToString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < plots.size(); i++) {
            FarmPlot plot = plots.get(i);
            if (i > 0) {
                sb.append(";");
            }
            sb.append(plot.getPlotId()).append("|");
            sb.append(plot.isUnlocked() ? "1" : "0").append("|");
            sb.append(plot.getCurrentStage()).append("|");
            sb.append(plot.getCropType()).append("|");
            sb.append(plot.getPlantedTime()).append("|");
            sb.append(plot.getLastStageChangeTime()).append("|");
            sb.append(plot.isWatered() ? "1" : "0").append("|");
            sb.append(plot.getMatureReadyTime());
        }
        return sb.toString();
    }
}
