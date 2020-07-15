package ru.mts.megogo.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CommonUtils {

    /**
     * Получить число из строки
     *
     * @param str строка с числом
     * @return число, либо null, если не удалось выполнить перевод в число
     */
    public static Float getNumber(String str) {
        if (str == null) {
            return null;
        }
        Matcher matcher = Pattern.compile("(\\d[\\d\\s]*)([.,]\\d+)?").matcher(str);
        if (!matcher.find()) {
            return null;
        }
        String numberString = matcher.group().replaceAll(",", ".").replaceAll(" ", "");
        Float number = null;
        try {
            number = Float.parseFloat(numberString);
        } catch (NumberFormatException ex) {
            log.error("Cannot parse float from string: " + str, ex);
        }
        return number;
    }

    /**
     * Получить число из строки строкой
     *
     * @param str строка с числом
     * @return строка с числом, либо null, если не удалось выполнить перевод в число
     */
    public static String getStringNumber(String str) {
        return Optional.ofNullable(getNumber(str))
                .map(String::valueOf)
                .map(record -> record.replaceAll("\\.0$", ""))
                .orElse(null);
    }

}
