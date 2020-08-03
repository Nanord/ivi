package com.stm.ivi.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateUtils {

    public static void printExecutionTime(long start) {
        long end = System.currentTimeMillis() - start;
        long millis = end % 1000;
        long second = (end / 1000) % 60;
        long minute = (end / (1000 * 60)) % 60;
        long hour = (end / (1000 * 60 * 60)) % 24;
        String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
        log.info("Execution time: {}", time);
    }

    public static String getCurrentTimeForFolderName() {
        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM_dd_YYYY__HH_mm_ss");
        return dateFormat.format(date);
    }
}
