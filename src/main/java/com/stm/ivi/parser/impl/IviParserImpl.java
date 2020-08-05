package com.stm.ivi.parser.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stm.ivi.parser.IviParser;
import com.stm.ivi.pojo.BuyInfo;
import com.stm.ivi.pojo.Film;
import com.stm.ivi.pojo.ivi.IviBuyInfo;
import com.stm.ivi.pojo.ivi.IviBuyInfoResultWrapper;
import com.stm.ivi.pojo.ivi.IviBuyInfoWrapper;
import com.stm.ivi.pojo.ivi.IviBuyRange;
import com.stm.ivi.pojo.ivi.IviCategory;
import com.stm.ivi.pojo.ivi.IviCategoryWrapper;
import com.stm.ivi.pojo.ivi.IviCountry;
import com.stm.ivi.pojo.ivi.IviCountryWrapper;
import com.stm.ivi.pojo.ivi.IviFilm;
import com.stm.ivi.service.JsoupApiHelper;
import com.stm.ivi.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class IviParserImpl implements IviParser {

    @Autowired
    private JsoupApiHelper helper;

    @Override
    public Film mapIviFilmToFilm(IviFilm iviFilm) {
        if(iviFilm == null) {
            return null;
        }
        log.info("Parse film: {}", iviFilm.getShareLink());
        return new Film()
                .setId(mapObjToString(iviFilm.getId()))
                .setBuyInfo(receiveBuyInfo(iviFilm))
                .setNameRus(iviFilm.getTitle())
                .setNameOrigin(iviFilm.getOriginalTitle())
                .setYear(mapObjToString(iviFilm.getYear()))
                .setRatingKinopoisk(iviFilm.getKinopoiskRating())
                .setRatingIMDB(iviFilm.getImdbRating())
                .setRatingIvi(iviFilm.getIviRating())
                .setFeesInWorld(mapObjToString(iviFilm.getGrossWorld()))
                .setFeesInRussia(mapObjToString(iviFilm.getGrossRussia()))
                .setReleaseDateInWorld(iviFilm.getReleaseDate())
                .setGenreList(getGenres(iviFilm.getGenres()))
                .setCountry(getCountry(iviFilm.getCountry()))
                .setActorList(iviFilm.getArtists())
                .setDirector(receiveDirector(iviFilm.getId()))
                .setUrl(iviFilm.getShareLink());
    }

    private String mapObjToString(Object value) {
        return Optional.ofNullable(value)
                .map(String::valueOf)
                .orElse("-");
    }

    private String receiveDirector(Long id) {
        return Optional.ofNullable(id)
                .map(String::valueOf)
                .flatMap(idStr -> helper.getWebPageLong(String.format(Constants.PERSON_URL, idStr)))
                .flatMap(document -> document.getElementsByAttributeValue("data-test", "actors_directors_block").stream()
                            .map(main -> main.getElementsByClass("slimPosterBlock__textSection"))
                            .flatMap(Collection::stream)
                            .map(main -> {
                                String name = main.getElementsByClass("slimPosterBlock__title").text();
                                String secondName = main.getElementsByClass("slimPosterBlock__secondTitle").text();
                                return String.format("%s %s", name, secondName);
                            })
                            .findFirst())
                .orElse("-");
    }

    private BuyInfo receiveBuyInfo(IviFilm iviFilm) {
        if(StringUtils.equals(iviFilm.getTypeContent(), "AVOD")) {
            return new BuyInfo();
        }
        return Optional.ofNullable(iviFilm.getId())
                .map(String::valueOf)
                .flatMap(idStr -> helper.getForObject(
                        String.format(Constants.API_BUY_INFO, idStr, helper.getIviSession()),
                        new TypeReference<IviBuyInfoResultWrapper>() {},
                        "Cannot get buy info"))
                .map(IviBuyInfoResultWrapper::getResult)
                .map(buyInfoWrapper -> {
                    String min = Optional.ofNullable(buyInfoWrapper.getPriceRanges())
                            .map(IviBuyRange::getPrice)
                            .map(IviBuyRange.Price::getMin)
                            .orElse(null);
                    List<IviBuyInfo> iviBuyInfoList = buyInfoWrapper.getIviBuyInfoList();
                    boolean isAvailableBuy = iviBuyInfoList.stream()
                            .map(IviBuyInfo::getType)
                            .anyMatch(type -> StringUtils.equals(type, "content"));
                    String buySubscription = iviBuyInfoList.stream()
                            .filter(iviBuyInfo -> StringUtils.equals(iviBuyInfo.getType(), "subscription"))
                            .map(IviBuyInfo::getPriceSubscription)
                            .findFirst()
                            .orElse("-");
                    String buySD = receiveCostFromIviBuyInfoList("content", "eternal", "SD", iviBuyInfoList);
                    String buyHD = receiveCostFromIviBuyInfoList("content", "eternal", "HD", iviBuyInfoList);
                    String buyUHD = receiveCostFromIviBuyInfoList("content", "eternal", "4KHDR", iviBuyInfoList);
                    String rentSD = receiveCostFromIviBuyInfoList("content", "temporal", "SD", iviBuyInfoList);
                    String rentHD = receiveCostFromIviBuyInfoList("content", "temporal", "HD", iviBuyInfoList);
                    String rendUHD = receiveCostFromIviBuyInfoList("content", "temporal", "4KHDR", iviBuyInfoList);
                    return new BuyInfo()
                            .setBuySubscription(buySubscription)
                            .setBuySD(buySD)
                            .setBuyHD(buyHD)
                            .setBuyUHD(buyUHD)
                            .setRentSD(rentSD)
                            .setRentHD(rentHD)
                            .setRendUHD(rendUHD);
                })
                .orElse(new BuyInfo());
    }

    private String receiveCostFromIviBuyInfoList(String type, String durationType, String quality, List<IviBuyInfo> iviBuyInfoList) {
        return iviBuyInfoList.stream()
                .filter(iviBuyInfo -> StringUtils.equals(iviBuyInfo.getType(), type))
                .filter(iviBuyInfo -> StringUtils.equals(iviBuyInfo.getDurationType(), durationType))
                .filter(iviBuyInfo -> StringUtils.equals(iviBuyInfo.getQuality(), quality))
                .findFirst()
                .map(IviBuyInfo::getPrice)
                .orElse("-");
    }

    private List<String> getGenres(List<Long> iviGenres) {
        IviCategoryWrapper categoryWrapper = helper.getForObject(String.format(Constants.API_CATEGORIES_AND_GENRES_URL, helper.getIviSession()),
                new TypeReference<IviCategoryWrapper>(){}, "Cannot get category").orElse(null);
        if (Objects.nonNull(categoryWrapper)) {
            return categoryWrapper.getResult().stream()
                    .filter(category -> category.getId() == 14) // id категории фильмов 14
                    .map(IviCategory::getGenres)
                    .flatMap(Collection::stream)
                    .filter(genre -> iviGenres.contains(genre.getId()))
                    .map(IviCategory::getTitle)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private String getCountry(Integer countryId) {
        if(countryId == null) {
            return "-";
        }
        IviCountryWrapper countryWrapper = helper.getForObject(String.format(Constants.API_COUNTRY_URL, helper.getIviSession()),
                new TypeReference<IviCountryWrapper>(){}, "Cannot get country").orElse(null);
        if (Objects.nonNull(countryWrapper)) {
            IviCountry country = countryWrapper.getResult().entrySet().stream()
                    .filter(entry -> entry.getKey().equals(String.valueOf(countryId)))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            return Objects.isNull(country) ? StringUtils.EMPTY : country.getTitle();
        }
        return "-";
    }

}
