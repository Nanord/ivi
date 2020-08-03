package com.stm.ivi.configuration;

import com.stm.ivi.service.webdriver.WebDriverService;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfiguration {

    @Autowired
    private WebDriverService webDriverService;

    @Bean
    public RemoteWebDriver remoteWebDriver() {
        return webDriverService.execute(driver -> driver);
    }
}
