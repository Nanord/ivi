package com.stm.megogo.utils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegExpHelper {

    public static Optional<String> findMatcherString(Pattern pattern, String text, Integer group) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group(group));
        }
        return Optional.empty();
    }

    public static Optional<String> findMatcherString(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
            return Optional.ofNullable(matcher.group());
        }
        return Optional.empty();
    }

    public static Optional<String> findMatcherString(String regExp, String text, Integer group) {
        return findMatcherString(Pattern.compile(regExp), text, group);
    }

    public static Optional<String> findMatcherString(String regExp, String text) {
        return findMatcherString(Pattern.compile(regExp), text);
    }

}
