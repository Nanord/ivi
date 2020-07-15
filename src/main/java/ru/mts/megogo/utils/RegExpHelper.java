package ru.mts.megogo.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RegExpHelper {

    public static String buildRegExp(Collection<String> regExpList) {
        return regExpList.stream().map(str -> "(" +str + ")").collect(Collectors.joining("|"));
    }

    public static boolean find(String regExp, String text) {
        return Pattern.compile(regExp).matcher(text).find();
    }

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

    public static List<String> findAllMatchersString(String regExp, String text) {
        return findAllMatchersString(Pattern.compile(regExp), text);
    }

    public static List<String> findAllMatchersString(Pattern pattern, String text) {
        List<String> result = new ArrayList<>();
        Matcher matcher = pattern.matcher(text);
        for(int i = 1; matcher.find(); i++) {
            for(int j = 0; j <= matcher.groupCount(); j++) {
                result.add(matcher.group(j));
            }
        }
        return result;
    }
}
