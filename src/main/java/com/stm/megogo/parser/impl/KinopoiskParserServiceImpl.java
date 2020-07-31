package com.stm.megogo.parser.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stm.megogo.pojo.Film;
import com.stm.megogo.service.PageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.stm.megogo.parser.KinopoiskParserService;
import com.stm.megogo.utils.MultithreadingUtils;
import com.stm.megogo.utils.RegExpHelper;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KinopoiskParserServiceImpl implements KinopoiskParserService {

    @Autowired
    private PageService pageService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Film parse(Document document) {
        if (document == null) {
            return null;
        }
        Film film = new Film();
        String id = receiveId(document.baseUri());
        String nameRus = document.getElementsByClass("moviename-title-wrapper").text();
        String nameOrigin = document.getElementsByClass("alternativeHeadline").text();
        String ratingKP = document.getElementsByClass("rating_ball").text();
        String ratingIMDb = receiveRatingIMDb(document);
        List<String> actorList = receiveActorList(document.getElementById("actorList"));
        List<String> studioList = receiveStudioList(
                document.baseUri().replace("/old", "/studio"));
        List<String> awardList = receiveAwardList(
                document.baseUri().replace("/old", "/awards"));
        receiveDataFromTable(document, film);
        Film res = film
                .setId(id)
                .setNameRus(nameRus)
                .setNameOrigin(nameOrigin)
                .setRatingIMDB(ratingIMDb)
                .setRatingKinopoisk(ratingKP)
                .setActorList(actorList)
                .setStudioList(studioList)
                .setAwardList(awardList)
                .setUrl(document.baseUri().replace("/old", ""));
        log.info("Parse Kinopoisk {}", film.getUrl());
        return res;
    }

    private void receiveDataFromTable(Document document, Film film) {
        Optional<Elements> optionalRows = Optional.ofNullable(document.getElementById("infoTable"))
                .map(infoElement -> infoElement.getElementsByTag("tr"));
        if(!optionalRows.isPresent()) {
            return;
        }
        optionalRows.get()
                .stream()
                .map(row -> row.getElementsByTag("td"))
                .filter(column -> column.size() == 2)
                .forEach(column -> {
                    String key = StringUtils.trim(column.get(0).text());
                    String value = StringUtils.trim(column.get(1).text());
                    putDataFromColumn(key, value, film);
                });
    }

    private void putDataFromColumn(String key, String value, Film film) {
        if(StringUtils.isEmpty(key) || StringUtils.isEmpty(value)) {
            return;
        }
        if(StringUtils.equalsIgnoreCase(key, "год")) {
            film.setYear(value);
        }
        if(StringUtils.equalsIgnoreCase(key, "страна")) {
            film.setCountry(value);
        }
        if(StringUtils.equalsIgnoreCase(key, "режиссер")) {
            film.setDirector(value);
        }
        if(StringUtils.equalsIgnoreCase(key, "сборы в мире")) {
            film.setFeesInWorld(receiveFee(value));
        }
        if(StringUtils.equalsIgnoreCase(key, "сборы в России")) {
            film.setFeesInRussia(receiveFee(value));
        }
        if(StringUtils.equalsIgnoreCase(key, "премьера (РФ)")) {
            film.setReleaseDateInRussia(receiveReleaseData(value));
        }
        if(StringUtils.equalsIgnoreCase(key, "премьера (мир)")) {
            film.setReleaseDateInWorld(receiveReleaseData(value));
        }
        if(StringUtils.equalsIgnoreCase(key, "релиз на DVD") || StringUtils.equalsIgnoreCase(key, "цифровой релиз")) {
            film.setReleaseDataInDigital(receiveReleaseData(value));
        }
        if(StringUtils.equalsIgnoreCase(key, "жанр")) {
            film.setGenreList(receiveGenre(value));
        }
    }

    private List<String> receiveActorList(Element actorElement) {
        return Optional.ofNullable(actorElement)
                .map(el -> el.getElementsByTag("ul"))
                .filter(CollectionUtils::isNotEmpty)
                .map(Elements::first)
                .map(ul -> ul.getElementsByTag("li"))
                .map(li -> li.stream()
                        .map(Element::text)
                        .filter(actor -> !StringUtils.contains(actor, "..."))
                        .map(StringUtils::trim)
                        .filter(StringUtils::isNotEmpty)
                        .limit(5)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private List<String> receiveStudioList(String url) {
        Document page = MultithreadingUtils
                .getObjectFromAsynkTask(pageService.getPage(url, "Kinopoisk studio page"));
        return Optional.ofNullable(page)
                .map(document -> document.getElementsContainingText("Производство:"))
                .filter(CollectionUtils::isNotEmpty)
                .map(elements -> {
                    Collections.reverse(elements);
                    return elements;
                })
                .flatMap(elements -> elements.stream()
                        .filter(el -> StringUtils.equals(el.tagName(), "tbody"))
                        .findFirst())
                .map(tbody -> tbody.getElementsByTag("tr"))
                .map(rows -> rows.stream()
                        .map(row -> row.getElementsByTag("td"))
                        .filter(column -> column.size() == 2)
                        .map(column -> column.get(1))
                        .map(Element::text)
                        .map(StringUtils::trim)
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private List<String> receiveAwardList(String url) {
        Document page = MultithreadingUtils
                .getObjectFromAsynkTask(pageService.getPage(url, "Kinopoisk awards page"));
        return Optional.ofNullable(page)
                .map(document -> document.getElementsByClass("js-rum-hero"))
                .filter(CollectionUtils::isNotEmpty)
                .map(Elements::first)
                .map(mainTable -> mainTable.getElementsByTag("table"))
                .map(tables -> tables.stream()
                        .filter(table -> StringUtils.equals(table.attr("cellpadding"), "0"))
                        .map(table -> table.getElementsByTag("tr"))
                        .filter(CollectionUtils::isNotEmpty)
                        .map(Elements::first)
                        .map(Element::text)
                        .map(StringUtils::trim)
                        .filter(StringUtils::isNotEmpty)
                        .filter(award -> StringUtils.containsIgnoreCase(award, "cмотрите также"))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private String receiveFee(String fee) {
        return Optional.ofNullable(fee)
                .map(findFee -> findFee.replaceAll("([^\\d]*$)|(\\s)", ""))
                .flatMap(findFee -> RegExpHelper.findMatcherString("\\$(\\d+\\s*)*$", findFee))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotEmpty)
                .orElse(null);
    }

    private String receiveReleaseData(String release) {
        return Optional.ofNullable(release)
                .flatMap(findRelease -> RegExpHelper.findMatcherString("\\d+\\s+.*\\d{4}", findRelease))
                .map(StringUtils::trim)
                .filter(StringUtils::isNotEmpty)
                .orElse(null);
    }

    private List<String> receiveGenre(String genre) {
        return Optional.ofNullable(genre)
                .map(findGenre -> findGenre.split(","))
                .map(Arrays::stream)
                .map(stream -> stream
                        .map(s -> s.replaceAll("\\.{3}\\s*слова", ""))
                        .map(StringUtils::trim)
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private String receiveRatingIMDb(Document document) {
        return document.getElementsByClass("block_2").stream()
                .map(element -> element.getElementsContainingText("IMDb"))
                .map(org.springframework.util.CollectionUtils::lastElement)
                .filter(Objects::nonNull)
                .map(Element::text)
                .findFirst()
                .flatMap(text -> RegExpHelper.findMatcherString(":\\s*(\\d+\\.\\d+)", text, 1))
                .orElse(null);
    }

    private String receiveId(String url) {
        return Arrays.stream(url.split("/"))
                .filter(Objects::nonNull)
                .filter(text -> text.matches("\\d+"))
                .findFirst()
                .orElse(null);
    }

    private <T> Optional<T> castStingToObject(String str, TypeReference<T> typeReference) {
        try {
            return Optional.ofNullable(objectMapper.readValue(str, typeReference));
        } catch (IOException e) {
            log.warn("Cannot read Value from " + str + " how type " + typeReference.getType().getTypeName());
            return Optional.empty();
        }
    }

}
