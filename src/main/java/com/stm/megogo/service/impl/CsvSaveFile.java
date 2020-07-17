package com.stm.megogo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stm.megogo.parser.ParserService;
import com.stm.megogo.pojo.Film;
import com.stm.megogo.service.SaveFile;
import com.stm.megogo.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import com.stm.megogo.utils.DateUtils;

import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@ConditionalOnProperty(value = "save.type", havingValue = "csv", matchIfMissing = true)
public class CsvSaveFile implements SaveFile {

    private static final String OUT_FOLDER = "parsed";
    private static final String FILE_NAME = "megogo.csv";

    @Value("${output.path}")
    private String outputPathProperty;

    private String outputPath;

    @Autowired
    private ParserService parserService;
    @Autowired
    @Qualifier("threadPoolTaskExecutorForWriteFile")
    private ThreadPoolTaskExecutor threadPoolTaskExecutorForWriteFile;

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
        stringBuilder.append("Студия;");
        stringBuilder.append("Страна;");
        stringBuilder.append("Жанр;");
        stringBuilder.append("Рейтинг на Кинопоиске;");
        stringBuilder.append("Рейтинг на IMDb;");
        stringBuilder.append("Сборы фильма в мире;");
        stringBuilder.append("Сборы фильма в России;");
        stringBuilder.append("Режиссер;");
        stringBuilder.append("Актер;");
        stringBuilder.append("Награды;");
        stringBuilder.append("Дата релиза в России;");
        stringBuilder.append("Дата цифрового релиза;");
        stringBuilder.append("Условие предоставления;");
        stringBuilder.append("Стоимость покупки;");
        stringBuilder.append("Наличие подписки;");
        stringBuilder.append("URL;");
        stringBuilder.append(Constants.LINE_SEPARATOR);
        return stringBuilder.toString();
    }

    public String mapFilmToCSV(Film film) {
        setEmptyElementIfCollectionEmpty(film.getStudioList());
        setEmptyElementIfCollectionEmpty(film.getActorList());
        setEmptyElementIfCollectionEmpty(film.getGenreList());

        StringBuilder stringBuilder = new StringBuilder();
        for (String studio : film.getStudioList()) {
            for (String actor : film.getActorList()) {
                for (String genre : film.getGenreList()) {
                    stringBuilder.append(film.getId());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getNameRus());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getNameOrigin());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getYear());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(studio);
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getCountry());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(genre);
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getRatingKinopoisk());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getRatingIMDB());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getFeesInWorld());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getFeesInRussia());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getDirector());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(actor);
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(String.join(" | ", film.getAwardList()));
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getReleaseDateInRussia());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getReleaseDataInDigital());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getProvisionFor() != null ? film.getProvisionFor() : "Бесплатно");
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getPurchasePrice() != null ? film.getPurchasePrice() : "Бесплатно");
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getSubscriptionAvailability() ? "+" : "-");
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(film.getUrl());
                    stringBuilder.append(Constants.CELL_SEPARATOR);
                    stringBuilder.append(Constants.LINE_SEPARATOR);
                }
            }
        }
        return stringBuilder.toString();
    }

    private void setEmptyElementIfCollectionEmpty(List<String> collection) {
        if(collection == null) {
            collection = new ArrayList<>();
        }
        if(collection.isEmpty()) {
            collection.add(StringUtils.EMPTY);
        }
    }
}
