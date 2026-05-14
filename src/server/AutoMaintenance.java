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
        try (java.io.FileInputStream fis = new java.io.FileInputStream("data/config/config.properties")) {
            prop.load(fis);
            autoMaintenanceEnabled = Boolean.parseBoolean(prop.getProperty("server.autorestart", "false"));
            maintenanceHour = Integer.parseInt(prop.getProperty("server.maintenance.hour", "4"));
            maintenanceMin = Integer.parseInt(prop.getProperty("server.maintenance.min", "0"));
        } catch (Exception e) {
            autoMaintenanceEnabled = false;
            maintenanceHour = 4;
            maintenanceMin = 0;
        }
    }

    @Override
    public void run() {
        loadConfig();
        AutoMaintenance = autoMaintenanceEnabled;
        while (!Maintenance.isRunning && !isRunning) {
            try {
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
