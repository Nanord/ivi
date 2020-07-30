package com.stm.megogo.parser.impl;

import com.stm.megogo.parser.CatologProducer;
import com.stm.megogo.parser.ParserService;
import com.stm.megogo.service.SaveFile;
import com.stm.megogo.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.stm.megogo.parser.KinopoiskParserService;
import com.stm.megogo.service.PageService;
import com.stm.megogo.utils.MultithreadingUtils;

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
        Optional.ofNullable(MultithreadingUtils.getObjectFromAsynkTask(pageService.getPage(url, "Kinopoisk film page")))
               .map(document -> kinopoiskParserService.parse(document))
                .ifPresent(film -> saveFile.save(film));
    }

}
