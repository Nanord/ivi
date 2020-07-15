package ru.mts.megogo.service.webdriver;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.mts.megogo.exception.RetryException;
import ru.mts.megogo.retrying.RetryCount;

import java.util.Objects;

@Slf4j
@Component
@ConditionalOnProperty(value = "webdriver.browser.name", havingValue = "chrome")
public class ChromeWebDriver extends WebDriverWrapper {

    @Override
    public ChromeDriver getWebDriver() {
        try {
            return new RetryCount(3).<ChromeDriver>createStrategy()
                    .setTimeOutAfterFailCallFunction(3000)
                    .setFunction(this::getChromeWebDriver)
                    .successIf(Objects::nonNull)
                    .run();
        } catch (RetryException e) {
            log.error("Retry exception while getting firefox web driver");
        }

        return null;
    }

    public ChromeDriver getChromeWebDriver() {
        ChromeDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.setHeadless(true);
            options.addArguments("--no-sandbox");
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--ignore-ssl-errors");
            options.addArguments("--disable-extensions");
            options.addArguments("--disable-gpu");
            options.addArguments("--disable-dev-shm-usage");

            driver = new ChromeDriver(options);

            return driver;
        } catch (Exception e) {
            log.error("Exception on getting webdriver", e);
        }
        return driver;
    }
}
