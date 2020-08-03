package com.stm.ivi.utils;

public class Constants {
    public static final String BASE_URL = "https://www.ivi.ru";

    // Айдишник категории фильмов = 14.
    // https://api.ivi.ru/mobileapi/categories/v6?app_version=870
    public static final String API_MOVIES_URL = "https://api.ivi.ru/mobileapi/catalogue/v5/?category=14&from=%s&to=9999999&withpreorderable=true&app_version=870&session=%s";
    public static final String API_CATEGORIES_AND_GENRES_URL = "https://api.ivi.ru/mobileapi/categories/v6?app_version=870&session=%s";
    public static final String API_COUNTRY_URL = "https://api.ivi.ru/mobileapi/countries/v6/?app_version=870&session=%s";

    public static final String DEFAULT_VALUE = "default";

    public final static String LINE_SEPARATOR = System.getProperty("line.separator");
    public final static char CELL_SEPARATOR = ';';
}
