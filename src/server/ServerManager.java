package server;

/*
 *
 *
 * @author EMTI
 */
import EMTI.FileRunner;
import EMTI.Functions;
import boss.AnTromManager;
import boss.BrolyManager;
import minigame.DecisionMaker.DecisionMaker;
import minigame.LuckyNumber.LuckyNumber;
import models.Consign.ConsignShopManager;
import jdbc.daos.HistoryTransactionDAO;
import boss.BossManager;
import boss.BossGroupScheduler;
import boss.OtherBossManager;
import boss.TreasureUnderSeaManager;
import boss.SnakeWayManager;
import boss.RedRibbonHQManager;
import boss.GasDestroyManager;
import boss.YardartManager;
import boss.ChristmasEventManager;
import boss.FinalBossManager;
import boss.HalloweenEventManager;
import boss.HungVuongEventManager;
import boss.LunarNewYearEventManager;
import boss.SkillSummonedManager;
import boss.TrungThuEventManager;
import bot.BotManager;
import consts.ConstDataEventNAP;
import consts.ConstDataEventSM;

import java.io.IOException;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicBoolean;

import network.inetwork.ISession;
import network.Network;
import server.io.MyKeyHandler;
import server.io.MySession;
import services.ClanService;
import services.NgocRongNamecService;
import utils.Logger;
import utils.TimeUtil;

import java.util.*;

import models.The23rdMartialArtCongress.The23rdMartialArtCongressManager;
import models.DeathOrAliveArena.DeathOrAliveArenaManager;
import event.EventManager;

import java.io.DataOutputStream;
import jdbc.daos.EventDAO;
import models.WorldMartialArtsTournament.WorldMartialArtsTournamentManager;
import network.MessageSendCollect;
import models.ShenronEvent.ShenronEventManager;
import models.SuperRank.SuperRankManager;
import network.Message;
import network.inetwork.ISessionAcceptHandler;
import services.func.TopService;

public class ServerManager {

    private static final boolean IS_LINUX_OR_HEADLESS;

    static {
        boolean isLinux = System.getProperty("os.name", "").toLowerCase().contains("linux");
        boolean isHeadless = GraphicsEnvironment.isHeadless();
        IS_LINUX_OR_HEADLESS = isLinux || isHeadless;
    }


    public static String timeStart;

    public static final Map CLIENTS = new HashMap();

    public static String NAME = "Local";
    public static String IP = "127.0.0.1";
    public static int PORT = 14445;

    private static ServerManager instance;

    public static boolean isRunning;

    /**
     * Ensures {@link #close()} / signal shutdown only run the full save sequence once.
     */
    private static final AtomicBoolean GRACEFUL_SHUTDOWN_DONE = new AtomicBoolean(false);

    public void init() {
        Manager.gI();
        HistoryTransactionDAO.deleteHistory();
    }

    public static ServerManager gI() {
        if (instance == null) {
            instance = new ServerManager();
            instance.init();
        }
        return instance;
    }

