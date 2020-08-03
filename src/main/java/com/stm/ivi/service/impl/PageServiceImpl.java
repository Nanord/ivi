package com.stm.ivi.service.impl;

import com.stm.ivi.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class PageServiceImpl implements PageService {

    @Value("${timeout.before.req.ivi}")
    private Integer timeOutBeforeGetPageMegogo;

    @Autowired
    private RemoteWebDriver driver;

    @Override
    public CompletableFuture<Document> getPage(String url, String logText) {
        return null;
    }

}
