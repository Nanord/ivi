package com.stm.megogo.parser.impl;

import com.stm.megogo.parser.ParserService;
import com.stm.megogo.pojo.BuyInfo;
import com.stm.megogo.pojo.CatalogInfo;
import com.stm.megogo.service.SaveFile;
import com.stm.megogo.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import com.stm.megogo.parser.KinopoiskParserService;
import com.stm.megogo.service.PageService;
import com.stm.megogo.utils.MultithreadingUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ParserServiceImpl implements ParserService {
    @Autowired
    private PageService pageService;
    @Autowired
    private KinopoiskParserService kinopoiskParserService;
    @Autowired
    private SaveFile saveFile;

    @Override
    public void parse() {
        start(String.format(Constants.FILMS_URL, Constants.FIRST_PAGE));
    }

    private void start(String catalogUrl) {
        if (StringUtils.isEmpty(catalogUrl)) {
            log.info("Catalog collected!");
            return;
        }
        Document catalogPage = MultithreadingUtils.getObjectFromAsynkTask(pageService.getPage(catalogUrl, "Kinopoisk Catalog page"));
        Optional.ofNullable(catalogPage)
                .map(this::parseCatalogPageAndGetNextPageUrl)
                .ifPresent(this::start);
    }

    private String parseCatalogPageAndGetNextPageUrl(Document document) {
        if (document == null) {
            return null;
        }
        log.info("Parse catalog document: {}", document.location());
        return document.getElementsByClass("selection-list").stream()
                .findFirst()
                .map(mainElement -> {
                    parseCatalog(mainElement);
                    //return receiveNextPageUrl(document);
                    return "";
                })
                .orElse(null);
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

    private void parseCatalog(Element mainElement) {
        mainElement.getElementsByClass("desktop-seo-selection-film-item selection-list__film").stream()
                .map(item -> {
                    CatalogInfo catalogInfo = new CatalogInfo();
                    String url = Constants.BASE_URL + item.getElementsByClass("selection-film-item-meta__link").attr("href");;
                    if (StringUtils.isEmpty(url)) {
                        return null;
                    }
                    item.getElementsByClass("film-item-buy-buttons").stream()
                            .map(buyButton -> buyButton.getElementsByTag("a"))
                            .flatMap(Collection::stream)
                            .findFirst()
                            .ifPresent(buyButton -> {
                                catalogInfo.setIsSubscription(StringUtils
                                        .containsIgnoreCase(buyButton.text(), "По подписке КиноПоиск HD"));
                                if (StringUtils.containsIgnoreCase(buyButton.text(), "от")) {
                                    BuyInfo buyInfo = pageService.receiveBuyInfo(Constants.BASE_URL + buyButton.attr("href"));
                                    catalogInfo.setBuyInfo(buyInfo);
                                }
                            });
                    return catalogInfo
                            .setFilmUrl(url);
                })
                .filter(Objects::nonNull)
                .forEach(this::startParsing);
    }

    private void startParsing(CatalogInfo catalogInfo) {
        Optional.ofNullable(MultithreadingUtils.getObjectFromAsynkTask(pageService.getPage(catalogInfo.getFilmUrl() + "old", "Kinopoisk film page")))
                .map(document -> kinopoiskParserService.parse(document))
                .map(film -> {
                    BuyInfo buyInfo = catalogInfo.getBuyInfo();
                    if(buyInfo == null) {
                        return film;
                    }
                    film.setBuy(buyInfo.getBuy());
                    film.setRent(buyInfo.getRent());
                    film.setBuyWithSubscription(buyInfo.getBuyWithSubscription());
                    film.setRentWithSubscription(buyInfo.getRentWithSubscription());
                })
                .ifPresent(film -> saveFile.save(film));
    }

}
