package server;

/*
 *
 *
 * @author EMTI
 */
import EMTI.Functions;
import jdbc.DBConnecter;
import jdbc.daos.PlayerDAO;
import map.ItemMap;
import map.Zone;
import player.LinhDanhThue;
import player.Player;
import network.SessionManager;
import network.inetwork.ISession;
import server.io.MySession;
import services.Service;
import services.func.ChangeMapService;
import services.func.SummonDragon;
import services.func.TransactionService;
import services.NgocRongNamecService;
import utils.Logger;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import models.DragonNamecWar.TranhNgoc;
import services.func.SummonDragonNamek;
import utils.Util;

public class Client implements Runnable {

    private static Client instance;

    private final Map<Long, Player> players_id = new HashMap<>();
    private final Map<Integer, Player> players_userId = new HashMap<>();
    private final Map<String, Player> players_name = new HashMap<>();
    private final List<Player> players = new ArrayList<>();

    /**
     * Serialize disconnect/save vs login/load per account.
     * Map phình theo số userId từng xuất hiện (không remove entry); private server thường chấp nhận được.
     */
    private static final ConcurrentHashMap<Integer, ReentrantLock> ACCOUNT_LOGIN_LOCKS = new ConcurrentHashMap<>();

    public static ReentrantLock accountLock(int userId) {
        return ACCOUNT_LOGIN_LOCKS.computeIfAbsent(userId, id -> new ReentrantLock());
    }

    private Client() {
        new Thread(this, "Update Client").start();
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public static Client gI() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }

    public void put(Player player) {
        if (!players_id.containsKey(player.id)) {
            this.players_id.put(player.id, player);
        }
        if (!players_name.containsValue(player)) {
            this.players_name.put(player.name, player);
        }
        if (!players_userId.containsValue(player)) {
            this.players_userId.put(player.getSession().userId, player);
        }
        if (!players.contains(player)) {
            this.players.add(player);
        }

    }

    private void remove(MySession session) {
        if (session.player != null) {
            Player p = session.player;
            this.remove(p);
            p.dispose();
            session.player = null;
        }
        if (session.joinedGame) {
            session.joinedGame = false;
            try {
                DBConnecter.executeUpdate("update account set last_time_logout = ? where id = ?",
                        new Timestamp(System.currentTimeMillis()), session.userId);
            } catch (Exception e) {
                Logger.logException(Client.class, e);
            }
        }
        ServerManager.gI().disconnect(session);
    }

    private void remove(Player player) {
        // Snapshot trước mọi thao tác có thể làm mất name/session (dispose offline / dispose session).
        long indexPlayerId = player.id;
        String indexName = player.name;
        int indexUserId = player.getSession() != null ? player.getSession().userId : -1;
        // Gỡ khỏi index online trước cleanup/save: tránh hệ thống khác coi player còn "online"
        // trong lúc exitMap/trade/clan... Chống đọc DB cũ khi reconnect không dựa vào việc giữ index
        // mà nhờ accountLock trong kickSession (userId>0) serialize với NDVSqlFetcher.login.
        removeFromIndexes(indexPlayerId, indexName, indexUserId, player);
        try {
            runLogoutCleanup(player);
        } catch (Exception e) {
            Logger.logException(Client.class, e,
                    "Cleanup logout fail (player=" + player.name + "), vẫn tiếp tục save");
        } finally {
            try {
                PlayerDAO.updatePlayer(player);
            } finally {
                player.linhDanhThueList.clear();
            }
        }
    }

    private void removeFromIndexes(long playerId, String characterName, int accountUserId, Player playerRef) {
        this.players_id.remove(playerId);
        if (characterName != null) {
            this.players_name.remove(characterName);
        } else {
            this.players_name.values().remove(playerRef);
        }
        if (accountUserId > 0) {
            this.players_userId.remove(accountUserId);
        } else {
            this.players_userId.values().remove(playerRef);
        }
        this.players.remove(playerRef);
    }

