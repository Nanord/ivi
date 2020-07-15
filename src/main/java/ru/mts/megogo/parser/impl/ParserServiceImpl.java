package ru.mts.megogo.parser.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ParserServiceImpl implements ParserService {

    @Autowired
    private CatologProducer catologProducer;
    @Autowired
    @Qualifier("threadPoolTaskExecutorForParser")
    private ThreadPoolTaskExecutor threadPoolTaskExecutorForParser;
    @Autowired
    @Qualifier("threadPoolTaskExecutorForWriteFile")
    private ThreadPoolTaskExecutor threadPoolTaskExecutorForWriteFile;
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
        CompletableFuture<List<Void>> collect = startParseMegogo().stream()
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        MultithreadingUtils.joinResult()));
        MultithreadingUtils.getObjectFromAsynkTask(collect);
    }

    private List<CompletableFuture<Void>> startParseMegogo() {
        List<CompletableFuture<Void>> result = new LinkedList<>();
        String url = MultithreadingUtils.takeObjectFromQueue(filmItemUrlQueue, Constants.DEFAULT_VALUE);
        while (!StringUtils.equals(url, Constants.DEFAULT_VALUE)) {
            String finalUrl = url;
            CompletableFuture<Void> futureFilm = pageService.getPage(url, "Megogo film page")
                    .thenApplyAsync(megogoParserService::parse, threadPoolTaskExecutorForParser)
                    .exceptionally(ex -> MultithreadingUtils.handleException("Exception during parse Megogo film page",
                            finalUrl,
                            null,
                            ex))
                    .thenApplyAsync(kinopoiskParserService::parse, threadPoolTaskExecutorForParser)
                    .exceptionally(ex -> MultithreadingUtils.handleException("Exception during parse Kinopoisk film page",
                            finalUrl,
                            null,
                            ex))
                    .thenAcceptAsync(saveFile::save, threadPoolTaskExecutorForWriteFile);
            result.add(futureFilm);
            url = MultithreadingUtils.takeObjectFromQueue(filmItemUrlQueue, Constants.DEFAULT_VALUE);
        }
        return result;
    }

    private String takeNextTask() {
        return MultithreadingUtils.takeObjectFromQueue(filmItemUrlQueue, Constants.DEFAULT_VALUE);
    }

    private CompletableFuture<Void> doWork(String url) {
        return pageService.getPage(url, "Megogo film page")
                .thenApplyAsync(megogoParserService::parse, threadPoolTaskExecutorForParser)
                .exceptionally(ex -> MultithreadingUtils.handleException("Exception during parse Megogo film page",
                        url,
                        null,
                        ex))
                .thenApplyAsync(kinopoiskParserService::parse, threadPoolTaskExecutorForParser)
                .exceptionally(ex -> MultithreadingUtils.handleException("Exception during parse Kinopoisk film page",
                        url,
                        null,
                        ex))
                .thenAcceptAsync(saveFile::save, threadPoolTaskExecutorForWriteFile);
    }

}
