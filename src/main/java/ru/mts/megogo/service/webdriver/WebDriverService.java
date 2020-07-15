package ru.mts.megogo.service.webdriver;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import ru.mts.megogo.retrying.RetryCount;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebDriverService {

    private final WebDriverWrapper webDriverWrapper;

    public boolean getWithRetry(@NonNull RemoteWebDriver driver, Consumer<RemoteWebDriver> driverConsumer) {
        try {
            return new RetryCount(3).<Boolean>createStrategy()
                    .setTimeOutAfterFailCallFunction(3000)
                    .setFunction(() -> {
                        driverConsumer.accept(driver);
                        return true;
                    })
                    .retryIfException(WebDriverException.class)
                    .run();
        } catch (Exception e) {
            log.warn("Exception after retrying");
            return false;
        }
    }

    /**
     * Return page source of URL.
     *
     * @return page source as string or empty string if throwing exception
     */
    public String getPageSource(String url) {
        RemoteWebDriver driver = null;
        try {
            driver = webDriverWrapper.getWebDriver();

            if (driver == null) {
                return StringUtils.EMPTY;
            }

            driver.get(url);

            String pageSource = driver.getPageSource();
            log.debug("Page source for URL: {}", url);
            return pageSource;
        } catch (Exception e) {
            log.error("Exception on getting page source", e);
        } finally {
            webDriverWrapper.quit(driver);
        }

        return StringUtils.EMPTY;
    }

    public <T> T execute(Function<RemoteWebDriver, T> consumer) {
        return execute(null, consumer);
    }

    public <T> T execute(T defaultResult, Function<RemoteWebDriver, T> consumer) {
        RemoteWebDriver driver = null;
        try {
            driver = webDriverWrapper.getWebDriver();

            if (driver == null) {
                return defaultResult;
            }

            return consumer.apply(driver);
        } catch (Exception e) {
            log.error("Exception in  method execute", e);
            return null;
        }
    }
}