    private void runLogoutCleanup(Player player) {
        if (player.beforeDispose) {
            return;
        }
        player.beforeDispose = true;
        player.mapIdBeforeLogout = currentMapIdOrHome(player);
        TranhNgoc.gI().removePlayersBlue(player);
        TranhNgoc.gI().removePlayersRed(player);
        if (player.idNRNM != -1) {
            if (player.zone != null) {
                ItemMap itemMap = new ItemMap(player.zone, player.idNRNM, 1, player.location.x, player.location.y, -1);
                Service.gI().dropItemMap(player.zone, itemMap);
            }
            // Clear global state phải luôn chạy: reInitNgocRongNamec() chỉ respawn slot có pNrNamec[i]=="",
            // nếu skip do zone null thì viên ngọc bị orphan tới khi reset server.
            NgocRongNamecService.gI().pNrNamec[player.idNRNM - 353] = "";
            NgocRongNamecService.gI().idpNrNamec[player.idNRNM - 353] = -1;
            player.idNRNM = -1;
        }
        // exitMap đụng nhiều field nullable (pvp, effectSkill, effectSkin, iDMark) — cô lập lỗi
        // ở đây để forceDetachFromZone bên dưới luôn chạy, không để Zone giữ player đã dispose.
        try {
            ChangeMapService.gI().exitMap(player);
        } catch (Exception e) {
            Logger.logException(Client.class, e,
                    "exitMap fail (player=" + player.name + "), force detach zone");
        }
        forceDetachFromZone(player);
        TransactionService.gI().cancelTrade(player);
        if (player.clan != null) {
            player.clan.removeMemberOnline(null, player);
        }
        if (SummonDragon.gI().playerSummonShenron != null
                && SummonDragon.gI().playerSummonShenron.id == player.id) {
            SummonDragon.gI().isPlayerDisconnect = true;
        }
        if (SummonDragonNamek.gI().playerSummonShenron != null
                && SummonDragonNamek.gI().playerSummonShenron.id == player.id) {
            SummonDragonNamek.gI().isPlayerDisconnect = true;
        }
        if (player.shenronEvent != null) {
            player.shenronEvent.isPlayerDisconnect = true;
        }
        if (player.mobMe != null) {
            player.mobMe.mobMeDie();
        }
        if (player.pet != null) {
            if (player.pet.mobMe != null) {
                player.pet.mobMe.mobMeDie();
            }
            ChangeMapService.gI().exitMap(player.pet);
        }
        // Giữ nguyên list lính đánh thuê cho bước save kế tiếp; một lính lỗi không được làm dừng vòng.
        for (int i = player.linhDanhThueList.size() - 1; i >= 0; i--) {
            try {
                LinhDanhThue ldt = player.linhDanhThueList.get(i);
                if (ldt != null && ldt.zone != null) {
                    ChangeMapService.gI().exitMap(ldt);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private int currentMapIdOrHome(Player player) {
        if (player.zone != null && player.zone.map != null) {
            return player.zone.map.mapId;
        }
        return player.gender + 21;
    }

    // Đảm bảo Zone không giữ reference tới player sắp dispose, kể cả khi exitMap nổ giữa chừng.
    private void forceDetachFromZone(Player player) {
        Zone zone = player.zone;
        if (zone == null) {
            return;
        }
        try {
            zone.removePlayer(player);
        } catch (Exception ignored) {
        }
        player.zone = null;
    }

    public void kickSession(MySession session) {
        if (session == null) {
            return;
        }
        int uid = session.userId;
        if (uid <= 0) {
            session.disconnect();
            this.remove(session);
            return;
        }
        ReentrantLock lock = accountLock(uid);
        lock.lock();
        try {
            session.disconnect();
            this.remove(session);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Đá người đang trong index online: có session → kickSession; ghost (session null, ví dụ sau bug cũ) → remove(player).
     */
    public void kickOnlinePlayer(Player player) {
        if (player == null) {
            return;
        }
        if (player.getSession() != null) {
            kickSession((MySession) player.getSession());
        } else {
            remove(player);
        }
    }

    public Player getPlayer(long playerId) {
        return this.players_id.get(playerId);
    }

    public Player getRandPlayer() {
        if (this.players.isEmpty()) {
            return null;
        }
        return this.players.get(Util.nextInt(players.size()));
    }

    public Player getPlayerByUser(int userId) {
        return this.players_userId.get(userId);
    }

    public Player getPlayer(String name) {
        return this.players_name.get(name);
    }

    public Player getPlayerByID(int playerId) {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player != null && player.id == playerId) {
                return player;
            }
        }
        return null;
    }

    public void close() {
        Logger.log(Logger.YELLOW, "BEGIN KICK OUT SESSION " + players.size() + "\n");
        while (!players.isEmpty()) {
            Player pl = players.remove(0);
            if (pl != null && pl.getSession() != null) {
                this.kickSession((MySession) pl.getSession());
            }
        }
        Logger.success("SUCCESSFUL\n");
    }

    private void update() {
        for (int i = SessionManager.gI().getSessions().size() - 1; i >= 0; i--) {
            try {
                if (i < SessionManager.gI().getSessions().size()) {
                    ISession s = SessionManager.gI().getSessions().get(i);
                    MySession session = (MySession) s;
                    if (session == null) {
                        SessionManager.gI().getSessions().remove(i);
                        continue;
                    }
                    if (session.timeWait > 0) {
                        session.timeWait--;
                        if (session.timeWait == 0) {
                            kickSession(session);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore concurrent modification exception
            }
        }
    }

    @Override
    public void run() {
        while (ServerManager.isRunning) {
            long st = System.currentTimeMillis();
            try {
                update();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Functions.sleep(Math.max(1000 - (System.currentTimeMillis() - st), 10));
        }
    }

    public void show(Player player) {
        String txt = "";
        txt += "sessions: " + SessionManager.gI().getNumSession() + "\n";
        txt += "players_id: " + players_id.size() + "\n";
        txt += "players_userId: " + players_userId.size() + "\n";
        txt += "players_name: " + players_name.size() + "\n";
        txt += "players: " + players.size() + "\n";
        Service.gI().sendThongBao(player, txt);
    }
}
