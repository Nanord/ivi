package ru.mts.megogo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.mts.megogo.exception.ProcessingException;
import ru.mts.megogo.parser.ParserService;
import ru.mts.megogo.pojo.Film;
import ru.mts.megogo.service.ProcessingService;
import ru.mts.megogo.utils.DateUtils;
import ru.mts.megogo.utils.MultithreadingUtils;

import javax.naming.ConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static ru.mts.megogo.utils.Constants.LINE_SEPARATOR;

@Service
@Slf4j
public class ProcessingServiceImpl implements ProcessingService {

    @Autowired
    private ParserService parserService;

    @Override
    public void start() throws ProcessingException {
        long startTime = System.currentTimeMillis();
        try {
            parserService.parse();
        } catch (Exception e) {
            log.error("Exception in parser");
            throw new ProcessingException("Exception in parser", e);
        }
        DateUtils.printExecutionTime(startTime);

    }

}
