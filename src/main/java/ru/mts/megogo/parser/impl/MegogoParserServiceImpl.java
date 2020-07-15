package ru.mts.megogo.parser.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import ru.mts.megogo.parser.MegogoParserService;
import ru.mts.megogo.pojo.Film;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MegogoParserServiceImpl implements MegogoParserService {

    @Override
    public Film parse(Document document) {
        if(document == null) {
            return null;
        }
        Element mainElement = receiveMainElement(document);
        if(Objects.nonNull(mainElement)) {
            return null;
        }
        String id = mainElement.attr("data-obj-id");
        String nameRus = mainElement.attr("data-title");
        String nameEng = mainElement.getElementsByClass("video-title-original").text();
        String kinopoiskUrl = receiveKinopoiskUrl(mainElement);
        String provisionFor = receiveProvisionFor(mainElement);
        String purchasePrice = receivePrice(mainElement, provisionFor);
        boolean subscriptionAvailability = false;
        if(StringUtils.containsIgnoreCase(provisionFor, "подписка")) {
            subscriptionAvailability = true;
        }
        Film film = new Film()
                .setId(id)
                .setNameRus(nameRus)
                .setNameOrigin(nameEng)
                .setKinopoiskUrl(kinopoiskUrl)
                .setProvisionFor(provisionFor)
                .setPurchasePrice(purchasePrice)
                .setSubscriptionAvailability(subscriptionAvailability)
                .setUrl(document.location());
        log.info("Parse Megogo {}", document.location());
        return film;
    }

    private Element receiveMainElement(Document document) {
        return document.getElementsByClass("player-place").stream()
                .map(playerPlace -> playerPlace.getElementsByTag("section"))
                .flatMap(Collection::stream)
                .findFirst()
                .orElse(null);
    }

    private String receiveKinopoiskUrl(Element mainElement) {
        return mainElement.getElementsByClass("videoInfoPanel-rating").stream()
                .map(ratingElement -> ratingElement.getElementsByTag("a"))
                .map(a -> a.attr("href"))
                .filter(href -> StringUtils.contains(href, "kinopoisk"))
                .findFirst()
                .orElse(null);
    }

    private String receiveProvisionFor(Element mainElement) {
        return Optional.of(mainElement.getElementsByClass("video-subscription"))
                .map(Elements::text)
                .filter(StringUtils::isNotEmpty)
                .orElseGet(() -> Optional.of(mainElement.getElementsByClass("video-purchased"))
                        .map(Elements::text)
                        .filter(StringUtils::isNotEmpty)
                        .orElse(null));
    }

    private String receivePrice(Element mainElement, String provisionFor) {
        if(StringUtils.isEmpty(provisionFor)) {
            return null;
        }
        return mainElement.getElementsByClass("stubs-big").stream()
                .findFirst()
                .map(paymentElement -> {
                    String text = null;
                    if(StringUtils.containsIgnoreCase(provisionFor, "подписка")) {
                        text = paymentElement.getElementsByClass("subscriptions-try-and-buy-text").text();
                    }
                    if(StringUtils.containsIgnoreCase(provisionFor, "покупка")) {
                        text = paymentElement.getElementsByClass("pQuality__1").stream()
                                .map(element -> {
                                    String title = element.getElementsByClass("pQuality__title").text();
                                    String paymentText = receivePaymentText(element);
                                    return String.format("%s: %s", title, paymentText);
                                })
                                .collect(Collectors.joining(" | "));
                    }
                    return text;
                })
                .orElse(null);
    }

    private String receivePaymentText(Element element) {
        return element.getElementsByClass("pQuality__item").stream()
                .map(item -> {
                    String quality = item.getElementsByClass("pQualityItem__quality").text();
                    String cost = item.getElementsByClass("pQualityItemPrice__value").text();
                    String currency = item.getElementsByClass("pQualityItemPrice__currency").text();
                    return String.format("%s %s%s", quality, cost, currency);
                })
                .collect(Collectors.joining(" "));
    }
}