    public static void main(String[] args) {
        timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
        if (IS_LINUX_OR_HEADLESS) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    Logger.warning(">> Shutdown signal received; saving game data...\n");
                    ServerManager.gI().shutdownGracefully(false);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, "Shutdown-save"));
            ServerManager.gI().run();
            return;
        }
        new server.ui.ServerManagerUI().setVisible(true);
    }

    public void run() {
        System.out.println("DEBUG: ServerManager.run() STARTING...");
        isRunning = true;

        try {
            System.out.println("DEBUG: activeServerSocket...");
            activeServerSocket();

            System.out.println("DEBUG: activeCommandLine...");
            activeCommandLine();

            System.out.println("DEBUG: Starting service threads...");
            new Thread(NgocRongNamecService.gI(), "Update NRNM").start();
            new Thread(SuperRankManager.gI(), "Update Super Rank").start();
            new Thread(The23rdMartialArtCongressManager.gI(), "Update DHVT23").start();
            new Thread(DeathOrAliveArenaManager.gI(), "Update Võ Đài Sinh Tử").start();
            new Thread(WorldMartialArtsTournamentManager.gI(), "Update WMAT").start();
            new Thread(AutoMaintenance.gI(), "Update Bảo Trì Tự Động").start();
            new Thread(ShenronEventManager.gI(), "Update Shenron").start();

            System.out.println("DEBUG: Loading Bosses...");
            BossManager.gI().loadBoss();
            Manager.MAPS.forEach(map.Map::initBoss);
            EventManager.gI().init();

            System.out.println("DEBUG: Starting Boss Threads...");

            // === Gộp Core Boss → 1 thread (trước: 7 thread riêng) ===
            BossGroupScheduler coreBossScheduler = new BossGroupScheduler(
                    "Core Boss",
                    new BossManager[] {
                            BossManager.gI(),
                            YardartManager.gI(),
                            FinalBossManager.gI(),
                            BrolyManager.gI(),
                            AnTromManager.gI(),
                            OtherBossManager.gI(),
                            SkillSummonedManager.gI()
                    },
                    new boolean[] {
                            false, // BossManager: không remove
                            false, // YardartManager: không remove
                            false, // FinalBossManager: không remove
                            false, // BrolyManager: không remove
                            false, // AnTromManager: không remove
                            true, // OtherBossManager: remove on error
                            true // SkillSummonedManager: remove on error
                    });
            new Thread(coreBossScheduler, "Core Boss Scheduler").start();

            // === Gộp Phó Bản Boss → 1 thread (trước: 4 thread riêng) ===
            BossGroupScheduler phoBanBossScheduler = new BossGroupScheduler(
                    "PhoBan Boss",
                    new BossManager[] {
                            RedRibbonHQManager.gI(),
                            TreasureUnderSeaManager.gI(),
                            SnakeWayManager.gI(),
                            GasDestroyManager.gI()
                    },
                    new boolean[] {
                            true, // RedRibbonHQManager: remove on error
                            true, // TreasureUnderSeaManager: remove on error
                            true, // SnakeWayManager: remove on error
                            true // GasDestroyManager: remove on error
                    });
            new Thread(phoBanBossScheduler, "PhoBan Boss Scheduler").start();

            // === Sự kiện Boss → giữ thread riêng ===
            new Thread(TrungThuEventManager.gI(), "Update trung thu event boss").start();
            if (config.EventConfig.HALLOWEEN_EVENT) {
                new Thread(HalloweenEventManager.gI(), "Update halloween event boss").start();
            }
            if (config.EventConfig.CHRISTMAS_EVENT) {
                new Thread(ChristmasEventManager.gI(), "Update christmas event boss").start();
            }
            if (config.EventConfig.HUNG_VUONG_EVENT) {
                new Thread(HungVuongEventManager.gI(), "Update Hung Vuong event boss").start();
            }
            if (config.EventConfig.LUNAR_NEW_YEAR) {
                new Thread(LunarNewYearEventManager.gI(), "Update lunar new year event boss").start();
            }
            new Thread(LuckyNumber.gI(), "Update Lucky Number").start();
            new Thread(DecisionMaker.gI(), "Update Decision Maker").start();
            new Thread(() -> {
                while (isRunning) {
                    try {
                        long st = System.currentTimeMillis();
                        if (config.EventConfig.TOP_SM_EVENT) {
                            ConstDataEventSM.isRunningSK = ConstDataEventSM.isActiveEvent();
                        }
                        if (config.EventConfig.TOP_NAP_EVENT) {
                            ConstDataEventNAP.isRunningSK = ConstDataEventNAP.isActiveEvent();
                        }
                        Functions.sleep(Math.max(500 - (System.currentTimeMillis() - st), 10));

                        TopService.gI().updateTop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, "Update SK").start();

            System.out.println("DEBUG: BotManager init...");
            BotManager.gI().init();
            new Thread(BotManager.gI(), "Bot Manager").start();
            Logger.success(">> SERVER START SUCCESS <<\n");

        } catch (Throwable t) {
            System.err.println("CRITICAL ERROR IN ServerManager.run:");
            t.printStackTrace();
        }
    }

    private void activeServerSocket() {
        try {
            Network.gI().init().setAcceptHandler(new ISessionAcceptHandler() {
                @Override
                public void sessionInit(ISession is) {
                    if (!canConnectWithIp(is.getIP())) {
                        is.disconnect();
                        return;
                    }
                    is.setMessageHandler(Controller.gI())
                            .setSendCollect(new MessageSendCollect() {
                                @Override
                                public void doSendMessage(ISession session, DataOutputStream dos, Message msg)
                                        throws Exception {
                                    try {
                                        byte[] data = msg.getData();
                                        if (session.sentKey()) {
                                            byte b = this.writeKey(session, msg.command);
                                            dos.writeByte(b);
                                        } else {
                                            dos.writeByte(msg.command);
                                        }
                                        if (data != null) {
                                            int size = data.length;
                                            if (msg.command == -32 || msg.command == -33 || msg.command == -66 || msg.command == -74
                                                    || msg.command == 11 || msg.command == -67 || msg.command == -87
                                                    || msg.command == 66) {
                                                byte b2 = this.writeKey(session, (byte) size);
                                                dos.writeByte(b2 - 128);
                                                byte b3 = this.writeKey(session, (byte) (size >> 8));
                                                dos.writeByte(b3 - 128);
                                                byte b4 = this.writeKey(session, (byte) (size >> 16));
                                                dos.writeByte(b4 - 128);
                                            } else if (session.sentKey()) {
                                                byte byte1 = this.writeKey(session, (byte) (size >> 8));
                                                dos.writeByte(byte1);
                                                byte byte2 = this.writeKey(session, (byte) (size & 0xFF));
                                                dos.writeByte(byte2);
                                            } else {
                                                dos.writeShort(size);
                                            }
                                            if (session.sentKey()) {
                                                for (int i = 0; i < data.length; ++i) {
                                                    data[i] = this.writeKey(session, data[i]);
                                                }
                                            }
                                            dos.write(data);
                                        } else {
                                            dos.writeShort(0);
                                        }
                                        dos.flush();
                                        msg.cleanup();
                                    } catch (IOException iOException) {
                                        // empty catch block
                                    }
                                }
                            })
                            .setKeyHandler(new MyKeyHandler())
                            .startCollect();
                }

                @Override
                public void sessionDisconnect(ISession session) {
                    try {
                        if (session == null) {
                            Logger.warning("sessionDisconnect called with null session");
                            return;
                        }
                        if (!(session instanceof MySession)) {
                            Logger.warning("sessionDisconnect received non-MySession: " + session.getClass().getName());
                            session.disconnect();
                            return;
                        }
                        Client.gI().kickSession((MySession) session);
                    } catch (Exception e) {
                        Logger.logException(ServerManager.class, e, "Error during session disconnect");
                    }
                }
            }).setTypeSessioClone(MySession.class)
                    .setDoSomeThingWhenClose(() -> {
                        Logger.error("SERVER CLOSE\n");
                        System.exit(0);
                    })
                    .start(PORT);
        } catch (

        Exception e) {
            Logger.error("Failed to start server network: " + e.getMessage() + "\n");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private boolean canConnectWithIp(String ipAddress) {
        synchronized (CLIENTS) {
            Object o = CLIENTS.get(ipAddress);
            if (o == null) {
                CLIENTS.put(ipAddress, 1);
                return true;
            } else {
                int n = Integer.parseInt(String.valueOf(o));
                if (n < Manager.MAX_PER_IP) {
                    n++;
                    CLIENTS.put(ipAddress, n);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    private void activeCommandLine() {
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String line = sc.nextLine();
                if (line.equals("baotri")) {
                    new Thread(() -> {
                        Maintenance.gI().start(5);
                    }).start();
                } else if (line.equals("athread")) {
                    System.out.println("Số thread hiện tại của Server: " + Thread.activeCount());
                } else if (line.equals("nplayer")) {
                    System.out.println(
                            "Số lượng người chơi hiện tại của Server: " + Client.gI().getPlayers().size());
                } else if (line.equals("shop")) {
                    Manager.gI().updateShop();
                    System.out.println("===========================DONE UPDATE SHOP===========================");
                } else if (line.equals("a")) {
                    new Thread(() -> {
                        Client.gI().close();
                    }).start();
                }
                // === Lệnh quản lý AntiDDoS ===
                else if (line.equals("ddos")) {
                    System.out.println("=== AntiDDoS Status ===");
                    System.out.println("Banned IPs: " + network.server.AntiLoginDDoS.getBannedIPs());
                    System.out.println("DbAsync backlog: " + jdbc.DbAsyncTask.gI().getBacklog() + " tasks");
                } else if (line.startsWith("ban ")) {
                    String ip = line.substring(4).trim();
                    network.server.AntiLoginDDoS.ban(ip, "Admin ban thủ công");
                    System.out.println("Đã ban IP: " + ip);
                } else if (line.startsWith("unban ")) {
                    String ip = line.substring(6).trim();
                    network.server.AntiLoginDDoS.unban(ip);
                    System.out.println("Đã gỡ ban IP: " + ip);
                }
            }
        }, "Active line").start();
    }

    public void disconnect(MySession session) {
        String ip = session.ipAddress;
        if (ip == null) {
            ip = session.getIP();
        }
        if (ip == null) {
            return;
        }
        synchronized (CLIENTS) {
            Object o = CLIENTS.get(ip);
            if (o != null) {
                int n = Integer.parseInt(String.valueOf(o));
                n--;
                if (n <= 0) {
                    CLIENTS.remove(ip);
                } else {
                    CLIENTS.put(ip, n);
                }
            }
        }
    }

    private static String describeSession(ISession session) {
        if (session == null) {
            return "session=null";
        }
        String base = "session@" + Integer.toHexString(System.identityHashCode(session))
                + " ip=" + session.getIP();
        if (!(session instanceof MySession)) {
            return base + " type=" + session.getClass().getName();
        }
        MySession mySession = (MySession) session;
        return base
                + " userId=" + mySession.userId
                + " joined=" + mySession.joinedGame
                + " player=" + (mySession.player != null
                        ? mySession.player.name + "#" + mySession.player.id
                        : "null");
    }

    public void close() {
        shutdownGracefully(true);
    }

    /**
     * Saves persistent game state and tears down connections. Used by maintenance flow and JVM shutdown (SIGTERM).
     *
     * @param exitProcess if {@code true}, runs legacy restart.bat hook and {@link System#exit(int)} (maintenance path).
     *                    If {@code false}, only saves and returns (shutdown-hook path; JVM exit follows naturally).
     */
    private void shutdownGracefully(boolean exitProcess) {
        if (!GRACEFUL_SHUTDOWN_DONE.compareAndSet(false, true)) {
            return;
        }
        isRunning = false;
        try {
            ClanService.gI().close();
        } catch (Exception e) {
            Logger.error("Lỗi save clan!\n");
        }
        try {
            ConsignShopManager.gI().save();
        } catch (Exception e) {
            Logger.error("Lỗi save shop ký gửi!\n");
        }
        Client.gI().close();
        EventDAO.save();

        // === Đảm bảo mọi tác vụ DB async được hoàn tất trước khi tắt ===
        try {
            Logger.warning(">> Đang flush hàng đợi DB async...\n");
            jdbc.DbAsyncTask.gI().forceFlush();
        } catch (Exception e) {
            Logger.error("Lỗi khi flush DbAsyncTask: " + e.getMessage() + "\n");
        }

        Logger.success("SUCCESSFULLY MAINTENANCE!\n");

        if (!exitProcess) {
            return;
        }

        if (IS_LINUX_OR_HEADLESS) {
            try {
                Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c",
                    "sleep 5; ./serverctl.sh start-java >> logs/autorestart.log 2>&1 &"});
            } catch (IOException e) {
                Logger.error("Không thể spawn auto restart: " + e.getMessage() + "\n");
            }
        } else {
            try {
                String batchFilePath = "restart.bat";
                FileRunner.runBatchFile(batchFilePath);
            } catch (IOException e) {
            }
        }
        System.exit(0);
    }
}
