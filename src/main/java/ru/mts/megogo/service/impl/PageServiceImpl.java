package ru.mts.megogo.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.mts.megogo.capcharesolver.CaptchaResolver;
import ru.mts.megogo.exception.GetPageException;
import ru.mts.megogo.retrying.RetryStrategy;
import ru.mts.megogo.service.PageService;
import ru.mts.megogo.utils.MultithreadingUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PageServiceImpl implements PageService {

    @Value("${timeout.before.get.page.megogo}")
    private Integer timeOutBeforeGetPageMegogo;

    @Value("${timeout.before.get.page.kinopoisk}")
    private Integer timeOutBeforeGetPageKinopoisk;

    @Autowired
    @Qualifier("threadPoolTaskExecutorForGetPageMegogo")
    private ThreadPoolTaskExecutor threadPoolTaskExecutorForGetPageMegogo;
    @Autowired
    @Qualifier("threadPoolTaskExecutorForGetPageKinopoisk")
    private ThreadPoolTaskExecutor threadPoolTaskExecutorForGetPageKinopoisk;
    @Autowired
    private RemoteWebDriver driver;
    @Autowired
    private CaptchaResolver captchaResolver;

    @Override
    public CompletableFuture<Document> getPage(String url, String logText) {
        ThreadPoolTaskExecutor executor = threadPoolTaskExecutorForGetPageMegogo;
        Integer timeOutBeforeGetPage = timeOutBeforeGetPageMegogo;
        if(StringUtils.contains(url, "kinopoisk")) {
            executor = threadPoolTaskExecutorForGetPageKinopoisk;
            timeOutBeforeGetPage = timeOutBeforeGetPageKinopoisk;
        }
        Integer finalTimeOutBeforeGetPage = timeOutBeforeGetPage;
        return CompletableFuture
                .supplyAsync(
                        () -> {
                            if(StringUtils.contains(url, "kinopoisk")) {
                                return connectSelenium(url, logText, finalTimeOutBeforeGetPage);
                            }
                            return connectJsoup(url, logText, finalTimeOutBeforeGetPage);
                        },
                        executor)
                .exceptionally(ex -> MultithreadingUtils.handleException("Cannot get page", url, null, ex));
    }

    private Document connectJsoup(String url, String logText, Integer timeOutBeforeGetPage) {
        if(StringUtils.isEmpty(url)) {
            log.info("Url is empty for: {}", logText);
            return null;
        }
        log.info("Get page for {}: {} ", logText, url);
        return RetryStrategy.<Document>newRetryStrategy(10)
                .retryIfException(IOException.class)
                .retryIfException(SocketTimeoutException.class)
                .setTimeOutAfterFailCallFunction(5000)
                .setTimeOutBeforeCallFunction(timeOutBeforeGetPage)
                .successIf(Objects::nonNull)
                .successIf(document -> Objects.nonNull(document.body()))
                .setFunction(() -> Jsoup.connect(url)
                        .followRedirects(true)
                        .maxBodySize(0)
                        .timeout(100000)
                        .method(Connection.Method.GET)
                        .ignoreContentType(true)
                        .execute()
                        .parse())
                .run();
    }

    private Document connectSelenium(String url, String logText, Integer timeOutBeforeGetPage) {
        if(StringUtils.isEmpty(url)) {
            log.info("Url is empty for: {}", logText);
            return null;
        }
        try {
            Thread.sleep(timeOutBeforeGetPage);
        } catch (InterruptedException e) {
            log.error("Ex", e);
        }
        driver.get(url);
        Document document = Optional.ofNullable(driver.getPageSource())
                .map(Jsoup::parse)
                .orElse(null);
        if(document == null) {
            log.error("Cannot get page {}", url);
        }
        if(captchaResolver.isCaptcha(document)) {
            log.warn("Kinopoisk captcha! {}", url);
            document = connectSelenium(captchaResolver.getAnswerForCaptchaDoc(document), logText, timeOutBeforeGetPage);
        }
        if (document != null) {
            document.setBaseUri(url);
        }
        return document;
    }

}
