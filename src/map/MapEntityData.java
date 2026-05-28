package map;

import java.util.List;

public class MapEntityData {

    public final int mapId;
    public final byte[] npcId;
    public final short[] npcX;
    public final short[] npcY;
    public final byte[] mobTemp;
    public final byte[] mobLevel;
    public final int[] mobHp;
    public final short[] mobX;
    public final short[] mobY;
    public final List<WayPoint> wayPoints;

    public MapEntityData(
            int mapId,
            byte[] npcId,
            short[] npcX,
            short[] npcY,
            byte[] mobTemp,
            byte[] mobLevel,
            int[] mobHp,
            short[] mobX,
            short[] mobY,
            List<WayPoint> wayPoints) {
        this.mapId = mapId;
        this.npcId = npcId;
        this.npcX = npcX;
        this.npcY = npcY;
        this.mobTemp = mobTemp;
        this.mobLevel = mobLevel;
        this.mobHp = mobHp;
        this.mobX = mobX;
        this.mobY = mobY;
        this.wayPoints = wayPoints;
    }
}
