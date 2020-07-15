package ru.mts.megogo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.mts.megogo.parser.ParserService;
import ru.mts.megogo.pojo.Film;
import ru.mts.megogo.service.SaveFile;
import ru.mts.megogo.utils.DateUtils;

import javax.annotation.PostConstruct;
import javax.naming.ConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import static ru.mts.megogo.utils.Constants.LINE_SEPARATOR;

@Service
@Slf4j
@ConditionalOnProperty(value = "save.type", havingValue = "csv", matchIfMissing = true)
public class CsvSaveFile implements SaveFile {

    private static final String OUT_FOLDER = "parsed";
    private static final String FILE_NAME = "mongogo.csv";

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
            writeDataToFile(film.toCSV(), outputPath);
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
        stringBuilder.append(LINE_SEPARATOR);
        return stringBuilder.toString();
    }
}