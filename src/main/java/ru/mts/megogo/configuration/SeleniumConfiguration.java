package ru.mts.megogo.configuration;

import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.mts.megogo.service.webdriver.WebDriverService;

@Configuration
public class SeleniumConfiguration {

    @Autowired
    private WebDriverService webDriverService;

    @Bean
    public RemoteWebDriver remoteWebDriver() {
        return webDriverService.execute(driver -> driver);
    }
}
