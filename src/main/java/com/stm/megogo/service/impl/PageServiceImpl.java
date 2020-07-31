package com.stm.megogo.service.impl;

import com.stm.megogo.capcharesolver.CaptchaResolver;
import com.stm.megogo.retrying.RetryStrategy;
import com.stm.megogo.service.PageService;
import com.stm.megogo.utils.MultithreadingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.SocketTimeoutException;
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
        return driver.findElementsByTagName("button").stream()
                .filter(button -> StringUtils.containsIgnoreCase(button.getText(), buttonText))
                .findFirst();
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
        driver.get(url);
        findButton("купить и смотреть")
                .ifPresent(WebElement::click);
        findButton("купить и смотреть")
                .ifPresent(WebElement::click);
        Document document = Optional.ofNullable(driver.getPageSource())
                .map(Jsoup::parse)
                .orElse(null);
        if(document == null) {
            return null;
        }
        return document.getElementsByTag("div").stream()
                .filter(div -> StringUtils.containsIgnoreCase(div.attr("class"), "PurchaseOptionCard__header_film"))
                .map(header -> {
                    BuyInfo buyInfo = new BuyInfo();
                    String type = header.getElementsByTag("h2").text();
                    List<Element> collect = new ArrayList<>(header.getElementsByAttributeValueContaining("class", "PurchaseOptionCard__price"));
                    if(collect.size() != 2) {
                        return null;
                    }
                    if(StringUtils.containsIgnoreCase(type, "покупка")) {
                        buyInfo.setBuyWithSubscription(collect.get(0).text());
                        buyInfo.setBuy(collect.get(1).text());
                    }
                    if(StringUtils.containsIgnoreCase(type, "аренда")) {
                        buyInfo.setRentWithSubscription(collect.get(0).text());
                        buyInfo.setRent(collect.get(1).text());
                    }
                    return buyInfo;
                })
                .findFirst()
                .orElse(null);
    }


}
