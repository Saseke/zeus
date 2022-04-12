package com.songmengyuan.zeus.common.config.util;

public class TrafficUtil {
    private static final Long BYTE_UNIT_MAX = 1024L;
    private static final Long KB_UNIT_MAX = (long)(1024 * 1024);
    private static final Long MB_UNIT_MAX = (long)1024 * KB_UNIT_MAX;
    private static final Long GB_UNIT_MAX = (long)1024 * MB_UNIT_MAX;

    /**
     * 计算流量数据的大小，返回合适的数据
     */
    public static String calculateTraffic(double traffic) {
        double bak = traffic;
        if (traffic < BYTE_UNIT_MAX) {
            return traffic + " byte";
        } else if (traffic < KB_UNIT_MAX) {
            bak = bak / 1024;
            return bak + " kb";
        } else if (traffic < MB_UNIT_MAX) {
            bak = bak / 1024;
            return bak + " mb";
        } else {
            bak = bak / 1024;
            return bak + " gb";
        }
    }
}
