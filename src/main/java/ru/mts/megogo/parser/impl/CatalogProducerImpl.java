package ru.mts.megogo.parser.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import ru.mts.megogo.parser.CatologProducer;
import ru.mts.megogo.service.PageService;
import ru.mts.megogo.utils.Constants;
import ru.mts.megogo.utils.MultithreadingUtils;

import java.util.Collection;
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
        parsingCatalog(Constants.FILMS_URL);
    }

    /**
     * По сути однопоточный
     * @param catalogUrl
     * @return
     */
    private CompletableFuture<Void> parsingCatalog(String catalogUrl) {
        if(StringUtils.isEmpty(catalogUrl)) {
            log.info("Catalog collected!");
            return null;
        }
        return pageService.getPage(catalogUrl, "Megogo Catalog page")
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
        return document.getElementsByTag("section").stream()
                .map(section -> section.getElementsByClass("type-catalog"))
                .flatMap(Collection::stream)
                .findFirst()
                .map(mainElement -> {
                    putFilmUrlToQueue(mainElement);
                    return receiveNextPageUrl(document);
                })
                .orElse(null);
    }

    private void putFilmUrlToQueue(Element mainElement) {
        mainElement.getElementsByClass("videoItem").stream()
                .map(videoItem -> videoItem.getElementsByClass("overlay"))
                .map(overlay -> overlay.attr("href"))
                .filter(StringUtils::isNotEmpty)
                .map(url -> Constants.BASE_URL + url)
                .forEach(url -> MultithreadingUtils.putObjectInQueue(url, filmItemUrlQueue));
    }

    private String receiveNextPageUrl(Document document) {
        return document.getElementsByClass("pagination").stream()
                .map(pagination -> pagination.getElementsByTag("a"))
                .map(linkElement -> linkElement.attr("href"))
                .findFirst()
                .map(urlNextPage -> Constants.BASE_URL + urlNextPage)
                .orElse(null);
    }

}
