package jdbc.daos;

import jdbc.DBConnecter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class MapEntityDAO {

    public static List<MapEntityRow> getAllMapEntities() throws Exception {
        List<MapEntityRow> rows = new ArrayList<>();
        String sql = "select id, npcs, mobs, waypoints from map_template order by id asc";
        try (Connection con = DBConnecter.getConnectionServer();
                PreparedStatement ps = con.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new MapEntityRow(
                        rs.getInt("id"),
                        rs.getString("npcs"),
                        rs.getString("mobs"),
                        rs.getString("waypoints")));
            }
        }
        return rows;
    }

    public static class MapEntityRow {

        public final int mapId;
        public final String npcs;
        public final String mobs;
        public final String waypoints;

        public MapEntityRow(int mapId, String npcs, String mobs, String waypoints) {
            this.mapId = mapId;
            this.npcs = npcs;
            this.mobs = mobs;
            this.waypoints = waypoints;
        }
    }
}
