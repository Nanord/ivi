package com.stm.ivi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stm.ivi.parser.ParserService;
import com.stm.ivi.pojo.Film;
import com.stm.ivi.service.SaveFile;
import com.stm.ivi.utils.Constants;
import com.stm.ivi.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@ConditionalOnProperty(value = "save.type", havingValue = "csv", matchIfMissing = true)
public class CsvSaveFile implements SaveFile {

    private static final String OUT_FOLDER = "parsed";
    private static final String FILE_NAME = "ivi.csv";
    private static final Map<String, String> MAP_REPLACE_EXCESS_WORD_FOR_REPORTS = new HashMap<String, String>() {{
        put("₽", "р.");
        put(";", "");
        put("\n", " ");
        put("É", "E");
        put("ô", "o");
    }};

    @Value("${output.path}")
    private String outputPathProperty;

    private String outputPath;

    @Autowired
    private ParserService parserService;

    private TypeReference<List<Film>> urlPropsTypeReference = new TypeReference<List<Film>>() {};

    @PostConstruct
    private void init() throws ConfigurationException {
        if (StringUtils.isEmpty(outputPathProperty)) {
            throw new ConfigurationException("output.path is empty!");
        }
        String startParserTime = DateUtils.getCurrentTimeForFolderName();
        outputPath = MessageFormat.format(
                "{0}{1}{2}_{3}{4}",
                outputPathProperty,
                File.separator,
                OUT_FOLDER,
                startParserTime,
                File.separator);
        File directory = new File(outputPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        if (!directory.isDirectory()) {
            throw new ConfigurationException("Please set a correct directory name");
        }
        writeDataToFile(receiveHeader(), outputPath);
    }


    @Override
    public void save(Film film) {
        if (Objects.isNull(film)) {
            log.warn("Film is null during save");
            return;
        }
        log.info("Save film: {}", film.getUrl());
        try {
            writeDataToFile(mapFilmToCSV(film), outputPath);
        } catch (Exception e) {
            log.error("Exception in save file", e);
        }

    }

    private void writeDataToFile(String data, String outputPath) {
        if(StringUtils.isEmpty(data)) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(outputPath + FILE_NAME, true);
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException e) {
            log.error("Error writing file!", e);
        }
    }

    private String receiveHeader() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id;");
        stringBuilder.append("Название на русском;");
        stringBuilder.append("Название на языке оригинала;");
        stringBuilder.append("Год;");
        stringBuilder.append("Страна;");
        stringBuilder.append("Жанр;");
        stringBuilder.append("Рейтинг на Кинопоиске;");
        stringBuilder.append("Рейтинг на IMDb;");
        stringBuilder.append("Сборы фильма в мире;");
        stringBuilder.append("Сборы фильма в России;");
        stringBuilder.append("Режиссер;");
        stringBuilder.append("Актер;");
        stringBuilder.append("Дата релиза;");
        stringBuilder.append("Стоимость подписки;");
        stringBuilder.append("Стоимость покупки SD;");
        stringBuilder.append("Стоимость покупки HD;");
        stringBuilder.append("Стоимость покупки UHD;");
        stringBuilder.append("Стоимость аренды SD;");
        stringBuilder.append("Стоимость аренды HD;");
        stringBuilder.append("Стоимость аренды UHD;");
        stringBuilder.append("URL;");
        stringBuilder.append(Constants.LINE_SEPARATOR);
        return stringBuilder.toString();
    }

    public String mapFilmToCSV(Film film) {


        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(getStringFromValue(film.getId()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getNameRus()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getNameOrigin()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getYear()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getCountry()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromCollection(film.getGenreList()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getRatingKinopoisk()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getRatingIMDB()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getFeesInWorld()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getFeesInRussia()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getDirector()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromCollection(film.getActorList()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getReleaseDateInWorld()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(film.getBuyInfo().getBuySubscription());
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(film.getBuyInfo().getBuySD());
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(film.getBuyInfo().getBuyHD());
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(film.getBuyInfo().getBuyUHD());
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(film.getBuyInfo().getRentSD());
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(film.getBuyInfo().getRentHD());
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(film.getBuyInfo().getRendUHD());
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(getStringFromValue(film.getUrl()));
        stringBuilder.append(Constants.CELL_SEPARATOR);
        stringBuilder.append(Constants.LINE_SEPARATOR);

        return stringBuilder.toString();
    }

    private String getStringFromValue(String str) {
        if(StringUtils.isEmpty(str)) {
            return "-";
        }
        return validateValue(str);
    }

    public String validateValue(String value) {
        return Optional.ofNullable(value)
                .filter(StringUtils::isNotEmpty)
                .map(str -> {
                    for(Map.Entry<String, String> entry: MAP_REPLACE_EXCESS_WORD_FOR_REPORTS.entrySet()) {
                        str = str.replaceAll(entry.getKey(), entry.getValue());
                    }
                    return str;
                })
                .orElse(StringUtils.EMPTY);
    }

    private String getStringFromCollection(List<String> collection) {
        if(CollectionUtils.isEmpty(collection)) {
            return "-";
        }
        return String.join(", ", collection);
    }
}
