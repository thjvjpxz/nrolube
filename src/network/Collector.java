package network;

import network.inetwork.IMessageHandler;
import network.inetwork.IMessageSendCollect;
import network.inetwork.ISession;
import network.inetwork.ISessionAcceptHandler;
import utils.Logger;

import java.io.DataInputStream;
import java.net.Socket;

public final class Collector implements Runnable {
    private ISession session;
    private DataInputStream dis;
    private IMessageSendCollect collect;
    private IMessageHandler messageHandler;

    public Collector(ISession session, Socket socket) {
        this.session = session;
        setSocket(socket);
    }

    public Collector setSocket(Socket socket) {
        try {
            this.dis = new DataInputStream(socket.getInputStream());
        } catch (Exception ignored) {
        }
        return this;
    }

    public void run() {
        ISession currentSession = this.session;
        try {
            while (currentSession != null && currentSession.isConnected()) {
                Message msg = this.collect.readMessage(currentSession, this.dis);

                if (msg.command == -27) {
                    currentSession.sendKey();
                } else {
                    this.messageHandler.onMessage(currentSession, msg);
                }
                msg.cleanup();
            }
        } catch (Exception ex) {
            try {
                if (currentSession != null) {
                    ISessionAcceptHandler handler = Network.gI().getAcceptHandler();
                    if (handler != null) {
                        handler.sessionDisconnect(currentSession);
                    }
                }
            } catch (Exception exception) {
                Logger.logException(Collector.class, exception,
                    "sessionDisconnect ném -- save logout có thể bị cắt cho "
                        + (currentSession != null ? currentSession.getIP() : "unknown"));
            } finally {
                if (currentSession != null && currentSession.isConnected()) {
                    Logger.warning("Fallback disconnect cho session " + currentSession.getIP()
                        + " sau khi sessionDisconnect thất bại/bị chặn");
                    currentSession.disconnect();
                }
            }
            return;
        }

        if (currentSession != null) {
            try {
                ISessionAcceptHandler handler = Network.gI().getAcceptHandler();
                if (handler != null) {
                    handler.sessionDisconnect(currentSession);
                }
            } catch (Exception exception) {
                Logger.logException(Collector.class, exception,
                    "sessionDisconnect ném (loop exit) cho "
                        + (currentSession != null ? currentSession.getIP() : "unknown"));
            } finally {
                if (currentSession.isConnected()) {
                    Logger.warning("Fallback disconnect cho session " + currentSession.getIP()
                        + " sau khi loop exit");
                    currentSession.disconnect();
                }
            }
        }
    }

    public void setCollect(IMessageSendCollect collect) {
        this.collect = collect;
    }

    public void setMessageHandler(IMessageHandler handler) {
        this.messageHandler = handler;
    }

    public void close() {
        if (this.dis != null) {
            try {
                this.dis.close();
            } catch (Exception ignored) {
            }
        }
    }

    public void dispose() {
        this.session = null;
        this.dis = null;
        this.collect = null;
    }
}
