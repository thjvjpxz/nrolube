package map;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import utils.Logger;

public class MapEntityParser {

    private static final int NPC_FIELD_COUNT = 3;
    private static final int MOB_FIELD_COUNT = 5;
    private static final int WAYPOINT_FIELD_COUNT = 10;

    public MapEntityData parse(int mapId, String npcs, String mobs, String waypoints)
            throws MapEntityParseException {
        NpcParseResult npcResult = parseNpcs(mapId, npcs);
        MobParseResult mobResult = parseMobs(mapId, mobs);
        List<WayPoint> parsedWaypoints = parseWaypoints(mapId, waypoints);
        return new MapEntityData(
                mapId,
                npcResult.npcId,
                npcResult.npcX,
                npcResult.npcY,
                mobResult.mobTemp,
                mobResult.mobLevel,
                mobResult.mobHp,
                mobResult.mobX,
                mobResult.mobY,
                parsedWaypoints);
    }

    private NpcParseResult parseNpcs(int mapId, String raw) throws MapEntityParseException {
        JSONArray dataArray = parseColumn(mapId, "npcs", raw, normalizeQuotedArray(raw));
        byte[] npcId = new byte[dataArray.size()];
        short[] npcX = new short[dataArray.size()];
        short[] npcY = new short[dataArray.size()];
        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray entry = parseEntry(mapId, "npcs", dataArray.get(i), NPC_FIELD_COUNT);
            npcId[i] = parseByte(mapId, "npcs", entry.get(0));
            npcX[i] = parseShort(mapId, "npcs", entry.get(1));
            npcY[i] = parseShort(mapId, "npcs", entry.get(2));
        }
        return new NpcParseResult(npcId, npcX, npcY);
    }

    private MobParseResult parseMobs(int mapId, String raw) throws MapEntityParseException {
        JSONArray dataArray = parseColumn(mapId, "mobs", raw, normalizeQuotedArray(raw));
        byte[] mobTemp = new byte[dataArray.size()];
        byte[] mobLevel = new byte[dataArray.size()];
        int[] mobHp = new int[dataArray.size()];
        short[] mobX = new short[dataArray.size()];
        short[] mobY = new short[dataArray.size()];
        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray entry = parseEntry(mapId, "mobs", dataArray.get(i), MOB_FIELD_COUNT);
            mobTemp[i] = parseByte(mapId, "mobs", entry.get(0));
            mobLevel[i] = parseByte(mapId, "mobs", entry.get(1));
            mobHp[i] = parseInt(mapId, "mobs", entry.get(2));
            mobX[i] = parseShort(mapId, "mobs", entry.get(3));
            mobY[i] = parseShort(mapId, "mobs", entry.get(4));
        }
        return new MobParseResult(mobTemp, mobLevel, mobHp, mobX, mobY);
    }

    private List<WayPoint> parseWaypoints(int mapId, String raw) throws MapEntityParseException {
        JSONArray dataArray = parseColumn(mapId, "waypoints", raw, normalizeWaypointArray(raw));
        List<WayPoint> wayPoints = new ArrayList<>();
        for (int i = 0; i < dataArray.size(); i++) {
            JSONArray entry = parseEntry(mapId, "waypoints", dataArray.get(i), WAYPOINT_FIELD_COUNT);
            WayPoint wp = new WayPoint();
            wp.name = String.valueOf(entry.get(0));
            wp.minX = parseShort(mapId, "waypoints", entry.get(1));
            wp.minY = parseShort(mapId, "waypoints", entry.get(2));
            wp.maxX = parseShort(mapId, "waypoints", entry.get(3));
            wp.maxY = parseShort(mapId, "waypoints", entry.get(4));
            wp.isEnter = parseBooleanFlag(mapId, "waypoints", entry.get(5));
            wp.isOffline = parseBooleanFlag(mapId, "waypoints", entry.get(6));
            wp.goMap = parseShort(mapId, "waypoints", entry.get(7));
            wp.goX = parseShort(mapId, "waypoints", entry.get(8));
            wp.goY = parseShort(mapId, "waypoints", entry.get(9));
            wayPoints.add(wp);
        }
        return wayPoints;
    }

    private JSONArray parseColumn(int mapId, String column, String... candidates) throws MapEntityParseException {
        for (String candidate : candidates) {
            Object parsed = JSONValue.parse(candidate == null || candidate.isBlank() ? "[]" : candidate);
            if (parsed instanceof JSONArray dataArray) {
                return dataArray;
            }
        }
        throw fail(mapId, column, "không phải mảng JSON");
    }

    private JSONArray parseEntry(int mapId, String column, Object value, int minSize)
            throws MapEntityParseException {
        Object parsed = value instanceof JSONArray ? value : JSONValue.parse(String.valueOf(value));
        if (!(parsed instanceof JSONArray entry)) {
            throw fail(mapId, column, "entry không phải mảng");
        }
        if (entry.size() < minSize) {
            throw fail(mapId, column, "entry thiếu field: " + entry);
        }
        return entry;
    }

    private byte parseByte(int mapId, String column, Object value) throws MapEntityParseException {
        int parsed = parseInt(mapId, column, value);
        if (parsed < Byte.MIN_VALUE || parsed > Byte.MAX_VALUE) {
            throw fail(mapId, column, "giá trị vượt byte: " + value);
        }
        return (byte) parsed;
    }

    private short parseShort(int mapId, String column, Object value) throws MapEntityParseException {
        int parsed = parseInt(mapId, column, value);
        if (parsed < Short.MIN_VALUE || parsed > Short.MAX_VALUE) {
            throw fail(mapId, column, "giá trị vượt short: " + value);
        }
        return (short) parsed;
    }

    private int parseInt(int mapId, String column, Object value) throws MapEntityParseException {
        try {
            if (value instanceof Number number) {
                return number.intValue();
            }
            return Integer.parseInt(String.valueOf(value));
        } catch (Exception e) {
            throw fail(mapId, column, "giá trị không phải số: " + value, e);
        }
    }

    private boolean parseBooleanFlag(int mapId, String column, Object value) throws MapEntityParseException {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return parseByte(mapId, column, value) == 1;
    }

    private String normalizeQuotedArray(String raw) {
        return raw == null ? "[]" : raw.replaceAll("\\\"", "");
    }

    private String normalizeWaypointArray(String raw) {
        if (raw == null) {
            return "[]";
        }
        return raw.replaceAll("\\[\"\\[", "[[")
                .replaceAll("\\]\"\\]", "]]")
                .replaceAll("\",\"", ",");
    }

    private MapEntityParseException fail(int mapId, String column, String message) {
        Logger.warning("[remap] Parse lỗi mapId=" + mapId + ", column=" + column + ": " + message + "\n");
        return new MapEntityParseException("mapId=" + mapId + ", column=" + column + ": " + message);
    }

    private MapEntityParseException fail(int mapId, String column, String message, Exception cause) {
        Logger.warning("[remap] Parse lỗi mapId=" + mapId + ", column=" + column + ": " + message + "\n");
        return new MapEntityParseException("mapId=" + mapId + ", column=" + column + ": " + message, cause);
    }

    private static class NpcParseResult {

        private final byte[] npcId;
        private final short[] npcX;
        private final short[] npcY;

        private NpcParseResult(byte[] npcId, short[] npcX, short[] npcY) {
            this.npcId = npcId;
            this.npcX = npcX;
            this.npcY = npcY;
        }
    }

    private static class MobParseResult {

        private final byte[] mobTemp;
        private final byte[] mobLevel;
        private final int[] mobHp;
        private final short[] mobX;
        private final short[] mobY;

        private MobParseResult(byte[] mobTemp, byte[] mobLevel, int[] mobHp, short[] mobX, short[] mobY) {
            this.mobTemp = mobTemp;
            this.mobLevel = mobLevel;
            this.mobHp = mobHp;
            this.mobX = mobX;
            this.mobY = mobY;
        }
    }
}
