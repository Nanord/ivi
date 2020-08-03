package com.stm.ivi.service;

import com.stm.ivi.pojo.BuyInfo;
import org.jsoup.nodes.Document;

import java.util.concurrent.CompletableFuture;

public interface PageService {
    CompletableFuture<Document> getPage(String url, String logText);
}
