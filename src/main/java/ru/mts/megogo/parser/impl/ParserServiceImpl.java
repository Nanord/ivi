package ru.mts.megogo.parser.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.mts.megogo.parser.CatologProducer;
import ru.mts.megogo.parser.KinopoiskParserService;
import ru.mts.megogo.parser.MegogoParserService;
import ru.mts.megogo.parser.ParserService;
import ru.mts.megogo.service.PageService;
import ru.mts.megogo.service.SaveFile;
import ru.mts.megogo.utils.Constants;
import ru.mts.megogo.utils.MultithreadingUtils;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;

@Service
@Slf4j
@ConditionalOnProperty(value = "use.multithreading", havingValue = "false", matchIfMissing = true)
public class ParserServiceImpl implements ParserService {

    @Autowired
    private CatologProducer catologProducer;
    @Autowired
    private PageService pageService;
    @Autowired
    private BlockingQueue<String> filmItemUrlQueue;
    @Autowired
    private MegogoParserService megogoParserService;
    @Autowired
    private KinopoiskParserService kinopoiskParserService;
    @Autowired
    private SaveFile saveFile;

    @Override
    public void parse() {
        catologProducer.produceCatalog();
        startParseMegogo();
    }

    private void startParseMegogo() {
        String url = MultithreadingUtils.takeObjectFromQueue(filmItemUrlQueue, Constants.DEFAULT_VALUE);
        while (!StringUtils.equals(url, Constants.DEFAULT_VALUE)) {
            doWork(url);
            url = takeNextTask();
        }
    }

    private String takeNextTask() {
        return MultithreadingUtils.takeObjectFromQueue(filmItemUrlQueue, Constants.DEFAULT_VALUE);
    }

    private void doWork(String url) {
        Optional.ofNullable(MultithreadingUtils.getObjectFromAsynkTask(pageService.getPage(url, "Megogo film page")))
                .map(document -> megogoParserService.parse(document))
                .map(film -> kinopoiskParserService.parse(film))
                .ifPresent(film -> saveFile.save(film));
    }

}
