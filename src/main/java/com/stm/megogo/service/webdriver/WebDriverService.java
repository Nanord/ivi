package com.stm.megogo.service.webdriver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebDriverService {

    private final WebDriverWrapper webDriverWrapper;

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
