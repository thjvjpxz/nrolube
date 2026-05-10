package server.io;

/*
 *
 *
 * @author EMTI
 */

import java.net.Socket;

import player.Player;
import server.Controller;
import data.DataGame;
import jdbc.daos.NDVSqlFetcher;
import item.Item;
import java.io.IOException;
import network.Session;
import network.Message;
import server.Client;
import server.Maintenance;
import server.Manager;
import models.AntiLogin;
import services.Service;
import utils.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utils.TimeUtil;
import utils.Util;

public class MySession extends Session {

    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();
    public Player player;

    public byte timeWait = 100;
    public boolean sentKey;

    public static final byte[] KEYS = { 0 };
    public byte curR, curW;

    public String ipAddress;
    public String deviceId;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;
    public byte zoomLevel;

    public long lastTimeLogout;
    public boolean joinedGame;

    public long lastTimeReadMessage;

    public boolean actived;

    public boolean check;

    public int goldBar;
    public long gold;
    public int eventPoint;
    public List<Item> itemsReward;
    public String dataReward;
    public boolean is_gift_box;
    public double bdPlayer;

    public int version;
    public int cash;
    public int danap;
    public int vip;
    public int luotquay;

    public boolean finishUpdate;

    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            this.sendMessage(msg);
            msg.cleanup();
            sentKey = true;
        } catch (IOException e) {
        }
    }

    public void login(String username, String password) {
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.gI().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
        if (Manager.LOCAL) {
            Service.gI().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (Maintenance.isRunning) {
            Service.gI().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }
        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.MAX_PLAYER) {
            Service.gI().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player == null) {
            NDVSqlFetcher.AccountLoginOutcome loginOutcome = null;
            try {
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;
                loginOutcome = NDVSqlFetcher.login(this, al);
                if (loginOutcome.player != null) {
                    bindLoadedPlayerThenBroadcastLogin(loginOutcome.player, st);
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (loginOutcome != null && loginOutcome.player != null) {
                    Player pl = loginOutcome.player;
                    // Đã bind session/player hoặc đã put → kickSession (cleanup zone + Client); không thì dispose tay.
                    if (this.player == pl || Client.gI().getPlayer(pl.id) == pl) {
                        Client.gI().kickSession(this);
                    } else {
                        pl.dispose();
                    }
                }
            } finally {
                // heldAccountGate != null: lock vẫn giữ sau NDVSqlFetcher.login — mở sau put/bind, cùng thread.
                if (loginOutcome != null && loginOutcome.heldAccountGate != null) {
                    loginOutcome.heldAccountGate.unlock();
                }
            }
        }
    }

    // Đưa player đã load vào Client + gửi gói vào game; phải gọi trong cùng stack với NDVSqlFetcher.login khi có heldAccountGate.
    private void bindLoadedPlayerThenBroadcastLogin(Player pl, long startMs) {
        DataGame.sendSmallVersion(this);
        DataGame.sendBgItemVersion(this);

        pl.setSession(this);
        this.player = pl;

        this.timeWait = 0;
        this.joinedGame = true;
        pl.nPoint.calPoint();
        pl.nPoint.setHp(Util.maxIntValue(pl.nPoint.hp));
        pl.nPoint.setMp(Util.maxIntValue(pl.nPoint.mp));
        pl.zone.addPlayer(pl);
        if (pl.pet != null) {
            pl.pet.nPoint.calPoint();
            pl.pet.nPoint.setHp(Util.maxIntValue(pl.nPoint.hp));
            pl.pet.nPoint.setMp(Util.maxIntValue(pl.nPoint.mp));
        }

        Client.gI().put(pl);
        DataGame.sendVersionGame(this);
        DataGame.sendDataItemBG(this);
        Controller.gI().sendInfo(this);
        Logger.warning(
                TimeUtil.getCurrHour() + "h" + TimeUtil.getCurrMin() + "m: Thành công đăng nhập người chơi "
                        + this.player.name + ": " + (System.currentTimeMillis() - startMs) + " ms\n");
        if (this.player.notify != null && !this.player.notify.equals("null")
                && !this.player.notify.isEmpty() && this.player.notify.length() > 0) {
            Service.gI().sendThongBao(this.player, this.player.notify);
            this.player.notify = null;
        }
    }

    @Override
    public void disconnect() {
        super.disconnect();
        if (this.deviceId != null && !this.deviceId.equals("UNKNOWN_DEVICE")) {
            if (network.server.EMTIServer.deviceFirewall.containsKey(this.deviceId)) {
                int devCount = network.server.EMTIServer.deviceFirewall.get(this.deviceId);
                if (devCount > 0) {
                    network.server.EMTIServer.deviceFirewall.put(this.deviceId, devCount - 1);
                } else {
                    network.server.EMTIServer.deviceFirewall.remove(this.deviceId);
                }
            }
        }
    }
}
