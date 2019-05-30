package com.togocourier.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeAgo {

    private static final Map<String, Long> times = new LinkedHashMap<>();

    static {
        times.put("yr", TimeUnit.DAYS.toMillis(365));
        times.put("mon", TimeUnit.DAYS.toMillis(30));
        times.put("week", TimeUnit.DAYS.toMillis(7));
        times.put("day", TimeUnit.DAYS.toMillis(1));
        times.put("hr", TimeUnit.HOURS.toMillis(1));
        times.put("min", TimeUnit.MINUTES.toMillis(1));
        times.put("sec", TimeUnit.SECONDS.toMillis(1));
    }

    private static String toRelative(long duration, int maxLevel) {
        StringBuilder res = new StringBuilder();
        int level = 0;
        for (Map.Entry<String, Long> time : times.entrySet()) {
            long timeDelta = duration / time.getValue();
            if (timeDelta > 0) {
                res.append(timeDelta)
                        .append(" ")
                        .append(time.getKey())
                        .append(timeDelta > 1 ? "s" : "")
                        .append(", ");
                // duration -= time.getValue() * timeDelta;
                // level++;
                break;
            }
            if (level == maxLevel) {
                break;
            }
        }
        if ("".equals(res.toString())) {
            return "0 sec ago";
        } else {
            res.setLength(res.length() - 2);
            res.append(" ago");
            return res.toString();
        }
    }

    public static String toRelative(long duration) {
        return toRelative(duration, times.size());
    }

    public static String toRelative(String start, String end) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            Date startDate = simpleDateFormat.parse(start);
            Date endDate = simpleDateFormat.parse(end);

            assert startDate.after(endDate);
            return toRelative(endDate.getTime() - startDate.getTime());
        } catch (Exception e) {
            return null;
        }
    }

    public static String toRelative(long start, long end) {
        return toRelative(end-start);
    }

    public static String toRelative(Date start, Date end, int level) {
        assert start.after(end);
        return toRelative(end.getTime() - start.getTime(), level);
    }
}