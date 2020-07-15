package ru.mts.megogo.service.webdriver;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
@RequiredArgsConstructor
public abstract class WebDriverWrapper {

    @PostConstruct
    public void setProperties() {
        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
    }

    public abstract RemoteWebDriver getWebDriver();

    public void quit(RemoteWebDriver driver) {
        try {
            if (driver != null) {
                driver.quit();
            }
        } catch (Exception e) {
            log.error("Exception on quitting webdriver", e);
        }
    }
}
