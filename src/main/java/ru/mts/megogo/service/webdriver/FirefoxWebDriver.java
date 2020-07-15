package ru.mts.megogo.service.webdriver;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.mts.megogo.exception.RetryException;
import ru.mts.megogo.retrying.RetryCount;

import java.util.Objects;

@Slf4j
@Component
@ConditionalOnProperty(value = "webdriver.browser.name", havingValue = "firefox", matchIfMissing = true)
public class FirefoxWebDriver extends WebDriverWrapper {

    //@Value("${proxy}")
    protected String proxyStr;

    @Override
    public FirefoxDriver getWebDriver() {
        try {
            return new RetryCount(3).<FirefoxDriver>createStrategy()
                    .setTimeOutAfterFailCallFunction(3000)
                    .setFunction(this::getFirefoxWebDriver)
                    .successIf(Objects::nonNull)
                    .run();
        } catch (RetryException e) {
            log.error("Retry exception while getting firefox web driver");
        }

        return null;
    }

    public FirefoxDriver getFirefoxWebDriver() {
        FirefoxDriver driver = null;
        try {
            FirefoxOptions options = new FirefoxOptions();
            options.setHeadless(true);
            options.setAcceptInsecureCerts(true);

            driver = new FirefoxDriver(options);

            return driver;
        } catch (Exception e) {
            log.error("Exception on getting webdriver", e);
        }

        return driver;
    }
}
