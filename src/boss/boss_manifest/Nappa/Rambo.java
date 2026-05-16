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

public class Rambo extends Boss {

    private long st;

    public Rambo() throws Exception {
        super(BossID.RAMBO, true, true, BossesData.RAMBO);
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
