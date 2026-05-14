package jdbc.daos;

import jdbc.DBConnecter;
import java.sql.Timestamp;

public class AccountDAO {

    public static int markLogin(int accountId, int serverId, String ipAddress, Timestamp loginAt) throws Exception {
        return DBConnecter.executeUpdate(
                "update account set server_login = ?, last_time_login = ?, ip_address = ? where id = ?",
                serverId, loginAt, ipAddress, accountId);
    }

    public static int markLogout(int accountId, Timestamp logoutAt) throws Exception {
        return DBConnecter.executeUpdate(
                "update account set server_login = -1, last_time_logout = ? where id = ?",
                logoutAt, accountId);
    }

    public static int resetServerLogin(int accountId) throws Exception {
        return DBConnecter.executeUpdate(
                "update account set server_login = -1 where id = ?",
                accountId);
    }
}
