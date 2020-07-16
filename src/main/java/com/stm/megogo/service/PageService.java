package com.stm.megogo.service;

import org.jsoup.nodes.Document;

import java.util.concurrent.CompletableFuture;

public interface PageService {
    CompletableFuture<Document> getPage(String url, String logText);
}
