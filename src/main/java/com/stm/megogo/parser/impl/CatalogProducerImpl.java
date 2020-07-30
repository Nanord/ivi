package com.stm.megogo.parser.impl;

import com.stm.megogo.parser.CatologProducer;
import com.stm.megogo.service.PageService;
import com.stm.megogo.utils.Constants;
import com.stm.megogo.utils.MultithreadingUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CatalogProducerImpl implements CatologProducer {

    @Autowired
    @Qualifier("threadPoolTaskExecutorForCatalog")
    private ThreadPoolTaskExecutor threadPoolTaskExecutorForCatalog;
    @Autowired
    private PageService pageService;
    @Autowired
    private BlockingQueue<String> filmItemUrlQueue;

    @Override
    public void produceCatalog() {
        parsingCatalog(String.format(Constants.FILMS_URL, Constants.FIRST_PAGE));
    }

    /**
     * По сути однопоточный
     *
     * @param catalogUrl
     * @return
     */
    private CompletableFuture<Void> parsingCatalog(String catalogUrl) {
        if (StringUtils.isEmpty(catalogUrl)) {
            log.info("Catalog collected!");
            return null;
        }
        return pageService.getPage(catalogUrl, "Kinopoisk Catalog page")
                .thenApplyAsync(
                        this::parseCatalogPageAndGetNextPageUrl,
                        threadPoolTaskExecutorForCatalog)
                .exceptionally(ex -> MultithreadingUtils.handleException("Exceptions during parse catalog",
                        "",
                        null,
                        ex))
                .thenComposeAsync(this::parsingCatalog, threadPoolTaskExecutorForCatalog);
    }

    private String parseCatalogPageAndGetNextPageUrl(Document document) {
        if (document == null) {
            return null;
        }
        log.info("Parse catalog document: {}", document.location());
        return document.getElementsByClass("selection-list").stream()
                .findFirst()
                .map(mainElement -> {
                    putFilmUrlToQueue(mainElement);
                    return receiveNextPageUrl(document);
                })
                .orElse(null);
    }

    private void putFilmUrlToQueue(Element mainElement) {
        mainElement.getElementsByClass("selection-film-item-meta__link").stream()
                .map(item -> Constants.BASE_URL + item.getElementsByTag("a").attr("href"))
                .filter(StringUtils::isNotEmpty)
                .forEach(link -> MultithreadingUtils.putObjectInQueue(link, filmItemUrlQueue));
    }

    private String receiveNextPageUrl(Document document) {
        Integer currentPage = document.getElementsByClass("paginator__page-number paginator__page-number_is-active").stream()
                .map(Element::text)
                .map(Integer::valueOf)
                .findFirst()
                .orElse(null);
        Integer lastPage = document.getElementsByClass("paginator__page-number").stream()
                .map(Element::text)
                .map(Integer::valueOf)
                .max(Integer::compareTo)
                .orElse(null);
        if (Objects.nonNull(currentPage) && Objects.nonNull(lastPage) && currentPage < lastPage) {
            return String.format(Constants.FILMS_URL, ++currentPage);
        }
        return null;
    }

}
