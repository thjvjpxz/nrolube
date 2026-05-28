package services;

import java.util.ArrayList;
import java.util.List;
import jdbc.daos.MapEntityDAO;
import jdbc.daos.MapEntityDAO.MapEntityRow;
import map.MapEntityData;
import map.MapEntityParseException;
import map.MapEntityParser;
import models.Template.MapTemplate;
import server.Manager;
import utils.Logger;

public class MapEntityReloadService {

    private static MapEntityReloadService instance;
    private final MapEntityParser parser = new MapEntityParser();

    public static MapEntityReloadService gI() {
        if (instance == null) {
            instance = new MapEntityReloadService();
        }
        return instance;
    }

    public RemapResult reload() {
        RemapResult result = new RemapResult();
        List<MapEntityRow> rows;
        try {
            rows = MapEntityDAO.getAllMapEntities();
        } catch (Exception e) {
            result.errorCount++;
            Logger.logException(MapEntityReloadService.class, e, "Lỗi đọc entity map từ map_template");
            return result;
        }
        for (MapEntityRow row : rows) {
            try {
                MapEntityData data = parser.parse(row.mapId, row.npcs, row.mobs, row.waypoints);
                MapTemplate template = findTemplate(data.mapId);
                map.Map liveMap = MapService.gI().getMapById(data.mapId);
                if (template == null || liveMap == null) {
                    throw new IllegalStateException("Không tìm thấy map/template live mapId=" + data.mapId);
                }
                applyToTemplate(template, data);
                applyToLiveMap(liveMap, data);
                result.mapCount++;
                result.npcCount += data.npcId.length;
                result.mobCount += data.mobTemp.length;
                result.waypointCount += data.wayPoints.size();
            } catch (MapEntityParseException e) {
                result.errorCount++;
            } catch (Exception e) {
                result.errorCount++;
                Logger.logException(MapEntityReloadService.class, e, "Lỗi reload entity mapId=" + row.mapId);
            }
        }
        return result;
    }

    private void applyToTemplate(MapTemplate template, MapEntityData data) {
        template.npcId = data.npcId;
        template.npcX = data.npcX;
        template.npcY = data.npcY;
        template.mobTemp = data.mobTemp;
        template.mobLevel = data.mobLevel;
        template.mobHp = data.mobHp;
        template.mobX = data.mobX;
        template.mobY = data.mobY;
        template.wayPoints = new ArrayList<>(data.wayPoints);
    }

    private void applyToLiveMap(map.Map liveMap, MapEntityData data) {
        liveMap.wayPoints = new ArrayList<>(data.wayPoints);
        int npcCreated = liveMap.reloadNpc(data.npcId, data.npcX, data.npcY);
        int mobInstances = liveMap.reloadMob(data.mobTemp, data.mobLevel, data.mobHp, data.mobX, data.mobY);
        Logger.warning("[remap] mapId=" + data.mapId
                + " reload npc=" + npcCreated
                + ", mobSpawn=" + data.mobTemp.length
                + ", mobInstance=" + mobInstances
                + ", waypoint=" + data.wayPoints.size()
                + ". Nên chạy lúc ít người nếu map đang combat.\n");
    }

    private MapTemplate findTemplate(int mapId) {
        if (Manager.MAP_TEMPLATES == null) {
            return null;
        }
        for (MapTemplate template : Manager.MAP_TEMPLATES) {
            if (template != null && template.id == mapId) {
                return template;
            }
        }
        return null;
    }

    public static class RemapResult {

        public int mapCount;
        public int npcCount;
        public int mobCount;
        public int waypointCount;
        public int errorCount;

        public String toMessage() {
            return "Đã reload map entity: map=" + mapCount
                    + ", npc=" + npcCount
                    + ", mob=" + mobCount
                    + ", waypoint=" + waypointCount
                    + ", lỗi=" + errorCount;
        }
    }
}
