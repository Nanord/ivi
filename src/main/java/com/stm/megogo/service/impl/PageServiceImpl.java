package com.stm.megogo.service.impl;

import com.stm.megogo.capcharesolver.CaptchaResolver;
import com.stm.megogo.pojo.BuyInfo;
import com.stm.megogo.retrying.RetryStrategy;
import com.stm.megogo.service.PageService;
import com.stm.megogo.utils.Constants;
import com.stm.megogo.utils.MultithreadingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PageServiceImpl implements PageService {

    @Value("${timeout.before.get.page.megogo}")
    private Integer timeOutBeforeGetPageMegogo;

    @Value("${timeout.before.get.page.kinopoisk}")
    private Integer timeOutBeforeGetPageKinopoisk;

    @Value("${yandex.login}")
    private String yandexLogin;

    @Value("${yandex.password}")
    private String yandexPassword;

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

    @PostConstruct
    private void login() {
        driver.get(Constants.YANDEX_LOGIN_PAGE);
        driver.findElementById("passp-field-login").sendKeys(yandexLogin);
        findButton("Войти")
                .ifPresent(WebElement::click);
        driver.findElementById("passp-field-passwd").sendKeys(yandexPassword);
        findButton("Войти")
                .ifPresent(WebElement::click);
        findButton("Не сейчас")
                .ifPresent(WebElement::click);
    }

    private Optional<WebElement> findButton(String buttonText) {
        return RetryStrategy.<Optional<WebElement>>newRetryStrategy(10)
                .retryIfException(Exception.class)
                .setFunction(() ->  driver.findElementsByTagName("button").stream()
                        .filter(button -> StringUtils.containsIgnoreCase(button.getText(), buttonText))
                        .findFirst())
                .run();
    }

    @Override
    public CompletableFuture<Document> getPage(String url, String logText) {
        return CompletableFuture
                .supplyAsync(
                        () -> {
                            if(StringUtils.contains(url, "kinopoisk")) {
                                return connectSelenium(url, logText, timeOutBeforeGetPageKinopoisk);
                            }
                            return connectJsoup(url, logText, timeOutBeforeGetPageKinopoisk);
                        },
                        threadPoolTaskExecutorForGetPageKinopoisk)
                .exceptionally(ex -> MultithreadingUtils.handleException("Cannot get page", url, null, ex));
    }

    private Document connectJsoup(String url, String logText, Integer timeOutBeforeGetPage) {
        if(StringUtils.isEmpty(url)) {
            log.info("Url is empty for: {}", logText);
            return null;
        }
        Document res = RetryStrategy.<Document>newRetryStrategy(10)
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
        log.info("Get page for {}: {} ", logText, url);
        return res;
    }

    private Document connectSelenium(String url, String logText, Integer timeOutBeforeGetPage) {
        if(StringUtils.isEmpty(url)) {
            log.info("Url is empty for: {}", logText);
            return null;
        }
        MultithreadingUtils.sleep(timeOutBeforeGetPage);
        driver.get(url);
        Document document = Optional.ofNullable(driver.getPageSource())
                .map(Jsoup::parse)
                .orElse(null);
        log.info("Get page for {}: {} ", logText, url);
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

    @Override
    public BuyInfo receiveBuyInfo(String url) {
        try {
            MultithreadingUtils.sleep(timeOutBeforeGetPageKinopoisk);
            driver.get(url);
            Optional<WebElement> buyAndWatch = findButton("купить и смотреть");
            while (!buyAndWatch.isPresent()) {
                buyAndWatch = findButton("купить и смотреть");
            }
            buyAndWatch.ifPresent(WebElement::click);
            MultithreadingUtils.sleep(1000);
            driver.findElementsByClassName("passp-previous-step-button").stream()
                    .findFirst()
                    .ifPresent(WebElement::click);
            Document document = Optional.ofNullable(driver.getPageSource())
                    .map(Jsoup::parse)
                    .orElse(null);
            if(document == null) {
                return null;
            }
            Optional<Element> optHeaders = document.getElementsByTag("div").stream()
                    .filter(div -> StringUtils.containsIgnoreCase(div.attr("class"), "PurchaseOptionCard__header_film"))
                    .findFirst();
            while (!optHeaders.isPresent()) {
                document = Optional.ofNullable(driver.getPageSource())
                        .map(Jsoup::parse)
                        .orElse(null);
                if(document == null) {
                    return null;
                }
                optHeaders = document.getElementsByTag("div").stream()
                        .filter(div -> StringUtils.containsIgnoreCase(div.attr("class"), "PurchaseOptionCard__header_film"))
                        .findFirst();
            }
            BuyInfo buyInfo = new BuyInfo();
            document.getElementsByTag("div").stream()
                    .filter(div -> StringUtils.containsIgnoreCase(div.attr("class"), "PurchaseOptionCard__header_film"))
                    .forEach(header -> {
                        String type = header.getElementsByTag("h2").text();
                        List<Element> collect = header.getElementsByAttributeValueContaining("class", "PurchaseOptionCard__price").stream()
                                .map(price -> price.getElementsByTag("span"))
                                .flatMap(Collection::stream)
                                .skip(1)
                                .collect(Collectors.toList());
                        if(collect.size() != 2) {
                            return;
                        }
                        if(StringUtils.containsIgnoreCase(type, "покупка")) {
                            buyInfo.setBuyWithSubscription(collect.get(0).text());
                            buyInfo.setBuy(collect.get(1).text());
                        }
                        if(StringUtils.containsIgnoreCase(type, "аренда")) {
                            buyInfo.setRentWithSubscription(collect.get(0).text());
                            buyInfo.setRent(collect.get(1).text());
                        }
                    });
            return buyInfo;
        } catch (Exception e) {
            log.info("cannot get buy info! {}", url);
        }
        return null;
    }


}
