package server;

/*
 *
 *
 * @author EMTI
 */

import EMTI.Functions;
import java.time.LocalTime;
import utils.Logger;

public class AutoMaintenance extends Thread {

    private static final String CONFIG_PATH = "data/config/config.properties";

    public static boolean AutoMaintenance = false;
    private static AutoMaintenance instance;
    public static boolean isRunning;

    private boolean autoMaintenanceEnabled;
    private int maintenanceHour;
    private int maintenanceMin;

    public static AutoMaintenance gI() {
        if (instance == null) {
            instance = new AutoMaintenance();
        }
        return instance;
    }

    private void loadConfig() {
        java.util.Properties prop = new java.util.Properties();
        try (java.io.FileInputStream fis = new java.io.FileInputStream(CONFIG_PATH)) {
            prop.load(fis);
            autoMaintenanceEnabled = Boolean.parseBoolean(prop.getProperty("server.autorestart", "false"));
            maintenanceHour = parseConfigInt(prop, "server.maintenance.hour", 4, 0, 23);
            maintenanceMin = parseConfigInt(prop, "server.maintenance.min", 0, 0, 59);
        } catch (Exception e) {
            autoMaintenanceEnabled = false;
            maintenanceHour = 4;
            maintenanceMin = 0;
            Logger.error("Không thể đọc cấu hình bảo trì tự động: " + e.getMessage() + "\n");
        }
    }

    private int parseConfigInt(java.util.Properties prop, String key, int defaultValue, int minValue, int maxValue) {
        try {
            int value = Integer.parseInt(prop.getProperty(key, String.valueOf(defaultValue)).trim());
            if (value < minValue || value > maxValue) {
                Logger.error("Cấu hình " + key + " không hợp lệ: " + value + "\n");
                return defaultValue;
            }
            return value;
        } catch (NumberFormatException e) {
            Logger.error("Cấu hình " + key + " không phải số: " + prop.getProperty(key) + "\n");
            return defaultValue;
        }
    }

    @Override
    public void run() {
        while (!Maintenance.isRunning && !isRunning) {
            try {
                loadConfig();
                AutoMaintenance = autoMaintenanceEnabled;
                if (AutoMaintenance) {
                    LocalTime currentTime = LocalTime.now();
                    if (currentTime.getHour() == maintenanceHour && currentTime.getMinute() == maintenanceMin) {
                        Logger.log(Logger.PURPLE, "Đang tiến hành quá trình bảo trì tự động\n");
                        Maintenance.gI().start(60);
                        isRunning = true;
                        AutoMaintenance = false;
                    }
                }
                Functions.sleep(1000);
            } catch (Exception e) {
            }
        }
    }

}
