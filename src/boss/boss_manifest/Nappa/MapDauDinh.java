package boss.boss_manifest.Nappa;

/*
 *
 *
 * @author EMTI
 */

import boss.Boss;
import boss.BossID;
import boss.BossesData;
import utils.Util;

public class MapDauDinh extends Boss {

    private long st;

    public MapDauDinh() throws Exception {
        super(BossID.MAP_DAU_DINH, true, true, BossesData.MAP_DAU_DINH);
    }

    @Override
    public void joinMap() {
        super.joinMap();
        st = System.currentTimeMillis();
    }

    @Override
    public void autoLeaveMap() {
        if (this.zone != null && this.zone.hasRealPlayer()) {
            st = System.currentTimeMillis();
        } else if (Util.canDoWithTime(st, 900000)) {
            this.leaveMapForImmediateRespawn();
        }
    }
}
