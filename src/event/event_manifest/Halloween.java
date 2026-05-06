package event.event_manifest;

/**
 *
 * @author EMTI
 */

import boss.BossID;
import event.Event;

public class Halloween extends Event {

    @Override
    public void boss() {
        createBoss(BossID.BIMA, 10);
        createBoss(BossID.MATROI, 10);
        createBoss(BossID.DOI, 10);
    }
}
