package mob;

import utils.Util;

/**
 * Option roll cho MobReward.
 * Fixed: id + param co dinh.
 * Random: id + min/max, resolveParam() random trong khoang.
 */
public class MobRewardOption {

    public int id;
    public Integer param;
    public Integer min;
    public Integer max;

    public boolean isRandom() {
        return min != null && max != null;
    }

    public boolean isValid() {
        if (id < 0) {
            return false;
        }
        if (isRandom()) {
            return true;
        }
        return param != null;
    }

    /**
     * Resolve param khi tao item.
     * Neu random: Util.nextInt theo min/max, tu dong swap neu min > max de khong crash.
     * Neu fixed: tra ve param.
     */
    public int resolveParam() {
        if (isRandom()) {
            int lo = min;
            int hi = max;
            if (lo > hi) {
                int tmp = lo;
                lo = hi;
                hi = tmp;
            }
            return Util.nextInt(lo, hi);
        }
        return param != null ? param : 0;
    }
}
