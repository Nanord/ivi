package com.stm.megogo.service;

import com.stm.megogo.pojo.BuyInfo;
import org.jsoup.nodes.Document;

import java.util.concurrent.CompletableFuture;

public interface PageService {
    CompletableFuture<Document> getPage(String url, String logText);
    BuyInfo receiveBuyInfo(String url);
}
